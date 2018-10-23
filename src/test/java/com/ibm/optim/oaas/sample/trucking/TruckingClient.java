package com.ibm.optim.oaas.sample.trucking;

import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.ibm.optim.oaas.sample.trucking.model.Hub;
import com.ibm.optim.oaas.sample.trucking.model.Shipment;
import com.ibm.optim.oaas.sample.trucking.model.Solution;
import com.ibm.optim.oaas.sample.trucking.model.Spoke;
import com.ibm.optim.oaas.sample.trucking.model.TruckType;

/**
 * Simple client to write unit tests.
 *
 */
public class TruckingClient {
  private final Client client;
  private final WebTarget base;

  public TruckingClient(final String baseurl) {
    this.client = ClientBuilder.newBuilder().register(JacksonJsonProvider.class).build();
    this.base = client.target(baseurl);
  }

  private Entity<?> getEmptyEntity() {
    return Entity.text("");
  }

  public void initialize() {
    base
    .path("rest/v1/trucking/initialize")
    .request()
    .post(getEmptyEntity());
  }

  public List<Shipment> getShipments() {
    final List<Shipment> shipments = base
        .path("rest/v1/trucking/shipments")
        .request()
        .accept(MediaType.APPLICATION_JSON)
        .get(new GenericType<List<Shipment>>() {});
    return shipments;
  }

  public Shipment addShipment(final Shipment s) {
    final Shipment shipment = base
        .path("rest/v1/trucking/shipments")
        .request()
        .header("content-type", "application/json")
        .accept("application/json")
        .post(Entity.entity(s, MediaType.APPLICATION_JSON), Shipment.class);
    return shipment;
  }

  public void deleteShipments() {
    base
    .path("rest/v1/trucking/shipments")
    .request()
    .delete();
  }

  public Shipment getShipment(final String id) {
    final Shipment shipment = base
        .path("rest/v1/trucking/shipments")
        .path(id)
        .request()
        .accept(MediaType.APPLICATION_JSON)
        .get(Shipment.class);
    return shipment;
  }

  public Shipment updateShipment(final Shipment s) {
    final Shipment response = base
        .path("rest/v1/trucking/shipments")
        .path(s.getId())
        .request()
        .header("content-type", MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .get(Shipment.class);
    return response;
  }

  public void deleteShipment(final String id) {
    base
    .path("rest/v1/trucking/shipments")
    .path(id)
    .request()
    .delete();
  }

  public void solve() {
    base
    .path("rest/v1/trucking/solve")
    .request()
    .post(getEmptyEntity());
  }

  public List<Hub> getHubs() {
    final List<Hub> hubs = base
        .path("rest/v1/trucking/hubs")
        .request()
        .accept(MediaType.APPLICATION_JSON)
        .get(new GenericType<List<Hub>>() {});
    return hubs;
  }

  public List<Spoke> getSpokes() {
    final List<Spoke> spokes = base
        .path("rest/v1/trucking/spokes")
        .request()
        .accept(MediaType.APPLICATION_JSON)
        .get(new GenericType<List<Spoke>>() {});
    return spokes;
  }

  public List<TruckType> getTruckTypes() {
    final List<TruckType> truckTypes = base
        .path("rest/v1/trucking/truckTypes")
        .request()
        .accept(MediaType.APPLICATION_JSON)
        .get(new GenericType<List<TruckType>>() {});
    return truckTypes;
  }

  public Solution getSolution() {
    final Solution solution = base
        .path("rest/v1/trucking/solution")
        .request()
        .accept(MediaType.APPLICATION_JSON)
        .get(Solution.class);
    return solution;
  }

  public void deleteSolution(final String id) {
    base
    .path("rest/v1/trucking/solution")
    .path(id)
    .request()
    .delete();
  }
}
