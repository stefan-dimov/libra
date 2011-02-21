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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.pde.core.project.IBundleProjectDescription;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.natures.PDE;
import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
import org.eclipse.wst.common.project.facet.core.FacetedProjectFramework;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;

public class OSGiBundleUtils {
	
	public static final String OSGI_BUNDLE = "osgi.bundle"; //$NON-NLS-1$
	public static final IProjectFacet OSGI_BUNDLE_FACET = ProjectFacetsManager.getProjectFacet(OSGI_BUNDLE);
	public static final IProjectFacetVersion OSGI_BUNDLE_FACET_42 = OSGI_BUNDLE_FACET.getVersion("4.2"); //$NON-NLS-1$
	
	public static final String BUILD_PROPERTIES = "build.properties"; //$NON-NLS-1$
	public static final String MANIFEST_URI = "META-INF/MANIFEST.MF"; //$NON-NLS-1$
	
	public static boolean hasPluginNature(IProject project) throws CoreException {
		return project.hasNature(IBundleProjectDescription.PLUGIN_NATURE);
	}
	
	public static boolean hasFeatureNature(IProject project) throws CoreException {
		return project.hasNature(PDE.FEATURE_NATURE);
	}
	
	public static boolean hasUpdateSiteNature(IProject project) throws CoreException {
		return project.hasNature(PDE.SITE_NATURE);
	}
	
	public static boolean isOSGiBundle(IProject project) throws CoreException {
		return FacetedProjectFramework.hasProjectFacet(project, OSGI_BUNDLE);
	}

	public static boolean isJavaProject(IProject project) throws CoreException {
		return project.hasNature(JavaCore.NATURE_ID);
	}

	public static boolean isWebProject(IProject project) throws CoreException {
		return FacetedProjectFramework.hasProjectFacet(project, IModuleConstants.JST_WEB_MODULE);
	}

	public static boolean isJpaProject(IProject project) throws CoreException {
		return FacetedProjectFramework.hasProjectFacet(project, "jpt.jpa");
	}
	
	public static boolean isRequiredPlugins(IClasspathEntry entry) {
		return PDECore.REQUIRED_PLUGINS_CONTAINER_PATH.equals(entry.getPath());
	}
	
	public static boolean hasRequiredPlugins(IClasspathEntry[] entries) {
		for (IClasspathEntry entry : entries) {
			if (isRequiredPlugins(entry)) {
				return true;
			}
		}
		return false;
	}

}
