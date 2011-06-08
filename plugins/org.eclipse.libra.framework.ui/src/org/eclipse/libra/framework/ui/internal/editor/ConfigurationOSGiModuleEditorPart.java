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
package org.eclipse.libra.framework.ui.internal.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.libra.framework.core.FrameworkInstanceConfiguration;
import org.eclipse.libra.framework.core.FrameworkInstanceDelegate;
import org.eclipse.libra.framework.core.IOSGIFrameworkInstance;
import org.eclipse.libra.framework.core.IOSGIFrameworkWorkingCopy;
import org.eclipse.libra.framework.core.Trace;
import org.eclipse.libra.framework.ui.ContextIds;
import org.eclipse.libra.framework.ui.FrameworkUIPlugin;
import org.eclipse.libra.framework.ui.Messages;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.help.IWorkbenchHelpSystem;
import org.eclipse.wst.server.ui.editor.ServerEditorPart;


/**
 * felix configuration  module editor page.
 */
@SuppressWarnings("restriction")
public class ConfigurationOSGiModuleEditorPart extends ServerEditorPart implements ISelectionProvider {
	protected IOSGIFrameworkWorkingCopy framework2;
	protected FrameworkInstanceConfiguration configuration;
	
	protected Table osgiBundlesTable;
	protected int selection = -1;
	protected Button addBundleProject;
	protected Button remove;

	protected PropertyChangeListener listener;


	public ConfigurationOSGiModuleEditorPart() {
		super();

	}

