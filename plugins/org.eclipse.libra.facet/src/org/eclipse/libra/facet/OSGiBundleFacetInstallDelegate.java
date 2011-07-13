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

import static org.eclipse.libra.facet.OSGiBundleFacetUtils.JAVAX_EL_PACKAGE;
import static org.eclipse.libra.facet.OSGiBundleFacetUtils.JAVAX_PERSISTENCE_PACKAGE;
import static org.eclipse.libra.facet.OSGiBundleFacetUtils.JAVAX_SERVLET_HTTP_PACKAGE;
import static org.eclipse.libra.facet.OSGiBundleFacetUtils.JAVAX_SERVLET_JSP_EL_PACKAGE;
import static org.eclipse.libra.facet.OSGiBundleFacetUtils.JAVAX_SERVLET_JSP_PACKAGE;
import static org.eclipse.libra.facet.OSGiBundleFacetUtils.JAVAX_SERVLET_JSP_TAGEXT_PACKAGE;
import static org.eclipse.libra.facet.OSGiBundleFacetUtils.JAVAX_SERVLET_PACKAGE;
import static org.eclipse.libra.facet.OSGiBundleFacetUtils.META_PERSISTENCE_HEADER;
import static org.eclipse.libra.facet.OSGiBundleFacetUtils.WEB_CONTEXT_PATH_HEADER;
import static org.eclipse.libra.facet.OSGiBundleFacetUtils.WEB_INF_CLASSES;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.osgi.service.resolver.VersionRange;
import org.eclipse.pde.core.project.IBundleClasspathEntry;
import org.eclipse.pde.core.project.IBundleProjectDescription;
import org.eclipse.pde.core.project.IBundleProjectService;
import org.eclipse.pde.core.project.IPackageExportDescription;
import org.eclipse.pde.core.project.IPackageImportDescription;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.internal.util.FacetedProjectUtilities;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
import org.eclipse.wst.common.project.facet.core.IDelegate;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.osgi.framework.Version;


public class OSGiBundleFacetInstallDelegate implements IDelegate {

	public void execute(IProject project, IProjectFacetVersion fv,
			Object configObject, IProgressMonitor monitor) throws CoreException {
		OSGiBundleFacetInstallConfig config = (OSGiBundleFacetInstallConfig) configObject;
		doExecute(project, config, monitor);
	}

	private void doExecute(IProject project,
			OSGiBundleFacetInstallConfig config, IProgressMonitor monitor)
					throws CoreException {
		setBundleRoot(project);
		createBundleProjectDescription(project, config, monitor);
		addRequiredPluginsClasspathContainer(project, monitor);

		if (OSGiBundleFacetUtils.isJpaProject(project)) {
			moveMetaInfToRoot(project, monitor);
		}
	}

	private void setBundleRoot(IProject project) throws CoreException {
		IPath bundleRoot = null;
		if (OSGiBundleFacetUtils.isWebProject(project)) {
			IVirtualComponent component = ComponentCore.createComponent(project);
			bundleRoot = component.getRootFolder().getProjectRelativePath();
		}

		if (bundleRoot != null) {
			IBundleProjectService bundleProjectService = Activator.getDefault().getBundleProjectService();
			bundleProjectService.setBundleRoot(project, bundleRoot);
		}
	}

