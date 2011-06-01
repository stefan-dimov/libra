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
package org.eclipse.libra.framework.equinox.internal;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.libra.framework.core.FrameworkInstanceConfiguration;
import org.eclipse.libra.framework.core.OSGIFrameworkInstanceBehaviorDelegate;
import org.eclipse.libra.framework.core.ProgressUtil;
import org.eclipse.libra.framework.core.Trace;
import org.eclipse.libra.framework.equinox.EquinoxFrameworkInstance;
import org.eclipse.libra.framework.equinox.IEquinoxVersionHandler;
import org.eclipse.libra.framework.equinox.Messages;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;


public class EquinoxFrameworkInstanceBehavior extends
		OSGIFrameworkInstanceBehaviorDelegate {

	protected transient EquinoxConfigurationPublishHelper publishHelper = new EquinoxConfigurationPublishHelper(
			this);

	/**
	 * EquinoxFrameworkInstanceBehavior.
	 */
	public EquinoxFrameworkInstanceBehavior() {
		super();
	}
	
	public IEquinoxVersionHandler getEquinoxVersionHandler() {
		return getEquinoxRuntimeInstance().getEquinoxVersionHandler();
	}

	public EquinoxFrameworkInstance getEquinoxRuntimeInstance() {
		return (EquinoxFrameworkInstance) getServer().loadAdapter(EquinoxFrameworkInstance.class, null);
	}

	public String getFrameworkClass() {
		return getEquinoxVersionHandler().getFrameworkClass();
	}

	public String[] getFrameworkProgramArguments(boolean starting) {
		return getEquinoxVersionHandler().getFrameworkProgramArguments(
				getBaseDirectory(), getFrameworkInstance().isDebug(), starting);
	}

	public String[] getExcludedFrameworkProgramArguments(boolean starting) {
		return getEquinoxVersionHandler().getExcludedFrameworkProgramArguments(
				getFrameworkInstance().isDebug(), starting);
	}

	public String[] getFrameworkVMArguments() {
		IPath installPath = getServer().getRuntime().getLocation();
		// If installPath is relative, convert to canonical path and hope for
		// the best
		if (!installPath.isAbsolute()) {
			try {
				String installLoc = (new File(installPath.toOSString()))
						.getCanonicalPath();
				installPath = new Path(installLoc);
			} catch (IOException e) {
				// Ignore if there is a problem
			}
		}

		IPath deployPath = getBaseDirectory();
		// If deployPath is relative, convert to canonical path and hope for the
		// best
		if (!deployPath.isAbsolute()) {
			try {
				String deployLoc = (new File(deployPath.toOSString()))
						.getCanonicalPath();
				deployPath = new Path(deployLoc);
			} catch (IOException e) {
				// Ignore if there is a problem
			}
		}

		return getEquinoxVersionHandler().getFrameworkVMArguments(installPath, null, deployPath, false);
	}
	
	protected void publishServer(int kind, IProgressMonitor monitor)
			throws CoreException {
		if (getServer().getRuntime() == null)
			return;

		IPath confDir = getBaseDirectory();
		IStatus status = getEquinoxVersionHandler().prepareDeployDirectory(
				confDir);

		if (status != null && !status.isOK())
			throw new CoreException(status);

		monitor = ProgressUtil.getMonitorFor(monitor);
		monitor.beginTask(Messages.publishServerTask, 600);

		// TODO OSAMI 1) Cleanup 2) Backup and Publish,

		// if (status != null && !status.isOK())
		// throw new CoreException(status);

		monitor.done();

		setServerPublishState(IServer.PUBLISH_STATE_NONE);
	}

	@SuppressWarnings("rawtypes")
	protected void publishModules(int kind, List modules, List deltaKind2,
			MultiStatus multi, IProgressMonitor monitor) {


		IPath confDir = getBaseDirectory();

		FrameworkInstanceConfiguration equinoxConfiguration;
		try {
			equinoxConfiguration = getEquinoxRuntimeInstance().getEquinoxConfiguration();
			publishHelper.exportBundles(modules, equinoxConfiguration, confDir);
			getEquinoxVersionHandler().prepareFrameworkConfigurationFile(confDir,
					publishHelper.getWorkspaceBundles(equinoxConfiguration,"reference:file:", " ")+publishHelper.getServerModules(modules,"reference:file:", " " ),
					publishHelper.getTargetBundles(equinoxConfiguration,"reference:file:", " "));

		} catch (CoreException e) {
			Trace.trace(Trace.SEVERE, "Publishing failed", e);
		}
	}

	protected void publishModule(int kind, int deltaKind, IModule[] moduleTree,
			IProgressMonitor monitor) throws CoreException {
		if (getServer().getServerState() != IServer.STATE_STOPPED) {
			if (deltaKind == ServerBehaviourDelegate.ADDED
					|| deltaKind == ServerBehaviourDelegate.REMOVED)
				setServerRestartState(true);
		}

		Properties p = loadModulePublishLocations();

		setModulePublishState(moduleTree, IServer.PUBLISH_STATE_NONE);

		saveModulePublishLocations(p);
	}
}
