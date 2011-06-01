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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.libra.framework.core.internal.dependency.DependencyUtil;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.ModelEntry;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.core.target.provisional.ITargetDefinition;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.internal.Module;
import org.eclipse.wst.server.core.model.ServerDelegate;


public abstract class FrameworkInstanceDelegate extends ServerDelegate implements
		IOSGIFrameworkInstance {

	public static final String DEFAULT_DEPLOYDIR = "auto";

	private FrameworkInstanceConfiguration configuration;

	public FrameworkInstanceDelegate() {
		super();
	}

	@Override
	public IModule[] getChildModules(IModule[] module) {
		if (module == null)
			return null;

		if ("osgi.bundle".equalsIgnoreCase(module[0].getModuleType().getId())) {

			IPluginModelBase modelBase = null;
			IPluginModelBase[] plugins = PluginRegistry.getActiveModels();
			for (IPluginModelBase iPluginModelBase : plugins) {
				if (module[0].getName().equalsIgnoreCase(
						iPluginModelBase.getBundleDescription().getName())) {
					modelBase = iPluginModelBase;
					break;
				}
			}
			// get dependencies
			if (modelBase != null) {
				Object[] dependents = DependencyUtil.getDependencies(modelBase
						.getBundleDescription());

				List<IModule> modules = new ArrayList<IModule>();
				for (Object dep : dependents) {
					BundleDescription depBundle = (BundleDescription) dep;
					ModelEntry entry = PluginRegistry.findEntry(depBundle
							.getSymbolicName());
					if (entry.hasWorkspaceModels()) { // isWorkspaceBundle(depBundle)){
						IPluginModelBase base = entry.getModel();
						IModule[] iModules = ((Module) module[0])
								.getModuleFactory().getModules(
										base.getUnderlyingResource()
												.getProject(),
										new NullProgressMonitor());
						for (IModule iModule : iModules) {
							modules.add(iModule);
						}
						// modelBase e eklemek gerek
						// module yap liste koy
					}

				}
				IModule[] moduleArr=new IModule[modules.size()];
				modules.toArray(moduleArr);
				return moduleArr;
			}
		}
		return new IModule[0];
	}

	@Override
	public IModule[] getRootModules(IModule module) throws CoreException {
		if (module == null)
			return null;

		if ("osgi.bundle".equalsIgnoreCase(module.getModuleType().getId())) {

			IPluginModelBase modelBase = null;
			IPluginModelBase[] plugins = PluginRegistry.getActiveModels();
			for (IPluginModelBase iPluginModelBase : plugins) {
				if (module.getName().equalsIgnoreCase(
						iPluginModelBase.getBundleDescription().getName())) {
					modelBase = iPluginModelBase;
					break;
				}
			}
			// get dependencies
			if (modelBase != null) {
				//modelbase bu listeye çıkacaklardan herhangibirinin childimi??
			}
		}
		
		// TODO - OSAMI If this module is a child composed in a composite,
		// return the module that represents the parent
		// if this is the parent then - no action needed. Just return the
		// composite
		// if ("osami.bundle".equals(module.getModuleType().getId())) {
		// IStatus status = canModifyModules(new IModule[] { module }, null);
		// if (status == null || !status.isOK())
		// throw new CoreException(status);
		return new IModule[] { module };
		// }

		// return null;
	}

	public IStatus canModifyModules(IModule[] add, IModule[] remove) {
		if (add != null) {
			int size = add.length;
			for (int i = 0; i < size; i++) {
				IModule module = add[i];
				if (!"osgi.bundle".equals(module.getModuleType().getId()))
					return new Status(IStatus.ERROR,
							FrameworkCorePlugin.PLUGIN_ID, 0,
							Messages.errorOSGiBundlesOnly, null);

				// TODO - OSAMI After adding faceted projects enable this
				// check....

				// if (module.getProject() != null) {
				// status = FacetUtil.verifyFacets(module.getProject(),
				// getServer());
				// if (status != null && !status.isOK())
				// return status;
				// }
			}
		}

		return Status.OK_STATUS;
	}

	public FrameworkInstanceConfiguration getFrameworkInstanceConfiguration()
			throws CoreException {

		if (configuration == null) {
			IFolder folder = getServer().getServerConfiguration();
			if (folder == null || !folder.exists()) {
				String path = null;
				if (folder != null) {
					path = folder.getFullPath().toOSString();
					IProject project = folder.getProject();
					if (project != null && project.exists()
							&& !project.isOpen())
						throw new CoreException(
								new Status(
										IStatus.ERROR,
										FrameworkCorePlugin.PLUGIN_ID,
										0,
										NLS.bind(
												Messages.errorConfigurationProjectClosed,
												path, project.getName()), null));
				}
				throw new CoreException(new Status(IStatus.ERROR,
						FrameworkCorePlugin.PLUGIN_ID, 0, NLS.bind(
								Messages.errorNoConfiguration, path), null));
			}

			// String id = getServer().getServerType().getId();
			configuration = new FrameworkInstanceConfiguration(folder, this);

			try {
				configuration.load(folder, null);
			} catch (CoreException ce) {
				configuration = null;
				throw ce;
			}
		}
		return configuration;

	}

	public String getInstanceDirectory() {
		return getAttribute(PROPERTY_INSTANCE_DIR, (String) null);
	}

	public String getDeployDirectory() {
		// Default to value used by prior WTP versions
		return getAttribute(PROPERTY_DEPLOY_DIR, "bundles");
	}

	public void setInstanceDirectory(String instanceDir) {
		setAttribute(PROPERTY_INSTANCE_DIR, instanceDir);
	}

	public void setDeployDirectory(String deployDir) {
		setAttribute(PROPERTY_DEPLOY_DIR, deployDir);
	}

	public boolean isDebug() {
		return getAttribute(PROPERTY_DEBUG, false);
	}

	@Override
	public void setDefaults(IProgressMonitor monitor) {
		super.setDefaults(monitor);
		setDeployDirectory(DEFAULT_DEPLOYDIR);
	}

	@Override
	public void importRuntimeConfiguration(IRuntime runtime,
			IProgressMonitor monitor) throws CoreException {
		super.importRuntimeConfiguration(runtime, monitor);
	}

	@Override
	public void modifyModules(IModule[] add, IModule[] remove,
			IProgressMonitor monitor) throws CoreException {

		IStatus status = canModifyModules(add, remove);
		if (status == null || !status.isOK())
			throw new CoreException(status);

		if (add != null) {
			int size = add.length;
			for (int i = 0; i < size; i++) {
				// IModule module3 = add[i];
				// TODO - OSAMI Do something to add the module....
			}
		}

		if (remove != null) {
			int size2 = remove.length;
			for (int j = 0; j < size2; j++) {
				// IModule module3 = remove[j];
				// TODO - OSAMI Do something to remove the module....

			}
		}
		// config.save(config.getFolder(), monitor);
	}

	@SuppressWarnings("restriction")
	public abstract ITargetDefinition createDefaultTarget()
			throws CoreException;

}