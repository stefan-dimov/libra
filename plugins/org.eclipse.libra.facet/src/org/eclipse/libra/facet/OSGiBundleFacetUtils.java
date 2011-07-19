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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.pde.core.plugin.ModelEntry;
import org.eclipse.pde.core.project.IBundleProjectDescription;
import org.eclipse.pde.core.project.IBundleProjectService;
import org.eclipse.pde.internal.core.IPluginModelListener;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PluginModelDelta;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.project.facet.core.FacetedProjectFramework;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;

@SuppressWarnings("restriction")
public class OSGiBundleFacetUtils {
	
	public static final String PDE_MODEL_LISTENER_JOBFAMILY = "PDE_MODEL_LISTENER_JOBFAMILY"; //$NON-NLS-1$
	public static final String RESOURCE_MODEL_LISTENER_JOBFAMILY = "RESOURCE_MODEL_LISTENER_JOBFAMILY"; //$NON-NLS-1$
	public static final String OSGI_BUNDLE = "osgi.bundle"; //$NON-NLS-1$
	public static final IProjectFacet OSGI_BUNDLE_FACET = ProjectFacetsManager.getProjectFacet(OSGI_BUNDLE);
	public static final IProjectFacetVersion OSGI_BUNDLE_FACET_42 = OSGI_BUNDLE_FACET.getVersion("4.2"); //$NON-NLS-1$
	
	public static final String WEB_FACET = "jst.web"; //$NON-NLS-1$
	public static final String JPA_FACET = "jpt.jpa"; //$NON-NLS-1$
	
	public static final String FEATURE_NATURE_ID = "org.eclipse.pde.FeatureNature"; //$NON-NLS-1$
	public static final String SITE_NATURE_ID = "org.eclipse.pde.UpdateSiteNature"; //$NON-NLS-1$
	public static final String MANIFEST_BUILDER_ID = "org.eclipse.pde.ManifestBuilder"; //$NON-NLS-1$
	public static final String SCHEMA_BUILDER_ID = "org.eclipse.pde.SchemaBuilder"; //$NON-NLS-1$
	
	public static final Path REQUIRED_PLUGINS_CONTAINER_PATH = new Path("org.eclipse.pde.core.requiredPlugins"); //$NON-NLS-1$
	
	public static final String BUILD_PROPERTIES = "build.properties"; //$NON-NLS-1$
	public static final String CONTEXTROOT = "context-root"; //$NON-NLS-1$
	
	public static final String WEB_INF_CLASSES = "WEB-INF/classes/"; //$NON-NLS-1$
	public static final String META_INF = "META-INF"; //$NON-NLS-1$
	public static final String MANIFEST_URI = "META-INF/MANIFEST.MF"; //$NON-NLS-1$
	
	public static final String WEB_CONTEXT_PATH_HEADER = "Web-ContextPath"; //$NON-NLS-1$
	public static final String META_PERSISTENCE_HEADER = "Meta-Persistence"; //$NON-NLS-1$
	
	public static final String JAVAX_SERVLET_PACKAGE = "javax.servlet"; //$NON-NLS-1$
	public static final String JAVAX_SERVLET_HTTP_PACKAGE = "javax.servlet.http"; //$NON-NLS-1$
	public static final String JAVAX_SERVLET_JSP_PACKAGE = "javax.servlet.jsp"; //$NON-NLS-1$
	public static final String JAVAX_SERVLET_JSP_EL_PACKAGE = "javax.servlet.jsp.el"; //$NON-NLS-1$
	public static final String JAVAX_SERVLET_JSP_TAGEXT_PACKAGE = "javax.servlet.jsp.tagext"; //$NON-NLS-1$
	public static final String JAVAX_EL_PACKAGE = "javax.el"; //$NON-NLS-1$
	public static final String JAVAX_PERSISTENCE_PACKAGE = "javax.persistence;jpa=\"%s\""; //$NON-NLS-1$
	
	public static boolean hasPluginNature(IProject project) throws CoreException {
		return project.hasNature(IBundleProjectDescription.PLUGIN_NATURE);
	}
	
	public static boolean hasFeatureNature(IProject project) throws CoreException {
		return project.hasNature(FEATURE_NATURE_ID);
	}
	
	public static boolean hasUpdateSiteNature(IProject project) throws CoreException {
		return project.hasNature(SITE_NATURE_ID);
	}
	
	public static boolean isOSGiBundle(IProject project) throws CoreException {
		return FacetedProjectFramework.hasProjectFacet(project, OSGI_BUNDLE);
	}

	public static boolean isJavaProject(IProject project) throws CoreException {
		return project.hasNature(JavaCore.NATURE_ID);
	}

	public static boolean isWebProject(IProject project) throws CoreException {
		return FacetedProjectFramework.hasProjectFacet(project, WEB_FACET);
	}
	
	public static boolean isWebApplicationBundle(IProject project) throws CoreException {
		return isWebProject(project) && isOSGiBundle(project);
	}	

