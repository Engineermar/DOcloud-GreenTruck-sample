package com.ibm.optim.oaas.sample.trucking;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;

import com.ibm.optim.oaas.sample.trucking.model.Hub;
import com.ibm.optim.oaas.sample.trucking.model.Shipment;
import com.ibm.optim.oaas.sample.trucking.model.Solution;
import com.ibm.optim.oaas.sample.trucking.model.Spoke;
import com.ibm.optim.oaas.sample.trucking.model.TruckType;

public class TruckingIT {

	final String SERVER = "http://localhost:9080/";

	@Test
	public void testShipments() throws Exception {
		TruckingClient client = new TruckingClient(SERVER);

		client.initialize();

		List<Shipment> shipments = client.getShipments();
		assertNotNull(shipments);
		assertEquals(shipments.size(), 30);

		Shipment s = new Shipment();
		s.setOrigin("A");
		s.setDestination("F");
		s.setTotalVolume(500);

		Shipment rs = client.addShipment(s);
		assertNotNull(rs.getId());
		assertEquals(s.getOrigin(), rs.getOrigin());
		assertEquals(s.getDestination(), rs.getDestination());
		assertEquals(s.getTotalVolume(), rs.getTotalVolume());

		Shipment gs = client.getShipment(rs.getId());
		assertNotNull(gs);
		assertEquals(rs.getOrigin(), gs.getOrigin());
		assertEquals(rs.getDestination(), gs.getDestination());
		assertEquals(rs.getTotalVolume(), gs.getTotalVolume());

		client.deleteShipment(rs.getId());

		Shipment ds = client.getShipment(rs.getId());
		assertNull(ds);

		client.deleteShipments();

		List<Shipment> dshipments = client.getShipments();
		assertNotNull(dshipments);
		assertEquals(dshipments.size(), 0);

	}

	@Test
	public void testInitialize() throws Exception {
		TruckingClient client = new TruckingClient(SERVER);

		client.initialize();

		List<TruckType> truckTypes = client.getTruckTypes();
		assertNotNull(truckTypes);
		assertFalse(truckTypes.isEmpty());

		List<Spoke> spokes = client.getSpokes();
		assertNotNull(spokes);
		assertFalse(spokes.isEmpty());

		List<Hub> hubs = client.getHubs();
		assertNotNull(hubs);
		assertFalse(hubs.isEmpty());

		List<Shipment> shipments = client.getShipments();
		assertNotNull(shipments);
		assertFalse(shipments.isEmpty());
	}

	@Test
	public void testSolve() throws Exception {
		TruckingClient client = new TruckingClient(SERVER);

		client.initialize();

		client.solve();

		Solution solution = client.getSolution();
		assertNotNull(solution);
		assertNotNull(solution.getJobid());

	}
}
