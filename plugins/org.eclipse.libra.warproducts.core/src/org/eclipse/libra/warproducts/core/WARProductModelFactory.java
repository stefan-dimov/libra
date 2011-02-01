/******************************************************************************* 
* Copyright (c) 2010, 2011 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Holger Staudacher - initial API and implementation
*******************************************************************************/ 
package org.eclipse.libra.warproducts.core;

import org.eclipse.pde.internal.core.iproduct.IProduct;
import org.eclipse.pde.internal.core.iproduct.IProductModel;
import org.eclipse.pde.internal.core.product.ProductModelFactory;


public class WARProductModelFactory extends ProductModelFactory {

  public WARProductModelFactory( final IProductModel model ) {
    super( model );
    
  }

  public IProduct createProduct() {
    IProduct product = super.createProduct();
    return new WARProduct( product );
  }
  
}
