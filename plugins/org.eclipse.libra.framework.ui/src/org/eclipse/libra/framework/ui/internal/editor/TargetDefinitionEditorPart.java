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
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;
import org.eclipse.jface.window.Window;
import org.eclipse.libra.framework.core.FrameworkInstanceConfiguration;
import org.eclipse.libra.framework.core.IOSGIFrameworkInstance;
import org.eclipse.libra.framework.core.IOSGIFrameworkWorkingCopy;
import org.eclipse.libra.framework.core.Trace;
import org.eclipse.pde.internal.core.ICoreConstants;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.target.TargetPlatformService;
import org.eclipse.pde.internal.core.target.provisional.ITargetDefinition;
import org.eclipse.pde.internal.core.util.VMUtil;
import org.eclipse.pde.internal.ui.IHelpContextIds;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.SWTFactory;
import org.eclipse.pde.internal.ui.shared.target.ArgumentsFromContainerSelectionDialog;
import org.eclipse.pde.internal.ui.shared.target.ITargetChangedListener;
import org.eclipse.pde.internal.ui.shared.target.TargetContentsGroup;
import org.eclipse.pde.internal.ui.shared.target.TargetLocationsGroup;
import org.eclipse.pde.internal.ui.util.LocaleUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.wst.server.ui.editor.ServerEditorPart;


@SuppressWarnings("restriction")
public class TargetDefinitionEditorPart extends ServerEditorPart {

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	protected ITargetDefinition targetDefinition;
	protected IOSGIFrameworkWorkingCopy server2;
	protected FrameworkInstanceConfiguration configuration;

	private Text fNameText;
	private TabItem fLocationTab;
	private TargetLocationsGroup fLocationTree;
	private TargetContentsGroup fContentTree;

	protected PropertyChangeListener listener;
	private Button fDefaultJREButton;
	private Button fNamedJREButton;
	private Combo fNamedJREsCombo;
	private Button fExecEnvButton;
	private TreeSet fExecEnvChoices;
	private Combo fExecEnvsCombo;
	private TreeSet fOSChoices;
	private Combo fOSCombo;
	private Combo fWSCombo;
	private TreeSet fArchChoices;
	private Combo fArchCombo;
	private Combo fNLCombo;
	private Text fProgramArgs;
	private Text fVMArgs;

	private TreeSet fWSChoices;

	private TreeSet fNLChoices;

	public TargetDefinitionEditorPart() {
		super();

	}

	public ITargetDefinition getTargetDefinition() {
		if (configuration != null && targetDefinition == null)
			targetDefinition = configuration.getTargetDefinition();
		return targetDefinition;
	}

