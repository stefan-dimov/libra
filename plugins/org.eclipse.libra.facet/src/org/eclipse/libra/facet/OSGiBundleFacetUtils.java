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

import java.text.SimpleDateFormat;
import java.util.Date;
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

	static IPluginModelListener addPDEModelListener() {
		IPluginModelListener pdeModelListener = new IPluginModelListener() {
			
			public void modelsChanged(PluginModelDelta delta) {
				
				synchronized (OSGiBundleFacetUtils.class) {
					ModelEntry[] changedEntries = delta.getChangedEntries();
					if (changedEntries.length == 0)
						return;
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
								Activator.logError("Could not update the context root in WTP model of the project " + project.getName(), e);	//$NON-NLS-1$
							continue;
						}
					}
					if (ht.size() > 0) {
						Job j = new Job("Change context root in WTP model") {	//$NON-NLS-1$
							protected IStatus run(IProgressMonitor monitor) {
								for (IProject project : ht.keySet()) {
									OSGiBundleFacetUtils.setContextRootInWTPModel(project, ht.get(project));
								}
								return Status.OK_STATUS;
							}
							public boolean belongsTo(Object family) {
								return PDE_MODEL_LISTENER_JOBFAMILY.equals(family);
							};
						};
						j.schedule();						
					}
			}
		}
			
		}; 
		PDECore.getDefault().getModelManager().addPluginModelListener(pdeModelListener);
		return pdeModelListener;
	}
	
	static boolean isProjectWithChangedWebContextRootInWTP (IResourceDelta delta) {
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
	
	synchronized static private Hashtable<IProject, String> getProjectsWithChangedWebContextRootInWTP(IResourceDelta delta) {
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
				Activator.logError("Could not update the context root in PDE model of the project " + project.getName(), e);	//$NON-NLS-1$
			}
		}		
		return ht;
	}
	
	static IResourceChangeListener addResChangeListener() {
		IResourceChangeListener resChangeListener = new IResourceChangeListener() {
			
			public void resourceChanged(IResourceChangeEvent event) {
				
				synchronized (OSGiBundleFacetUtils.class) {
					final Hashtable<IProject, String> ht = getProjectsWithChangedWebContextRootInWTP(event.getDelta());
					if (ht.size() > 0) {
						Job job = new Job("Change context root in PDE model") {	//$NON-NLS-1$
							protected IStatus run(IProgressMonitor monitor) {
								
								for (IProject project : ht.keySet()) {
									try {
										OSGiBundleFacetUtils.setContextRootInPDEModel(project, ht.get(project));
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
						
						job.schedule();						
					}	
				}
			}
		};
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resChangeListener, IResourceChangeEvent.POST_CHANGE);		
		return resChangeListener;
	}

}