	private void createBundleProjectDescription(IProject project,
			OSGiBundleFacetInstallConfig config, IProgressMonitor monitor)
					throws CoreException {
		IBundleProjectService bundleProjectService = Activator.getDefault().getBundleProjectService();
		IBundleProjectDescription bundleProjectDescription = bundleProjectService.getDescription(project);

		bundleProjectDescription.setSymbolicName(config.getSymbolicName());
		bundleProjectDescription.setBundleVersion(config.getVersion());

		String bundleName = config.getName();
		if (bundleName != null && bundleName.trim().length() > 0) {
			bundleProjectDescription.setBundleName(bundleName);
		}

		String bundleVendor = config.getVendor();
		if (bundleVendor != null && bundleVendor.trim().length() > 0) {
			bundleProjectDescription.setBundleVendor(bundleVendor);
		}

		bundleProjectDescription.setEquinox(true);
		bundleProjectDescription.setExtensionRegistry(false);
		bundleProjectDescription.setNatureIds(getNatureIds(bundleProjectDescription));
		bundleProjectDescription.setLaunchShortcuts(getLaunchShortcuts(project));

		Map<String, String> headers = getAdditionalHeaders(project);
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			bundleProjectDescription.setHeader(entry.getKey(), entry.getValue());
		}

		bundleProjectDescription.setPackageExports(getPackageExports(project));
		bundleProjectDescription.setPackageImports(getPackageImports(bundleProjectDescription));
		bundleProjectDescription.setBinIncludes(getBinIncludes(bundleProjectDescription));
		bundleProjectDescription.setBundleClasspath(getBundleClasspath(bundleProjectDescription));

