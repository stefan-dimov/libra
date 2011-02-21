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
package org.eclipse.libra.facet.ui.popup.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.libra.facet.OSGiBundleUtils;
import org.eclipse.libra.facet.ui.wizards.ConvertProjectsToBundlesWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;


public class ConvertProjectsToBundlesAction implements IObjectActionDelegate {

	private ISelection fSelection;
	
	/**
	 * Constructor for ConvertProjectsToBundlesAction.
	 */
	public ConvertProjectsToBundlesAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
//		shell = targetPart.getSite().getShell();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		IProject[] unconverted = getUnconvertedProjects();
		if (unconverted.length == 0) {
			MessageDialog.openInformation(getDisplay().getActiveShell(), "Find Project to Convert", "There are no projects to convert. "); 
			return;
		}
		
		IProject[] selected = getSelectedProjects();

		ConvertProjectsToBundlesWizard wizard = new ConvertProjectsToBundlesWizard(unconverted, selected);

		final Display display = getDisplay();
		final WizardDialog dialog = new WizardDialog(display.getActiveShell(), wizard) {
			@Override
			protected Point getInitialSize() {
				// force the dialog width to prevent extensive resizing by initial warning message
				return getShell().computeSize(500, SWT.DEFAULT);
			}
		};
		BusyIndicator.showWhile(display, new Runnable() {
			public void run() {
				dialog.open();
			}
		});
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.fSelection = selection;
	}
	
	public Display getDisplay() {
		Display display = Display.getCurrent();
		if (display == null)
			display = Display.getDefault();
		return display;
	}
	
	private IProject[] getUnconvertedProjects() {
		List<IProject> unconverted = new ArrayList<IProject>();
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (IProject project : projects) {
			try {
				if (project.isOpen() && 
						!OSGiBundleUtils.hasFeatureNature(project) &&
						!OSGiBundleUtils.hasUpdateSiteNature(project) &&
						!OSGiBundleUtils.isOSGiBundle(project)) {
					unconverted.add(project);
				}
			} catch (CoreException e) {
				// do nothing
			}
		}
		return unconverted.toArray(new IProject[unconverted.size()]);
	}
	
	private IProject[] getSelectedProjects() {
		List<IProject> selected = new ArrayList<IProject>();
		
		if (fSelection instanceof IStructuredSelection) {
			Object[] objs = ((IStructuredSelection) fSelection).toArray();
			for (Object obj : objs) {
				IProject project = null;
				if (obj instanceof IProject) {
					project = (IProject) obj;
				} else if (obj instanceof IAdaptable) {
					project = (IProject) ((IAdaptable) obj).getAdapter(IProject.class);
				} else {
					IAdapterManager manager = Platform.getAdapterManager();
					project = (IProject) manager.getAdapter(obj, IProject.class);
				}
				
				if (project != null) {
					selected.add(project);
				}
			}
		}
		
		return selected.toArray(new IProject[selected.size()]);
	}

}
