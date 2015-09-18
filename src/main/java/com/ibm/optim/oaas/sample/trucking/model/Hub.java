package com.ibm.optim.oaas.sample.trucking.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Class used to specify a hub in a logistic network for the <code>truck.mod</code>
 * shipment problem model.
 * 
 * Instances of this class are mapped to entries of the <code>Hubs</code> tuple set of
 * the <code>truck.mod</code> model, which provides the list of all Hubs in the
 * logistic network. Properties are mapped to the corresponding fields of the
 * <code>location</code> tuple definition.
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Hub extends Location {

	List<Route> routes = new ArrayList<Route>();
	List<LoadTime> loadtimes = new ArrayList<LoadTime>();

	public Hub() {
	}

	public Hub(String name) {
		super(name);
	}

	public List<Route> getRoutes() {
		return routes;
	}

	public List<LoadTime> getLoadTimes() {
		return loadtimes;
	}
}
