/*******************************************************************************
 * Copyright (c) 2010, 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Kaloyan Raev (SAP AG) - initial API and implementation
 *******************************************************************************/
package org.eclipse.libra.facet;

import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.pde.core.project.IBundleProjectService;
import org.eclipse.pde.internal.core.IPluginModelListener;
import org.eclipse.pde.internal.core.PDECore;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * The activator class controls the plug-in life cycle
 */
@SuppressWarnings("restriction")
public class Activator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.libra.facet"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	private ServiceReference<IBundleProjectService> ref;
	private IBundleProjectService service;
	private IPluginModelListener pdeModelListener;
	private IResourceChangeListener resChangeListener;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		this.ref = context.getServiceReference(IBundleProjectService.class);
		this.service = (IBundleProjectService) context.getService(ref);
		addListeners();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		removeListeners();
		context.ungetService(this.ref);
		
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	public IBundleProjectService getBundleProjectService() {
		return service;
	}

	private void addListeners() {
		pdeModelListener = OSGiBundleFacetUtils.addPDEModelListener();
		resChangeListener = OSGiBundleFacetUtils.addResChangeListener();
	}
	
	private void removeListeners() {
		PDECore.getDefault().getModelManager().removePluginModelListener(pdeModelListener);
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(resChangeListener);
	}
	
	public static void logError(String msg) {
        logError(msg, null);
    }

	/**
	 * Log the specified exception or error.
	 */
	public static void logError(Throwable throwable) {
		logError(throwable.getLocalizedMessage(), throwable);
	}

	/**
	 * Log the specified message and exception or error.
	 */
	public static void logError(String msg, Throwable throwable) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, msg, throwable));
	}
	
	/**
	 * Log the specified status.
	 */
	public static void log(IStatus status) {
		plugin.getLog().log(status);
    }
		
	/**
	 * Log the specified message and exception or error.
	 */
	public static void logInfo(String msg) {
		log(new Status(IStatus.INFO, PLUGIN_ID, IStatus.OK, msg, null));
	}

}

