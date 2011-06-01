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

import org.eclipse.core.resources.IProject;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.web.internal.deployables.FlatComponentDeployable;

@SuppressWarnings("restriction")
public class OsgiModuleDeployable extends FlatComponentDeployable {


	IVirtualComponent component;
	
	public OsgiModuleDeployable(IProject project, IVirtualComponent component) {
		super(project);
		this.component = component;
	}

	public String getVersion() {
		IFacetedProject facetedProject = null;
		try {
			facetedProject = ProjectFacetsManager.create(component.getProject());
			if (facetedProject !=null && ProjectFacetsManager.isProjectFacetDefined("osgi.bundle")) {
				IProjectFacet projectFacet = ProjectFacetsManager.getProjectFacet("osgi.bundle");
				return facetedProject.getInstalledVersion(projectFacet).getVersionString();
			}
		} catch (Exception e) {
			//Ignore
		}
		return "1.0"; //$NON-NLS-1$
	}
}