	/**
	 * 
	 */
	protected void addChangeListener() {
		listener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (FrameworkInstanceConfiguration.ADD_BUNDLE.equals(event
						.getPropertyName())) {
					initialize();
				} else if (FrameworkInstanceConfiguration.REMOVE_BUNDLE
						.equals(event.getPropertyName())) {
					initialize();
				}
			}
		};
		configuration.addPropertyChangeListener(listener);

	}

	public IStatus resolveBundles(final ITargetDefinition definition) {
		if (!definition.isResolved()) {
			definition.resolve(new NullProgressMonitor());
		}
		fContentTree.setInput(definition);
		fLocationTree.setInput(definition);
		if (definition.isResolved()
				&& definition.getBundleStatus().getSeverity() == IStatus.ERROR) {
			fLocationTab.setImage(PlatformUI.getWorkbench().getSharedImages()
					.getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
		} else {
			fLocationTab.setImage(null);
		}
		return Status.OK_STATUS;
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.pde.internal.ui.wizards.target.TargetDefinitionPage#targetChanged
	 * ()
	 */
	protected void targetChanged(ITargetDefinition definition) {
		if (fContentTree == null || fLocationTree == null)
			return;

		if (definition != null) {

			String name = definition.getName();
			if (name == null) {
				name = "";
			}

			if (name.trim().length() > 0)
				fNameText.setText(name);

			fLocationTree.setInput(definition);
			fContentTree.setInput(definition);
			updateArgsEnv(definition);

		}
	}

	public void targetDefinitionContentsChanged(
			final ITargetDefinition definition, Object source, boolean resolve,
			boolean forceResolve) {

		boolean setCancelled = false;
		
		makeDirty(definition);
		
		targetDefinition = definition;

		try {
			new UIJob(PDEUIMessages.TargetDefinitionContentPage_0) {
				public IStatus runInUIThread(IProgressMonitor monitor) {
					return resolveBundles(definition);
				}

			}.schedule();
		} catch (Exception e) {
			Trace.trace(Trace.SEVERE, "", e);
		}
		if (fContentTree != source) {
			if (setCancelled) {
				fContentTree.setCancelled();
			} else {
				fContentTree.setInput(definition);
			}
		}
		if (fLocationTree != source) {
			fLocationTree.setInput(definition);
		}
		if (definition.isResolved()
				&& definition.getBundleStatus().getSeverity() == IStatus.ERROR) {
			fLocationTab.setImage(PlatformUI.getWorkbench().getSharedImages()
					.getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
		} else {
			fLocationTab.setImage(null);
		}


		updateArgsEnv(definition);

		//fElementViewer.refresh();

	}

	private void makeDirty(final ITargetDefinition definition) {
		//This command does nothing but execute sets the dirty flag
		//for the editor because the content of the target definition has
		//changed
		execute(new TargetChangedCommand(definition));
	}

	private void updateArgsEnv(final ITargetDefinition definition) {
		String presetValue = (definition.getOS() == null) ? EMPTY_STRING : definition.getOS();
		fOSCombo.setText(presetValue);
		presetValue = (definition.getWS() == null) ? EMPTY_STRING : definition.getWS();
		fWSCombo.setText(presetValue);
		presetValue = (definition.getArch() == null) ? EMPTY_STRING : definition.getArch();
		fArchCombo.setText(presetValue);
		presetValue = (definition.getNL() == null) ? EMPTY_STRING : LocaleUtil.expandLocaleName(definition.getNL());
		fNLCombo.setText(presetValue);

		IPath jrePath = definition.getJREContainer();
		if (jrePath == null || jrePath.equals(JavaRuntime.newDefaultJREContainerPath())) {
			fDefaultJREButton.setSelection(true);
		} else {
			String ee = JavaRuntime.getExecutionEnvironmentId(jrePath);
			if (ee != null) {
				fExecEnvButton.setSelection(true);
				fExecEnvsCombo.select(fExecEnvsCombo.indexOf(ee));
			} else {
				String vm = JavaRuntime.getVMInstallName(jrePath);
				if (vm != null) {
					fNamedJREButton.setSelection(true);
					fNamedJREsCombo.select(fNamedJREsCombo.indexOf(vm));
				}
			}
		}

		if (fExecEnvsCombo.getSelectionIndex() == -1)
			fExecEnvsCombo.setText(fExecEnvChoices.first().toString());

		if (fNamedJREsCombo.getSelectionIndex() == -1)
			fNamedJREsCombo.setText(VMUtil.getDefaultVMInstallName());

		updateJREWidgets();

		presetValue = (definition.getProgramArguments() == null) ? EMPTY_STRING : definition.getProgramArguments();
		fProgramArgs.setText(presetValue);
		presetValue = (definition.getVMArguments() == null) ? EMPTY_STRING : definition.getVMArguments();
		fVMArgs.setText(presetValue);
	}

	private void addTargetListeners() {

		ITargetChangedListener listener2 = new ITargetChangedListener() {
			public void contentsChanged(ITargetDefinition definition,
					Object source, boolean resolve, boolean forceResolve) {
				targetDefinitionContentsChanged(definition, source, resolve,
						forceResolve);
			}
		};
		fContentTree.addTargetChangedListener(listener2);
		fLocationTree.addTargetChangedListener(listener2);
	}

	public void createPartControl(Composite parent) {
		FormToolkit toolkit = getFormToolkit(parent.getDisplay());
		targetChanged(getTargetDefinition());

		Composite comp = SWTFactory.createComposite(parent, 1, 1,
				GridData.FILL_BOTH, 0, 0);

		Composite nameComp = SWTFactory.createComposite(comp, 2, 1,
				GridData.FILL_HORIZONTAL, 0, 0);

		SWTFactory.createLabel(nameComp,
				PDEUIMessages.TargetDefinitionContentPage_4, 1);

		fNameText = SWTFactory.createSingleText(nameComp, 1);
		fNameText.setEditable(false);

		TabFolder tabs = new TabFolder(comp, SWT.NONE);
		tabs.setLayoutData(new GridData(GridData.FILL_BOTH));

		fLocationTab = new TabItem(tabs, SWT.NONE);
		fLocationTab.setText(PDEUIMessages.LocationSection_0);

		Composite pluginTabContainer = SWTFactory.createComposite(tabs, 1, 1,
				GridData.FILL_BOTH);
		SWTFactory.createWrapLabel(pluginTabContainer,
				PDEUIMessages.TargetDefinitionContentPage_LocationDescription,
				2, 400);
		fLocationTree = TargetLocationsGroup.createInForm(pluginTabContainer,
				toolkit);
		fLocationTab.setControl(pluginTabContainer);

		TabItem contentTab = new TabItem(tabs, SWT.NONE);
		contentTab.setText(PDEUIMessages.TargetDefinitionContentPage_6);
		Composite contentTabContainer = SWTFactory.createComposite(tabs, 1, 1,
				GridData.FILL_BOTH);
		SWTFactory.createWrapLabel(contentTabContainer,
				PDEUIMessages.ContentSection_1, 2, 400);
		fContentTree = TargetContentsGroup.createInForm(contentTabContainer,
				toolkit);
		contentTab.setControl(contentTabContainer);
		
		
		TabItem envTab = new TabItem(tabs, SWT.NONE);
		envTab.setText(PDEUIMessages.TargetDefinitionEnvironmentPage_3);
		Composite envTabContainer = SWTFactory.createComposite(tabs, 1, 1, GridData.FILL_BOTH);
		createTargetEnvironmentGroup(envTabContainer);
		createJREGroup(envTabContainer);
		envTab.setControl(envTabContainer);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(envTabContainer, IHelpContextIds.EDIT_TARGET_WIZARD_ENVIRONMENT_TAB);

		TabItem argsTab = new TabItem(tabs, SWT.NONE);
		argsTab.setText(PDEUIMessages.TargetDefinitionEnvironmentPage_4);
		argsTab.setControl(createArgumentsGroup(tabs));
		PlatformUI.getWorkbench().getHelpSystem().setHelp(argsTab.getControl(), IHelpContextIds.EDIT_TARGET_WIZARD_ARGUMENT_TAB);



		initialize();

		addTargetListeners();

	}
	
	private void createJREGroup(Composite container) {
		Group group = SWTFactory.createGroup(container, PDEUIMessages.EnvironmentBlock_jreTitle, 2, 1, GridData.FILL_HORIZONTAL);

		initializeJREValues();

		SWTFactory.createWrapLabel(group, PDEUIMessages.JRESection_description, 2);

		fDefaultJREButton = SWTFactory.createRadioButton(group, PDEUIMessages.JRESection_defaultJRE, 2);
		fDefaultJREButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateJREWidgets();
				getTargetDefinition().setJREContainer(JavaRuntime.newDefaultJREContainerPath());
				makeDirty(getTargetDefinition());
			}
		});

		fNamedJREButton = SWTFactory.createRadioButton(group, PDEUIMessages.JRESection_JREName);
		fNamedJREButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateJREWidgets();
				getTargetDefinition().setJREContainer(JavaRuntime.newJREContainerPath(VMUtil.getVMInstall(fNamedJREsCombo.getText())));
				makeDirty(getTargetDefinition());
			}
		});

		fNamedJREsCombo = SWTFactory.createCombo(group, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY, 1, VMUtil.getVMInstallNames());
		fNamedJREsCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getTargetDefinition().setJREContainer(JavaRuntime.newJREContainerPath(VMUtil.getVMInstall(fNamedJREsCombo.getText())));
				makeDirty(getTargetDefinition());
			}
		});

		fExecEnvButton = SWTFactory.createRadioButton(group, PDEUIMessages.JRESection_ExecutionEnv);
		fExecEnvButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateJREWidgets();
				getTargetDefinition().setJREContainer(JavaRuntime.newJREContainerPath(VMUtil.getExecutionEnvironment(fExecEnvsCombo.getText())));
				makeDirty(getTargetDefinition());
			}
		});

		fExecEnvsCombo = SWTFactory.createCombo(group, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY, 1, (String[]) fExecEnvChoices.toArray(new String[fExecEnvChoices.size()]));
		fExecEnvsCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getTargetDefinition().setJREContainer(JavaRuntime.newJREContainerPath(VMUtil.getExecutionEnvironment(fExecEnvsCombo.getText())));
				makeDirty(getTargetDefinition());
			}
		});

	}
	
	
	/**
	 * Initializes the combo with possible execution environments
	 */
	protected void initializeJREValues() {
		fExecEnvChoices = new TreeSet();
		IExecutionEnvironmentsManager manager = JavaRuntime.getExecutionEnvironmentsManager();
		IExecutionEnvironment[] envs = manager.getExecutionEnvironments();
		for (int i = 0; i < envs.length; i++)
			fExecEnvChoices.add(envs[i].getId());
	}

	protected void updateJREWidgets() {
		fNamedJREsCombo.setEnabled(fNamedJREButton.getSelection());
		fExecEnvsCombo.setEnabled(fExecEnvButton.getSelection());
	}
	private void createTargetEnvironmentGroup(Composite container) {
		Group group = SWTFactory.createGroup(container, PDEUIMessages.EnvironmentBlock_targetEnv, 2, 1, GridData.FILL_HORIZONTAL);

		initializeChoices();

		SWTFactory.createWrapLabel(group, PDEUIMessages.EnvironmentSection_description, 2);

		SWTFactory.createLabel(group, PDEUIMessages.Preferences_TargetEnvironmentPage_os, 1);

		fOSCombo = SWTFactory.createCombo(group, SWT.SINGLE | SWT.BORDER, 1, (String[]) fOSChoices.toArray(new String[fOSChoices.size()]));
		fOSCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getTargetDefinition().setOS(getModelValue(fOSCombo.getText()));
				makeDirty(getTargetDefinition());
			}
		});

		SWTFactory.createLabel(group, PDEUIMessages.Preferences_TargetEnvironmentPage_ws, 1);

		fWSCombo = SWTFactory.createCombo(group, SWT.SINGLE | SWT.BORDER, 1, (String[]) fWSChoices.toArray(new String[fWSChoices.size()]));
		fWSCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getTargetDefinition().setWS(getModelValue(fWSCombo.getText()));
				makeDirty(getTargetDefinition());
			}
		});

		SWTFactory.createLabel(group, PDEUIMessages.Preferences_TargetEnvironmentPage_arch, 1);

		fArchCombo = SWTFactory.createCombo(group, SWT.SINGLE | SWT.BORDER, 1, (String[]) fArchChoices.toArray(new String[fArchChoices.size()]));
		fArchCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getTargetDefinition().setArch(getModelValue(fArchCombo.getText()));
				makeDirty(getTargetDefinition());
			}
		});

		SWTFactory.createLabel(group, PDEUIMessages.Preferences_TargetEnvironmentPage_nl, 1);

		fNLCombo = SWTFactory.createCombo(group, SWT.SINGLE | SWT.BORDER, 1, (String[]) fNLChoices.toArray(new String[fNLChoices.size()]));
		fNLCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String value = fNLCombo.getText();
				int index = value.indexOf("-"); //$NON-NLS-1$
				if (index > 0)
					value = value.substring(0, index);
				getTargetDefinition().setNL(getModelValue(value));
				makeDirty(getTargetDefinition());
			}
		});
	}
	
	/**
	 * Returns the given string or <code>null</code> if the string is empty.
	 * Used when setting a value in the target definition.
	 * 
	 * @param value
	 * @return trimmed value or <code>null</code>
	 */
	private String getModelValue(String value) {
		if (value != null) {
			value = value.trim();
			if (value.length() == 0) {
				return null;
			}
		}
		return value;
	}

	/**
	* Delimits a comma separated preference and add the items to the given set
	* @param set
	* @param preference
	*/
	private void addExtraChoices(Set set, String preference) {
		StringTokenizer tokenizer = new StringTokenizer(preference, ","); //$NON-NLS-1$
		while (tokenizer.hasMoreTokens()) {
			set.add(tokenizer.nextToken().trim());
		}
	}
	/**
	 * Loads combo choices fromt he platform and from PDE core preferences
	 */
	private void initializeChoices() {
		IEclipsePreferences node = new InstanceScope().getNode(PDECore.PLUGIN_ID);

		fOSChoices = new TreeSet();
		String[] os = Platform.knownOSValues();
		for (int i = 0; i < os.length; i++) {
			fOSChoices.add(os[i]);
		}
		String pref = node.get(ICoreConstants.OS_EXTRA, EMPTY_STRING);
		if (!EMPTY_STRING.equals(pref)) {
			addExtraChoices(fOSChoices, pref);
		}

		fWSChoices = new TreeSet();
		String[] ws = Platform.knownWSValues();
		for (int i = 0; i < ws.length; i++) {
			fWSChoices.add(ws[i]);
		}
		pref = node.get(ICoreConstants.WS_EXTRA, EMPTY_STRING);
		if (!EMPTY_STRING.equals(pref)) {
			addExtraChoices(fWSChoices, pref);
		}

		fArchChoices = new TreeSet();
		String[] arch = Platform.knownOSArchValues();
		for (int i = 0; i < arch.length; i++) {
			fArchChoices.add(arch[i]);
		}
		pref = node.get(ICoreConstants.ARCH_EXTRA, EMPTY_STRING);
		if (!EMPTY_STRING.equals(pref)) {
			addExtraChoices(fArchChoices, pref);
		}

		fNLChoices = new TreeSet();
		String[] nl = LocaleUtil.getLocales();
		for (int i = 0; i < nl.length; i++) {
			fNLChoices.add(nl[i]);
		}
		pref = node.get(ICoreConstants.NL_EXTRA, EMPTY_STRING);
		if (!EMPTY_STRING.equals(pref)) {
			addExtraChoices(fNLChoices, pref);
		}
	} 
	private Control createArgumentsGroup(Composite parent) {
		Composite container = SWTFactory.createComposite(parent, 1, 1, GridData.FILL_BOTH);

		SWTFactory.createWrapLabel(container, PDEUIMessages.JavaArgumentsTab_description, 1);

		Group programGroup = SWTFactory.createGroup(container, PDEUIMessages.JavaArgumentsTab_progamArgsGroup, 1, 1, GridData.FILL_HORIZONTAL);

		fProgramArgs = SWTFactory.createText(programGroup, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL, 1, 200, 60, GridData.FILL_BOTH);
		fProgramArgs.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getTargetDefinition().setProgramArguments(fProgramArgs.getText().trim());
				makeDirty(getTargetDefinition());
			}
		});

		Composite programButtons = SWTFactory.createComposite(programGroup, 1, 1, GridData.HORIZONTAL_ALIGN_END, 0, 0);

		Button programVars = SWTFactory.createPushButton(programButtons, PDEUIMessages.JavaArgumentsTab_programVariables, null, GridData.HORIZONTAL_ALIGN_END);
		programVars.addSelectionListener(getVariablesListener(fProgramArgs));

		Group vmGroup = new Group(container, SWT.NONE);
		vmGroup.setLayout(new GridLayout(1, false));
		vmGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		vmGroup.setText(PDEUIMessages.JavaArgumentsTab_vmArgsGroup);

		fVMArgs = SWTFactory.createText(vmGroup, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL, 1, 200, 60, GridData.FILL_BOTH);
		fVMArgs.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getTargetDefinition().setVMArguments(fVMArgs.getText().trim());
				makeDirty(getTargetDefinition());
			}
		});

		Composite buttons = SWTFactory.createComposite(vmGroup, 2, 1, GridData.HORIZONTAL_ALIGN_END, 0, 0);

		Button vmArgs = SWTFactory.createPushButton(buttons, PDEUIMessages.JavaArgumentsTab_addVMArgs, null, GridData.HORIZONTAL_ALIGN_END);
		vmArgs.addSelectionListener(getVMArgsListener(fVMArgs));

		Button vmVars = SWTFactory.createPushButton(buttons, PDEUIMessages.JavaArgumentsTab_vmVariables, null, GridData.HORIZONTAL_ALIGN_END);
		vmVars.addSelectionListener(getVariablesListener(fVMArgs));
		return container;
	}

	
	/**
	 * Provide a listener for the Add VM Arguments button.
	 * The listener invokes the <code>VMArgumentsSelectionDialog</code> and 
	 * updates the selected VM Arguments back in the VM Arguments Text Box
	 * 
	 * @param textControl
	 * @return	<code>SelectionListener</code> for the Add VM Arguments button
	 */
	private SelectionListener getVMArgsListener(final Text textControl) {
		return new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ArgumentsFromContainerSelectionDialog dialog = new ArgumentsFromContainerSelectionDialog(getEditorSite()
						.getShell(), getTargetDefinition());
				if (dialog.open() == Window.OK) {
					String[] args = dialog.getSelectedArguments();
					if (args != null && args.length > 0) {
						StringBuffer resultBuffer = new StringBuffer();
						for (int index = 0; index < args.length; ++index) {
							resultBuffer.append(args[index] + " "); //$NON-NLS-1$
						}
						fVMArgs.insert(resultBuffer.toString());
					}
				}
			}
		};
	}

	/**
	 * Provide a listener for the Variables button.
	 * The listener invokes the <code>StringVariableSelectionDialog</code> and 
	 * updates the selected Variables back in the VM Arguments Text Box
	 * 
	 * @param textControl
	 * @return	<code>SelectionListener</code> for the Variables button
	 */
	private SelectionListener getVariablesListener(final Text textControl) {
		return new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getEditorSite()
						.getShell());
				dialog.open();
				String variable = dialog.getVariableExpression();
				if (variable != null) {
					textControl.insert(variable);
				}
			}
		};
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
				IOSGIFrameworkInstance.class, null);

		try {
			configuration = ts.getFrameworkInstanceConfiguration();
			if (configuration != null
					&& configuration.getTargetDefinition() != null)
				getTargetDefinition().resolve(new NullProgressMonitor());
		} catch (CoreException e) {
			Trace.trace(Trace.SEVERE, "cannot access configuration", e);
		}

		if (server != null)
			server2 = (IOSGIFrameworkWorkingCopy) server.loadAdapter(
					IOSGIFrameworkWorkingCopy.class, null);

		addChangeListener();
		initialize();
	}

	/**
	 * 
	 */
	protected void initialize() {

		ITargetDefinition definition = getTargetDefinition();
		if (definition != null)
			definition.resolve(new NullProgressMonitor());
		targetChanged(definition);
	}

	/*
	 * @see IWorkbenchPart#setFocus()
	 */
	public void setFocus() {

	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		super.doSave(monitor);
		IOSGIFrameworkInstance runtimeInstance = (IOSGIFrameworkInstance) server
				.loadAdapter(IOSGIFrameworkInstance.class, null);

		try {
			runtimeInstance.getFrameworkInstanceConfiguration()
					.setTargetDefinition(getTargetDefinition());
			TargetPlatformService.getDefault().saveTargetDefinition(
					getTargetDefinition());
		} catch (CoreException e) {
			e.printStackTrace();
		}

	}
}
