/******************************************************************************* 
* Copyright (c) 2010 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
*******************************************************************************/ 
package org.eclipse.libra.warproducts.ui.validation;

import java.util.Map;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.pde.internal.ui.elements.DefaultContentProvider;

public class PluginStatusDialogContentProvider extends DefaultContentProvider
  implements ITreeContentProvider
{
  
  private Map input;

  public PluginStatusDialogContentProvider( final Map input ) {
    this.input = input;
  }

  public Object[] getChildren( final Object parentElement ) {
    return ( Object[] )input.get( parentElement );
  }

  public Object getParent( final Object element ) {
    return null;
  }

  public boolean hasChildren( final Object element ) {
    boolean validType = element instanceof String 
                        || element instanceof BundleDescription;
    return input.containsKey( element ) && validType;
  }

  public Object[] getElements( final Object inputElement ) {
    return ( ( Map )inputElement ).keySet().toArray();
  }

  public void setInput( final Map input ) {
    this.input = input;
  }
}