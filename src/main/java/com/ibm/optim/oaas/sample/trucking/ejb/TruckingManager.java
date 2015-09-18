package com.ibm.optim.oaas.sample.trucking.ejb;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import com.ibm.optim.oaas.sample.trucking.model.Hub;
import com.ibm.optim.oaas.sample.trucking.model.Shipment;
import com.ibm.optim.oaas.sample.trucking.model.Spoke;
import com.ibm.optim.oaas.sample.trucking.model.TruckType;
import com.mongodb.DBObject;

/**
 * All operations coming from the REST API are provided by a simple EJB
 * implementing this interface. The implementation is done is such a way that
 * there is only one DOcloud job running at a time, and there is a single
 * solution at a time in the database.
 *
 */
public interface TruckingManager {

	/**
	 * Initializes the database with reference data packaged with the
	 * application.
	 */
	void initialize();

	/**
	 * Returns the list of truck types.
	 * 
	 * @return the list of truck types.
	 */
	List<TruckType> getTruckTypes();

	/**
	 * Returns the list of hubs.
	 * 
	 * @return the list of hubs.
	 */
	List<Hub> getHubs();

	/**
	 * Returns the list of spokes.
	 * 
	 * @return the list of spokes.
	 */
	List<Spoke> getSpokes();

	/**
	 * Returns the list of shipments.
	 * 
	 * @return the list of shipments.
	 */
	List<Shipment> getShipments();

	/**
	 * Deletes all shipments.
	 */
	void deleteShipments();

	/**
	 * Adds a shipment and returns the added shipment with the id assigned.
	 * 
	 * @param s
	 *            the shipment to add.
	 * @return the added shipment.
	 */
	Shipment addShipment(Shipment s);

	/**
	 * Returns the shipment with the given id or null.
	 * 
	 * @param id
	 *            the shipment id.
	 * @return the shipment or <code>null</code> if not found.
	 */
	Shipment getShipment(String id);

	/**
	 * Updates an existing shipment.
	 * 
	 * @param s
	 *            the shipment to update.
	 */
	void updateShipment(Shipment s);

	/**
	 * Deletes a given shipment.
	 * 
	 * @param id
	 *            the shipment id.
	 */
	void deleteShipment(String id);

	/**
	 * Writes the data content to the given output stream. This function is used
	 * to send data to the DOcloud engine.
	 * 
	 * @param out
	 *            the output stream.
	 * @throws IOException
	 */
	void getSnapshot(OutputStream out) throws IOException;

	/**
	 * Submits a DOcloud job and returns only when the job is processed.
	 */
	void solve();

	/**
	 * Submits a DOcloud job and returns as soon as the job is submitted.
	 * @throws Exception 
	 */
	void solveAsync() throws Exception;

	/**
	 * Returns the solution.
	 * 
	 * @return the solution.
	 */
	DBObject getSolution();

	/**
	 * Deletes the current solution.
	 */
	void deleteSolution();

	/**
	 * Deletes all jobs in DOcloud for the subscription.
	 */
	void deleteAllJobs();

}
