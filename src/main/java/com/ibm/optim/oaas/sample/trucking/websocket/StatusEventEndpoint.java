package com.ibm.optim.oaas.sample.trucking.websocket;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Websocket endpoint. When a client connects to the websocket endpoint it is
 * stored in a global list of sessions. Then the endpoint provides an API to
 * broadcast messages to all connected clients.
 *
 */
@ServerEndpoint(value = "/StatusEventEndpoint")
public class StatusEventEndpoint {

	private static Logger LOG = Logger.getLogger(StatusEventEndpoint.class
			.getName());

	/**
	 * Set of currently open sessions.
	 */
	private static final Set<Session> sessions = Collections
			.synchronizedSet(new HashSet<Session>());

	/**
	 * Object mapper used to generate JSON message content.
	 */
	private static ObjectMapper mapper = new ObjectMapper();

	/**
	 * Stores the last message sent.
	 */
	private static volatile RequestStatus lastStatus;

	@OnOpen
	public void onOpen(Session session, EndpointConfig ec) {

		LOG.log(Level.INFO, "New websoket session joining");

		// add the session to receive new broadcast messages
		sessions.add(session);

		// send the last broadcast message to this new session to be up to date
		RequestStatus status = lastStatus;
		if (status != null) {
			try {
				session.getBasicRemote().sendText(
						mapper.writeValueAsString(status));
			} catch (JsonProcessingException e) {
				LOG.log(Level.WARNING, "JSON error", e);
			} catch (IOException e) {
				LOG.log(Level.WARNING, "Message error", e);
			}
		}
	}

	@OnClose
	public void onClose(Session session, CloseReason reason) {
		LOG.log(Level.INFO, "Closing websoket session");
		sessions.remove(session);
	}

	/**
	 * Broadcasts a status.
	 * 
	 * @param status
	 *            the status.
	 */
	public static synchronized void broadcastStatus(RequestStatus status) {
		// if we already sent this message, just skip
		if (lastStatus != null && lastStatus.equals(status)) {
			return;
		}
		LOG.log(Level.INFO, "Boradcasting {0}, {1}, {2} ", new Object[] {
				status.isRequested(), status.getJobid(), status.getStatus() });

		// save this message as a reference
		lastStatus = status;

		// broadcast
		try {
			broadcastMessage(mapper.writeValueAsString(status));
		} catch (JsonProcessingException e) {
			LOG.log(Level.WARNING, "JSON error", e);
		}
	}

	/**
	 * Broadcasts a string message.
	 * 
	 * @param message
	 *            the message.
	 */
	private static void broadcastMessage(String message) {
		synchronized (sessions) {
			for (Session session : sessions) {
				try {
					if (session.isOpen()) {
						session.getBasicRemote().sendText(message);
					}
				} catch (IOException e) {
					LOG.log(Level.WARNING, "Boradcast error", e);
				}
			}
		}
	}

	@OnError
	public void onError(Throwable t) {
		LOG.log(Level.WARNING, "Websocket error", t);
	}
}
