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

import static org.eclipse.libra.facet.OSGiBundleFacetUtils.JAVAX_PERSISTENCE_PACKAGE;
import static org.eclipse.libra.facet.OSGiBundleFacetUtils.JPA_FACET;
import static org.eclipse.libra.facet.OSGiBundleFacetUtils.META_PERSISTENCE_HEADER;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.pde.core.project.IBundleProjectDescription;
import org.eclipse.pde.core.project.IBundleProjectService;
import org.eclipse.pde.core.project.IPackageImportDescription;
import org.eclipse.wst.common.project.facet.core.IDelegate;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;


public class OSGiBundleFacetEventHandler implements IDelegate {

	public void execute(IProject project, IProjectFacetVersion fv,
			Object config, IProgressMonitor monitor) throws CoreException {
		
		if (JPA_FACET.equals(fv.getProjectFacet().getId()) && OSGiBundleFacetUtils.isOSGiBundle(project)) {
			IBundleProjectService bundleProjectService = Activator.getDefault().getBundleProjectService();
			IBundleProjectDescription bundleProjectDescription = bundleProjectService.getDescription(project);
			
			// add the Meta-Persistence manifest header, if it already exists - keep its value
			String metaPersistenceHeader = bundleProjectDescription.getHeader(META_PERSISTENCE_HEADER);
			if (metaPersistenceHeader == null) {
				metaPersistenceHeader = ""; //$NON-NLS-1$
			}
			bundleProjectDescription.setHeader(META_PERSISTENCE_HEADER, metaPersistenceHeader); 
			
			// remove the javax.persistence package import, if it already exists
			ArrayList<IPackageImportDescription> imports = new ArrayList<IPackageImportDescription>();
			if (imports != null) {
				imports.addAll(Arrays.asList(bundleProjectDescription.getPackageImports()));
				
				for (IPackageImportDescription imp : imports) {
					if (JAVAX_PERSISTENCE_PACKAGE.equals(imp.getName())) {
						imports.remove(imp);
						break;
					}
				}
			}
			
			// add the javax.persistence package import
			IPackageImportDescription imp = bundleProjectService.newPackageImport(String.format(JAVAX_PERSISTENCE_PACKAGE, fv.getVersionString()), null, false);
			imports.add(imp);
			
			IPackageImportDescription[] newImports = imports.toArray(new IPackageImportDescription[imports.size()]);
			bundleProjectDescription.setPackageImports(newImports);
			
			// save the changes
			bundleProjectDescription.apply(monitor);
		}

	}

}
