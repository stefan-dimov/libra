/*******************************************************************************
 * Copyright (c) 2010 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors:
 * EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.libra.warproducts.ui.exportwizard;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.libra.warproducts.ui.Messages;
import org.eclipse.libra.warproducts.ui.WARProductConstants;
import org.eclipse.pde.internal.ui.IPDEUIConstants;
import org.eclipse.pde.internal.ui.util.SWTUtil;
import org.eclipse.pde.internal.ui.wizards.exports.AbstractExportTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class DestinationGroup extends AbstractExportTab {

  protected static final String EXPORT_DIRECTORY = "exportDirectory"; //$NON-NLS-1$
  protected static final String DESTINATION = "destination"; //$NON-NLS-1$
  protected static final String ZIP_FILENAME = "zipFileName"; //$NON-NLS-1$
  
  protected Combo archiveCombo;
  protected Button browseFile;
  private ExportPage page;

  public DestinationGroup( final ExportPage page ) {
    super( page );
    this.page = page;
  }

  public Control createControl( final Composite parent ) {
    Composite container = new Composite( parent, SWT.NONE );
    GridLayout layout = new GridLayout( 3, false );
    container.setLayout( layout );
    layout.marginHeight = 0;
    container.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
    Label description = new Label( container, SWT.NONE );
    description.setText( Messages.DestinationGroupFile );
    archiveCombo = new Combo( container, SWT.BORDER );
    archiveCombo.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
    browseFile = new Button( container, SWT.PUSH );
    browseFile.setText( Messages.DestinationGroupBrowe );
    browseFile.setLayoutData( new GridData() );
    SWTUtil.setButtonDimensionHint( browseFile );
    return container;
  }

  protected void initialize( final IDialogSettings settings ) {
    initialize( settings, null );
  }

  protected void initialize( final IDialogSettings settings, 
                             final IFile file ) 
  {
    initializeCombo( settings, ZIP_FILENAME, archiveCombo );
    updateDestination( file );
    hookListeners();
  }

  protected void initializeCombo( final IDialogSettings settings,
                                  final String key,
                                  final Combo combo )
  {
    super.initializeCombo( settings, key, combo );
    if( !isValidLocation( combo.getText().trim() ) ) // If default value is
                                                     // invalid, make it blank
      combo.setText( "" ); //$NON-NLS-1$
  }

  protected void updateDestination( final IFile file ) {
    try {
      if( file != null ) {
        Combo combo = archiveCombo;
        QualifiedName location 
          = IPDEUIConstants.DEFAULT_PRODUCT_EXPORT_LOCATION;
        String destination = file.getPersistentProperty( location );
        if( destination != null ) {
          if( combo.indexOf( destination ) == -1 )
            combo.add( destination, 0 );
          combo.setText( destination );
        }
      }
    } catch( final CoreException e ) {
    }
  }

  protected void hookListeners() {
    browseFile.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( final SelectionEvent e ) {
        String[] filters 
          = new String[] { "*" + WARProductConstants.ARCHIVE_EXTENSION }; //$NON-NLS-1$
        chooseFile( archiveCombo, filters ); //$NON-NLS-1$
      }
    } );
    archiveCombo.addModifyListener( new ModifyListener() {
      public void modifyText( final ModifyEvent e ) {
        page.pageChanged();
      }
    } );
  }

  protected void saveSettings( final IDialogSettings settings ) {
    saveCombo( settings, ZIP_FILENAME, archiveCombo );
    IFile file = page.getProductFile();
    try {
      if( file != null && file.exists() ) {
        QualifiedName location 
          = IPDEUIConstants.DEFAULT_PRODUCT_EXPORT_LOCATION;
        file.setPersistentProperty( location,
                                    archiveCombo.getText().trim() );
      }
    } catch( final CoreException e ) {
    }
  }

  protected String validate() {
    String result = null;
    if( archiveCombo.getText().trim().length() == 0 ) {
      result = Messages.DestinationGroupWarning;
    } else if( !isValidLocation( archiveCombo.getText().trim() ) ) {
      result = Messages.DestinationGroupWarningDirectory;
    }
    return result;
  }

  protected String getFileName() {
    String result = null;
    String path = archiveCombo.getText();
    if( path != null && path.length() > 0 ) {
      String fileName = new Path( path ).lastSegment();
      if( !fileName.endsWith( WARProductConstants.ARCHIVE_EXTENSION ) ) {
        fileName += WARProductConstants.ARCHIVE_EXTENSION;
      }
      result = fileName;
    }
    return result;
  }

  protected String getDestination() {
    String result = ""; //$NON-NLS-1$
    String path = archiveCombo.getText();
    if( path.length() > 0 ) {
      path = new Path( path ).removeLastSegments( 1 ).toOSString();
      result = new File( path ).getAbsolutePath();
    }
    return result; 
  }
  
}
