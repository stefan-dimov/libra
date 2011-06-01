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
package org.eclipse.libra.framework.knopflerfish;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.frameworkadmin.BundleInfo;
import org.eclipse.libra.framework.core.FrameworkInstanceConfiguration;
import org.eclipse.libra.framework.core.FrameworkInstanceDelegate;
import org.eclipse.libra.framework.core.OSGIFrameworkInstanceBehaviorDelegate;
import org.eclipse.libra.framework.core.Trace;
import org.eclipse.libra.framework.knopflerfish.internal.KnopflerfishFrameworkInstanceBehavior;
import org.eclipse.pde.internal.core.target.TargetPlatformService;
import org.eclipse.pde.internal.core.target.provisional.IBundleContainer;
import org.eclipse.pde.internal.core.target.provisional.IResolvedBundle;
import org.eclipse.pde.internal.core.target.provisional.ITargetDefinition;
import org.eclipse.pde.internal.core.target.provisional.NameVersionDescriptor;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IRuntime;


public class KnopflerfishFrameworkInstance extends FrameworkInstanceDelegate
		implements IKnopflerfishFrameworkInstance {

	protected transient IKnopflerfishVersionHandler versionHandler;

	@Override
	public IStatus canModifyModules(IModule[] add, IModule[] remove) {
		IStatus status = super.canModifyModules(add, remove);
		if (!status.isOK())
			return status;

		if (getKnopflerfishVersionHandler() == null)
			return new Status(IStatus.ERROR, KnopflerfishPlugin.PLUGIN_ID, 0,
					Messages.errorNoRuntime, null);

		if (add != null) {
			int size = add.length;
			for (int i = 0; i < size; i++) {
				IModule module = add[i];
				IStatus status2 = getKnopflerfishVersionHandler().canAddModule(
						module);
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
			getKnopflerfishConfiguration();
		} catch (CoreException e) {
			Trace.trace(Trace.SEVERE, "Can't setup for Felix configuration.", e);
		}
	}

	@Override
	public void importRuntimeConfiguration(IRuntime runtime,
			IProgressMonitor monitor) throws CoreException {

		super.importRuntimeConfiguration(runtime, monitor);
		OSGIFrameworkInstanceBehaviorDelegate fsb = (OSGIFrameworkInstanceBehaviorDelegate) getServer()
				.loadAdapter(KnopflerfishFrameworkInstanceBehavior.class, null);
		if (fsb != null) {
			IPath tempDir = fsb.getTempDirectory();
			if (!tempDir.isAbsolute()) {
				IPath rootPath = ResourcesPlugin.getWorkspace().getRoot()
						.getLocation();
				tempDir = rootPath.append(tempDir);
			}
			setInstanceDirectory(tempDir.toPortableString());
		}

		try {
			getKnopflerfishConfiguration();
		} catch (CoreException e) {
			Trace.trace(Trace.SEVERE, "Can't setup for Felix configuration.", e);
		}
	}

	public KnopflerfishFramework getKnopflerfishRuntime() {
		if (getServer().getRuntime() == null)
			return null;
		return (KnopflerfishFramework) getServer().getRuntime().loadAdapter(
				KnopflerfishFramework.class, null);
	}

	public IKnopflerfishVersionHandler getKnopflerfishVersionHandler() {
		if (versionHandler == null) {
			if (getServer().getRuntime() == null
					|| getKnopflerfishRuntime() == null)
				return null;

			versionHandler = getKnopflerfishRuntime().getVersionHandler();
		}
		return versionHandler;
	}

	public FrameworkInstanceConfiguration getKnopflerfishConfiguration()
			throws CoreException {

		return getFrameworkInstanceConfiguration();

	}

	@SuppressWarnings("restriction")
	@Override
	public ITargetDefinition createDefaultTarget() throws CoreException {
		

		IPath installPath = getServer().getRuntime().getLocation();

		ITargetDefinition targetDefinition = TargetPlatformService.getDefault()
				.newTarget();
		targetDefinition.setName(getServer().getName());
		IBundleContainer[] containers = getDefaultBundleContainers(installPath);

		targetDefinition.setBundleContainers(containers);
		targetDefinition.resolve(new NullProgressMonitor());

		IResolvedBundle[] targetBundles = targetDefinition.getAllBundles();
		List<NameVersionDescriptor> includedB = new ArrayList<NameVersionDescriptor>();
		for (IResolvedBundle b : targetBundles) {
			if (b.getStatus().getSeverity() == IStatus.OK) {

				if (shouldInclude(b.getBundleInfo())) {
					if (b.getStatus().getCode() == IResolvedBundle.STATUS_PLUGIN_DOES_NOT_EXIST) {
						includedB.add(new NameVersionDescriptor(b
								.getBundleInfo().getSymbolicName(), null,
								NameVersionDescriptor.TYPE_PLUGIN));
					} else {
						includedB.add(new NameVersionDescriptor(b
								.getBundleInfo().getSymbolicName(), null));
					}
				}

			}

		}
		targetDefinition.setIncluded(includedB
				.toArray(new NameVersionDescriptor[includedB.size()]));

		TargetPlatformService.getDefault().saveTargetDefinition(
				targetDefinition);
		return targetDefinition;
	}

	private boolean shouldInclude(BundleInfo bundleInfo) {
		String bundles[] = {"log_api-3.0.5.jar",
				"console_api-3.0.1.jar",
				"cm_api-3.0.1.jar",
				"log-3.0.5.jar",
				"console-3.0.1.jar",
				"consoletty-3.0.1.jar",
				"frameworkcommands-3.0.3.jar",
				"logcommands-3.0.1.jar",
				"useradmin_api-3.0.1.jar"};
		
		for (String bundleName : bundles) {
			if(bundleInfo.getLocation().toString().indexOf(bundleName)>0)
				return true;
		}
		return false;
	}

	@SuppressWarnings("restriction")
	private IBundleContainer[] getDefaultBundleContainers(IPath installPath) {
		IBundleContainer[] containers = new IBundleContainer[8];
		containers[0] = TargetPlatformService.getDefault()
				.newDirectoryContainer(
						installPath.append("osgi").makeAbsolute()
								.toPortableString());
		containers[1] = TargetPlatformService.getDefault()
				.newDirectoryContainer(
						installPath.append("osgi").append("jars").append("log")
								.makeAbsolute().toPortableString());

		containers[2] = TargetPlatformService.getDefault()
				.newDirectoryContainer(
						installPath.append("osgi").append("jars").append("console").makeAbsolute()
								.toPortableString());

		containers[3] = TargetPlatformService.getDefault()
				.newDirectoryContainer(
						installPath.append("osgi").append("jars").append("cm")
								.makeAbsolute().toPortableString());
		containers[4] = TargetPlatformService.getDefault()
				.newDirectoryContainer(
						installPath.append("osgi").append("jars")
								.append("consoletty").makeAbsolute()
								.toPortableString());
		containers[5] = TargetPlatformService.getDefault()
				.newDirectoryContainer(
						installPath.append("osgi").append("jars")
								.append("frameworkcommands").makeAbsolute()
								.toPortableString());
		containers[6] = TargetPlatformService.getDefault()
				.newDirectoryContainer(
						installPath.append("osgi").append("jars")
								.append("logcommands").makeAbsolute()
								.toPortableString());
		containers[7] = TargetPlatformService.getDefault()
				.newDirectoryContainer(
						installPath.append("osgi").append("jars")
								.append("useradmin").makeAbsolute()
								.toPortableString());

		return containers;

	}

}
