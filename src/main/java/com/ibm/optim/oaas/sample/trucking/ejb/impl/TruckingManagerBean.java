package com.ibm.optim.oaas.sample.trucking.ejb.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.optim.oaas.client.OperationException;
import com.ibm.optim.oaas.client.job.JobClient;
import com.ibm.optim.oaas.client.job.JobClientFactory;
import com.ibm.optim.oaas.client.job.JobExecutor;
import com.ibm.optim.oaas.client.job.JobExecutorFactory;
import com.ibm.optim.oaas.client.job.JobNotFoundException;
import com.ibm.optim.oaas.client.job.JobRequest;
import com.ibm.optim.oaas.client.job.JobResponse;
import com.ibm.optim.oaas.client.job.model.JobExecutionStatus;
import com.ibm.optim.oaas.client.job.model.JobFailureInfo;
import com.ibm.optim.oaas.sample.Environment;
import com.ibm.optim.oaas.sample.trucking.ejb.TruckingManager;
import com.ibm.optim.oaas.sample.trucking.model.Hub;
import com.ibm.optim.oaas.sample.trucking.model.Shipment;
import com.ibm.optim.oaas.sample.trucking.model.Spoke;
import com.ibm.optim.oaas.sample.trucking.model.TruckType;
import com.ibm.optim.oaas.sample.trucking.websocket.RequestStatus;
import com.ibm.optim.oaas.sample.trucking.websocket.StatusEventEndpoint;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;

/**
 * Session Bean implementation of the core application services.
 */
