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

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.libra.facet.Activator;
import org.eclipse.libra.facet.OSGiBundleFacetInstallConfig;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.wst.common.project.facet.ui.AbstractFacetWizardPage;


public class OSGiBundleFacetInstallPage extends AbstractFacetWizardPage {
	
	private static final String WIZARD_PAGE_NAME = "osgi.bundle.facet.install.page"; //$NON-NLS-1$
	private static final String IMG_PATH_BUNDLE_WIZBAN = "icons/wizban/bundle_wizban.png"; //$NON-NLS-1$
	
	private OSGiBundleFacetInstallConfig config;

	public OSGiBundleFacetInstallPage() {
		super(WIZARD_PAGE_NAME);
        
        setTitle(Messages.OSGiBundleFacetInstallPage_Title);
        setDescription(Messages.OSGiBundleFacetInstallPage_Description);
        setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IMG_PATH_BUNDLE_WIZBAN));
	}

	public void setConfig(Object config) {
		this.config = (OSGiBundleFacetInstallConfig) config;
	}

	public void createControl(Composite parent) {
		DataBindingContext dbc = new DataBindingContext();
		WizardPageSupport.create(this, dbc);
		
		Composite container = new Composite(parent, SWT.NONE);
		
		Label symbolicNameLabel = new Label(container, SWT.NONE);
		symbolicNameLabel.setText(Messages.OSGiBundleFacetInstallPage_SymbolicName);
		Text symbolicNameText = new Text(container, SWT.BORDER | SWT.SINGLE);
		dbc.bindValue(
				SWTObservables.observeText(symbolicNameText, SWT.Modify), 
				config.getSymbolicNameValue(),
				new UpdateValueStrategy().setAfterConvertValidator(new OSGiBundleFacetInstallConfig.SymbolicNameValidator()), 
				null);
		
		Label versionLabel = new Label(container, SWT.NONE);
		versionLabel.setText(Messages.OSGiBundleFacetInstallPage_Version);
		Text versionText = new Text(container, SWT.BORDER | SWT.SINGLE);
		dbc.bindValue(
				SWTObservables.observeText(versionText, SWT.Modify), 
				config.getVersionValue(),
				new UpdateValueStrategy().setAfterConvertValidator(new OSGiBundleFacetInstallConfig.VersionValidator()), 
				null);
		
		Label nameLabel = new Label(container, SWT.NONE);
		nameLabel.setText(Messages.OSGiBundleFacetInstallPage_Name);
		Text nameText = new Text(container, SWT.BORDER | SWT.SINGLE);
		dbc.bindValue(
				SWTObservables.observeText(nameText, SWT.Modify), 
				config.getNameValue(),
				null, 
				null);
		
		Label vendorLabel = new Label(container, SWT.NONE);
		vendorLabel.setText(Messages.OSGiBundleFacetInstallPage_Vendor);
		Text vendorText = new Text(container, SWT.BORDER | SWT.SINGLE);
		dbc.bindValue(
				SWTObservables.observeText(vendorText, SWT.Modify), 
				config.getVendorValue(),
				null, 
				null);
		
		GridLayoutFactory.swtDefaults().numColumns(2).generateLayout(container);
		setControl(container);
		Dialog.applyDialogFont(container);
	}

}
