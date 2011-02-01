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

import org.eclipse.jface.text.IDocument;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.core.IWritable;
import org.eclipse.pde.internal.core.text.IDocumentElementNode;
import org.eclipse.pde.internal.core.text.XMLEditingModel;
import org.xml.sax.helpers.DefaultHandler;


public class WebXMLModel extends XMLEditingModel {

  private WebXmlDocumentHandler documentHandler;
  private final WebXmlNodeFactory nodeFactory;
  private IDocumentElementNode documentRoot;
  
  public WebXMLModel( final IDocument document, final boolean isReconciling ) {
    super( document, isReconciling );
    this.nodeFactory = new WebXmlNodeFactory(this);
  }

  protected DefaultHandler createDocumentHandler( final IModel model,
                                                  final boolean reconciling )
  {
    if( documentHandler == null ) {
      documentHandler = new WebXmlDocumentHandler( getDocument(), 
                                                   nodeFactory, 
                                                   reconciling ); 
    }
    return documentHandler; 
  }

  public Object getDocumentRoot() {
    return documentRoot;
  }
  
  protected IWritable getRoot() {
    return null;
  }

  public void setDocumentRoot( final IDocumentElementNode documentRoot ) {
    this.documentRoot = documentRoot;
  }
}
