/*******************************************************************************
 * <copyright>
 *
 * Copyright (c) 2005, 2010 SAP AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kaloyan Raev - initial API, implementation and documentation
 *
 * </copyright>
 *
 *******************************************************************************/
package org.eclipse.libra.facet.ui.operations;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.libra.facet.OSGiBundleUtils;
import org.eclipse.ui.actions.WorkspaceModifyOperation;


public class ConvertProjectsToBundlesOperation extends WorkspaceModifyOperation {
	
	private IProject[] fProjects;

	public ConvertProjectsToBundlesOperation(IProject[] projects) {
		this.fProjects = projects;
	}

	@Override
	protected void execute(IProgressMonitor monitor) throws CoreException,
			InvocationTargetException, InterruptedException {

		// first convert non-Web projects
		for (IProject project : fProjects) {
			if (!OSGiBundleUtils.isWebProject(project)) {
				new ConvertProjectToBundleOperation(project).execute(monitor);
			}
		}
		
		// then convert the Web projects
		// this will make the dependencies from referenced libraries to be calculated correctly 
		for (IProject project : fProjects) {
			if (OSGiBundleUtils.isWebProject(project)) {
				new ConvertProjectToBundleOperation(project).execute(monitor);
			}
		}
	}

}