	/**
	 * 
	 */
	protected void addChangeListener() {
		listener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (FrameworkInstanceConfiguration.ADD_BUNDLE.equals(event.getPropertyName())) {
					initialize();
				} else if (FrameworkInstanceConfiguration.REMOVE_BUNDLE.equals(event.getPropertyName())) {
					initialize();
				}
			}
		};
		configuration.addPropertyChangeListener(listener);

	}

	/**
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		FormToolkit toolkit = getFormToolkit(parent.getDisplay());

		ScrolledForm form = toolkit.createScrolledForm(parent);
		toolkit.decorateFormHeading(form.getForm());
		form.setText(Messages.configurationEditorOSGIModulesPageTitle);
		form.setImage(FrameworkUIPlugin.getImage(FrameworkUIPlugin.IMG_WEB_MODULE));
		GridLayout layout = new GridLayout();
		layout.marginTop = 6;
		layout.marginLeft = 6;
		form.getBody().setLayout(layout);
		
		getSite().setSelectionProvider(this);  

		Section section = toolkit.createSection(form.getBody(),
				ExpandableComposite.TITLE_BAR | Section.DESCRIPTION);
		section.setText(Messages.configurationEditorOSGIModulesSection);
		section.setDescription(Messages.configurationEditorOSGIModulesDescription);
		section.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite composite = toolkit.createComposite(section);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 5;
		layout.marginWidth = 10;
		layout.verticalSpacing = 5;
		layout.horizontalSpacing = 15;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		IWorkbenchHelpSystem whs = PlatformUI.getWorkbench().getHelpSystem();
		whs.setHelp(composite, ContextIds.CONFIGURATION_EDITOR_MODULES);
		toolkit.paintBordersFor(composite);
		section.setClient(composite);

		osgiBundlesTable = toolkit.createTable(composite, SWT.V_SCROLL | SWT.SINGLE
				| SWT.FULL_SELECTION);
		osgiBundlesTable.setHeaderVisible(true);
		osgiBundlesTable.setLinesVisible(true);
		whs.setHelp(osgiBundlesTable,
				ContextIds.CONFIGURATION_EDITOR_MODULES_LIST);

		TableLayout tableLayout = new TableLayout();

		TableColumn col = new TableColumn(osgiBundlesTable, SWT.NONE);
		col.setText("Name");
		ColumnWeightData colData = new ColumnWeightData(8, 250, true);
		tableLayout.addColumnData(colData);

		TableColumn col2 = new TableColumn(osgiBundlesTable, SWT.NONE);
		col2.setText("Id");
		colData = new ColumnWeightData(13, 250, true);
		tableLayout.addColumnData(colData);

		TableColumn col3 = new TableColumn(osgiBundlesTable, SWT.NONE);
		col3.setText("Version");
		colData = new ColumnWeightData(5, 50, true);
		tableLayout.addColumnData(colData);


		osgiBundlesTable.setLayout(tableLayout);

		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 450;
		data.heightHint = 120;
		osgiBundlesTable.setLayoutData(data);
		osgiBundlesTable.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectOsgiBundle();
			}
		});

		Composite rightPanel = toolkit.createComposite(composite);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		rightPanel.setLayout(layout);
		data = new GridData();
		rightPanel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_BEGINNING));

		form.setContent(section);
		form.reflow(true);

		initialize();
	}



	public void dispose() {
		super.dispose();
		if (configuration != null) {
			configuration.removePropertyChangeListener(listener);
		}

	}

	/*
	 * (non-Javadoc) Initializes the editor part with a site and input.
	 */
	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);

		IOSGIFrameworkInstance ts = (IOSGIFrameworkInstance) server.loadAdapter(
				FrameworkInstanceDelegate.class, null);

		try {
			configuration = ts.getFrameworkInstanceConfiguration();
		} catch (CoreException e) {
			Trace.trace(Trace.SEVERE, "cannot access configuration",e);
		}
		
		if (server != null)
			framework2 = (IOSGIFrameworkWorkingCopy) server.loadAdapter(
					IOSGIFrameworkWorkingCopy.class, null);

		addChangeListener();
		initialize();
	}

	/**
	 * 
	 */
	protected void initialize() {
		if (osgiBundlesTable == null)
			return;

		osgiBundlesTable.removeAll();
		setErrorMessage(null);
	}

	/**
	 * 
	 */
	protected void selectOsgiBundle() {
		if (readOnly)
			return;

		try {
			selection = osgiBundlesTable.getSelectionIndex();
			remove.setEnabled(true);
			for(ISelectionChangedListener changedListener: selectionChangedListeners)
				changedListener.selectionChanged(new SelectionChangedEvent(this, getSelection()));
		} catch (Exception e) {
			selection = -1;
			remove.setEnabled(false);
		}
	}


	/*
	 * @see IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		if (osgiBundlesTable != null)
			osgiBundlesTable.setFocus();
	}

	
	/**
	 * This keeps track of all the {@link org.eclipse.jface.viewers.ISelectionChangedListener}s that are listening to this editor.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected Collection<ISelectionChangedListener> selectionChangedListeners = new ArrayList<ISelectionChangedListener>();

	/**
	 * This keeps track of the selection of the editor as a whole.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ISelection editorSelection = StructuredSelection.EMPTY;
	
	


	public ISelection getSelection() {
		final TableItem[] sel = osgiBundlesTable.getSelection();
		if(sel == null || sel.length == 0)
			return StructuredSelection.EMPTY;
		
		return new IStructuredSelection() {
			
			public boolean isEmpty() {
					return false;
			}
			
			public List toList() {
				List<IPluginModelBase> all =  new ArrayList<IPluginModelBase>();
				for(TableItem s: sel)
					all.add((IPluginModelBase)s.getData());
				return all;
			}
			
			public Object[] toArray() {
				
				return toList().toArray();
			}
			
			public int size() {
				
				return sel.length;
			}
			
			public Iterator iterator() {
				return toList().iterator();
			}
			
			public Object getFirstElement() {
				return sel[0].getData();
			}
		};
		
	}

	/**
	 * This implements {@link org.eclipse.jface.viewers.ISelectionProvider}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener2) {
		selectionChangedListeners.add(listener2);
	}

	/**
	 * This implements {@link org.eclipse.jface.viewers.ISelectionProvider}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener listener2) {
		selectionChangedListeners.remove(listener2);
	}

	public void setSelection(ISelection selection) {
		// TODO Auto-generated method stub
		
	}



	
}
