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
package org.eclipse.libra.facet.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.libra.facet.ui.operations.ConvertProjectsToBundlesOperation;


public class ConvertProjectsToBundlesWizard extends Wizard {

	private IProject[] fUnconverted;
	private IProject[] fSelected;
	
	private ConvertProjectsToBundlesWizardPage mainPage;
	
	public ConvertProjectsToBundlesWizard(IProject[] unconverted, IProject[] selected) {
		this.fUnconverted = unconverted;
		this.fSelected = selected;
		
//		setDefaultPageImageDescriptor(PDEPluginImages.DESC_CONVJPPRJ_WIZ);
		setWindowTitle("Convert to OSGi Bundle Projects");
//		setDialogSettings(PDEPlugin.getDefault().getDialogSettings());
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		mainPage = new ConvertProjectsToBundlesWizardPage(fUnconverted, fSelected);
		addPage(mainPage);
	}

	public boolean performFinish() {
		IProject[] projects = mainPage.getProjects();
		
		IRunnableWithProgress convertOperation = new ConvertProjectsToBundlesOperation(projects);
		try {
			getContainer().run(false, true, convertOperation);
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

}