	public static boolean isJpaProject(IProject project) throws CoreException {
		return FacetedProjectFramework.hasProjectFacet(project, JPA_FACET);
	}
	
	public static boolean isRequiredPlugins(IClasspathEntry entry) {
		return REQUIRED_PLUGINS_CONTAINER_PATH.equals(entry.getPath());
	}
	
	public static boolean hasRequiredPlugins(IClasspathEntry[] entries) {
		for (IClasspathEntry entry : entries) {
			if (isRequiredPlugins(entry)) {
				return true;
			}
		}
		return false;
	}

	public static String getContextRootFromWTPModel(IProject project) {
		IVirtualComponent component = ComponentCore.createComponent(project);
		String contextRoot = component.getMetaProperties().getProperty(OSGiBundleFacetUtils.CONTEXTROOT);
		// add leading slash if not available
		if (contextRoot.charAt(0) != '/') {
			contextRoot = '/' + contextRoot;
}
		return contextRoot;
	}
	
	public static void setContextRootInWTPModel(IProject project, String contextRoot) {
		IVirtualComponent component = ComponentCore.createComponent(project);
		component.setMetaProperty(OSGiBundleFacetUtils.CONTEXTROOT, contextRoot);
	}	
	
	public static String getContextRootFromPDEModel(IProject project) throws CoreException {
		IBundleProjectService bundleProjectService = Activator.getDefault().getBundleProjectService();
		IBundleProjectDescription bundleProjectDescription = bundleProjectService.getDescription(project);
		String rootContext = bundleProjectDescription.getHeader(WEB_CONTEXT_PATH_HEADER);
		return rootContext;
	}
	
	public static void setContextRootInPDEModel(IProject project, String contextRoot) throws CoreException {
		IBundleProjectService bundleProjectService = Activator.getDefault().getBundleProjectService();
		IBundleProjectDescription bundleProjectDescription = bundleProjectService.getDescription(project); 
		bundleProjectDescription.setHeader(WEB_CONTEXT_PATH_HEADER, contextRoot);
		bundleProjectDescription.apply(new NullProgressMonitor());
	}	

	/**
	 * Adds PDE model listener to the PDE model
	 * 
	 * @return the listener
	 */
	static IPluginModelListener addPDEModelListener() {
		IPluginModelListener pdeModelListener = new IPluginModelListener() {
			public void modelsChanged(PluginModelDelta delta) {
				synchronized (OSGiBundleFacetUtils.class) {
					final Hashtable<IProject, String> projectToContextRoot = getProjectsWithChangedWebContextRootInPDE(delta);
					if (projectToContextRoot.size() > 0) {
						Job j = createChangeWTPModelJob(projectToContextRoot);
						j.schedule();						
					}
				}
			}
			
		}; 
		PDECore.getDefault().getModelManager().addPluginModelListener(pdeModelListener);
		return pdeModelListener;
	}
	
	/**
	 * Finds out all the projects with changed context root in PDE model
	 * 
	 * @param delta - the PDE change
	 * @return Hashtable with the projects mapped to their new context root values
	 */
	synchronized static private Hashtable<IProject, String> getProjectsWithChangedWebContextRootInPDE(PluginModelDelta delta) {
		ModelEntry[] changedEntries = delta.getChangedEntries();		
		// check for fake events (false positive that something is changed)
		final Hashtable<IProject, String> projectToContextRoot = new Hashtable<IProject, String>();
		if (changedEntries.length == 0)
			return projectToContextRoot;
		// iterates over all the changed entries to find the affected bundles
		for (ModelEntry projectModel : changedEntries) {
			String projectName = projectModel.getModel().toString();
			final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			try {
				if (shouldModelsBeSyncronized(project)) {  
					String contextRootFromPDEModel = OSGiBundleFacetUtils.getContextRootFromPDEModel(project);
					projectToContextRoot.put(project, contextRootFromPDEModel);
				}
			} catch (CoreException e) {
				if (project != null)
					Activator.logError("Could not update the context root in WTP model of the project " + project.getName(), e);	//$NON-NLS-1$
				continue;
			}
		}
		return projectToContextRoot;
	}
	
