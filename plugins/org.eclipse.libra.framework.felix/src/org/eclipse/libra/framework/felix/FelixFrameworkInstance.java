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

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.libra.framework.core.FrameworkInstanceConfiguration;
import org.eclipse.libra.framework.core.FrameworkInstanceDelegate;
import org.eclipse.libra.framework.core.OSGIFrameworkInstanceBehaviorDelegate;
import org.eclipse.libra.framework.core.Trace;
import org.eclipse.libra.framework.felix.internal.FelixRuntimeInstanceBehavior;
import org.eclipse.pde.internal.core.target.TargetPlatformService;
import org.eclipse.pde.internal.core.target.provisional.IBundleContainer;
import org.eclipse.pde.internal.core.target.provisional.ITargetDefinition;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IRuntime;


public class FelixFrameworkInstance extends FrameworkInstanceDelegate implements
		IFelixFrameworkInstance {

	protected transient IFelixVersionHandler versionHandler;

	@Override
	public IStatus canModifyModules(IModule[] add, IModule[] remove) {
		IStatus status = super.canModifyModules(add, remove);
		if (!status.isOK())
			return status;

		if (getFelixVersionHandler() == null)
			return new Status(IStatus.ERROR, FelixPlugin.PLUGIN_ID, 0,
					Messages.errorNoRuntime, null);

		if (add != null) {
			int size = add.length;
			for (int i = 0; i < size; i++) {
				IModule module = add[i];
				IStatus status2 = getFelixVersionHandler().canAddModule(module);
				if (status2 != null && !status2.isOK())
					return status2;
			}
		}
		return Status.OK_STATUS;
	}

	@Override
	public void setDefaults(IProgressMonitor monitor) {
		super.setDefaults(monitor);
		try {
			getFelixConfiguration();
		} catch (CoreException e) {
			Trace.trace(Trace.SEVERE, "Can't setup for Felix configuration.",e);
		}
	}
	@Override
	public void importRuntimeConfiguration(IRuntime runtime,
			IProgressMonitor monitor) throws CoreException {

		super.importRuntimeConfiguration(runtime, monitor);
		OSGIFrameworkInstanceBehaviorDelegate fsb = (OSGIFrameworkInstanceBehaviorDelegate)getServer().loadAdapter(FelixRuntimeInstanceBehavior.class, null);
		if(fsb != null ){
			IPath tempDir = fsb.getTempDirectory();
			if (!tempDir.isAbsolute()) {
					IPath rootPath = ResourcesPlugin.getWorkspace().getRoot().getLocation();
					tempDir = rootPath.append(tempDir);
			}
			setInstanceDirectory(tempDir.toPortableString());
		}
		
		try {
			getFelixConfiguration();
		} catch (CoreException e) {
			Trace.trace(Trace.SEVERE, "Can't setup for Felix configuration.",e);
		}
	}
	
	
	public FelixFramework getFelixRuntime() {
		if (getServer().getRuntime() == null)
			return null;
		return (FelixFramework) getServer().getRuntime().loadAdapter(
				FelixFramework.class, null);
	}

	public IFelixVersionHandler getFelixVersionHandler() {
		if (versionHandler == null) {
			if (getServer().getRuntime() == null || getFelixRuntime() == null)
				return null;

			versionHandler = getFelixRuntime().getVersionHandler();
		}
		return versionHandler;
	}

	public FrameworkInstanceConfiguration getFelixConfiguration() throws CoreException {

		return getFrameworkInstanceConfiguration();

	}

	@SuppressWarnings("restriction")
	private IBundleContainer[] getDefaultBundleContainers(IPath installPath) {
		IBundleContainer[] containers = new IBundleContainer[2];
		containers[0] = TargetPlatformService.getDefault()
		.newDirectoryContainer(
				installPath.append("bin").makeAbsolute()
						.toPortableString());
		containers[1] = TargetPlatformService.getDefault()
		.newDirectoryContainer(
				installPath.append("bundle").makeAbsolute()
						.toPortableString());
		return containers;
		
	}

	@SuppressWarnings("restriction")
	@Override
	public ITargetDefinition createDefaultTarget() throws CoreException {
		IPath installPath = getServer().getRuntime().getLocation();

		ITargetDefinition targetDefinition = TargetPlatformService.getDefault().newTarget();
		targetDefinition.setName(getServer().getName());
		IBundleContainer[] containers = getDefaultBundleContainers(installPath);
		
		targetDefinition.setBundleContainers(containers);
		TargetPlatformService.getDefault().saveTargetDefinition(
				targetDefinition);
		return targetDefinition;
	}

}
