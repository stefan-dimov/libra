/*******************************************************************************
 * Copyright (c) 2010 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors:
 * EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.libra.warproducts.ui.editor;

import org.eclipse.core.runtime.IPath;
import org.eclipse.libra.warproducts.ui.Messages;
import org.eclipse.libra.warproducts.ui.WARProductConstants;
import org.eclipse.pde.internal.ui.PDELabelProvider;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.pde.internal.ui.editor.FormLayoutFactory;
import org.eclipse.pde.internal.ui.editor.PDEFormPage;
import org.eclipse.pde.internal.ui.editor.product.PluginSection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.help.IWorkbenchHelpSystem;

public class ConfigurationPage extends PDEFormPage {

  public static final String PLUGIN_ID = "plugin-dependencies"; //$NON-NLS-1$
  public static final String FEATURE_ID = "feature-dependencies"; //$NON-NLS-1$
  private PluginSection pluginSection = null;
  private LibrarySection librarySection = null;

  public ConfigurationPage( final FormEditor editor, final boolean useFeatures )
  {
    super( editor, PLUGIN_ID, Messages.ConfigurationPageTitle );
  }

  protected String getHelpResource() {
    return WARProductConstants.HELP_CONTEXT_CONFIGURATION_PAGE;
  }

  protected void createFormContent( final IManagedForm managedForm ) {
    super.createFormContent( managedForm );
    ScrolledForm form = managedForm.getForm();
    FormToolkit toolkit = managedForm.getToolkit();
    PDELabelProvider labelProvider = PDEPlugin.getDefault().getLabelProvider();
    form.setImage( labelProvider.get( PDEPluginImages.DESC_REQ_PLUGINS_OBJ ) );
    form.setText( Messages.ConfigurationPageTitle );
    fillBody( managedForm, toolkit );
    IWorkbenchHelpSystem helpSystem = PlatformUI.getWorkbench().getHelpSystem();
    String contextId = WARProductConstants.HELP_CONTEXT_CONFIGURATION_PAGE;
    helpSystem.setHelp( form.getBody(), contextId );
  }

  private void fillBody( final IManagedForm managedForm, 
                         final FormToolkit toolkit ) 
  {
    Composite body = managedForm.getForm().getBody();
    body.setLayout( FormLayoutFactory.createFormGridLayout( false, 1 ) );
    pluginSection = new PluginSectionExtended( this, body );
    managedForm.addPart( pluginSection );
    librarySection = new LibrarySection( this, body );
    managedForm.addPart( librarySection );
  }

  public boolean includeOptionalDependencies() {
    boolean result = false;
    if( pluginSection != null ) {
      result = pluginSection.includeOptionalDependencies();
    }
    return result;
  }
  
  public boolean selectReveal( final Object object ) {
    boolean result = false;
    if( object instanceof IPath ) {
      result = librarySection.selectReveal( object );
    } else {
      result = super.selectReveal( object );
    }
    return result;
  }
}
