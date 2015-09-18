package com.ibm.optim.oaas.sample.trucking.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class used to specify a spoke in a logistic network for the <code>truck.mod</code>
 * shipment problem model.
 * 
 * Instances of this class are mapped to entries of the <code>Spokes</code> tuple set
 * of the <code>truck.mod</code> model. Properties are mapped to the corresponding
 * fields of the <code>spoke</code> tuple definition.
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Spoke {

	int minDepTime; // Earliest departure time at spoke
	int maxArrTime; // Latest arrive time at spoke

	@JsonProperty("_id")
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Spoke() {
	};

	public Spoke(String name, int minDepTime, int maxArrTime) {
		this.name = name;
		this.minDepTime = minDepTime;
		this.maxArrTime = maxArrTime;
	}

	public int getMinDepTime() {
		return minDepTime;
	}

	public void setMinDepTime(int minDepTime) {
		this.minDepTime = minDepTime;
	}

	public int getMaxArrTime() {
		return maxArrTime;
	}

	public void setMaxArrTime(int maxArrTime) {
		this.maxArrTime = maxArrTime;
	}

}