@Local(TruckingManager.class)
@TransactionAttribute(value = TransactionAttributeType.NOT_SUPPORTED)
@Singleton(name = "TruckingManager")
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class TruckingManagerBean implements TruckingManager {

	private static Logger LOG = Logger.getLogger(TruckingManagerBean.class
			.getName());

	@Resource(name = "mongo/truckingDB")
	DB mongoDB;

	@Resource(lookup = "concurrent/docloud")
	ExecutorService jobService;

	@Resource(lookup = "concurrent/monitor")
	ScheduledExecutorService monitorService;

	JobClient jobclient;

	JobExecutor executor;

	URL modFile;

	ObjectMapper mapper;

	JacksonDBCollection<Shipment, String> shipments;

	/**
	 * Default constructor.
	 */
	public TruckingManagerBean() {

	}

	@PostConstruct
	public void setUp() {

		// look for the DOcloud service configuration
		JsonNode docloud = Environment.getInstance().getDOcloud();
		if (docloud!=null){
			JsonNode credential = docloud.get("credentials");
			String url = credential.get("url").asText();
			String client_id = credential.get("client_id").asText();

			LOG.log(Level.INFO, "Using Bluemix DOcloud at {0}", url);

			// creates the DOcloud client
			jobclient = JobClientFactory.createDefault(url, client_id);
			
		} else {
			 docloud = Environment.getInstance().getService("docloud");
			if (docloud == null) {
				LOG.severe("DOcloud config not found");
			} else {
				JsonNode credential = docloud.get("credentials");
				String url = credential.get("url").asText();
				String client_id = credential.get("client_id").asText();
	
				LOG.log(Level.INFO, "Using user-defined DOcloud at {0}", url);
	
				// creates the DOcloud client
				jobclient = JobClientFactory.createDefault(url, client_id);
			}
		}
		// get the OPL model file
		modFile = TruckingManagerBean.class.getResource("truck.mod");
		if (modFile == null) {
			LOG.severe("OPL modfile not found");
		}

		// creates an object mapper use to read/write JSON
		mapper = new ObjectMapper();

		// creates the executor that will be sued by the DOcloud client
		executor = JobExecutorFactory.custom().service(jobService).build();

		// if the MongoDB datastore does not contain data, populate it
		// automatically
		if (!mongoDB.collectionExists("hubs")) {
			LOG.info("Initializing database");
			initialize();
		}

		// start the monitoring service to process asynchronous requests
		monitorService.scheduleAtFixedRate(monitorJob, 0, 2, TimeUnit.SECONDS);

		// prepare the shipment collection for easy access
		shipments = JacksonDBCollection.wrap(
				mongoDB.getCollection("shipments"), Shipment.class,
				String.class);
	}

	/**
	 * This is the runnable executed by the monitoring thread.
	 */
	Runnable monitorJob = new Runnable() {
		public void run() {
			DBObject obj = isJobRequested();
			if (obj == null) {
				// if there is not current active request just make sure all
				// clients
				// have the latest solution
				broadcastLatestSolution();

			} else {
				// if there is request, check its staus

				Object jobid = obj.get("jobid");
				if (jobid == null) {
					// if the jobid was not assigned after 10 seconds, there
					// must be an issue
					// so cancel the request.
					Object ts = obj.get("ts");
					Date now = new Date();
					if (jobid == null && ts != null
							&& (now.getTime() - ((Date) ts).getTime()) > 10000) {
						LOG.log(Level.WARNING,
								"Cancelling job because the jobid was not set");
						requestJobDone();
						broadcastLatestSolution();
					}

				} else {
					// check execution status
					String id = (String) jobid;
					try {
						JobExecutionStatus status = jobclient
								.getJobExecutionStatus(id);
						switch (status) {
						case CREATED:
						case NOT_STARTED:
						case RUNNING:
						case INTERRUPTING:
							StatusEventEndpoint
									.broadcastStatus(new RequestStatus(true,
											id, status));
							break;

						case FAILED:
							
							JobFailureInfo failure = jobclient.getFailureInfo(id);													
							LOG.warning("Failed " + failure.getMessage());
							StatusEventEndpoint	.broadcastStatus(new RequestStatus(false,
									id, status,failure.getMessage()));
							try {
								jobclient.deleteJob(id);
							} catch (Exception e) {
								// ignore any exception
							}
							requestJobDone();
							broadcastLatestSolution();
							break;
						case INTERRUPTED:

						case PROCESSED:
							if (requestJobProcessed(id) != null) {
								completedAsync(id);
							}
							broadcastLatestSolution();
							break;
						}

					} catch (JobNotFoundException e) {
						// job was not found, already processed
						requestJobDone();
						broadcastLatestSolution();

					} catch (OperationException e) {
						// ignore will retry at next monitor execution
					}
				}
			}
		}
	};

	/**
	 * Get the solution and stores it in MongoDB after an <code>async</code> request.
	 * 
	 * @param jobid
	 *            the job id.
	 */
	private void completedAsync(String jobid) {
		LOG.log(Level.INFO, "Complete async job {0}", jobid);
		try {
			// create the request
			TruckingJobOutput updater = new TruckingJobOutput(this.mongoDB);
			JobRequest request = jobclient.newRequest().output(updater)
					.timeout(60, TimeUnit.SECONDS).build();
			updater.setRequest(request);

			executor.monitor(request, jobid, null).get();

		} catch (Exception e) {
			LOG.log(Level.WARNING, "Error while completing job", e);
			e.printStackTrace();
		} finally {
			// always cleanup the job by deleting it
			if (jobid != null) {
				try {
					jobclient.deleteJob(jobid);
				} catch (Exception e) {
					// ignore any exception
				}
			}
			requestJobDone();
		}
	}

	/**
	 * Indicates whether a job has been requested.
	 * 
	 * @return <code>true</code> if a request is active <code>false</code>
	 *         otherwise.
	 */
	private DBObject isJobRequested() {
		DBObject obj = mongoDB.getCollection("jobs").findOne(
				new BasicDBObject("requested", true));
		return obj;

	}

	/**
	 * Setup a mutex to request a job.
	 * 
	 * @return the mutex object or <code>null</code> if it cannot be obtained.
	 */
	private DBObject requestJob() {
		DBObject obj = mongoDB.getCollection("jobs").findAndModify(
				new BasicDBObject("requested", false),
				new BasicDBObject("requested", true).append("jobid", null)
						.append("ts", new Date()).append("solution", false));
		return obj;

	}

	/**
	 * Completes the mutes with the job id when known.
	 * 
	 * @param jobid
	 *            the job id.
	 * @return the updated mutex.
	 */
	private DBObject requestJobCreated(String jobid) {
		DBObject obj = mongoDB.getCollection("jobs").findAndModify(
				new BasicDBObject("requested", true),
				new BasicDBObject("$set", new BasicDBObject("jobid", jobid)));
		return obj;
	}

	/**
	 * Complete the main mutex with a secondary mutex to indicate that the
	 * solution has been processed.
	 * 
	 * @param jobid
	 *            the job id
	 * @return the updated mutex or <code>null</code> if it cannot be obtained.
	 */
	private DBObject requestJobProcessed(String jobid) {
		DBObject obj = mongoDB.getCollection("jobs").findAndModify(
				new BasicDBObject("jobid", jobid).append("solution", false),
				new BasicDBObject("$set", new BasicDBObject("solution", true)));
		return obj;
	}

	/**
	 * Release the mutex when the job is completed.
	 * 
	 * @return the updated mutex.
	 */
	private DBObject requestJobDone() {
		DBObject obj = mongoDB.getCollection("jobs").findAndModify(
				new BasicDBObject("requested", true),
				new BasicDBObject("requested", false).append("jobid", null)
						.append("ts", null).append("solution", false));
		return obj;
	}

	@Override
	public void getSnapshot(OutputStream out) throws IOException {
		TruckingJobInput input = new TruckingJobInput(this.mongoDB);
		input.serialize(out);
	}

	@Override
	public void solve() {
		if (requestJob() == null) {
			LOG.log(Level.INFO, "Job is already requested, ignoring...");
			return;
		}

		String jobid = null;
		JobExecutionStatus status = null;
		try {
			// create the request
			TruckingJobOutput updater = new TruckingJobOutput(this.mongoDB);
			JobRequest request = jobclient.newRequest()
					.input("model.mod", modFile)
					.input(new TruckingJobInput(this.mongoDB))
					.livelog(System.out).output(updater)
					.timeout(5, TimeUnit.MINUTES).build();
			updater.setRequest(request);

			// submit and get the job id
			Future<JobResponse> submit = executor.execute(request);
			jobid = request.getJobId();
			requestJobCreated(jobid);
			requestJobProcessed(jobid);

			StatusEventEndpoint.broadcastStatus(new RequestStatus(true, jobid,
					JobExecutionStatus.CREATED));
			LOG.info("Job submitted " + jobid);

			// wait for completion
			JobResponse response = submit.get();

			status = response.getJob().getExecutionStatus();

			switch (status) {
			case PROCESSED:
				break;
			case FAILED:
				// get failure message if defined
				String message = "";
				if (response.getJob().getFailureInfo() != null) {
					message = response.getJob().getFailureInfo().getMessage();
				}
				LOG.warning("Failed " + message);
				break;
			default:
				break;
			}

		} catch (Exception e) {
			LOG.log(Level.WARNING, "Error while executing job", e);
		} finally {
			// always cleanup the job by deleting it
			if (jobid != null) {
				try {
					jobclient.deleteJob(jobid);
				} catch (Exception e) {
					// ignore any exception
				}
			}
			requestJobDone();
			broadcastLatestSolution();
		}
	}

	@Override
	public void solveAsync() throws Exception {
		if (requestJob() == null) {
			LOG.log(Level.INFO, "Optim is already requested, ignoring...");
			return;
		}

		String jobid = null;
		try {
			// create the request
			JobRequest request = jobclient.newRequest()
					.input("model.mod", modFile)
					.input(new TruckingJobInput(this.mongoDB)).build();

			// submit and get the job id
			Future<JobResponse> submit = executor.submit(request, null);
			jobid = request.getJobId();
			requestJobCreated(jobid);
			LOG.log(Level.INFO, "Job submitted " + jobid);
			StatusEventEndpoint.broadcastStatus(new RequestStatus(true, jobid,
					JobExecutionStatus.CREATED));

			// wait for completion of submission
			submit.get(60, TimeUnit.SECONDS);

		} catch (Exception e) {
			LOG.log(Level.WARNING, "Error submitting executing job", e);

			// always cleanup the job by deleting it
			if (jobid != null) {
				try {
					jobclient.deleteJob(jobid);
				} catch (Exception e1) {
					// ignore any exception
				}
			}
			requestJobDone();
			broadcastLatestSolution();
			throw e;
		}
	}

	@Override
	public void initialize() {

		// drop all collections
		mongoDB.getCollection("hubs").drop();
		mongoDB.getCollection("spokes").drop();
		mongoDB.getCollection("shipments").drop();
		mongoDB.getCollection("truckTypes").drop();
		mongoDB.getCollection("solutions").drop();
		mongoDB.getCollection("jobs").drop();

		// initialize the job status
		mongoDB.getCollection("jobs").insert(
				new BasicDBObject("requested", false));
		StatusEventEndpoint
				.broadcastStatus(new RequestStatus(false, null, null));

		// import sample data
		mongoImport(TruckingManagerBean.class.getResource("hubs.json"),
				mongoDB, "hubs", true);
		mongoImport(TruckingManagerBean.class.getResource("spokes.json"),
				mongoDB, "spokes", true);
		mongoImport(TruckingManagerBean.class.getResource("shipments.json"),
				mongoDB, "shipments", true);
		mongoImport(TruckingManagerBean.class.getResource("truckTypes.json"),
				mongoDB, "truckTypes", true);
	}

	/**
	 * Imports a JSON resource into a MongoDB collection.
	 * 
	 * @param resource
	 *            the JSON resource
	 * @param db
	 *            the MongoDB datastore
	 * @param collection
	 *            the collection to update
	 * @param drop
	 *            indicates if the existing collection must be dropped
	 */
	private void mongoImport(URL resource, DB db, String collection,
			boolean drop) {
		// keep a reference to the collection
		DBCollection c = db.getCollection(collection);

		// drop the content quickly, note that indexes must be created again if
		// necessary.
		if (drop)
			c.drop();

		int count = 0; // used to count the line
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				resource.openStream()))) {
			String line = null; // used to store the line as a string
			while ((line = reader.readLine()) != null) {
				count++; // count the lines

				// skip empty lines and line with comments
				line = line.trim();
				if (line.length() != 0 && !line.startsWith("#")) {
					// parse line
					DBObject obj = (DBObject) JSON.parse(line);
					// insert object to database
					try {
						c.insert(obj);
					} catch (MongoException e) {
						LOG.log(Level.WARNING,
								"Mongo DB exception while importing line "
										+ count, e);
					}
				}
			}
		} catch (IOException e) {
			LOG.log(Level.WARNING, "IO exception while importing resource", e);
		}
		LOG.log(Level.INFO,
				"Imported {0} objects from {1} lines in collection {2}",
				new Object[] { c.getCount(), count, collection });
	}

	@Override
	public List<Hub> getHubs() {
		JacksonDBCollection<Hub, String> col = JacksonDBCollection.wrap(
				mongoDB.getCollection("hubs"), Hub.class, String.class);
		List<Hub> res = new ArrayList<Hub>();
		for (Hub s : col.find()) {
			res.add(s);
		}
		return res;
	}

	@Override
	public List<Spoke> getSpokes() {
		JacksonDBCollection<Spoke, String> col = JacksonDBCollection.wrap(
				mongoDB.getCollection("spokes"), Spoke.class, String.class);
		List<Spoke> res = new ArrayList<Spoke>();
		for (Spoke s : col.find()) {
			res.add(s);
		}
		return res;
	}

	@Override
	public List<Shipment> getShipments() {
		List<Shipment> res = new ArrayList<Shipment>();
		for (Shipment s : shipments.find()) {
			res.add(s);
		}
		return res;
	}

	@Override
	public List<TruckType> getTruckTypes() {
		JacksonDBCollection<TruckType, String> col = JacksonDBCollection.wrap(
				mongoDB.getCollection("truckTypes"), TruckType.class,
				String.class);
		List<TruckType> res = new ArrayList<TruckType>();
		for (TruckType s : col.find()) {
			res.add(s);
		}
		return res;
	}

	@Override
	public void deleteSolution() {
		mongoDB.getCollection("solutions").findAndRemove(new BasicDBObject());
	}

	@Override
	public void deleteShipments() {
		shipments.remove(new BasicDBObject());
	}

	@Override
	public Shipment addShipment(Shipment s) {
		WriteResult<Shipment, String> result = shipments.insert(s);
		return result.getSavedObject();
	}

	@Override
	public Shipment getShipment(String id) {
		Shipment r = shipments.findOneById(id);
		return r;
	}

	@Override
	public void updateShipment(Shipment s) {
		shipments.updateById(s.getId(), s);
	}

	@Override
	public void deleteShipment(String id) {
		shipments.removeById(id);
	}

	@Override
	public DBObject getSolution() {
		DBObject sol = mongoDB.getCollection("solutions").findOne();
		return sol;
	}

	/**
	 * Returns the job id of the current solution.
	 * 
	 * @return the job id
	 */
	private String getSolutionJobId() {
		DBObject sol = mongoDB.getCollection("solutions").findOne(
				new BasicDBObject(), new BasicDBObject("jobid", true));
		return sol == null ? null : sol.get("jobid").toString();
	}

	/**
	 * Broadcast the latest solution to all clients.
	 */
	private void broadcastLatestSolution() {
		StatusEventEndpoint.broadcastStatus(new RequestStatus(false,
				getSolutionJobId(), JobExecutionStatus.PROCESSED));
	}

	@Override
	public void deleteAllJobs() {
		try {
			jobclient.deleteAllJobs();
		} catch (OperationException e) {			
			LOG.log(Level.WARNING,e.getLocalizedMessage(),e);
		}		
	}
}
