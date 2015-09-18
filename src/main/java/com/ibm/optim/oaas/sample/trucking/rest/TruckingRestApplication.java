package com.ibm.optim.oaas.sample.trucking.rest;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.core.Application;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.ibm.optim.oaas.sample.trucking.ejb.TruckingManager;
import com.wordnik.swagger.jaxrs.listing.ApiDeclarationProvider;
import com.wordnik.swagger.jaxrs.listing.ApiListingResourceJSON;
import com.wordnik.swagger.jaxrs.listing.ResourceListingProvider;

/**
 * REST application.
 */
public class TruckingRestApplication extends Application {

	private static Logger LOG = Logger.getLogger(TruckingRestApplication.class
			.getName());

	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		classes.add(ApiDeclarationProvider.class);
		classes.add(ApiListingResourceJSON.class);
		classes.add(ResourceListingProvider.class);

		return classes;
	}

	@Override
	public Set<Object> getSingletons() {

		Set<Object> s = new HashSet<Object>();

		InitialContext ctx;
		try {
			ctx = new InitialContext();
			TruckingManager manager = (TruckingManager) ctx
					.lookup("java:comp/env/TruckingManager");
			s.add(new TruckingRestResource(manager));

		} catch (NamingException e) {
			LOG.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		s.add(new JacksonJsonProvider());

		LOG.log(Level.INFO, "REST API initialized.");

		return s;
	}
}
