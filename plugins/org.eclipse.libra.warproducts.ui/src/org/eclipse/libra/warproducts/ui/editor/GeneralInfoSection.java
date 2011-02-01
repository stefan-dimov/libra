/*******************************************************************************
 * Copyright (c) 2009 EclipseSource Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html Contributors:
 * EclipseSource Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.libra.warproducts.ui.editor;

import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.internal.core.iproduct.IProduct;
import org.eclipse.pde.internal.core.iproduct.IProductModel;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.editor.FormEntryAdapter;
import org.eclipse.pde.internal.ui.editor.FormLayoutFactory;
import org.eclipse.pde.internal.ui.editor.PDEFormPage;
import org.eclipse.pde.internal.ui.editor.PDESection;
import org.eclipse.pde.internal.ui.parts.FormEntry;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

public class GeneralInfoSection extends PDESection {

  private FormEntry idEntry;
  private FormEntry nameEntry;
  private FormEntry versionEntry;
  private static int NUM_COLUMNS = 3;

  public GeneralInfoSection( final PDEFormPage page, final Composite parent ) {
    super( page, parent, Section.DESCRIPTION );
    createClient( getSection(), page.getEditor().getToolkit() );
  }

  protected void createClient( final Section section, 
                               final FormToolkit toolkit ) 
  {
    section.setText( PDEUIMessages.GeneralInfoSection_title );
    section.setDescription( PDEUIMessages.GeneralInfoSection_desc );
    TableWrapLayout wrapLayout 
      = FormLayoutFactory.createClearTableWrapLayout( false, 1 );
    section.setLayout( wrapLayout );
    TableWrapData data = new TableWrapData( TableWrapData.FILL_GRAB );
    data.colspan = 2;
    section.setLayoutData( data );
    Composite client = toolkit.createComposite( section );
    GridLayout gridLayout 
      = FormLayoutFactory.createSectionClientGridLayout( false, NUM_COLUMNS );
    client.setLayout( gridLayout );
    IEditorSite site = getPage().getPDEEditor().getEditorSite();
    IActionBars actionBars = site.getActionBars();
    createIdEntry( client, toolkit, actionBars );
    createVersionEntry( client, toolkit, actionBars );
    createNameEntry( client, toolkit, actionBars );
    toolkit.paintBordersFor( client );
    section.setClient( client );
    getModel().addModelChangedListener( this );
  }

  public void dispose() {
    IProductModel model = getModel();
    if( model != null ) {
      model.removeModelChangedListener( this );
    }
    super.dispose();
  }

  private void createNameEntry( final Composite client,
                                final FormToolkit toolkit,
                                final IActionBars actionBars )
  {
    nameEntry = new FormEntry( client,
                                toolkit,
                                PDEUIMessages.ProductInfoSection_productname,
                                null,
                                false );
    nameEntry.setFormEntryListener( new FormEntryAdapter( this, actionBars ) {

      public void textValueChanged( FormEntry entry ) {
        getProduct().setName( entry.getValue().trim() );
      }
      
    } );
    GridData gd = new GridData( GridData.FILL_HORIZONTAL );
    gd.grabExcessHorizontalSpace = true;
    gd.horizontalSpan = 2;
    nameEntry.getText().setLayoutData( gd );
    nameEntry.setEditable( isEditable() );
  }

  private void createIdEntry( final Composite client,
                              final FormToolkit toolkit,
                              final IActionBars actionBars )
  {
    idEntry = new FormEntry( client,
                              toolkit,
                              PDEUIMessages.ProductInfoSection_id,
                              null,
                              false );
    idEntry.setFormEntryListener( new FormEntryAdapter( this, actionBars ) {

      public void textValueChanged( FormEntry entry ) {
        getProduct().setId( entry.getValue().trim() );
      }
      
    } );
    GridData gd = new GridData( GridData.FILL_HORIZONTAL );
    gd.grabExcessHorizontalSpace = true;
    gd.horizontalSpan = 2;
    idEntry.getText().setLayoutData( gd );
    idEntry.setEditable( isEditable() );
  }

  private void createVersionEntry( final Composite client,
                                   final FormToolkit toolkit,
                                   final IActionBars actionBars )
  {
    versionEntry = new FormEntry( client,
                                   toolkit,
                                   PDEUIMessages.ProductInfoSection_version,
                                   null,
                                   false );
    versionEntry.setFormEntryListener( new FormEntryAdapter( this, actionBars )
    {

      public void textValueChanged( FormEntry entry ) {
        getProduct().setVersion( entry.getValue().trim() );
      }
      
    } );
    GridData gd = new GridData( GridData.FILL_HORIZONTAL );
    gd.grabExcessHorizontalSpace = true;
    gd.horizontalSpan = 2;
    versionEntry.getText().setLayoutData( gd );
    versionEntry.setEditable( isEditable() );
  }

  public void commit( final boolean onSave ) {
    idEntry.commit();
    nameEntry.commit();
    versionEntry.commit();
    super.commit( onSave );
  }

  public void cancelEdit() {
    idEntry.cancelEdit();
    nameEntry.cancelEdit();
    versionEntry.cancelEdit();
    super.cancelEdit();
  }

  private IProductModel getModel() {
    return ( IProductModel )getPage().getPDEEditor().getAggregateModel();
  }

  private IProduct getProduct() {
    return getModel().getProduct();
  }

  public void refresh() {
    IProduct product = getProduct();
    if( product.getId() != null ) {
      idEntry.setValue( product.getId(), true );
    }
    if( product.getName() != null ) {
      nameEntry.setValue( product.getName(), true );
    }
    if( product.getVersion() != null ) {
      versionEntry.setValue( product.getVersion(), true );
    }
    super.refresh();
  }

  public void modelChanged( final IModelChangedEvent e ) {
    if( e.getChangeType() == IModelChangedEvent.WORLD_CHANGED ) {
      handleModelEventWorldChanged( e );
    } else {
      String prop = e.getChangedProperty();
      Object[] objects = e.getChangedObjects();
      if( !( prop == null || objects == null 
             || !( objects[ 0 ] instanceof IProduct ) ) ) 
      {
        if( prop.equals( IProduct.P_UID ) ) {
          idEntry.setValue( e.getNewValue().toString(), true );
        } else if( prop.equals( IProduct.P_NAME ) ) {
          nameEntry.setValue( e.getNewValue().toString(), true );
        } else if( prop.equals( IProduct.P_VERSION ) ) {
          versionEntry.setValue( e.getNewValue().toString(), true );
        }
      }
    }
  }

  private void handleModelEventWorldChanged( final IModelChangedEvent event ) {
    refresh();
    getPage().setLastFocusControl( idEntry.getText() );
  }

  public boolean canPaste( final Clipboard clipboard ) {
    boolean result = false;
    Display d = getSection().getDisplay();
    Control c = d.getFocusControl();
    if( c instanceof Text ) {
      result = true;
    }
    return result;
  }
  
}
