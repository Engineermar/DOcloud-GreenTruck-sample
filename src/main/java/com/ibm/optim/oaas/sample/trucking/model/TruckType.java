package com.ibm.optim.oaas.sample.trucking.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class used to specify a truck type for the <code>truck.mod</code> shipment problem
 * model.
 * 
 * Instances of this class are mapped to entries of the <i>TruckTypes</i> tuple
 * set of the <code>truck.mod</code> model, which provides capacity, cost, and speed
 * information for each truck type. Properties are mapped to the corresponding
 * fields of the <code>truckType</code> tuple definition.
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TruckType {

	@JsonProperty("_id")
	String truckType;

	int capacity;
	int costPerMile;
	int milesPerHour; // speed

	public TruckType() {
	}

	public TruckType(String truckType, int capacity, int costPerMile,
			int milesPerHour) {
		super();
		this.truckType = truckType;
		this.capacity = capacity;
		this.costPerMile = costPerMile;
		this.milesPerHour = milesPerHour;
	}

	public String getTruckType() {
		return truckType;
	}

	public void setTruckType(String truckType) {
		this.truckType = truckType;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public int getCostPerMile() {
		return costPerMile;
	}

	public void setCostPerMile(int costPerMile) {
		this.costPerMile = costPerMile;
	}

	public int getMilesPerHour() {
		return milesPerHour;
	}

	public void setMilesPerHour(int milesPerHour) {
		this.milesPerHour = milesPerHour;
	}

}
