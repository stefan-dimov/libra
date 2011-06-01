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

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.libra.framework.core.FrameworkInstanceDelegate;
import org.eclipse.libra.framework.core.IOSGIFrameworkInstance;
import org.eclipse.libra.framework.ui.ContextIds;
import org.eclipse.libra.framework.ui.FrameworkUIPlugin;
import org.eclipse.libra.framework.ui.Messages;
import org.eclipse.libra.framework.ui.Trace;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.internal.core.target.TargetPlatformService;
import org.eclipse.pde.internal.core.target.provisional.ITargetDefinition;
import org.eclipse.pde.internal.ui.wizards.target.EditTargetDefinitionWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.help.IWorkbenchHelpSystem;
import org.eclipse.wst.server.core.IPublishListener;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.util.PublishAdapter;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;




@SuppressWarnings("restriction")
public class FrameworkInstanceLocationEditorSection extends ServerEditorSection {
	protected Section section;
	protected IOSGIFrameworkInstance frameworkInstance;
	
	protected boolean defaultDeployDirIsSet;
	
	protected Button frameworkInstanceDirCustom;
	
	protected Text frameworkInstanceDir;
	protected Button frameworkInstanceDirBrowse;

	protected Text targetPlatformName;
	protected Button targetPlatformEdit;
	
	protected PropertyChangeListener listener;
	protected IPublishListener publishListener;
	protected IPath workspacePath;
	
	protected boolean allowRestrictedEditing;
	protected IPath tempDirPath;
	protected IPath installDirPath;

	// Avoid hardcoding this at some point
	//private final static String METADATADIR = ".metadata";

	protected boolean updating=false;

	public FrameworkInstanceLocationEditorSection() {
		super();
	}

