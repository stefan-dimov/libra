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

import java.util.Arrays;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.validation.MultiValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.libra.facet.OSGiBundleFacetUtils;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualReference;


public class ConvertProjectsToBundlesWizardPage extends WizardPage {
	
	private class SelectionValidator extends MultiValidator {
		
		@Override
		protected IStatus validate() {
			if (fSelected.size() == 0) {
				return ValidationStatus.cancel(getDescription());
			} else if (missingReferences()) {
				return ValidationStatus.warning(Messages.ConvertProjectsToBundlesWizardPage_ReferencedProjectsNotSelected);
			}
			return ValidationStatus.ok();
		}

		private boolean missingReferences() {
			IProject[] selectedProjects = getProjects();
			for (IProject project : selectedProjects) {
				IVirtualComponent component = ComponentCore.createComponent(project);
				if (component != null) {
					IVirtualReference[] references = component.getReferences();
					for (IVirtualReference ref : references) {
						IProject refProject = ref.getReferencedComponent().getProject();
						try {
							if (refProject != null && 
									refProject != project && 
									!OSGiBundleFacetUtils.isOSGiBundle(refProject) && 
									!fSelected.contains(refProject)) {
								return true;
							}
						} catch (CoreException e) {
							// do nothing
						}
					}
				}
			};
			return false;
		}
		
	};

	private IObservableSet fUnconverted;
	private IObservableSet fSelected;

	public ConvertProjectsToBundlesWizardPage(IProject[] unconverted, IProject[] selected) {
		super("converToWAB"); //$NON-NLS-1$
		
		setTitle(Messages.ConvertProjectsToBundlesWizardPage_Title);
		setDescription(Messages.ConvertProjectsToBundlesWizardPage_Description);
		
		this.fUnconverted = new WritableSet(Arrays.asList(unconverted), IProject.class);
		this.fSelected = new WritableSet(Arrays.asList(selected), IProject.class);
	}

	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		
		createProjectsViewer(container);

		setControl(container);
		GridLayoutFactory.swtDefaults().margins(5, 0).numColumns(2).generateLayout(container);
		Dialog.applyDialogFont(container);
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(container, "org.eclipse.libra.convertToWAB");
	}

	public IProject[] getProjects() {
		return (IProject[]) fSelected.toArray(new IProject[fSelected.size()]);
	}

	private void createProjectsViewer(Composite parent) {
		DataBindingContext dbc = new DataBindingContext();
		WizardPageSupport.create(this, dbc);
		
		Label projectsLabel = new Label(parent, SWT.NONE);
		projectsLabel.setText(Messages.ConvertProjectsToBundlesWizardPage_AvailableProjects);
		GridDataFactory.swtDefaults().span(2, 1).applyTo(projectsLabel);
		
		final CheckboxTableViewer projectsViewer = CheckboxTableViewer.newCheckList(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(projectsViewer.getControl());
		projectsViewer.setLabelProvider(new WorkbenchLabelProvider());
		projectsViewer.setContentProvider(new ArrayContentProvider());
		projectsViewer.setInput(fUnconverted);
		projectsViewer.setComparator(new ViewerComparator());
		dbc.bindSet(ViewersObservables.observeCheckedElements(projectsViewer, IProject.class), fSelected);
		dbc.addValidationStatusProvider(new SelectionValidator());
		
		Composite buttonGroup = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().generateLayout(buttonGroup);
		
		Button selectAllButton = new Button(buttonGroup, SWT.PUSH);
		selectAllButton.setText(Messages.ConvertProjectsToBundlesWizardPage_SelectAll);
		selectAllButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fSelected.addAll(fUnconverted);
			}
		});
		GridDataFactory.fillDefaults().applyTo(selectAllButton);
		
		Button deselectAllButton = new Button(buttonGroup, SWT.PUSH);
		deselectAllButton.setText(Messages.ConvertProjectsToBundlesWizardPage_DeselectAll);
		deselectAllButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				for (Object o : fUnconverted) {
					fSelected.remove(o);
				}
				
			}
		});
		GridDataFactory.fillDefaults().applyTo(deselectAllButton);
		
		Button addReferencesButton = new Button(buttonGroup, SWT.PUSH);
		addReferencesButton.setText(Messages.ConvertProjectsToBundlesWizardPage_AddReferences);
		addReferencesButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectReferences();
			}
		});
		GridDataFactory.fillDefaults().indent(0, 8).applyTo(addReferencesButton);
		
		Label selectedCountLabel = new Label(buttonGroup, SWT.WRAP);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BOTTOM).grab(false, true).applyTo(selectedCountLabel);
		dbc.bindValue(SWTObservables.observeText(selectedCountLabel), new ComputedValue(String.class) {
			@Override
			protected Object calculate() {
				return NLS.bind(Messages.ConvertProjectsToBundlesWizardPage_SelectionCounter, fSelected.size(), fUnconverted.size());
			}
		});
	}
	
	private void selectReferences() {
		IProject[] selectedProjects = getProjects();
		for (IProject project : selectedProjects) {
			IVirtualComponent component = ComponentCore.createComponent(project);
			if (component != null) {
				IVirtualReference[] references = component.getReferences();
				for (IVirtualReference ref : references) {
					IProject refProject = ref.getReferencedComponent().getProject();
					try {
						if (refProject != null && refProject != project && !OSGiBundleFacetUtils.isOSGiBundle(refProject)) {
							fSelected.add(refProject);
						}
					} catch (CoreException e) {
						// do nothing
					}
				}
			}
		}
	}
	
}
