package com.ibm.optim.oaas.sample.trucking.ejb.impl;

import java.io.IOException;
import java.io.OutputStream;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.ibm.optim.oaas.client.OperationException;
import com.ibm.optim.oaas.client.job.AttachmentContentWriter;
import com.ibm.optim.oaas.client.job.AttachmentNotFoundException;
import com.ibm.optim.oaas.client.job.JobClient;
import com.ibm.optim.oaas.client.job.JobInput;
import com.ibm.optim.oaas.client.job.JobNotFoundException;
import com.ibm.optim.oaas.client.job.SubscriptionException;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * Class in charge of streaming input data to the DOcloud service. As input data
 * can be large, this implementation shows how to query the underlying MongoDB
 * datastore and stream the content to DOcloud without building a complete data
 * model in memory.
 *
 */
public class TruckingJobInput implements JobInput {

	DB mongoDB;
	int maxTrucks = 100;
	int maxVolume = 5000;

	/**
	 * Creates an input data controller using the given MongoDB datastore.
	 * 
	 * @param db
	 *            the MongoDB datastore.
	 */
	public TruckingJobInput(DB db) {
		mongoDB = db;
	}

	public DB getDB() {
		return mongoDB;
	}

	public int getMaxTrucks() {
		return maxTrucks;
	}

	public void setMaxTrucks(int maxTrucks) {
		this.maxTrucks = maxTrucks;
	}

	public int getMaxVolume() {
		return maxVolume;
	}

	public void setMaxVolume(int maxVolume) {
		this.maxVolume = maxVolume;
	}

	@Override
	public String getName() {
		return "trucking.json";
	}

	@Override
	public long getLength() {
		return -1;
	}

	@Override
	public boolean isRepeatable() {
		return false;
	}

	@Override
	public void upload(JobClient client, String jobid)
			throws OperationException, IOException, JobNotFoundException,
			AttachmentNotFoundException, SubscriptionException {
		client.uploadJobAttachment(jobid, getName(),
				new AttachmentContentWriter() {

					@Override
					public void writeTo(OutputStream outstream)
							throws IOException {
						serialize(outstream);
					}

					@Override
					public boolean isRepeatable() {
						return TruckingJobInput.this.isRepeatable();
					}

				});

	}

	public void serialize(OutputStream outstream) throws IOException,
			JsonProcessingException {

		JsonFactory factory = new JsonFactory();
		JsonGenerator jgen = factory.createGenerator(outstream);

		jgen.writeStartObject();

		serializeParameters(jgen);
		serializeHubs(jgen);
		serializeSpokes(jgen);
		serializeTruckTypes(jgen);
		serializeLoadTimes(jgen);
		serializeRoutes(jgen);
		serializeShipments(jgen);

		jgen.writeEndObject();
		jgen.flush();
	}

	private void serializeParameters(JsonGenerator jgen) throws IOException,
			JsonProcessingException {
		jgen.writeObjectFieldStart("Parameters");
		jgen.writeNumberField("maxTrucks", getMaxTrucks());
		jgen.writeNumberField("maxVolume", getMaxVolume());
		jgen.writeEndObject();
	}

	private void serializeTruckTypes(JsonGenerator jgen) throws IOException,
			JsonProcessingException {
		jgen.writeArrayFieldStart("TruckTypes");
		DBCursor c = getDB().getCollection("truckTypes").find();
		while (c.hasNext()) {
			DBObject obj = c.next();
			jgen.writeStartObject();
			jgen.writeStringField("truckType", obj.get("_id").toString());
			jgen.writeNumberField("capacity",
					((Number) obj.get("capacity")).intValue());
			jgen.writeNumberField("costPerMile",
					((Number) obj.get("costPerMile")).intValue());
			jgen.writeNumberField("milesPerHour",
					((Number) obj.get("milesPerHour")).intValue());
			jgen.writeEndObject();
		}
		jgen.writeEndArray();
	}

