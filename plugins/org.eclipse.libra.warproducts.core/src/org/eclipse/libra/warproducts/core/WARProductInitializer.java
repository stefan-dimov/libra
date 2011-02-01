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

import org.eclipse.libra.warproducts.core.validation.*;
import org.eclipse.pde.core.plugin.ModelEntry;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.core.iproduct.IProductPlugin;


public class WARProductInitializer {

  private IWARProduct product;

  public WARProductInitializer( final IWARProduct product ) {
    this.product = product;
  }

  public void initialize() {
    Validator validator = new Validator( product );
    Validation validation = validator.validate();
    if( !validation.isValid() ) {
      makeProductValid( validation.getErrors() );
    }
  }

  private void makeProductValid( final ValidationError[] errors ) {
    for( int i = 0; i < errors.length; i++ ) {
      ValidationError error = errors[ i ];
      if( error.getType() == ValidationError.BUNDLE_BANNED ) {
        IProductPlugin bundle = ( IProductPlugin )error.getCausingObject();
        removeBannedBundle( bundle );
      } else if( error.getType() == ValidationError.BUNDLE_MISSING ) {
        IProductPlugin bundle = ( IProductPlugin )error.getCausingObject();
        addRequiredBundle( bundle );
      } else if( error.getType() == ValidationError.LIBRARY_MISSING ) {
        WARProductUtil.addServletBridgeFromTarget( product );
      }
    }
  }

  private void removeBannedBundle( final IProductPlugin bundle ) {
    product.removePlugins( new IProductPlugin[] { bundle } );
  }
  
  private void addRequiredBundle( final IProductPlugin requiredBundle ) {
    ModelEntry entry = PluginRegistry.findEntry( requiredBundle.getId() );
    if( entry != null ) {
      WARProductModelFactory factory 
        = new WARProductModelFactory( product.getModel() );
      IProductPlugin bundle = factory.createPlugin();
      bundle.setId( requiredBundle.getId() );
      bundle.setVersion( requiredBundle.getVersion() );
      product.addPlugins( new IProductPlugin[] { bundle } );
    }
  }
  
}
