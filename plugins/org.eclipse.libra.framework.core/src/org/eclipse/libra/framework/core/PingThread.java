/*******************************************************************************
 *    Copyright (c) 2010 Eteration A.S. and others.
 *    All rights reserved. This program and the accompanying materials
 *    are made available under the terms of the Eclipse Public License v1.0
 *    which accompanies this distribution, and is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 *    
 *     Contributors:
 *        IBM Corporation - initial API and implementation
 *           - This code is based on WTP SDK frameworks and Tomcat Server Adapters
 *           org.eclipse.jst.server.core
 *           org.eclipse.jst.server.ui
 *           
 *       Naci Dai and Murat Yener, Eteration A.S. 
 *******************************************************************************/
package org.eclipse.libra.framework.core;

import org.eclipse.wst.server.core.IServer;

/**
 * Thread used to ping server to test when it is started.
 */
public class PingThread {
	// delay before pinging starts
	private static final int PING_DELAY = 2000;

	// delay between pings
	private static final int PING_INTERVAL = 250;

	// maximum number of pings before giving up
	private int maxPings;

	private boolean stop = false;
	private String url;
	private IServer server;
	private OSGIFrameworkInstanceBehaviorDelegate behaviour;

	/**
	 * Create a new PingThread.
	 * 
	 * @param server
	 * @param url
	 * @param maxPings the maximum number of times to try pinging, or -1 to continue forever
	 * @param behaviour
	 */
	public PingThread(IServer server, String url, int maxPings, OSGIFrameworkInstanceBehaviorDelegate behaviour) {
		super();
		this.server = server;
		this.url = url;
		this.maxPings = maxPings;
		this.behaviour = behaviour;
		Thread t = new Thread("Felix Ping Thread") {
			public void run() {
				ping();
			}
		};
		t.setDaemon(true);
		t.start();
	}

	/**
	 * Ping the server until it is started. Then set the server
	 * state to STATE_STARTED.
	 */
	protected void ping() {
		int count = 0;
		try {
			Thread.sleep(PING_DELAY);
		} catch (Exception e) {
			// ignore
		}
		behaviour.setServerStarted();
		stop = true;

	}

	/**
	 * Tell the pinging to stop.
	 */
	public void stop() {
		Trace.trace(Trace.FINEST, "Ping: stopping");
		stop = true;
	}
}