	private void serializeSpokes(JsonGenerator jgen) throws IOException,
			JsonProcessingException {
		jgen.writeArrayFieldStart("Spokes");
		DBCursor c = getDB().getCollection("spokes").find();
		while (c.hasNext()) {
			DBObject obj = c.next();
			jgen.writeStartObject();
			jgen.writeStringField("name", obj.get("_id").toString());
			jgen.writeNumberField("minDepTime",
					((Number) obj.get("minDepTime")).intValue());
			jgen.writeNumberField("maxArrTime",
					((Number) obj.get("maxArrTime")).intValue());
			jgen.writeEndObject();
		}
		jgen.writeEndArray();
	}

	private void serializeHubs(JsonGenerator jgen) throws IOException,
			JsonProcessingException {
		jgen.writeArrayFieldStart("Hubs");
		DBCursor c = getDB().getCollection("hubs").find(new BasicDBObject(),
				new BasicDBObject().append("_id", 1)); // select _id only
		while (c.hasNext()) {
			DBObject obj = c.next();
			jgen.writeStartObject();
			jgen.writeStringField("name", obj.get("_id").toString());
			jgen.writeEndObject();
		}
		jgen.writeEndArray();
	}

	private void serializeLoadTimes(JsonGenerator jgen) throws IOException,
			JsonProcessingException {
		jgen.writeArrayFieldStart("LoadTimes");
		/**
		 * db.hubs.aggregate([ {$project: { "loadtimes" : 1}}, {$unwind :
		 * "$loadtimes"}, {$project: { "truckType" : "$loadtimes.truckType",
		 * "loadTime" : "$loadtimes.loadTime"}}])
		 */

		AggregationOutput agg = getDB().getCollection("hubs").aggregate(
				new BasicDBObject().append("$project",
						new BasicDBObject().append("loadtimes", 1)),
				new BasicDBObject().append("$unwind", "$loadtimes"),
				new BasicDBObject().append(
						"$project",
						new BasicDBObject().append("truckType",
								"$loadtimes.truckType").append("loadTime",
								"$loadtimes.loadTime")));

		for (DBObject obj : agg.results()) {
			jgen.writeStartObject();
			jgen.writeStringField("hub", obj.get("_id").toString());
			jgen.writeStringField("truckType", obj.get("truckType").toString());
			jgen.writeNumberField("loadTime",
					((Number) obj.get("loadTime")).intValue());
			jgen.writeEndObject();
		}
		jgen.writeEndArray();
	}

	private void serializeRoutes(JsonGenerator jgen) throws IOException,
			JsonProcessingException {
		jgen.writeArrayFieldStart("Routes");
		/**
		 * db.hubs.aggregate([ {$project: { "routes" : 1}}, {$unwind :
		 * "$routes"}, {$project: { "spoke" : "$routes.spoke", "distance" :
		 * "$routes.distance"}}])
		 */

		AggregationOutput agg = getDB().getCollection("hubs").aggregate(
				new BasicDBObject().append("$project",
						new BasicDBObject().append("routes", 1)),
				new BasicDBObject().append("$unwind", "$routes"),
				new BasicDBObject().append("$project",
						new BasicDBObject().append("spoke", "$routes.spoke")
								.append("distance", "$routes.distance")));

		for (DBObject obj : agg.results()) {
			jgen.writeStartObject();
			jgen.writeStringField("spoke", obj.get("spoke").toString());
			jgen.writeStringField("hub", obj.get("_id").toString());
			jgen.writeNumberField("distance",
					((Number) obj.get("distance")).intValue());
			jgen.writeEndObject();
		}
		jgen.writeEndArray();
	}

	private void serializeShipments(JsonGenerator jgen) throws IOException,
			JsonProcessingException {
		jgen.writeArrayFieldStart("Shipments");
		DBCursor c = getDB().getCollection("shipments").find();
		while (c.hasNext()) {
			DBObject obj = c.next();
			jgen.writeStartObject();
			jgen.writeStringField("origin", obj.get("origin").toString());
			jgen.writeStringField("destination", obj.get("destination")
					.toString());
			jgen.writeNumberField("totalVolume",
					((Number) obj.get("totalVolume")).intValue());
			jgen.writeEndObject();
		}
		jgen.writeEndArray();
	}
}
