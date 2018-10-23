package com.ibm.optim.oaas.sample.trucking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.ibm.optim.oaas.sample.trucking.model.Hub;
import com.ibm.optim.oaas.sample.trucking.model.Shipment;
import com.ibm.optim.oaas.sample.trucking.model.Solution;
import com.ibm.optim.oaas.sample.trucking.model.Spoke;
import com.ibm.optim.oaas.sample.trucking.model.TruckType;

public class TruckingIT {
  private final String SERVER = "http://localhost:9080/";

  @Test
  public void testShipments() throws Exception {
    final TruckingClient client = new TruckingClient(SERVER);

    client.initialize();

    final List<Shipment> shipments = client.getShipments();
    assertNotNull(shipments);
    assertEquals(shipments.size(), 30);

    final Shipment s = new Shipment();
    s.setOrigin("A");
    s.setDestination("F");
    s.setTotalVolume(500);

    final Shipment rs = client.addShipment(s);
    assertNotNull(rs.getId());
    assertEquals(s.getOrigin(), rs.getOrigin());
    assertEquals(s.getDestination(), rs.getDestination());
    assertEquals(s.getTotalVolume(), rs.getTotalVolume());

    final Shipment gs = client.getShipment(rs.getId());
    assertNotNull(gs);
    assertEquals(rs.getOrigin(), gs.getOrigin());
    assertEquals(rs.getDestination(), gs.getDestination());
    assertEquals(rs.getTotalVolume(), gs.getTotalVolume());

    client.deleteShipment(rs.getId());

    final Shipment ds = client.getShipment(rs.getId());
    assertNull(ds);

    client.deleteShipments();

    final List<Shipment> dshipments = client.getShipments();
    assertNotNull(dshipments);
    assertEquals(dshipments.size(), 0);
  }

  @Test
  public void testInitialize() throws Exception {
    final TruckingClient client = new TruckingClient(SERVER);

    client.initialize();

    final List<TruckType> truckTypes = client.getTruckTypes();
    assertNotNull(truckTypes);
    assertFalse(truckTypes.isEmpty());

    final List<Spoke> spokes = client.getSpokes();
    assertNotNull(spokes);
    assertFalse(spokes.isEmpty());

    final List<Hub> hubs = client.getHubs();
    assertNotNull(hubs);
    assertFalse(hubs.isEmpty());

    final List<Shipment> shipments = client.getShipments();
    assertNotNull(shipments);
    assertFalse(shipments.isEmpty());
  }

  @Test
  public void testSolve() throws Exception {
    final TruckingClient client = new TruckingClient(SERVER);

    client.initialize();

    client.solve();

    final Solution solution = client.getSolution();
    assertNotNull(solution);
    assertNotNull(solution.getJobid());
  }
}