	/**
	 * Add listeners to detect undo changes and publishing of the server.
	 */
	protected void addChangeListeners() {
		listener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (updating)
					return;
				updating = true;
				if (IOSGIFrameworkInstance.PROPERTY_INSTANCE_DIR.equals(event.getPropertyName())) {
					updateServerDirButtons();
					updateServerDirFields();
					validate();
				}
				updating = false;
			}
		};
		server.addPropertyChangeListener(listener);
		
		publishListener = new PublishAdapter() {
			public void publishFinished(IServer server2, IStatus status) {
				boolean flag = false;
				if (status.isOK() && server2.getModules().length == 0)
					flag = true;
				if (flag != allowRestrictedEditing) {
					allowRestrictedEditing = flag;
					// Update the state of the fields
					PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
						public void run() {
							boolean customServerDir = false;
							if (!FrameworkInstanceLocationEditorSection.this.frameworkInstanceDirCustom.isDisposed())
								customServerDir = FrameworkInstanceLocationEditorSection.this.frameworkInstanceDirCustom.getSelection();
							if (!FrameworkInstanceLocationEditorSection.this.frameworkInstanceDirCustom.isDisposed())
								FrameworkInstanceLocationEditorSection.this.frameworkInstanceDirCustom.setEnabled(allowRestrictedEditing);
							if (!FrameworkInstanceLocationEditorSection.this.frameworkInstanceDir.isDisposed())
								FrameworkInstanceLocationEditorSection.this.frameworkInstanceDir.setEnabled(allowRestrictedEditing && customServerDir);
							if (!FrameworkInstanceLocationEditorSection.this.frameworkInstanceDirBrowse.isDisposed())
								FrameworkInstanceLocationEditorSection.this.frameworkInstanceDirBrowse.setEnabled(allowRestrictedEditing && customServerDir);
						}
					});
				}
			}
		};
		server.getOriginal().addPublishListener(publishListener);
	}
	
	/**
	 * Creates the SWT controls for this workbench part.
	 *
	 * @param parent the parent control
	 */
	public void createSection(Composite parent) {
		super.createSection(parent);
		FormToolkit toolkit = getFormToolkit(parent.getDisplay());

		section = toolkit.createSection(parent, ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED
			| ExpandableComposite.TITLE_BAR | Section.DESCRIPTION | ExpandableComposite.FOCUS_TITLE);
		section.setText(Messages.serverEditorLocationsSection);
		section.setDescription(Messages.serverEditorLocationsDescription);
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));

		Composite composite = toolkit.createComposite(section);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginHeight = 5;
		layout.marginWidth = 10;
		layout.verticalSpacing = 5;
		layout.horizontalSpacing = 15;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));
		IWorkbenchHelpSystem whs = PlatformUI.getWorkbench().getHelpSystem();
		whs.setHelp(composite, ContextIds.FRAMEWORK_INSTANCE_EDITOR);
		whs.setHelp(section, ContextIds.FRAMEWORK_INSTANCE_EDITOR);
		toolkit.paintBordersFor(composite);
		section.setClient(composite);

		frameworkInstanceDirCustom = toolkit.createButton(composite,
				NLS.bind(Messages.serverEditorServerDirCustom, Messages.serverEditorDoesNotModify), SWT.RADIO);
		GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.horizontalSpan = 3;
		frameworkInstanceDirCustom.setLayoutData(data);
		frameworkInstanceDirCustom.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (updating || !frameworkInstanceDirCustom.getSelection())
					return;
				updating = true;
				//execute(new SetTestEnvironmentCommand(felixRuntimeInstance, true));
				updateServerDirFields();
				updating = false;
				validate();
			}
		});

		// server directory
		Label label = createLabel(toolkit, composite, Messages.serverEditorServerDir);
		data = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		label.setLayoutData(data);

		frameworkInstanceDir = toolkit.createText(composite, null, SWT.SINGLE);
		data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.widthHint = 75;
		frameworkInstanceDir.setLayoutData(data);
		frameworkInstanceDir.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (updating)
					return;
				updating = true;
				//execute(new SetInstanceDirectoryCommand(felixRuntimeInstance, getServerDir()));
				updating = false;
				validate();
			}
		});

		frameworkInstanceDirBrowse = toolkit.createButton(composite, Messages.editorBrowse, SWT.PUSH);
		frameworkInstanceDirBrowse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent se) {
				DirectoryDialog dialog = new DirectoryDialog(frameworkInstanceDir.getShell());
				dialog.setMessage(Messages.serverEditorBrowseDeployMessage);
				dialog.setFilterPath(frameworkInstanceDir.getText());
				String selectedDirectory = dialog.open();
				if (selectedDirectory != null && !selectedDirectory.equals(frameworkInstanceDir.getText())) {
					updating = true;
					// Make relative if relative to the workspace
//					IPath path = new Path(selectedDirectory);
//					if (workspacePath.isPrefixOf(path)) {
//						int cnt = path.matchingFirstSegments(workspacePath);
//						path = path.removeFirstSegments(cnt).setDevice(null);
//						selectedDirectory = path.toOSString();
//					}
					//execute(new SetInstanceDirectoryCommand(felixRuntimeInstance, selectedDirectory));
					updateServerDirButtons();
					updateServerDirFields();
					updating = false;
					validate();
				}
			}
		});
		frameworkInstanceDirBrowse.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		
		
		// server directory
		Label label2 = createLabel(toolkit, composite, "Target Platform");
		data = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		label2.setLayoutData(data);

		targetPlatformName = toolkit.createText(composite, null, SWT.SINGLE);
		data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.widthHint = 75;
		targetPlatformName.setLayoutData(data);
		

		targetPlatformEdit = toolkit.createButton(composite, "Edit...", SWT.PUSH);
		targetPlatformEdit.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent se) {
				handleEdit();
			}
		});
		targetPlatformEdit.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		
		initialize();
	}

	protected Label createLabel(FormToolkit toolkit, Composite parent, String text) {
		Label label = toolkit.createLabel(parent, text);
		label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		return label;
	}

	/**
	 * @see ServerEditorSection#dispose()
	 */
	public void dispose() {
		if (server != null) {
			server.removePropertyChangeListener(listener);
			if (server.getOriginal() != null)
				server.getOriginal().removePublishListener(publishListener);
		}
	}

	/**
	 * @see ServerEditorSection#init(IEditorSite, IEditorInput)
	 */
	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);
		
		// Cache workspace and default deploy paths
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		workspacePath = root.getLocation();

		if (server != null) {
			frameworkInstance = (FrameworkInstanceDelegate) server.loadAdapter(FrameworkInstanceDelegate.class, null);
			addChangeListeners();
		}
		initialize();
	}

	/**
	 * Initialize the fields in this editor.
	 */
	protected void initialize() {
		if (frameworkInstanceDir== null || frameworkInstance == null)
			return;
		updating = true;

		IRuntime runtime = server.getRuntime();
		section.setDescription(Messages.serverEditorLocationsDescription2);
		if (runtime != null)
			installDirPath = runtime.getLocation();

		// determine if editing of locations is allowed
		allowRestrictedEditing = false;
		IPath basePath = new Path(frameworkInstance.getInstanceDirectory());
		if (!readOnly) {
			// If server has not been published, or server is published with no modules, allow editing
			// TODO Find better way to determine if server hasn't been published
			if ((basePath != null && !basePath.append("conf").toFile().exists())
					|| (server.getOriginal().getServerPublishState() == IServer.PUBLISH_STATE_NONE
							&& server.getOriginal().getModules().length == 0)) {
				allowRestrictedEditing = true;
			}
		}
		
		try {
			if (frameworkInstance != null && frameworkInstance.getFrameworkInstanceConfiguration() != null) {

				ITargetDefinition original = frameworkInstance.getFrameworkInstanceConfiguration()
						.getTargetDefinition();
				targetPlatformName.setText(original.getName());
				targetPlatformName.setEditable(false);
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Update server related fields
		updateServerDirButtons();
		updateServerDirFields();
		updating = false;
		validate();
	}
	
	protected String getServerDir() {
		String dir = null;
		if (frameworkInstanceDir != null) {
			dir = frameworkInstanceDir.getText().trim();
			IPath path = new Path(dir);
			// Adjust if the temp dir is known and has been entered
			if (tempDirPath != null && tempDirPath.equals(path))
				dir = null;
//			// If under the workspace, make relative
//			else if (workspacePath.isPrefixOf(path)) {
//				int cnt = path.matchingFirstSegments(workspacePath);
//				path = path.removeFirstSegments(cnt).setDevice(null);
//				dir = path.toOSString();
//			}
		}
		return dir;
	}
	
	protected void updateServerDirButtons() {
		if (frameworkInstance.getInstanceDirectory() == null) {
			IPath path = new Path(frameworkInstance.getInstanceDirectory());
			if (path != null && path.equals(installDirPath)) {
				frameworkInstanceDirCustom.setSelection(false);
			} else {
				frameworkInstanceDirCustom.setSelection(false);
			}
		} else {
			frameworkInstanceDirCustom.setSelection(false);
		}
	}
	
	protected void updateServerDirFields() {
		updateServerDir();
		boolean customServerDir = frameworkInstanceDirCustom.getSelection();
		frameworkInstanceDir.setEnabled(allowRestrictedEditing && customServerDir);
		frameworkInstanceDirBrowse.setEnabled(allowRestrictedEditing && customServerDir);
	}
	
	protected void updateServerDir() {
		IPath path = new Path(frameworkInstance.getInstanceDirectory());
//		if (workspacePath.isPrefixOf(path)) {
//			int cnt = path.matchingFirstSegments(workspacePath);
//			path = path.removeFirstSegments(cnt).setDevice(null);
//			frameworkInstanceDir.setText(path.toOSString());
//			// cache the relative temp dir path if that is what we have
//			if (tempDirPath == null) {
//				if (felixRuntimeInstance.getInstanceDirectory() == null)
//					tempDirPath = path;
//			}
//		} else
			frameworkInstanceDir.setText(path.toOSString());
	}
	

	
	/**
	 * @see ServerEditorSection#getSaveStatus()
	 */
	public IStatus[] getSaveStatus() {
		if (frameworkInstance != null) {
			// Check the instance directory
			String dir = frameworkInstance.getInstanceDirectory();
			if (dir != null) {
				IPath path = new Path(dir);
				// Must not be the same as the workspace location
				if (dir.length() == 0 || workspacePath.equals(path)) {
					return new IStatus [] {
							new Status(IStatus.ERROR, FrameworkUIPlugin.PLUGIN_ID, Messages.errorServerDirIsRoot)};
				}
//				// User specified value may not be under the ".metadata" folder of the workspace 
//				else if (workspacePath.isPrefixOf(path)
//						|| (!path.isAbsolute() && METADATADIR.equals(path.segment(0)))) {
//					int cnt = path.matchingFirstSegments(workspacePath);
//					if (METADATADIR.equals(path.segment(cnt))) {
//						return new IStatus [] {
//								new Status(IStatus.ERROR, FelixUIPlugin.PLUGIN_ID, NLS.bind(Messages.errorServerDirUnderRoot, METADATADIR))};
//					}
//				}
				else if (path.equals(installDirPath))
					return new IStatus [] {
						new Status(IStatus.ERROR, FrameworkUIPlugin.PLUGIN_ID,
								NLS.bind(Messages.errorServerDirCustomNotInstall,
										NLS.bind(Messages.serverEditorServerDirInstall, "").trim()))};
			}
			
			
		}
		// use default implementation to return success
		return super.getSaveStatus();
	}
	
	protected void validate() {
		if (frameworkInstance != null) {
			// Validate instance directory
			String dir = frameworkInstance.getInstanceDirectory();
			if (dir != null) {
				IPath path = new Path(dir);
				// Must not be the same as the workspace location
				if (dir.length() == 0 || workspacePath.equals(path)) {
					setErrorMessage(Messages.errorServerDirIsRoot);
					return;
				}
				// User specified value may not be under the ".metadata" folder of the workspace 
//				else if (workspacePath.isPrefixOf(path)
//						|| (!path.isAbsolute() && METADATADIR.equals(path.segment(0)))) {
//					int cnt = path.matchingFirstSegments(workspacePath);
//					if (METADATADIR.equals(path.segment(cnt))) {
//						setErrorMessage(NLS.bind(Messages.errorServerDirUnderRoot, METADATADIR));
//						return;
//					}
//				}
				else if (path.equals(installDirPath)) {
					setErrorMessage(NLS.bind(Messages.errorServerDirCustomNotInstall,
							NLS.bind(Messages.serverEditorServerDirInstall, "").trim()));
					return;
				}
			}
			
		}
		// All is okay, clear any previous error
		setErrorMessage(null);
	}


	protected void handleEdit() {
		
		try {
			if (frameworkInstance != null && frameworkInstance.getFrameworkInstanceConfiguration() != null) {

				ITargetDefinition original = frameworkInstance.getFrameworkInstanceConfiguration()
						.getTargetDefinition();

				EditTargetDefinitionWizard wizard = new EditTargetDefinitionWizard(
						original, true);
				wizard.setWindowTitle(Messages.configurationEditorTargetDefinitionTitle);
				WizardDialog dialog = new WizardDialog(this.getShell(),
						wizard);
				if (dialog.open() == Window.OK) {
					// Replace all references to the original with the new target
					ITargetDefinition newTarget = wizard.getTargetDefinition();
					frameworkInstance.getFrameworkInstanceConfiguration().setTargetDefinition(newTarget);
					TargetPlatformService.getDefault().saveTargetDefinition(newTarget);
				}
			}
		} catch (CoreException e) {
			Trace.trace(Trace.SEVERE, "failed to update target platform definition");
		}
	}
}