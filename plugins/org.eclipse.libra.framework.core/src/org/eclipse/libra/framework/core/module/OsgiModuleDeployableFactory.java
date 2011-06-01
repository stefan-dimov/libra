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
package org.eclipse.libra.framework.core.module;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.libra.framework.core.IOSGIFrameworkInstance;
import org.eclipse.libra.framework.core.Trace;
import org.eclipse.libra.framework.core.internal.command.AddOsgiModuleCommand;
import org.eclipse.libra.framework.core.internal.dependency.DependencyUtil;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.ModelEntry;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.internal.resources.VirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.eclipse.wst.server.core.util.ProjectModuleFactoryDelegate;


public class OsgiModuleDeployableFactory extends ProjectModuleFactoryDelegate {

	List<OsgiModuleDeployable> moduleDelegates = new ArrayList<OsgiModuleDeployable>();

	@Override
	public ModuleDelegate getModuleDelegate(IModule module) {
		for (OsgiModuleDeployable omd : moduleDelegates) {
			if (module == omd.getModule())
				return omd;
		}
		return null;
	}

	@Override
	protected IModule[] createModules(IProject project) {
		IVirtualComponent component = ComponentCore.createComponent(project);
		if (component == null) {
			component = new VirtualComponent(project, new Path("/")); //$NON-NLS-1$
		}
		if (component != null) {
			return createModuleDelegates(component);
		}
		return null;
	}

	private IModule[] createModuleDelegates(IVirtualComponent component) {
		if (component == null) {
			return null;
		}
		OsgiModuleDeployable moduleDelegate = null;
		IModule module = null;
		try {
			if (isValidModule(component.getProject())) {
				moduleDelegate = new OsgiModuleDeployable(
						component.getProject(), component);
				module = createModule(component.getName(), component.getName(),
						"osgi.bundle", moduleDelegate.getVersion(),
						moduleDelegate.getProject());
				moduleDelegate.initialize(module);

			}
		} catch (Exception e) {
			Trace.trace(Trace.SEVERE, e.getMessage(), e);
		} finally {
			if (module != null) {
				if (getModuleDelegate(module) == null)
					moduleDelegates.add(moduleDelegate);
			}
		}
		if (module == null)
			return null;
		return new IModule[] { module };
	}

	private boolean isValidModule(IProject project) {
		try {
			IFacetedProject facetedProject = ProjectFacetsManager
					.create(project);
			if (facetedProject == null)
				return false;
			IProjectFacet webFacet = ProjectFacetsManager
					.getProjectFacet("osgi.bundle");
			return facetedProject.hasProjectFacet(webFacet);
		} catch (Exception e) {
			return false;
		}
	}

}
