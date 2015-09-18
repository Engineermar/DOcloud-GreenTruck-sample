package com.ibm.optim.oaas.sample.trucking.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Class used to specify load time information at a hub for a truck type
 * in the <i>truck.mod</i> shipment problem model.
 * 
 * Instances of this class are mapped to entries of the <code>LoadTimes</code>
 * tuple set of the <code>truck.mod</code> model, which provides the load 
 * time information for all pairs (hub, truck type). Properties are mapped to
 * the corresponding fields of the <code>loadTimeInfo</code> tuple definition.
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoadTime {

	private String truckType;
	private int loadTime;

	public LoadTime() {
	}

	public LoadTime(String truckType, int loadTime) {
		super();
		this.truckType = truckType;
		this.loadTime = loadTime;
	}

	public String getTruckType() {
		return truckType;
	}

	public void setTruckType(String truckType) {
		this.truckType = truckType;
	}

	public int getLoadTime() {
		return loadTime;
	}

	public void setLoadTime(int loadTime) {
		this.loadTime = loadTime;
	}

}
