package com.ibm.optim.oaas.sample;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This utility class is used to get configuration parameters based on
 * environment variables as done in IBM Bluemix.
 */
public class Environment {

	private static Logger LOG = Logger.getLogger(Environment.class.getName());

	static private Environment _instance = new Environment();

	/**
	 * Returns the environment instance to access configuration information.
	 * 
	 * @return the environment instance.
	 */
	public static final Environment getInstance() {
		return _instance;
	}

	protected JsonNode vcap_application;

	protected JsonNode vcap_services;

	/**
	 * Creates the environment information based on the variables
	 * VCAP_APPLICATION and VCAP_SERVICES.
	 */
	protected Environment() {
		this(System.getenv("VCAP_APPLICATION"), System.getenv("VCAP_SERVICES"));
	}

	/**
	 * Creates the environment information based on the content of
	 * VCAP_APPLICATION and VCAP_SERVICES variables.
	 */
	protected Environment(String vcap_application, String vcap_services) {
		this.vcap_application = parseJsonVar(vcap_application);
		this.vcap_services = parseJsonVar(vcap_services);
		if (vcap_application == null) {
			LOG.warning("Not running in Bluemix");
		} else {
			LOG.info("Running in Bluemix on main endpoint " + getAppURI());
		}
	}

	/**
	 * Parses the VCAP_SERVICES variable content.
	 * 
	 * @param vcap
	 *            the VCAP_SERVICES variable content.
	 * @return the JSON data.
	 */
	protected JsonNode parseJsonVar(String vcap) {
		if (vcap == null) {
			return null;
		}
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode actualObj = mapper.readTree(vcap);
			return actualObj;
		} catch (Exception e) {
			LOG.log(Level.WARNING, "Exception parsing VCAP_SERVICES", e);
			return null;
		}
	}

	/**
	 * Returns the application URI as extracted from VCAP_APPLICATION.
	 * 
	 * @return the application URI.
	 */
	public String getAppURI() {
		if (vcap_application != null) {
			JsonNode uris = vcap_application.get("application_uris");
			if (uris != null && uris.get(0) != null)
				return uris.get(0).asText();
		}
		return null;
	}

	/**
	 * Returns the instance index as extracted from VCAP_APPLICATION.
	 * 
	 * @return the instance index.
	 */
	public int getInstanceIndex() {
		if (vcap_application != null) {
			return vcap_application.get("instance_index").asInt();
		}
		return -1;
	}

	/**
	 * Returns the definition of a service as extracted from VCAP_SERVICES.
	 * 
	 * @param name
	 *            the service name.
	 * @return the JSON data of the service or <code>null</code> if not found.
	 */
	public JsonNode getService(String name) {
		if (vcap_services != null) {
			try {
				// iterate over service types (object)
				for (Iterator<JsonNode> it = vcap_services.elements(); it
						.hasNext();) {
					// iterate over service entries of that type (array)
					for (Iterator<JsonNode> ite = it.next().elements(); ite
							.hasNext();) {
						JsonNode service = ite.next();
						JsonNode serviceName = service.get("name");
						if (serviceName != null
								&& name.equals(serviceName.asText()))
							return service;
					}
				}
			} catch (Exception e) {
				LOG.log(Level.SEVERE, "Exception parsing VCAP_SERVICES", e);
			}
		}
		return null;
	}

}
