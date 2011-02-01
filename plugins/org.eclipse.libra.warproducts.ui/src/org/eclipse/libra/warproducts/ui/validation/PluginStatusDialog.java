/******************************************************************************* 
* Copyright (c) 2010 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
*******************************************************************************/ 
package org.eclipse.libra.warproducts.ui.validation;

import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.pde.internal.ui.IHelpContextIds;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.IWorkbenchHelpSystem;

public class PluginStatusDialog extends TrayDialog {

  public Map input;
  private PluginStatusContentVisualizer viewer;

  public PluginStatusDialog( final Shell parentShell ) {
    super( parentShell );
    setShellStyle( getShellStyle() | SWT.RESIZE );
    viewer = new PluginStatusContentVisualizer();
  }

  public void setInput( final Map input ) {
    this.input = input;
    viewer.setInput( input );
  }

  protected void createButtonsForButtonBar( final Composite parent ) {
    createButton( parent,
                  IDialogConstants.OK_ID,
                  IDialogConstants.OK_LABEL,
                  true );
  }

  protected void configureShell( final Shell shell ) {
    super.configureShell( shell );
    IWorkbenchHelpSystem helpSystem = PlatformUI.getWorkbench().getHelpSystem();
    helpSystem.setHelp( shell, IHelpContextIds.PLUGIN_STATUS_DIALOG );
  }

  protected Control createDialogArea( final Composite parent ) {
    Composite container = ( Composite )super.createDialogArea( parent );
    GridData gd = new GridData( GridData.FILL_BOTH );
    gd.widthHint = 400;
    gd.heightHint = 300;
    container.setLayoutData( gd );
    Label label = new Label( container, SWT.NONE );
    label.setText( PDEUIMessages.PluginStatusDialog_label );
    viewer.createControls( container );
    Control control = viewer.getViewer().getControl();
    control.setLayoutData( new GridData( GridData.FILL_BOTH ) );
    getShell().setText( PDEUIMessages.PluginStatusDialog_pluginValidation );
    Dialog.applyDialogFont( container );
    return container;
  }

  public boolean close() {
    PDEPlugin.getDefault().getLabelProvider().disconnect( this );
    return super.close();
  }

  private IDialogSettings getDialogSettings() {
    IDialogSettings settings = PDEPlugin.getDefault().getDialogSettings();
    IDialogSettings section = settings.getSection( getDialogSectionName() );
    if( section == null )
      section = settings.addNewSection( getDialogSectionName() );
    return section;
  }

  protected String getDialogSectionName() {
    return PDEPlugin.getPluginId() + ".PLUGIN_STATUS_DIALOG"; //$NON-NLS-1$
  }

  protected IDialogSettings getDialogBoundsSettings() {
    return getDialogSettings();
  }

  public void refresh( final Map input ) {
    this.input = input;
    viewer.getViewer().setInput( input );
    viewer.getViewer().refresh();
  }
  
}
