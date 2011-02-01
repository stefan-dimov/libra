/*******************************************************************************
 * Copyright (c) 2010 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors:
 * EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.libra.warproducts.ui.newwizard;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.libra.warproducts.ui.Messages;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.pde.internal.ui.wizards.product.NewProductFileWizard;
import org.eclipse.ui.IWorkbench;

public class NewWarProductFileWizard extends NewProductFileWizard {

  private WARProductFileWizardPage mainPage;

  public void addPages() {
    mainPage = new WARProductFileWizardPage( "warproduct", getSelection() );  //$NON-NLS-1$
    addPage( mainPage );
  }

  public boolean performFinish() {
    boolean result = true;
    try {
      getContainer().run( false, true, getOperation() );
    } catch( final InvocationTargetException e ) {
      PDEPlugin.logException( e );
      result = false;
    } catch( final InterruptedException e ) {
      result = false;
    }
    return result;
  }

  private IRunnableWithProgress getOperation() {
    IRunnableWithProgress result = null;
    IFile file = mainPage.createNewFile();
    int option = mainPage.getInitializationOption();
    if( option == WARProductFileWizardPage.USE_LAUNCH_CONFIG ) {
      ILaunchConfiguration selectedLaunchConfiguration 
        = mainPage.getSelectedLaunchConfiguration();
      result = new WARProductFromConfigOperation( file,
                                                  selectedLaunchConfiguration );
    } else {
      result = new BaseWARProductCreationOperation( file );
    }
    return result;
  }

  public void init( final IWorkbench workbench,
                    final IStructuredSelection currentSelection )
  {
    super.init( workbench, currentSelection );
    setWindowTitle( Messages.NewWarProductFileWizard );
    setNeedsProgressMonitor( true );
  }

  protected void initializeDefaultPageImageDescriptor() {
    setDefaultPageImageDescriptor( PDEPluginImages.DESC_PRODUCT_WIZ );
  }
}
