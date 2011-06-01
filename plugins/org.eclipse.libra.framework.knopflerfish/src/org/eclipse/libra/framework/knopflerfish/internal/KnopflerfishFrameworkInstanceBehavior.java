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
package org.eclipse.libra.framework.knopflerfish.internal;

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
import org.eclipse.libra.framework.knopflerfish.IKnopflerfishVersionHandler;
import org.eclipse.libra.framework.knopflerfish.Knopflerfish31Handler;
import org.eclipse.libra.framework.knopflerfish.KnopflerfishFrameworkInstance;
import org.eclipse.libra.framework.knopflerfish.Messages;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;


public class KnopflerfishFrameworkInstanceBehavior extends
		OSGIFrameworkInstanceBehaviorDelegate {

	protected transient KnopflerfishConfigurationPublishHelper publishHelper = new KnopflerfishConfigurationPublishHelper(
			this);

	/**
	 * FelixRuntimeInstanceBehavior.
	 */
	public KnopflerfishFrameworkInstanceBehavior() {
		super();
	}
	
	public IKnopflerfishVersionHandler getKnopflerfishVersionHandler() {
		return getKnopflerfishRuntimeInstance().getKnopflerfishVersionHandler();
	}

	public KnopflerfishFrameworkInstance getKnopflerfishRuntimeInstance() {
		return (KnopflerfishFrameworkInstance) getServer().loadAdapter(KnopflerfishFrameworkInstance.class, null);
	}

	public String getFrameworkClass() {
		return getKnopflerfishVersionHandler().getFrameworkClass();
	}

	public String[] getFrameworkProgramArguments(boolean starting) {
		return getKnopflerfishVersionHandler().getFrameworkProgramArguments(
				getBaseDirectory(), getFrameworkInstance().isDebug(), starting);
	}

	public String[] getExcludedFrameworkProgramArguments(boolean starting) {
		return getKnopflerfishVersionHandler().getExcludedFrameworkProgramArguments(
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

		return getKnopflerfishVersionHandler().getFrameworkVMArguments(installPath, null, deployPath, false);
	}
	
	protected void publishServer(int kind, IProgressMonitor monitor)
			throws CoreException {
		if (getServer().getRuntime() == null)
			return;

		IPath confDir = getBaseDirectory();
		IStatus status = getKnopflerfishVersionHandler().prepareDeployDirectory(
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
		
		FrameworkInstanceConfiguration felixConfiguration;
		try {
			felixConfiguration = getKnopflerfishRuntimeInstance().getKnopflerfishConfiguration();
			publishHelper.exportBundles(modules, felixConfiguration, confDir);
			((Knopflerfish31Handler)getKnopflerfishVersionHandler()).prepareFrameworkConfigurationFile(installPath,confDir,publishHelper,felixConfiguration);

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

		// TODO OSAMI PUBLISH

		setModulePublishState(moduleTree, IServer.PUBLISH_STATE_NONE);

		saveModulePublishLocations(p);
	}
}
