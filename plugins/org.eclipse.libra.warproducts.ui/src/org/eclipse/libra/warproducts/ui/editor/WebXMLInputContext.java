/******************************************************************************* 
* Copyright (c) 2010 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
*******************************************************************************/ 
package org.eclipse.libra.warproducts.ui.editor;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.pde.core.IBaseModel;
import org.eclipse.pde.internal.ui.editor.PDEFormEditor;
import org.eclipse.pde.internal.ui.editor.context.XMLInputContext;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;


public class WebXMLInputContext extends XMLInputContext {
  
  public static final String CONTEXT_ID = "web-context"; //$NON-NLS-1$

  public WebXMLInputContext( final PDEFormEditor editor,
                             final IEditorInput input,
                             final boolean primary )
  {
    super( editor, input, primary );
    create();
  }

  protected void reorderInsertEdits( final ArrayList ops ) {
  }

  public String getId() {
    return CONTEXT_ID;
  }

  protected String getPartitionName() {
    return "___webxml_partition"; //$NON-NLS-1$
  }

  protected IBaseModel createModel( final IEditorInput input ) 
    throws CoreException 
  {
    IDocument document = getDocumentProvider().getDocument( input );
    boolean isReconciling = input instanceof IFileEditorInput;
    WebXMLModel model = new WebXMLModel( document, isReconciling );
    model.load();
    if( isReconciling ) {
      IFile file = ( ( IFileEditorInput ) input ).getFile();
      model.setUnderlyingResource( file );
      model.setCharset( file.getCharset() );
    }
    return model;
  }
}
