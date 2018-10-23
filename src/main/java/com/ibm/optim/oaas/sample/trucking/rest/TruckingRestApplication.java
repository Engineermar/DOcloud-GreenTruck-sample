package com.ibm.optim.oaas.sample.trucking.rest;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.ibm.optim.oaas.sample.trucking.ejb.TruckingManager;

/**
 * REST application.
 */
@ApplicationPath("/rest/v1/*")
public class TruckingRestApplication extends Application {
  private static final Logger LOG = Logger.getLogger(TruckingRestApplication.class.getName());

  @Override
  public Set<Object> getSingletons() {
    final Set<Object> s = new HashSet<>();

    InitialContext ctx;
    try {
      ctx = new InitialContext();
      final TruckingManager manager = (TruckingManager) ctx.lookup("java:comp/env/TruckingManager");
      s.add(new TruckingRestResource(manager));
    }
    catch (final NamingException e) {
      LOG.log(Level.SEVERE, e.getLocalizedMessage(), e);
    }
    s.add(new JacksonJsonProvider());

    LOG.log(Level.INFO, "REST API initialized.");

    return s;
  }
}
