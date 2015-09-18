package com.ibm.optim.oaas.sample.trucking.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Class used to specify a route between a spoke and a hub in a logistic network
 * for the <code>truck.mod</code> shipment problem model.
 * 
 * Instances of this class are mapped to entries of the <code>Routes</code> tuple set
 * of the <code>truck.mod</code> model, which provides the list of all (spoke, hub)
 * routes in the logistic network, with their associated distance. Properties
 * are mapped to the corresponding fields of the <code>routeInfo</code> tuple
 * definition.
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Route {

	private String spoke;
	private int distance;

	public String getSpoke() {
		return spoke;
	}

	public void setSpoke(String spoke) {
		this.spoke = spoke;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

}
