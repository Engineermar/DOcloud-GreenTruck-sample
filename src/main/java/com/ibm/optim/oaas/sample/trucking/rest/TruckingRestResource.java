package com.ibm.optim.oaas.sample.trucking.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import com.ibm.optim.oaas.sample.trucking.ejb.TruckingManager;
import com.ibm.optim.oaas.sample.trucking.model.Hub;
import com.ibm.optim.oaas.sample.trucking.model.Shipment;
import com.ibm.optim.oaas.sample.trucking.model.Spoke;
import com.ibm.optim.oaas.sample.trucking.model.TruckType;
import com.mongodb.DBObject;

/**
 * REST trucking resource.
 */
@Path("trucking")
//@Api(value = "/trucking", description = "Trucking sample API")
public class TruckingRestResource {
  private final TruckingManager manager;

  @Context
  HttpServletRequest request;

  public TruckingRestResource(final TruckingManager manager) {
    this.manager = manager;
  }

  //@ApiOperation(value = "Initializes the data store with demo data.")
  @POST
  @Path("/initialize")
  public void initialize() {
    manager.initialize();
  }

  //@ApiOperation(value = "Initializes the data store with demo data.")
  @POST
  @Path("/deleteAllJobs")
  public void deleteAllJobs() {
    manager.deleteAllJobs();
  }

  //@ApiOperation(value = "Returns the input data sent to the engine for optimization.")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/snapshot")
  public StreamingOutput snapshot() {
    return new StreamingOutput() {
      @Override
      public void write(final OutputStream output) throws IOException, WebApplicationException {
        try {
          manager.getSnapshot(output);
        } catch (final IOException e) {
          throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
      }
    };
  }

  //@ApiOperation(value = "Returns the list of hubs.", response = Hub.class, responseContainer = "List")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/hubs")
  //@ApiResponses(value = { @ApiResponse(code = 200, message = "The request executed successfully and the list of hubs is returned.") })
  public List<Hub> getHubs() {
    return manager.getHubs();
  }

  //@ApiOperation(value = "Returns the list of spokes.", response = Spoke.class, responseContainer = "List")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/spokes")
  //@ApiResponses(value = { @ApiResponse(code = 200, message = "The request executed successfully and the list of spokes is returned.") })
  public List<Spoke> getSpokes() {
    return manager.getSpokes();
  }

  //@ApiOperation(value = "Returns the list of shipments.", response = Shipment.class, responseContainer = "List")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/shipments")
  //@ApiResponses(value = { @ApiResponse(code = 200, message = "The request executed successfully and the list of shipments is returned.") })
  public List<Shipment> getShipments() {
    return manager.getShipments();
  }

  //@ApiOperation(value = "Adds a new shipment.", response = Shipment.class)
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/shipments")
  //@ApiResponses(value = { @ApiResponse(code = 200, message = "The request executed successfully and the added shipment is returned.") })
  public Shipment addShitment(final Shipment s) {
    return manager.addShipment(s);
  }

  //@ApiOperation(value = "Deletes all shipments.")
  @DELETE
  @Path("/shipments")
  //@ApiResponses(value = { @ApiResponse(code = 204, message = "The request executed successfully and the shipments have been deleted.") })
  public Response deleteShipments() {
    manager.deleteShipments();
    return Response.status(204).build();
  }

  //@ApiOperation(value = "Returns a shipment.", response = Shipment.class)
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/shipments/{id}")
  //@ApiResponses(value = { @ApiResponse(code = 200, message = "The request executed successfully and the shipment is returned.") })
  public Response getShipment(@PathParam("id") final String id) {
    final Shipment s = manager.getShipment(id);
    if (s != null) {
      return Response.status(200).entity(s).build();
    } else {
      return Response.status(404).build();
    }
  }

  //@ApiOperation(value = "Deletes a shipment.")
  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/shipments/{id}")
  //@ApiResponses(value = { @ApiResponse(code = 204, message = "The request executed successfully and the shipment has been deleted.") })
  public Response deleteShipment(@PathParam("id") final String id) {
    manager.deleteShipment(id);
    return Response.status(204).build();
  }

  //@ApiOperation(value = "Updates a shipment.")
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/shipments/{id}")
  //@ApiResponses(value = { @ApiResponse(code = 204, message = "The request executed successfully and the shipment has been updated.") })
  public Response updateShipment(@PathParam("id") final String id, final Shipment s) {
    if (!s.getId().equals(id)) {
      return Response.status(400).entity("Id mismatch").type(MediaType.TEXT_PLAIN_TYPE).build();
    }
    manager.updateShipment(s);
    return Response.status(204).build();
  }

  //@ApiOperation(value = "Returns the list of truck types.", response = TruckType.class, responseContainer = "List")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/truckTypes")
  //@ApiResponses(value = { @ApiResponse(code = 200, message = "The request executed successfully and the list of truck types is returned.") })
  public List<TruckType> getTruckTypes() {
    return manager.getTruckTypes();
  }

  //@ApiOperation(value = "Request to optimize the latest data (synchronously).")
  @POST
  @Path("/solve")
  public void solve() {
    manager.solve();
  }

  //@ApiOperation(value = "Request to optimize the latest data (asynchronously).")
  @POST
  @Path("/solveAsync")
  public Response solveAsync() {
    try {
      manager.solveAsync();
      return Response.status(204).build();
    } catch (final Exception e) {
      return Response.status(400).entity(new RestStatus(400,e.getLocalizedMessage())).build();
    }
  }

  //@ApiOperation(value = "Returns the latest solution if any.")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/solution")
  public DBObject solution() {
    return manager.getSolution();
  }

  //@ApiOperation(value = "Deletes the latest solution if any.")
  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/solution")
  public void deleteSolution() {
    manager.deleteSolution();
  }
}
