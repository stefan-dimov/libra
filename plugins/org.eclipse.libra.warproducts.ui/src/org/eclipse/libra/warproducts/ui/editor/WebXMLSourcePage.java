/*******************************************************************************
 * Copyright (c) 2010 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors:
 * EclipseSource - initial API and implementation
 *******************************************************************************/
package org.eclipse.libra.warproducts.ui.editor;

import org.eclipse.jface.viewers.*;
import org.eclipse.pde.internal.core.text.plugin.DocumentGenericNode;
import org.eclipse.pde.internal.ui.*;
import org.eclipse.pde.internal.ui.editor.PDEFormEditor;
import org.eclipse.pde.internal.ui.editor.XMLSourcePage;
import org.eclipse.pde.internal.ui.elements.DefaultContentProvider;
import org.eclipse.swt.graphics.Image;

public class WebXMLSourcePage extends XMLSourcePage {

  private ITreeContentProvider contentProvider;
  private ILabelProvider labelProvider;

  public WebXMLSourcePage( final PDEFormEditor editor,
                           final String id,
                           final String title )
  {
    super( editor, id, title );
  }

  public boolean isQuickOutlineEnabled() {
    return true;
  }

  public ILabelProvider createOutlineLabelProvider() {
    if( labelProvider == null ) {
      labelProvider = new WebXmlLabelProvider();
    }
    return labelProvider;
  }

  public ITreeContentProvider createOutlineContentProvider() {
    if( contentProvider == null ) {
      contentProvider = new WebXmlContentProvider();
    }
    return contentProvider;
  }

  public ViewerComparator createOutlineComparator() {
    return new ViewerComparator();
  }

  public void updateSelection( final Object object ) {
    if( object instanceof DocumentGenericNode ) {
      DocumentGenericNode node = ( DocumentGenericNode )object;
      selectAndReveal( node.getOffset(), node.getLength() );
    }
  }
  
  private static final class WebXmlLabelProvider extends LabelProvider {

    private PDELabelProvider pdeLabelProvider = PDEPlugin.getDefault()
      .getLabelProvider();

    public String getText( final Object element ) {
      String result = "";//$NON-NLS-1$
      if( element instanceof DocumentGenericNode ) {
        DocumentGenericNode node = ( DocumentGenericNode )element;
        result = node.getXMLTagName();
      } else {
        result = super.getText( element );
      }
      return result;
    }

    public Image getImage( final Object element ) {
      Image result = null;
      if( element instanceof DocumentGenericNode ) {
        result = pdeLabelProvider.get( PDEPluginImages.DESC_XML_ELEMENT_OBJ );
      }
      return result;
    }
  }
  private class WebXmlContentProvider extends DefaultContentProvider 
    implements ITreeContentProvider
  {

    public Object[] getElements( final Object inputElement ) {
      Object[] result = new Object[ 0 ];
      if( inputElement instanceof WebXMLModel ) {
        WebXMLModel model = ( WebXMLModel )inputElement;
        Object documentRoot = model.getDocumentRoot();
        result = new Object[ 1 ];
        result[ 0 ] = documentRoot;
      }
      return result;
    }

    public Object[] getChildren( final Object element ) {
      Object[] result = new Object[ 0 ];
      if( element instanceof DocumentGenericNode ) {
        DocumentGenericNode node = ( DocumentGenericNode )element;
        result = node.getChildNodes();
      }
      return result;
    }

    public Object getParent( final Object element ) {
      Object result = null;
      if( element instanceof DocumentGenericNode ) {
        DocumentGenericNode node = ( DocumentGenericNode )element;
        result = node.getParentNode();
      }
      return result;
    }

    public boolean hasChildren( final Object element ) {
      return getChildren( element ).length > 0;
    }
  }
}
