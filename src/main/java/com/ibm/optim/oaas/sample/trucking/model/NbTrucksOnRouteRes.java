package com.ibm.optim.oaas.sample.trucking.model;

/**
 * Class used to report the number of required trucks, for each truck type, in
 * solutions to the <code>truck.mod</code> model.
 * 
 * Instances of this class are mapped to entries of the
 * <code>NbTrucksOnRouteRes</code> post-processed tuple set of the <code>truck.mod</code>
 * model. Properties are mapped to the corresponding fields of the
 * <code>nbTrucksOnRouteRes</code> tuple definition.
 */
public class NbTrucksOnRouteRes {

	private String spoke;

	private String hub;

	private String truckType;

	private int nbTruck;

	public String getSpoke() {
		return spoke;
	}

	public void setSpoke(String spoke) {
		this.spoke = spoke;
	}

	public String getHub() {
		return hub;
	}

	public void setHub(String hub) {
		this.hub = hub;
	}

	public String getTruckType() {
		return truckType;
	}

	public void setTruckType(String truckType) {
		this.truckType = truckType;
	}

	public int getNbTruck() {
		return nbTruck;
	}

	public void setNbTruck(int nbTruck) {
		this.nbTruck = nbTruck;
	}
}
