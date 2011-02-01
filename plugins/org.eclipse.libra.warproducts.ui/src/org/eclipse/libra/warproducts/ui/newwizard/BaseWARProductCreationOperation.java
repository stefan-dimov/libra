/******************************************************************************* 
* Copyright (c) 2010 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
*******************************************************************************/ 
package org.eclipse.libra.warproducts.ui.newwizard;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.libra.warproducts.core.*;
import org.eclipse.libra.warproducts.ui.Messages;
import org.eclipse.libra.warproducts.ui.WARProductConstants;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.iproduct.IProduct;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.wizards.product.BaseProductCreationOperation;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ISetSelectionTarget;


public class BaseWARProductCreationOperation
  extends BaseProductCreationOperation
{

  private IContainer productParent;
  private IFile file;

  public BaseWARProductCreationOperation( final IFile file ) {
    super( file );    
    productParent = file.getParent();
    this.file = file;
  }
  
  protected void initializeProduct( final IProduct product ) {
    super.initializeProduct( product );
    InfrastructureCreator creator = new InfrastructureCreator( productParent );
    createWebInfContent( creator );
    if( product instanceof WARProduct ) {
      WARProduct warProduct = ( WARProduct )product;
      warProduct.addLaunchIni( creator.getLaunchIniPath() );
      warProduct.addWebXml( creator.getWebXmlPath() );
      WARProductInitializer initializer 
        = new WARProductInitializer( warProduct );
      initializer.initialize();
    }
  }

  protected void createWebInfContent( final InfrastructureCreator creator )
  {
    try {
      creator.createWebInf();
      creator.createLaunchIni();
      creator.createWebXml();
    } catch( final CoreException e ) {
      Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
      MessageDialog.openError( shell, 
                               Messages.NewWARProductError, 
                               e.getMessage() );
    }
  }
  
  protected void execute( final IProgressMonitor monitor )
    throws CoreException, InvocationTargetException, InterruptedException
  {
    monitor.beginTask( Messages.BaseWARProductCreationOperation, 2 );
    createContent();
    monitor.worked( 1 );
    openFile();
    monitor.done();
  }

  private void createContent() {
    WARWorkspaceProductModel model 
      = new WARWorkspaceProductModel( file, false );
    initializeProduct( model.getProduct() );
    model.save();
    model.dispose();
  }
  
  private void openFile() {
    Display.getCurrent().asyncExec( new Runnable() {
      public void run() {
        IWorkbenchWindow window = PDEPlugin.getActiveWorkbenchWindow();
        if( window != null ) {
          IWorkbenchPage page = window.getActivePage();
          if( page != null && file.exists() ) {
            IWorkbenchPart focusPart = page.getActivePart();
            if( focusPart instanceof ISetSelectionTarget ) {
              ISelection selection = new StructuredSelection( file );
              ( ( ISetSelectionTarget )focusPart ).selectReveal( selection );
            }
            try {
              IDE.openEditor( page, file, WARProductConstants.EDITOR_ID );
            } catch( final PartInitException e ) {
              PDECore.log( e );
            }
          }
        }
      }
    } );
  }
}
