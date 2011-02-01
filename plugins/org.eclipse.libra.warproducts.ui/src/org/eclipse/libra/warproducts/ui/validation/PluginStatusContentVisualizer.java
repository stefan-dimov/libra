/******************************************************************************* 
* Copyright (c) 2010, 2011 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Holger Staudacher - initial API and implementation
*******************************************************************************/ 
package org.eclipse.libra.warproducts.ui.validation;

import java.util.Map;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.widgets.Composite;


public class PluginStatusContentVisualizer {
  
  private TreeViewer treeViewer;
  private Map input;
  
  public void createControls( final Composite parent ) {
    treeViewer = new TreeViewer( parent );
    PluginStatusDialogContentProvider contentProvider 
      = new PluginStatusDialogContentProvider( input );
    treeViewer.setContentProvider( contentProvider );
    treeViewer.setLabelProvider( new PluginStatusDialogLableProvider() );
    treeViewer.setComparator( new ViewerComparator() );
    treeViewer.setInput( input );
    treeViewer.refresh();
  }
  
  public TreeViewer getViewer() {
    return treeViewer;
  }
  
  public void setInput( final Map input ) {
    this.input = input;
    if( treeViewer != null ) {
      treeViewer.setInput( input );
      treeViewer.refresh();
    }
  }
  
  
}
