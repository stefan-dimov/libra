/******************************************************************************* 
* Copyright (c) 2010, 2011 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Holger Staudacher - initial API and implementation
*******************************************************************************/ 
package org.eclipse.libra.warproducts.ui.editor;

import java.io.*;
import java.util.ArrayList;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.libra.warproducts.core.WARProductModel;
import org.eclipse.libra.warproducts.core.WARWorkspaceProductModel;
import org.eclipse.pde.core.*;
import org.eclipse.pde.internal.core.iproduct.IProductModel;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.editor.PDEFormEditor;
import org.eclipse.pde.internal.ui.editor.context.UTF8InputContext;
import org.eclipse.ui.*;

public class WARProductInputContext extends UTF8InputContext {

  public static final String CONTEXT_ID = "warproduct-context"; //$NON-NLS-1$

  public WARProductInputContext( PDEFormEditor editor,
                                 IEditorInput input,
                                 boolean primary )
  {
    super( editor, input, primary );
    create();
  }

  public String getId() {
    return CONTEXT_ID;
  }

  protected IBaseModel createModel( final IEditorInput input )
    throws CoreException
  {
    IProductModel model = null;
    if( input instanceof IStorageEditorInput ) {
      try {
        if( input instanceof IFileEditorInput ) {
          IFile file = ( ( IFileEditorInput )input ).getFile();
          model = new WARWorkspaceProductModel( file, true );
          model.load();
        } else if( input instanceof IStorageEditorInput ) {
          IStorageEditorInput storageInput = ( IStorageEditorInput )input;
          InputStream contents = storageInput.getStorage().getContents();
          InputStream is = new BufferedInputStream( contents );
          model = new WARProductModel();
          model.load( is, false );
        }
      } catch( final CoreException e ) {
        PDEPlugin.logException( e );
        model = null;
      }
    } else if( input instanceof IURIEditorInput ) {
      IFileStore store = EFS.getStore( ( ( IURIEditorInput )input ).getURI() );
      InputStream is = store.openInputStream( EFS.CACHE,
                                              new NullProgressMonitor() );
      model = new WARProductModel();
      model.load( is, false );
    }
    return model;
  }

  protected void addTextEditOperation( final ArrayList ops, 
                                       final IModelChangedEvent event )
  {
    // do nothing
  }

  protected void flushModel( final IDocument doc ) {
    if( getModel() instanceof IEditable ) {
      IEditable editableModel = ( IEditable )getModel();
      if( editableModel.isDirty() ) {
        try {
          StringWriter swriter = new StringWriter();
          PrintWriter writer = new PrintWriter( swriter );
          editableModel.save( writer );
          writer.flush();
          swriter.close();
          doc.set( swriter.toString() );
        } catch( final IOException e ) {
          PDEPlugin.logException( e );
        }
      }
    }
  }

  public void doRevert() {
    fEditOperations.clear();
    IProductModel model = ( IProductModel )getModel();
    try {
      InputStream is = ( ( IFile )model.getUnderlyingResource() ).getContents();
      model.reload( is, true );
      getEditor().doRevert();
    } catch( final CoreException e ) {
      PDEPlugin.logException( e );
    }
  }

  protected String getPartitionName() {
    return "___prod_partition"; //$NON-NLS-1$
  }
  
}
