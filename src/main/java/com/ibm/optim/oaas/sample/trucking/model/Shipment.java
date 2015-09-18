package com.ibm.optim.oaas.sample.trucking.model;

import org.mongojack.ObjectId;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class used to specify shipments to be planned by the <code>truck.mod</code> problem
 * model.
 * 
 * Instances of this class are mapped to entries of the <code>Shipments</code> tuple
 * set of the <code>truck.mod</code> model, which provides the list of quantities to
 * be shipped between an origin and a destination Spoke. Properties are mapped
 * to the corresponding fields of the <code>shipment</code> tuple definition.
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Shipment {

	@ObjectId
	@JsonProperty("_id")
	private String id;

	private String origin;
	private String destination;
	private int totalVolume;

	public Shipment() {
	}

	public Shipment(String origin, String destination, int totalVolume) {
		super();
		this.origin = origin;
		this.destination = destination;
		this.totalVolume = totalVolume;
	}

	public String getId() {
		return id;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public int getTotalVolume() {
		return totalVolume;
	}

	public void setTotalVolume(int totalVolume) {
		this.totalVolume = totalVolume;
	}

}