		bundleProjectDescription.apply(monitor);
	}

	private String[] getNatureIds(IBundleProjectDescription bundleProjectDescription) throws CoreException {
		String[] natureIds = bundleProjectDescription.getNatureIds();
		String[] newNatureIds = new String[natureIds.length + 1];
		for (int i = 0; i < natureIds.length; i++) {
			newNatureIds[i] = natureIds[i];
		}
		newNatureIds[newNatureIds.length - 1] = IBundleProjectDescription.PLUGIN_NATURE;

		return newNatureIds;
	}

	private String[] getLaunchShortcuts(IProject project) throws CoreException {
		if (OSGiBundleFacetUtils.isWebProject(project)) {
			return new String[] {
					"org.eclipse.pde.ui.EquinoxLaunchShortcut",  //$NON-NLS-1$
					"org.eclipse.wst.server.launchShortcut" //$NON-NLS-1$
			};
		}
		// use default OSGi Framework launchers
		return null;
	}

	private Map<String, String> getAdditionalHeaders(IProject project) throws CoreException {
		Map<String, String> headers = new HashMap<String, String>();

		if (OSGiBundleFacetUtils.isWebProject(project)) {
			headers.put(WEB_CONTEXT_PATH_HEADER, getContextRoot(project));
		}

		if (OSGiBundleFacetUtils.isJpaProject(project)) {
			headers.put(META_PERSISTENCE_HEADER, ""); //$NON-NLS-1$
		}

		return headers;
	}

	private String getContextRoot(IProject project) {
		IVirtualComponent component = ComponentCore.createComponent(project);
		String contextRoot = component.getMetaProperties().getProperty(OSGiBundleFacetUtils.CONTEXTROOT);
		// add leading slash if not available
		if (contextRoot.charAt(0) != '/') {
			contextRoot = '/' + contextRoot;
		}
		return contextRoot;
	}

	private IPackageExportDescription[] getPackageExports(IProject project) throws CoreException {
		IBundleProjectService bundleProjectService = Activator.getDefault().getBundleProjectService();
		List<IPackageExportDescription> list = new ArrayList<IPackageExportDescription>();

		if (OSGiBundleFacetUtils.isJavaProject(project)) {
			IJavaProject javaProject = JavaCore.create(project);
			IPackageFragmentRoot[] fragmentRoots = javaProject.getAllPackageFragmentRoots();
			for (IPackageFragmentRoot fragmentRoot : fragmentRoots) {
				if (fragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE && fragmentRoot.getParent().equals(javaProject)) {
					IJavaElement[] elements = fragmentRoot.getChildren();
					for (IJavaElement element : elements) {
						IPackageFragment fragment = (IPackageFragment) element;
						if (fragment.containsJavaResources()) {
							list.add(bundleProjectService.newPackageExport(fragment.getElementName(), null, true, null));
						}
					}
				}
			}
		}

		return list.toArray(new IPackageExportDescription[list.size()]);
	}

	private IPackageImportDescription[] getPackageImports(IBundleProjectDescription bundleProjectDescription) throws CoreException {
		IProject project = bundleProjectDescription.getProject();
		Map<String, IPackageImportDescription> packages = new TreeMap<String, IPackageImportDescription>();

		// look for existing package imports
		IPackageImportDescription[] imports = bundleProjectDescription.getPackageImports();
		if (imports != null) {
			for (IPackageImportDescription imp : imports) {
				packages.put(imp.getName(), imp);
			}
		}

		IBundleProjectService bundleProjectService = Activator.getDefault().getBundleProjectService();

		if (OSGiBundleFacetUtils.isWebProject(project)) {
			// add the most popular servlet packages
			addPackageImport(packages, JAVAX_SERVLET_PACKAGE, null, false);
			addPackageImport(packages, JAVAX_SERVLET_HTTP_PACKAGE, null, false);
			addPackageImport(packages, JAVAX_SERVLET_JSP_PACKAGE, null, false);
			addPackageImport(packages, JAVAX_SERVLET_JSP_EL_PACKAGE, null, false);
			addPackageImport(packages, JAVAX_SERVLET_JSP_TAGEXT_PACKAGE, null, false);
			addPackageImport(packages, JAVAX_EL_PACKAGE, null, false);

			// add packages exported by referenced components
			IVirtualComponent component = ComponentCore.createComponent(project);
			IVirtualReference[] references = component.getReferences();
			for (IVirtualReference ref : references) {
				IProject refProject = ref.getReferencedComponent().getProject();
				if (refProject != null && refProject != project && OSGiBundleFacetUtils.hasPluginNature(refProject)) {
					IPackageExportDescription[] exports = bundleProjectService.getDescription(refProject).getPackageExports();
					for (IPackageExportDescription export : exports) {
						String importName = export.getName();
						Version exportVersion = export.getVersion();
						VersionRange range = (exportVersion == null) ? null : new VersionRange(exportVersion.toString());
						addPackageImport(packages, importName, range, false);
					}
				}
			}
		}
		if (OSGiBundleFacetUtils.isJpaProject(project)) {
			String version = FacetedProjectUtilities.getProjectFacetVersion(project, OSGiBundleFacetUtils.JPA_FACET).getVersionString();
			addPackageImport(packages, String.format(JAVAX_PERSISTENCE_PACKAGE, version), null, false);
		}

		return packages.values().toArray(new IPackageImportDescription[packages.size()]);
	}

	private void addPackageImport(Map<String, IPackageImportDescription> packages, String importName, VersionRange range, boolean optional) {
		IBundleProjectService bundleProjectService = Activator.getDefault().getBundleProjectService();
		if (!packages.containsKey(importName)) {
			IPackageImportDescription imp = bundleProjectService.newPackageImport(importName, range, optional);
			packages.put(importName, imp);
		}
	}

	private IPath[] getBinIncludes(IBundleProjectDescription bundleProjectDescription) throws CoreException {
		IProject project = bundleProjectDescription.getProject();
		IVirtualComponent component = ComponentCore.createComponent(project);

		if (OSGiBundleFacetUtils.isWebProject(project)) {
			IPath bundleRoot = component.getRootFolder().getProjectRelativePath();
			IResource[] resources = project.getFolder(bundleRoot).members();
			List<IPath> binPaths = new ArrayList<IPath>();

			for (int i = 0; i < resources.length; i++) {
				String token = resources[i].getName();
				if (resources[i].getType() == IResource.FOLDER) {
					token += '/';
				}

				if (!token.equals(OSGiBundleFacetUtils.BUILD_PROPERTIES)) {
					binPaths.add(new Path(token));
				}
			}

			return binPaths.toArray(new IPath[binPaths.size()]);
		} else {
			// don't modify bin.includes by default
			return bundleProjectDescription.getBinIncludes();
		}
	}

	private IBundleClasspathEntry[] getBundleClasspath(IBundleProjectDescription bundleProjectDescription) throws CoreException {
		IProject project = bundleProjectDescription.getProject();
		IBundleClasspathEntry[] bundleClasspath = bundleProjectDescription.getBundleClasspath(); 
		
		IJavaProject javaProject = JavaCore.create(project);
		if (bundleClasspath == null) {
			IBundleProjectService bundleProjectService = Activator.getDefault().getBundleProjectService();
			
			IPath source = getRelativePath(project, getJavaSourceFolderPaths(javaProject)[0]);
			IPath binary = getRelativePath(project, javaProject.getOutputLocation());
			IPath library = (OSGiBundleFacetUtils.isWebProject(project)) 
					? new Path(WEB_INF_CLASSES) 	// add WEB-INF/classes for WABs
					: null; 						// add . for other OSGi bundles
			
			IBundleClasspathEntry classpath = bundleProjectService.newBundleClasspathEntry(
					source, binary, library);
			bundleClasspath = new IBundleClasspathEntry[] { classpath };
		} else {
			// TODO
		}
		
		// don't modify bin.includes by default
		return bundleClasspath;
	}

	private void addRequiredPluginsClasspathContainer(IProject project, IProgressMonitor monitor) throws CoreException {
		if (OSGiBundleFacetUtils.isJavaProject(project)) {
			IJavaProject javaProject = JavaCore.create(project);
			IClasspathEntry[] entries = javaProject.getRawClasspath();
			if (!OSGiBundleFacetUtils.hasRequiredPlugins(entries)) {
				IClasspathEntry[] newEntries = new IClasspathEntry[entries.length + 1];
				System.arraycopy(entries, 0, newEntries, 0, entries.length);
				newEntries[newEntries.length - 1] = JavaCore.newContainerEntry(OSGiBundleFacetUtils.REQUIRED_PLUGINS_CONTAINER_PATH);
				javaProject.setRawClasspath(newEntries, monitor);
			}
		}
	}

	private IPath[] getJavaSourceFolderPaths(IJavaProject javaProject) throws JavaModelException {
		List<IPath> paths = new ArrayList<IPath>();

		IPackageFragmentRoot[] fragmentRoots = javaProject.getAllPackageFragmentRoots();
		for (IPackageFragmentRoot fragmentRoot : fragmentRoots) {
			if (fragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE && fragmentRoot.getParent().equals(javaProject)) {
				paths.add(fragmentRoot.getPath());
			}
		}

		return paths.toArray(new IPath[paths.size()]);
	}

	private IPath getRelativePath(IProject project, IPath path) {
		return path.makeRelativeTo(project.getFullPath()).addTrailingSeparator();
	}

	private void moveMetaInfToRoot(IProject project, IProgressMonitor monitor) throws CoreException {
		// find the first META-INF folder as a second-level folder
		IFolder folder = null;
		IResource[] resources = project.members();
		for (IResource r : resources) {
			if (r.getType() == IResource.FOLDER) {
				IFolder f = (IFolder) r;
				IResource metaInf = f.findMember(OSGiBundleFacetUtils.META_INF);
				if (metaInf != null && metaInf.getType() == IResource.FOLDER) {
					folder = (IFolder) metaInf;
					break;
				}
			}
		}
		
		if (folder == null || !folder.exists())
			return;
		
		// copy all resources to /META-INF
		IResource[] members = folder.members();
		for (IResource member : members) {
			IPath destination = project.getFolder(OSGiBundleFacetUtils.META_INF).getFullPath().append(member.getName());  
			if (!project.getWorkspace().getRoot().exists(destination)) { // this check is needed for the /src/MANIFEST.MF added by the jst.utility facet
				member.move(destination, true, monitor);
			}
		}
		folder.delete(true, monitor);
	}

}
