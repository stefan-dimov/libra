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

import org.eclipse.core.runtime.CoreException;

public interface IOSGIFrameworkInstance {

	
	public static final String PROPERTY_DEBUG = "debug";
	public static final String PROPERTY_INSTANCE_DIR = "instanceDir";
	public static final String PROPERTY_DEPLOY_DIR = "deployDir";
	
	/**
	 * Gets the directory where the server instance exists.  If not set,
	 * the instance directory is derived from the testEnvironment setting.  
	 * 
	 * @return directory where the server instance exists. Returns null
	 * if not set.
	 */
	public String getInstanceDirectory();

	/**
	 * Gets the directory to which web applications are to be deployed.
	 * If relative, it is relative to the runtime base directory for the
	 * server.
	 * 
	 * @return directory where web applications are deployed
	 */
	public String getDeployDirectory();

	public boolean isDebug();
	public FrameworkInstanceConfiguration getFrameworkInstanceConfiguration() throws CoreException;
}
