/*******************************************************************************
 * Copyright (c) 2010 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors:
 * EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.libra.warproducts.ui.editor;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.*;
import org.eclipse.libra.warproducts.core.WARProduct;
import org.eclipse.libra.warproducts.ui.Messages;
import org.eclipse.pde.internal.core.iproduct.*;
import org.eclipse.pde.internal.ui.*;
import org.eclipse.pde.internal.ui.editor.FormOutlinePage;
import org.eclipse.pde.internal.ui.editor.PDEFormEditor;
import org.eclipse.swt.graphics.Image;

public class WARProductOutlinePage extends FormOutlinePage {

  private Object[] plugins;
  private Object[] libraries;
  private final Comparator libraryComparator;
  
  public WARProductOutlinePage( final PDEFormEditor editor ) {
    super( editor );
    libraryComparator = new LibraryComparator();
  }

  public void sort( final boolean sorting ) {
  }

  public ILabelProvider createLabelProvider() {
    ILabelProvider labelProvider =  super.createLabelProvider();
    return new ConfigurationPageLabelProvider( labelProvider );
  }
  
  protected Object[] getChildren( final Object parent ) {
    Object[] result = new Object[0];
    if( parent instanceof ConfigurationPage ) {
      result = getConfigurationPageChildren( parent );
    } else if( parent instanceof Object[] ) {
      result = ( Object[] )parent;
    }
    return result;
  }

  private Object[] getConfigurationPageChildren( final Object parent ) {
    Object[] result;
    ConfigurationPage page = ( ConfigurationPage )parent;
    IProductModel productModel = ( IProductModel )page.getModel();
    WARProduct product = ( WARProduct )productModel.getProduct();
    plugins = product.getPlugins();
    libraries = product.getLibraries();
    Arrays.sort( libraries, libraryComparator );
    result = new Object[ 2 ];
    result[ 0 ] = plugins;
    result[ 1 ] = libraries;
    return result;
  }

  protected String getParentPageId( final Object item ) {
    String result = super.getParentPageId( item );
    if( item instanceof IProductPlugin || item instanceof IPath) {
      result = ConfigurationPage.PLUGIN_ID;
    }
    if( item instanceof IProductFeature ) {
      result = ConfigurationPage.FEATURE_ID;
    }
    return result;
  }
  
  private class LibraryComparator implements Comparator {

    public int compare( final Object o1, 
                        final Object o2 ) {
      IPath p1 = ( IPath )o1;
      IPath p2 = ( IPath )o2;
      return p1.toOSString().compareTo( p2.toOSString() );
    }
  }
  
  private  class ConfigurationPageLabelProvider extends LabelProvider {
    
    private final ILabelProvider labelProvider;
    private final PDELabelProvider pdeLabelProvider;
    
    public ConfigurationPageLabelProvider( 
      final ILabelProvider labelProvider ) 
    {
      this.labelProvider = labelProvider;
      this.pdeLabelProvider = PDEPlugin.getDefault().getLabelProvider();
    }
    
    public String getText( final Object element ) {
      String result = "";//$NON-NLS-1$
      if(element instanceof IPath){
        IPath path = (IPath)element;
        result = path.segment( path.segmentCount() - 1 );
      } else if ( element == libraries ){
        result = Messages.LibrarySectionLibraries;
      } else if ( element == plugins ) {
        result = Messages.OutlinePluginsTitle;
      } else {
        result = labelProvider.getText( element );
      }
      return result;
    }
    
    public Image getImage( final Object element ) {
      Image result = null;
      if( element instanceof IPath || element == libraries ) {
        result = pdeLabelProvider.get( PDEPluginImages.DESC_JAR_LIB_OBJ );
      } else if( element == plugins ) {
        result = pdeLabelProvider.get( PDEPluginImages.DESC_PLUGIN_OBJ );
      } else {
        result = labelProvider.getImage( element );
      }
      return result;
    }
  }
}