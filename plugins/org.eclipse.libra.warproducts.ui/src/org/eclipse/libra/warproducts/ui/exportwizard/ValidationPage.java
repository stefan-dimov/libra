/******************************************************************************* 
* Copyright (c) 2010, 2011 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Holger Staudacher - initial API and implementation
*******************************************************************************/ 
package org.eclipse.libra.warproducts.ui.exportwizard;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.libra.warproducts.ui.Messages;
import org.eclipse.libra.warproducts.ui.WARProductConstants;
import org.eclipse.libra.warproducts.ui.validation.PluginStatusContentVisualizer;
import org.eclipse.libra.warproducts.ui.validation.PluginStatusDialogContentProvider;
import org.eclipse.pde.internal.ui.wizards.exports.AbstractExportWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.IWorkbenchHelpSystem;


public class ValidationPage extends AbstractExportWizardPage {
  
  private Composite container;
  private TreeViewer treeViewer;
  private Map input;

  public ValidationPage() {
    super( "Validation" ); //$NON-NLS-1$
    setTitle( Messages.ValidationPageTitle );
    setErrorMessage( Messages.ValidationPageDesc );
  }

  public void createControl( final Composite parent ) {
    if( input == null ) {
      input = new HashMap();
    }
    container = new Composite( parent, SWT.NONE );
    container.setBackground( new Color( container.getDisplay(), 255, 0, 0 ) );
    container.setLayout( new FormLayout() );
    PluginStatusContentVisualizer visualizer 
      = new PluginStatusContentVisualizer();
    visualizer.setInput( input );
    visualizer.createControls( container );
    FormData fd = new FormData();
    fd.left = new FormAttachment( 0 );
    fd.top = new FormAttachment( 0 );
    fd.right = new FormAttachment( 100 );
    fd.bottom = new FormAttachment( 100 );
    treeViewer = visualizer.getViewer();
    treeViewer.getControl().setLayoutData( fd );
    setControl( container );
    IWorkbench workbench = PlatformUI.getWorkbench();
    IWorkbenchHelpSystem helpSystem = workbench.getHelpSystem();
    String contextId = WARProductConstants.HELP_CONTEXT_VALIDATION_PAGE;
    helpSystem.setHelp( container, contextId );
  }

  protected void pageChanged() {
  }

  protected void saveSettings( final IDialogSettings settings ) {
  }
  
  public void setVisible( boolean visible ) {
    if( visible && treeViewer != null && input != null ) {
      setInput( input );
    }
    super.setVisible( visible );
  }

  public TreeViewer getTreeViewer() {
    return treeViewer;
  }
  
  public void setInput( final Map errors ) {
    input = errors;
    if( treeViewer != null ) {
      IContentProvider provider = treeViewer.getContentProvider();
      PluginStatusDialogContentProvider contentProvider 
        = ( PluginStatusDialogContentProvider )provider;
      contentProvider.setInput( input );
      treeViewer.setInput( input );
      treeViewer.refresh();
    }
  }
  
}
