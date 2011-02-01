/*******************************************************************************
 * Copyright (c) 2010 EclipseSource.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.libra.warproducts.ui.editor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.pde.internal.core.text.*;


public class WebXmlDocumentHandler extends DocumentHandler {

  private final DocumentNodeFactory nodeFactory;
  private final IDocument document;

  public WebXmlDocumentHandler( final IDocument document, 
                                final DocumentNodeFactory nodeFactory, 
                                final boolean reconciling ) {
    super( reconciling );
    this.nodeFactory = nodeFactory;
    this.document = document;
  }

  protected IDocumentElementNode getDocumentNode( 
    final String name,
    final IDocumentElementNode parent )
  {
    return nodeFactory.createDocumentNode( name, parent );
  }

  protected IDocumentAttributeNode getDocumentAttribute( 
    final String name,
    final String value,
    final IDocumentElementNode parent )
  {
    return nodeFactory.createAttribute( name, value, parent );
  }

  protected IDocumentTextNode getDocumentTextNode( 
    final String content,
    final IDocumentElementNode parent )
  {
    return nodeFactory.createDocumentTextNode( content, parent );
  }

  protected IDocument getDocument() {
    return document;
  }
}
