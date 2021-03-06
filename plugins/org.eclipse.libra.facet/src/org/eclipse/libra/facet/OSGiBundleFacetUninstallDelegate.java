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

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.pde.core.project.IBundleProjectDescription;
import org.eclipse.pde.core.project.IBundleProjectService;
import org.eclipse.wst.common.project.facet.core.IDelegate;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;


public class OSGiBundleFacetUninstallDelegate implements IDelegate {

	public void execute(IProject project, IProjectFacetVersion fv,
			Object configObject, IProgressMonitor monitor) throws CoreException {
		OSGiBundleFacetUninstallConfig config = (OSGiBundleFacetUninstallConfig) configObject;
		OSGiBundleFacetUninstallStrategy strategy = (config == null) ?
				OSGiBundleFacetUninstallStrategy.defaultStrategy() : 
				config.getStrategy();
		doExecute(project, strategy, monitor);
	}
	
	public void doExecute(IProject project, OSGiBundleFacetUninstallStrategy strategy, IProgressMonitor monitor) throws CoreException {
		// remove the plugin nature
		if (strategy == OSGiBundleFacetUninstallStrategy.FACET_AND_PLUGIN_NATURE_BUT_NOT_MANIFEST || 
				strategy == OSGiBundleFacetUninstallStrategy.FACET_AND_PLUGIN_NATURE_AND_MANIFEST) {
			removePluginNature(project, monitor);
			removeRequiredBundlesClasspathContainer(project, monitor);
			deleteBuildProperties(project, monitor);
		}
		
		// remove the MANIFEST.MF
		if (strategy == OSGiBundleFacetUninstallStrategy.FACET_AND_PLUGIN_NATURE_AND_MANIFEST) {
			cleanUpManifest(project, monitor);
		}
	}

	private void removePluginNature(IProject project, IProgressMonitor monitor) throws CoreException {
		IProjectDescription description = project.getDescription();
		if (description.hasNature(IBundleProjectDescription.PLUGIN_NATURE)) {
			String[] natures = description.getNatureIds();
			for (int i = 0; i < natures.length; i++) {
				if (natures[i].equals(IBundleProjectDescription.PLUGIN_NATURE)) {
					String[] newNatures = new String[natures.length - 1];
					System.arraycopy(natures, 0, newNatures, 0, i);
					System.arraycopy(natures, i + 1, newNatures, i, natures.length - i - 1);
					description.setNatureIds(newNatures);
					// workaround - see bug 344720
					removeFromBuildSpec(description, OSGiBundleFacetUtils.MANIFEST_BUILDER_ID);
					removeFromBuildSpec(description, OSGiBundleFacetUtils.SCHEMA_BUILDER_ID);
					// end of workaround
					project.setDescription(description, IResource.KEEP_HISTORY, monitor);
					return;
				}
			}
		}
	}
	
	// workaround - see bug 344720
	private void removeFromBuildSpec(IProjectDescription description, String builderId) {
		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(builderId)) {
				ICommand[] newCommands = new ICommand[commands.length - 1];
				System.arraycopy(commands, 0, newCommands, 0, i);
				System.arraycopy(commands, i + 1, newCommands, i, commands.length - i - 1);
				description.setBuildSpec(newCommands);
				return;
			}
		}
	}

	private void removeRequiredBundlesClasspathContainer(IProject project, IProgressMonitor monitor) throws CoreException {
		if (OSGiBundleFacetUtils.isJavaProject(project)) {
			IJavaProject javaProject = JavaCore.create(project);
			IClasspathEntry[] entries = javaProject.getRawClasspath();
			if (OSGiBundleFacetUtils.hasRequiredPlugins(entries)) {
				for (int i = 0; i < entries.length; i++) {
					if (OSGiBundleFacetUtils.isRequiredPlugins(entries[i])) {
						IClasspathEntry[] newEntries = new IClasspathEntry[entries.length - 1];
						System.arraycopy(entries, 0, newEntries, 0, i);
						System.arraycopy(entries, i + 1, newEntries, i, entries.length - i - 1);
						javaProject.setRawClasspath(newEntries, monitor);
						return;
					}
				}
			}
		}
	}

	private void deleteBuildProperties(IProject project, IProgressMonitor monitor) throws CoreException {
		IResource buildPropertiesFile = findResource(project, OSGiBundleFacetUtils.BUILD_PROPERTIES);
		buildPropertiesFile.delete(IResource.KEEP_HISTORY, monitor);
	}
	
	private void cleanUpManifest(IProject project, IProgressMonitor monitor) throws CoreException {
		IResource manifestFile = findResource(project, OSGiBundleFacetUtils.MANIFEST_URI);
		manifestFile.delete(IResource.KEEP_HISTORY, monitor);
		
		// delete the META-INF folder if empty
		IContainer metaInfFolder = manifestFile.getParent();
		if (metaInfFolder.members().length == 0) {
			metaInfFolder.delete(IResource.KEEP_HISTORY, monitor);
		}
	}
	
	private IResource findResource(IProject project, String memberURI) throws CoreException {
		IBundleProjectService bundleProjectService = Activator.getDefault().getBundleProjectService();
		IBundleProjectDescription bundleProjectDescription = bundleProjectService.getDescription(project);
		IPath bundleRoot = bundleProjectDescription.getBundleRoot();
		IPath memberPath = bundleRoot;
		if (memberPath == null) {
			memberPath = new Path(""); //$NON-NLS-1$
		}
		memberPath = memberPath.append(memberURI);
		return project.findMember(memberPath);
	}

}
