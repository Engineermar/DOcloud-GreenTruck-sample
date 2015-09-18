package com.ibm.optim.oaas.sample.trucking.websocket;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ibm.optim.oaas.client.job.model.JobExecutionStatus;

/**
 * Status of the optimization request sent back to the clients.
 *
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestStatus {

	boolean requested;

	String jobid;

	JobExecutionStatus jobStatus;
	
	String message;

	/**
	 * Creates a new status.
	 * 
	 * @param requested
	 *            <code>true</code> if an optimization request is currently
	 *            active.
	 * @param jobid
	 *            the job id created to process this request or
	 *            <code>null</code> if not yet defined.
	 * @param status
	 *            the job execution status or <code>null</code> if not yet
	 *            defined.
	 */
	public RequestStatus(boolean requested, String jobid,
			JobExecutionStatus status) {
		super();
		this.requested = requested;
		this.jobid = jobid;
		jobStatus = status;
	}

	/**
	 * Creates a new status
	 * 
	 * @param requested
	 *            <code>true</code> if an optimization request is currently
	 *            active.
	 * @param jobid
	 *            the job id created to process this request or
	 *            <code>null</code> if not yet defined.
	 * @param status
	 *            the job execution status or <code>null</code> if not yet
	 *            defined.
	 * @param message 
	 *            an additional message.
	 *            
	 */
	public RequestStatus(boolean requested, String jobid,
			JobExecutionStatus status, String message) {
		super();
		this.requested = requested;
		this.jobid = jobid;
		jobStatus = status;
		this.message=message;
	}
	
	public boolean isRequested() {
		return requested;
	}

	public void setRequested(boolean requested) {
		this.requested = requested;
	}

	public String getJobid() {
		return jobid;
	}

	public void setJobid(String jobid) {
		this.jobid = jobid;
	}

	public JobExecutionStatus getStatus() {
		return jobStatus;
	}

	public void setStatus(JobExecutionStatus status) {
		jobStatus = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((jobStatus == null) ? 0 : jobStatus.hashCode());
		result = prime * result + ((jobid == null) ? 0 : jobid.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + (requested ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RequestStatus other = (RequestStatus) obj;
		if (jobStatus != other.jobStatus)
			return false;
		if (jobid == null) {
			if (other.jobid != null)
				return false;
		} else if (!jobid.equals(other.jobid))
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (requested != other.requested)
			return false;
		return true;
	}


}
