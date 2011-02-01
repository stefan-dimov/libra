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

import org.eclipse.pde.core.IBaseModel;
import org.eclipse.pde.internal.ui.editor.PDEFormEditor;
import org.eclipse.pde.internal.ui.editor.context.InputContext;
import org.eclipse.pde.internal.ui.editor.context.InputContextManager;


public class WARProductInputContextManager extends InputContextManager {

  public WARProductInputContextManager( final PDEFormEditor editor ) {
    super( editor );
  }

  public IBaseModel getAggregateModel() {
    InputContext context = findContext(WARProductInputContext.CONTEXT_ID);
    return (context != null) ? context.getModel() : null;
  }
  
  public boolean isDirty() {
    return super.isDirty();
  }
  
}
