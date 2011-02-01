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

import org.eclipse.pde.internal.core.text.DocumentNodeFactory;
import org.eclipse.pde.internal.core.text.IDocumentElementNode;

public class WebXmlNodeFactory extends DocumentNodeFactory {

  private final WebXMLModel model;

  public WebXmlNodeFactory( final WebXMLModel model ) {
    this.model = model;
  }

  public IDocumentElementNode createDocumentNode( 
    final String name,
    final IDocumentElementNode parent )
  {
    IDocumentElementNode result = super.createDocumentNode( name, parent );
    if( parent == null ) {
      model.setDocumentRoot( result );
    }
    return result;
  }
}
