/******************************************************************************* 
* Copyright (c) 2010 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
*******************************************************************************/ 
package org.eclipse.libra.warproducts.core;

import org.eclipse.pde.internal.core.iproduct.IProduct;
import org.eclipse.pde.internal.core.iproduct.IProductModelFactory;
import org.eclipse.pde.internal.core.product.ProductModel;


public class WARProductModel extends ProductModel {
  
  private static final long serialVersionUID = 1400491263222645845L;
  private IWARProduct warProduct;
  private WARProductModelFactory factory;

  public IProduct getProduct() {
    IProduct product = super.getProduct();
    if( product instanceof WARProduct ) {
      warProduct = ( IWARProduct )product;
    }
    if( warProduct == null ) {
      warProduct = ( IWARProduct )getFactory().createProduct();
    }
    return warProduct;
  }
  
  public IProductModelFactory getFactory() {
    if( factory == null ) {
      factory = new WARProductModelFactory( this );
    }
    return factory;
  }
  
}
