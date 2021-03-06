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
package org.eclipse.libra.facet.ui.operations;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.libra.facet.OSGiBundleFacetUtils;
import org.eclipse.pde.core.project.IBundleProjectDescription;
import org.eclipse.pde.core.project.IBundleProjectService;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;


public class ConvertProjectToBundleOperation extends WorkspaceModifyOperation {
	
	protected IProject fProject;
	protected IBundleProjectService fBundleProjectService;
	protected IBundleProjectDescription fBundleProjectDescription;
	
	public ConvertProjectToBundleOperation(IProject project) {
		this.fProject = project;
	}

	protected void execute(IProgressMonitor monitor) throws CoreException,
			InvocationTargetException, InterruptedException {
		IFacetedProject fproj = ProjectFacetsManager.create(fProject, true, monitor);
		fproj.installProjectFacet(OSGiBundleFacetUtils.OSGI_BUNDLE_FACET_42, null, monitor);
	}

}