	/**
	 * Creates a job that will synchronize both models (from PDE to WTP)
	 * 
	 * @param projectToContextRoot - Hashtable containing all the projects that has to be synchronized
	 * @return the new job
	 */
	private static Job createChangeWTPModelJob(final Hashtable<IProject, String> projectToContextRoot) {
		Job j = new Job("Change context root in WTP model") {	//$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				for (IProject project : projectToContextRoot.keySet()) {
					OSGiBundleFacetUtils.setContextRootInWTPModel(project, projectToContextRoot.get(project));
				}
				return Status.OK_STATUS;
			}
			public boolean belongsTo(Object family) {
				return PDE_MODEL_LISTENER_JOBFAMILY.equals(family);
			};
		};
		return j;
	}
	
	private static boolean isVirtualComponentSettingsFileChanged(IResourceDelta delta) {
		IResource resource = delta.getResource();
		if ((resource == null) || !IProject.class.isInstance(resource))
			return false;	
		IResourceDelta[] ch = delta.getAffectedChildren(IResourceDelta.CHANGED);
		for (IResourceDelta d : ch) {
			IResource res = d.getResource();
			// find '.settings' folder
			if (isDotSettingsFolder(res)) {	
				IResourceDelta[] ch1 = d.getAffectedChildren(IResourceDelta.CHANGED);
				for (IResourceDelta d1 : ch1) {
					IResource res1 = d1.getResource();
					// find 'org.eclipse.wst.common.component' file
					if (isVirtualComponentSettingsFile(res1))	//$NON-NLS-1$
						return true;
				}
			}
			
		}
		return false;
	}
	
	/**
	 * Checks if the resource is the '.settings' folder of the project
	 * 
	 * @param res - The checked resource
	 * @return true if it's the correct folder
	 */
	private static boolean isDotSettingsFolder(IResource res) {
		return (res != null) && 
				IFolder.class.isInstance(res) && 
				res.getFullPath().lastSegment().equalsIgnoreCase(".settings");	//$NON-NLS-1$	
	}
	
	/**
	 * Checks if the resource is the 'org.eclipse.wst.common.component' file
	 * 
	 * @param res - The checked resource
	 * @return true if this is the correct file. Otherwise - false
	 */
	private static boolean isVirtualComponentSettingsFile(IResource res) {
		return (res != null) && 
				IFile.class.isInstance(res) && 
				res.getFullPath().lastSegment().equalsIgnoreCase("org.eclipse.wst.common.component");	//$NON-NLS-1$	
	}
	
	/**
	 * Creates hashtable containing the projects with changed context root in WTP model
	 * 
	 * @param delta
	 * @return Hashtable with projects mapped to the new context root values
	 */
	synchronized static private Hashtable<IProject, String> getProjectsWithChangedWebContextRootInWTP(IResourceDelta delta) {
		IResourceDelta[] children = delta.getAffectedChildren(IResourceDelta.CHANGED);
		final Hashtable<IProject, String> ht = new Hashtable<IProject, String>();
		for (IResourceDelta child : children) {
			if (!isVirtualComponentSettingsFileChanged(child))
				continue;			
			IProject project = (IProject)child.getResource();
			try {
				if (shouldModelsBeSyncronized(project)) {
					String contextRootFromWTPModel = OSGiBundleFacetUtils.getContextRootFromWTPModel(project);
					// add the project with changed context root to the hashtable
					ht.put(project, contextRootFromWTPModel);
				}
			} catch (CoreException e) {
				Activator.logError("Could not update the context root in PDE model of the project " + project.getName(), e);	//$NON-NLS-1$
			}
		}		
		return ht;
	}
	
	/**
	 * Checks if WTP and PDE models should be synchronized
	 * 
	 * @param project - The project that has to be checked
	 * @return true if it's WAB project with inconsistent models. Otherwise - false
	 * @throws CoreException
	 */
	private static boolean shouldModelsBeSyncronized(IProject project) throws CoreException {
		if (!isWebApplicationBundle(project)) 
			return false;
		String contextRootFromWTPModel = OSGiBundleFacetUtils.getContextRootFromWTPModel(project);
		String contextRootFromPDEModel = OSGiBundleFacetUtils.getContextRootFromPDEModel(project);
		return !contextRootFromWTPModel.equals(contextRootFromPDEModel);
	}
	
	/**
	 * Adds WTP model listener
	 * 
	 * @return the newly created listener
	 */
	static IResourceChangeListener addResChangeListener() {
		IResourceChangeListener resChangeListener = new IResourceChangeListener() {
			public void resourceChanged(IResourceChangeEvent event) {
				synchronized (OSGiBundleFacetUtils.class) {
					final Hashtable<IProject, String> projectToContextRoot = getProjectsWithChangedWebContextRootInWTP(event.getDelta());
					if (projectToContextRoot.size() > 0) {
						Job job = createChangePDEModelJob(projectToContextRoot);
						job.schedule();						
					}	
				}
			}
		};
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resChangeListener, IResourceChangeEvent.POST_CHANGE);		
		return resChangeListener;
	}
	
	/**
	 * Creates a job which will synchronize the models (from WTP to PDE)
	 * 
	 * @param projectToContextRoot - Hashtable which contains all the projects that has to be synchronized 
	 * @return The new job
	 */
	private static Job createChangePDEModelJob(final Hashtable<IProject, String> projectToContextRoot) {
		Job job = new Job("Change context root in PDE model") {	//$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				for (IProject project : projectToContextRoot.keySet()) {
					try {
						OSGiBundleFacetUtils.setContextRootInPDEModel(project, projectToContextRoot.get(project));
					} catch (CoreException e) {
						Activator.logError("Could not update the context root in PDE model of the project " + project.getName(), e);	//$NON-NLS-1$
					}
				}
				return Status.OK_STATUS;
			}
			public boolean belongsTo(Object family) {
				return RESOURCE_MODEL_LISTENER_JOBFAMILY.equals(family);
			};
		};
		return job;
	}

}
