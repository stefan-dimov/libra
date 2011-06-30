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



import java.util.Hashtable;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.pde.core.plugin.ModelEntry;
import org.eclipse.pde.core.project.IBundleProjectService;
import org.eclipse.pde.internal.core.IPluginModelListener;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PluginModelDelta;
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
		addPDEModelListener();
		addResChangeListener();
	}
	
	private void addPDEModelListener() {
		pdeModelListener = new IPluginModelListener() {
			
			public void modelsChanged(PluginModelDelta delta) {
				ModelEntry[] changedEntries = delta.getChangedEntries();
				final Hashtable<IProject, String> ht = new Hashtable<IProject, String>();
				for (ModelEntry projectModel : changedEntries) {
					String projectName = projectModel.getModel().toString();
					final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
					try {
						if ((project == null) || !OSGiBundleFacetUtils.isWebProject(project))
							continue;
						String contextRootFromPDEModel = OSGiBundleFacetUtils.getContextRootFromPDEModel(project);
						if (contextRootFromPDEModel == null)
							continue;
						String contextRootFromWTPModel = OSGiBundleFacetUtils.getContextRootFromWTPModel(project);						
						if (!contextRootFromPDEModel.equals(contextRootFromWTPModel)) 
							ht.put(project, contextRootFromPDEModel);														
					} catch (CoreException e) {
						if (project != null)
							logError("Could not update the context root in WTP model of the project " + project.getName(), e);	//$NON-NLS-1$
						continue;
					}
				}
				if (ht.size() > 0) {
					Job j = new Job("Change context root in WTP model") {	//$NON-NLS-1$
						protected IStatus run(IProgressMonitor monitor) {
							for (IProject project : ht.keySet()) 
								OSGiBundleFacetUtils.setContextRootInWTPModel(project, ht.get(project));									
							return Status.OK_STATUS;
						}
					};
					j.schedule();						
				}
			}
			
		}; 
		PDECore.getDefault().getModelManager().addPluginModelListener(pdeModelListener);
	}
	
	
	private boolean isProjectWithChangedWebContextRootInWTP(IResourceDelta delta) {
		IResource res = delta.getResource();
		if ((res == null) || !IProject.class.isInstance(res))
			return false;	
		IResourceDelta[] ch = delta.getAffectedChildren(IResourceDelta.CHANGED);
		for (IResourceDelta d : ch) {
			IResource r = d.getResource();
			if ((r != null) && 
				IFolder.class.isInstance(r) && 
				r.getFullPath().lastSegment().
						equalsIgnoreCase(".settings")) {	//$NON-NLS-1$
				
				
				IResourceDelta[] ch1 = d.getAffectedChildren(IResourceDelta.CHANGED);
				for (IResourceDelta d1 : ch1) {
					IResource r1 = d1.getResource();
					if ((r1 != null) && 
						IFile.class.isInstance(r1) && 
						r1.getFullPath().lastSegment().
							equalsIgnoreCase("org.eclipse.wst.common.component"))	//$NON-NLS-1$
						
						return true;
				}
			}
			
		}
		return false;
	}
	
	// org.eclipse.wst.common.component
	
	private Hashtable<IProject, String> getProjectsWithChangedWebContextRootInWTP(IResourceDelta delta) {
		IResourceDelta[] children = delta.getAffectedChildren(IResourceDelta.CHANGED);
		final Hashtable<IProject, String> ht = new Hashtable<IProject, String>();
		for (IResourceDelta child : children) {
			if (!isProjectWithChangedWebContextRootInWTP(child))
				continue;			
			IProject project = (IProject)child.getResource();
			try {
				if (!OSGiBundleFacetUtils.isOSGiBundle(project) && !OSGiBundleFacetUtils.isWebProject(project)) 
					continue;
				String contextRootFromWTPModel = OSGiBundleFacetUtils.getContextRootFromWTPModel(project);
				if (contextRootFromWTPModel == null)
					continue;
				String contextRootFromPDEModel = OSGiBundleFacetUtils.getContextRootFromPDEModel(project);
				if (!contextRootFromWTPModel.equals(contextRootFromPDEModel)) 
					ht.put(project, contextRootFromWTPModel);																				
			} catch (CoreException e) {
				logError("Could not update the context root in PDE model of the project " + project.getName(), e);	//$NON-NLS-1$
			}
		}		
		return ht;
	}
	
	private void addResChangeListener() {
		resChangeListener = new IResourceChangeListener() {
			@Override
			public void resourceChanged(IResourceChangeEvent event) {
				final Hashtable<IProject, String> ht = getProjectsWithChangedWebContextRootInWTP(event.getDelta());
				if (ht.size() > 0) {
					Job job = new Job("Change context root in PDE model") {	//$NON-NLS-1$
						protected IStatus run(IProgressMonitor monitor) {
							for (IProject project : ht.keySet()) {
								try {
									OSGiBundleFacetUtils.setContextRootInPDEModel(project, ht.get(project));
								} catch (CoreException e) {
									logError("Could not update the context root in PDE model of the project " + project.getName(), e);	//$NON-NLS-1$
								}
							}
							return Status.OK_STATUS;
						}
					};
					job.schedule();						
				}				
			}
		};
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resChangeListener, IResourceChangeEvent.POST_CHANGE);		
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

