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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.pde.core.project.IBundleProjectDescription;
import org.eclipse.pde.core.project.IBundleProjectService;
import org.eclipse.pde.core.project.IPackageImportDescription;
import org.eclipse.wst.common.project.facet.core.IDelegate;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;


public class OSGiBundleFacetEventHandler implements IDelegate {

	public void execute(IProject project, IProjectFacetVersion fv,
			Object config, IProgressMonitor monitor) throws CoreException {
		
		if ("jpt.jpa".equals(fv.getProjectFacet().getId()) && OSGiBundleUtils.isOSGiBundle(project)) {
			IBundleProjectService bundleProjectService = Activator.getDefault().getBundleProjectService();
			IBundleProjectDescription bundleProjectDescription = bundleProjectService.getDescription(project);
			
			// add the Meta-Persistence manifest header
			if (null == bundleProjectDescription.getHeader("Meta-Persistence")) {
				bundleProjectDescription.setHeader("Meta-Persistence", "");
			}
			
			// add the javax.persistence package import
			IPackageImportDescription[] imports = bundleProjectDescription.getPackageImports();
			boolean found = false;
			if (imports != null) {
				for (IPackageImportDescription imp : imports) {
					if ("javax.persistence".equals(imp.getName())) {
						found = true;
						break;
					}
				}
			}
			if (!found) {
				IPackageImportDescription imp = bundleProjectService.newPackageImport("javax.persistence", null, false);
				IPackageImportDescription[] newImports;
				if (imports == null) {
					newImports = new IPackageImportDescription[1];
				} else {
					newImports = new IPackageImportDescription[imports.length + 1];
					System.arraycopy(imports, 0, newImports, 0, imports.length);
				}
				newImports[newImports.length - 1] = imp;
				bundleProjectDescription.setPackageImports(newImports);
			}
			
			// save the changes
			bundleProjectDescription.apply(monitor);
		}

	}

}
