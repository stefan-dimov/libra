/*******************************************************************************
 *   Copyright (c) 2010 Eteration A.S. and others.
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *  
 *   Contributors:
 *      Naci Dai and Murat Yener, Eteration A.S. - Initial API and implementation
 *******************************************************************************/
package org.eclipse.libra.framework.felix;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


public class FelixPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.libra.framework.felix"; //$NON-NLS-1$

	// The shared instance
	private static FelixPlugin plugin;
	
	/**
	 * The constructor
	 */
	public FelixPlugin() {
		super();
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static FelixPlugin getDefault() {
		return plugin;
	}
	
	

	public static String getPreference(String id) {
		return Platform.getPreferencesService().getString(PLUGIN_ID, id, "", null);
	}
	
	public static void setPreference(String id, String value) {
		(new DefaultScope()).getNode(PLUGIN_ID).put(id, value);
	}
	
	public static IFelixVersionHandler getFelixVersionHandler(String id) {
//		if (id.indexOf("runtime") > 0)
//			id = id.substring(0, 30) + id.substring(38);
//		if (true)//"abc".equals(id))
			return new Felix2Handler();
	}


}
