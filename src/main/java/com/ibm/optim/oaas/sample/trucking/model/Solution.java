package com.ibm.optim.oaas.sample.trucking.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Class used as a container to collect all output information for a solution
 * calculated by the <code>truck.mod</code> model.
 * 
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Solution {

	private String jobid;

	private Double duration;

	private Date endedAt;

	private Double cost;

	private String solveStatus;

	private List<NbTrucksOnRouteRes> trucks = new ArrayList<NbTrucksOnRouteRes>();

	public Solution() {

	}

	public String getJobid() {
		return jobid;
	}

	public Double getDuration() {
		return duration;
	}

	public Date getEndedAt() {
		return endedAt;
	}

	public Double getCost() {
		return cost;
	}

	public List<NbTrucksOnRouteRes> getTrucks() {
		return trucks;
	}

	public String getSolveStatus() {
		return solveStatus;
	}

}
