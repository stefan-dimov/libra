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

import org.eclipse.libra.framework.core.FrameworkCorePlugin;
import org.eclipse.osgi.util.NLS;

/**
 * Translated messages.
 */
public class Messages extends NLS {



	public static String loadingTask;
	public static String errorCouldNotLoadConfiguration;
	public static String errorOSGiBundlesOnly;
	public static String configurationEditorActionAddOsgiModule;
	public static String errorConfigurationProjectClosed;
	public static String errorNoConfiguration;

	static {
		NLS.initializeMessages(FrameworkCorePlugin.PLUGIN_ID + ".Messages", Messages.class);
	}
}