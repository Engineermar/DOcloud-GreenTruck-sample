package com.ibm.optim.oaas.sample.trucking;

import java.util.List;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.ibm.optim.oaas.sample.trucking.model.Hub;
import com.ibm.optim.oaas.sample.trucking.model.Shipment;
import com.ibm.optim.oaas.sample.trucking.model.Solution;
import com.ibm.optim.oaas.sample.trucking.model.Spoke;
import com.ibm.optim.oaas.sample.trucking.model.TruckType;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

/**
 * Simple client to write unit tests.
 *
 */
public class TruckingClient {

	Client client;
	String baseurl;
	WebResource base;

	public TruckingClient(String baseurl) {
		ClientConfig config = new DefaultClientConfig();
		config.getClasses().add(JacksonJsonProvider.class);
		this.client = Client.create(config);
		this.baseurl = baseurl;
		this.base = client.resource(baseurl);
	}

	public void initialize() {
		base.path("rest/v1/trucking/initialize").post();

	}

	public List<Shipment> getShipments() {
		List<Shipment> response = base.path("rest/v1/trucking/shipments")
				.accept("application/json")
				.get(new GenericType<List<Shipment>>() {
				});
		return response;
	}

	public Shipment addShipment(Shipment s) {
		Shipment response = base.path("rest/v1/trucking/shipments")
				.header("content-type", "application/json")
				.accept("application/json").post(Shipment.class, s);
		return response;
	}

	public void deleteShipments() {
		base.path("rest/v1/trucking/shipments").delete();
	}

	public Shipment getShipment(String id) {
		try {
			Shipment response = base.path("rest/v1/trucking/shipments")
					.path(id).accept("application/json").get(Shipment.class);
			return response;
		} catch (UniformInterfaceException e) {
			if (e.getResponse().getStatus() == 404)
				return null;
			else
				throw e;
		}
	}

	public Shipment updateShipment(Shipment s) {
		Shipment response = base.path("rest/v1/trucking/shipments")
				.path(s.getId()).header("content-type", "application/json")
				.accept("application/json").get(Shipment.class);
		return response;
	}

	public void deleteShipment(String id) {
		base.path("rest/v1/trucking/shipments").path(id).delete();
	}

	public void solve() {
		base.path("rest/v1/trucking/solve").post();
	}

	List<Hub> getHubs() {
		List<Hub> response = base.path("rest/v1/trucking/hubs")
				.accept("application/json").get(new GenericType<List<Hub>>() {
				});
		return response;
	}

	List<Spoke> getSpokes() {
		List<Spoke> response = base.path("rest/v1/trucking/spokes")
				.accept("application/json").get(new GenericType<List<Spoke>>() {
				});
		return response;
	}

	List<TruckType> getTruckTypes() {
		List<TruckType> response = base.path("rest/v1/trucking/truckTypes")
				.accept("application/json")
				.get(new GenericType<List<TruckType>>() {
				});
		return response;
	}

	public Solution getSolution() {
		Solution response = base.path("rest/v1/trucking/solution")
				.accept("application/json").get(Solution.class);
		return response;
	}

	public void deleteSolution(String id) {
		base.path("rest/v1/trucking/solution").path(id).delete();
	}

}
