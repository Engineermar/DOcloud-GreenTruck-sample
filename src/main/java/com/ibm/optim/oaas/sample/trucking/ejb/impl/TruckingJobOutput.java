package com.ibm.optim.oaas.sample.trucking.ejb.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.optim.oaas.client.OperationException;
import com.ibm.optim.oaas.client.job.AttachmentNotFoundException;
import com.ibm.optim.oaas.client.job.JobClient;
import com.ibm.optim.oaas.client.job.JobNotFoundException;
import com.ibm.optim.oaas.client.job.JobOutput;
import com.ibm.optim.oaas.client.job.JobRequest;
import com.ibm.optim.oaas.client.job.model.Job;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;

/**
 * Class in charge of streaming output data from the DOcloud service. As output
 * data can be large, this implementation shows how to access and filter the
 * results from DOcloud and store them in the MongoDB datastore without
 * building a complete data model in memory.
 *
 */
public class TruckingJobOutput implements JobOutput {

	private static Logger LOG = Logger.getLogger(TruckingJobOutput.class
			.getName());

	DB mongoDB;
	String name;
	JobRequest request;

	/**
	 * Creates an output data controller using the given MongoDB datastore.
	 * 
	 * @param db
	 *            the MongoDB datastore.
	 */
	public TruckingJobOutput(DB db) {
		mongoDB = db;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;

	}

	@Override
	public Object getContent() {
		return null;
	}

	@Override
	public void download(JobClient client, String jobid)
			throws JobNotFoundException, AttachmentNotFoundException,
			OperationException, IOException {
		InputStream is = client.downloadJobAttachment(jobid, getName());
		try {
			deserialize(is);
		} finally {
			is.close();
		}
	}

	private void deserialize(InputStream is) throws IOException {
		JsonFactory factory = new JsonFactory();
		ObjectMapper mapper = new ObjectMapper();
		factory.setCodec(mapper);
		JsonParser jp = factory.createParser(is);
		Job job = null;
		try {
			job = request.getClient().getJob(request.getJobId());
		} catch (JobNotFoundException e) {
			LOG.log(Level.WARNING, e.getMessage(), e);
		} catch (OperationException e) {
			LOG.log(Level.WARNING, e.getMessage(), e);
		}

		DBCollection col = mongoDB.getCollection("solutions");

		BasicDBObject root = new BasicDBObject();
		root.append("jobid", request.getJobId());
		if (job != null) {
			root.append("duration", (job.getEndedAt().getTime() - job
					.getCreatedAt().getTime()) / 1000);
			root.append("endedAt", job.getEndedAt().getTime());
			root.append("solveStatus", job.getSolveStatus().toString());
		}

		jp.nextToken();
		while (jp.nextToken() != JsonToken.END_OBJECT) {
			String fieldname = jp.getCurrentName();

			JsonToken x = jp.nextToken();
			if (x == JsonToken.START_OBJECT) { // simple tuple
				JsonNode obj = jp.readValueAsTree();
				root.append("cost", obj.get("totalCost").asDouble());
			} else { // list
				if (fieldname.equals("NbTrucksOnRouteRes")) {
					ArrayList<BasicDBObject> trucks = new ArrayList<BasicDBObject>();

					root.append("trucks", trucks);

					while (jp.nextToken() != JsonToken.END_ARRAY) {
						JsonNode obj = jp.readValueAsTree();
						BasicDBObject truck = new BasicDBObject();
						truck.append("hub", obj.get("hub").asText());
						truck.append("spoke", obj.get("spoke").asText());
						truck.append("truckType", obj.get("truckType").asText());
						truck.append("nbTruck", obj.get("nbTruck").asInt());
						trucks.add(truck);
					}
				} else {
					jp.skipChildren();
				}
			}
		}

		col.findAndRemove(new BasicDBObject());
		col.insert(root);
	}

	public void setRequest(JobRequest request) {
		this.request = request;
	}

}
