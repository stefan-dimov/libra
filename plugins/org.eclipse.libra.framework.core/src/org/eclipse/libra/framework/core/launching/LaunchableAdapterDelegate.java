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
package org.eclipse.libra.framework.core.launching;

import org.eclipse.libra.framework.core.IOSGIFrameworkInstance;
import org.eclipse.libra.framework.core.Trace;
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.IServer;


/**
 * Launchable adapter delegate for OSGi resources.
 */
public class LaunchableAdapterDelegate extends
		org.eclipse.wst.server.core.model.LaunchableAdapterDelegate {
	/*
	 * @see LaunchableAdapterDelegate#getLaunchable(IServer, IModuleArtifact)
	 */
	public Object getLaunchable(IServer server, IModuleArtifact moduleObject) {
		Trace.trace(Trace.FINER, "IOSGIFrameworkInstance Launchable Adapter " + server + "-"
				+ moduleObject);
		if (server.getAdapter(IOSGIFrameworkInstance.class) == null)
			return null;
		// if (!(moduleObject instanceof Servlet) &&
		// !(moduleObject instanceof WebResource))
		return null;

	}
}
