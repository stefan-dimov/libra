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
package org.eclipse.libra.framework.core.internal.command;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.libra.framework.core.FrameworkInstanceConfiguration;
import org.eclipse.libra.framework.core.Messages;
import org.eclipse.libra.framework.core.internal.dependency.DependencyUtil;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.ModelEntry;
import org.eclipse.pde.core.plugin.PluginRegistry;




public class AddOsgiModuleCommand extends AbstractOperation {
	protected IPluginModelBase module;
	protected int modules = -1;
	protected FrameworkInstanceConfiguration configuration;


	public AddOsgiModuleCommand(FrameworkInstanceConfiguration configuration, IPluginModelBase module) {
		super( Messages.configurationEditorActionAddOsgiModule);
		this.module = module;
		this.configuration = configuration;
	}



	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		
		Object[] dependents = DependencyUtil.getDependencies(module.getBundleDescription());
		configuration.addOsgiBundle(module);
		for(Object dep: dependents){
			BundleDescription depBundle = (BundleDescription)dep;
			ModelEntry entry = PluginRegistry.findEntry(depBundle.getSymbolicName());		
			if(entry.hasWorkspaceModels()){ //isWorkspaceBundle(depBundle)){			
				configuration.addOsgiBundle(entry.getModel());
			}
			
		}
		return Status.OK_STATUS;
	}


	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return execute(monitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		configuration.removeBundle(module);
		return Status.OK_STATUS;
	}
	
	
}
