/*******************************************************************************
 * Copyright (c) 2010 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors:
 * EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.libra.warproducts.ui.exportwizard;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.libra.warproducts.core.*;
import org.eclipse.libra.warproducts.ui.Messages;
import org.eclipse.libra.warproducts.ui.WARProductConstants;
import org.eclipse.libra.warproducts.ui.validation.IValidationListener;
import org.eclipse.libra.warproducts.ui.validation.WARProductValidateAction;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.State;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.internal.core.TargetPlatformHelper;
import org.eclipse.pde.internal.core.exports.FeatureExportInfo;
import org.eclipse.pde.internal.core.iproduct.IProductPlugin;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.wizards.exports.ProductExportWizard;
import org.eclipse.ui.progress.IProgressConstants;
import org.osgi.framework.Version;

public class ExportWARProductWizard extends ProductExportWizard {

  private IWARProduct product;
  private IFile warProductFile;
  private ExportPage exportPage;
  private SelectionPage selectionPage;
  private ValidationPage validationPage;
  private boolean isProductValid;

  public ExportWARProductWizard() {
    this( null );
  }

  public ExportWARProductWizard( final IProject project ) {
    super( project );
  }

  public void addPages() {
    selectionPage = new SelectionPage();
    validationPage = new ValidationPage();
    exportPage = new ExportPage();
    loadProductFromSelection( getSelection() );
    if( product == null ) {
      addPage( selectionPage );
      addPage( validationPage );
    } else if( !isProductValid ) {
      addPage( validationPage );
    }
    addPage( exportPage );
  }

  public boolean canFinish() {
    boolean result = false;
    IWizardPage currentPage = getContainer().getCurrentPage();
    if(    currentPage != null
        && currentPage.equals( exportPage ) 
        && exportPage.isPageComplete() 
        && product != null ) 
    {
      result = true;
    }
    return result;
  }
  
  private void loadProductFromFile() {
    WARWorkspaceProductModel productModel 
      = new WARWorkspaceProductModel( warProductFile, false );
    try {
      productModel.load();
      if( !productModel.isLoaded() ) {
        MessageDialog.openError( getContainer().getShell(),
                                 PDEUIMessages.ProductExportWizard_error,
                                 PDEUIMessages.ProductExportWizard_corrupt ); 
      } else {
        product = ( IWARProduct )productModel.getProduct();
        validateProduct();
      }
    } catch( final CoreException e ) {
      MessageDialog.openError( getContainer().getShell(),
                               PDEUIMessages.ProductExportWizard_error,
                               PDEUIMessages.ProductExportWizard_corrupt ); 
    }
  }

  private void validateProduct() {
    WARProductValidateAction action = new WARProductValidateAction( product );
    action.addValidationListener( new IValidationListener() {
      
      public void validationFinished( final Map errors ) {
        validationPage.setInput( errors );
        isProductValid = errors.size() == 0;
      }
      
    } );
    action.run();
  }

  protected boolean confirmDelete() {
    boolean result = true;
    if( !exportPage.doExportToDirectory() ) {
      File zipFile 
        = new File( exportPage.getDestination(), exportPage.getFileName() );
      if( zipFile.exists() ) {
        String bind 
          = NLS.bind( PDEUIMessages.BaseExportWizard_confirmReplace_desc, 
                      zipFile.getAbsolutePath() );
        String confirmReplaceTitle 
          = PDEUIMessages.BaseExportWizard_confirmReplace_title;
        boolean openQuestion 
          = MessageDialog.openQuestion( getContainer().getShell(),
                                        confirmReplaceTitle,
                                        bind );
        if( !openQuestion ) {
          result = false;
        } else {
          zipFile.delete();
        }
      }
    }
    return result;
  }

  protected void scheduleExportJob() {
    FeatureExportInfo info = new FeatureExportInfo();
    info.toDirectory = exportPage.doExportToDirectory();
    info.exportSource = false;
    info.exportSourceBundle = false;
    info.allowBinaryCycles = false;
    info.exportMetadata = false;
    info.destinationDirectory = exportPage.getDestination();
    info.zipFileName = exportPage.getFileName();
    info.items = getPluginModels();
    String rootDirectory = "WEB-INF"; //$NON-NLS-1$
    WARProductExportOperation job 
      = new WARProductExportOperation( info,
                                       PDEUIMessages.ProductExportJob_name,
                                       product,
                                       rootDirectory );
    job.setUser( true );
    job.setRule( ResourcesPlugin.getWorkspace().getRoot() );
    job.schedule();
    job.setProperty( IProgressConstants.ICON_PROPERTY,
                     PDEPluginImages.DESC_FEATURE_OBJ );
  }

  private BundleDescription[] getPluginModels() {
    ArrayList list = new ArrayList();
    State state = TargetPlatformHelper.getState();
    IProductPlugin[] plugins = product.getPlugins();
    for( int i = 0; i < plugins.length; i++ ) {
      BundleDescription bundle = null;
      String v = plugins[ i ].getVersion();
      if( v != null && v.length() > 0 ) {
        bundle = state.getBundle( plugins[ i ].getId(),
                                  Version.parseVersion( v ) );
      }
      // if there's no version, just grab a bundle like before
      if( bundle == null ) {
        bundle = state.getBundle( plugins[ i ].getId(), null );
      }
      if( bundle != null ) {
        list.add( bundle );
      }
    }
    Object[] bundleArray = list.toArray( new BundleDescription[ list.size() ] );
    return ( BundleDescription[] )bundleArray;
  }

  public void loadProductFromFile( final IFile file ) {
    warProductFile = file;
    loadProductFromFile();
  }
  
  void resetProduct() {
    warProductFile = null;
  }
  
  protected void initialize( final IStructuredSelection selection )
  {
    if( selection.size() > 0 ) {
      loadProductFromSelection( selection );
    }
  }

  private void loadProductFromSelection( final IStructuredSelection selection )
  {
    Object object = selection.getFirstElement();
    if( object instanceof IFile ) {
      handleFileSelection( object );
    } else if( object instanceof IContainer ) {
      handleContainerSelection( object );
    }
  }

  private void handleFileSelection( final Object object ) {
    IFile file = ( IFile )object;
    boolean isWarProduct 
      = WARProductConstants.FILE_EXTENSION.equals( file.getFileExtension() );
    if( isWarProduct ) {
      loadProductFromFile( file );
    }
  }

  private void handleContainerSelection( final Object object ) {
    IContainer container = ( IContainer )object;
    try {
      if( container.isAccessible() ) {
        IResource[] resources = container.members();
        for( int i = 0; i < resources.length; i++ ) {
          IResource resource = resources[ i ];
          String resourceName = resource.getName();
          boolean hasWarProductExtension 
            = resourceName.endsWith( "." + WARProductConstants.FILE_EXTENSION ); //$NON-NLS-1$
          if( resource instanceof IFile && hasWarProductExtension ) {
            IFile file = ( IFile )resource;
            loadProductFromFile( file );
          }
        }
      }
    } catch( final CoreException e ) {
      MessageDialog.openError( getShell(), 
                               Messages.LoadWARProductError, 
                               e.getMessage() );
    }
  }

  public IFile getProductFile() {
    return warProductFile;
  }
  
  protected boolean performPreliminaryChecks()  {
    return product != null;
  }
  
  public IWizardPage getNextPage( final IWizardPage page ) {
    IWizardPage nextPage = null;
    if( page.equals( selectionPage ) ) {
      if( isProductValid ) {
        nextPage = exportPage;
      } else {
        nextPage = validationPage;
      }
    } else if( page.equals( validationPage ) ) {
      nextPage = exportPage;
    }
    
    return nextPage;
  }
 
}
