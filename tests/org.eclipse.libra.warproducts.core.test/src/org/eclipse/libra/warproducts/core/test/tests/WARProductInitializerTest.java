/******************************************************************************* 
* Copyright (c) 2010, 2011 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Holger Staudacher - initial API and implementation
*******************************************************************************/ 
package org.eclipse.libra.warproducts.core.test.tests;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.resources.*;
import org.eclipse.libra.warproducts.core.*;
import org.eclipse.libra.warproducts.core.validation.Validation;
import org.eclipse.libra.warproducts.core.validation.Validator;
import org.eclipse.pde.internal.core.iproduct.IProductPlugin;


public class WARProductInitializerTest extends TestCase {
  
  public void testValidInitialization() throws Exception {
    WARProductModel model = new WARProductModel();
    WARProductModelFactory factory = new WARProductModelFactory( model );
    IWARProduct product = ( IWARProduct )factory.createProduct();
    product.addPlugins( createFakeEnvironment( factory ) );
    addFakeServletBridge( product );
    WARProductInitializer initializer = new WARProductInitializer( product );
    initializer.initialize();
    Validator validator = new Validator( product );
    Validation validation = validator.validate();
    assertTrue( validation.isValid() );
  }

  private IProductPlugin[] createFakeEnvironment( 
    final WARProductModelFactory factory ) 
  {
    String[] bundles = Validator.REQUIRED_BUNDLES;
    List bundleList = new ArrayList();
    for( int i = 0; i < bundles.length; i++ ) {
      IProductPlugin plugin = factory.createPlugin();
      plugin.setId( bundles[ i ] );
      plugin.setVersion( "0.0.0" );
      bundleList.add( plugin );
    }
    IProductPlugin javaxServletBundle = factory.createPlugin();
    javaxServletBundle.setId( "javax.servlet" );
    javaxServletBundle.setVersion( "0.0.0" );
    bundleList.add( javaxServletBundle );
    IProductPlugin[] result = new IProductPlugin[ bundleList.size() ];
    bundleList.toArray( result );
    return result;
  }
  
  private void addFakeServletBridge( final IWARProduct product ) 
    throws Exception 
  {
    String bridgeId = "org.eclipse.equinox.servletbridge";
    File file = File.createTempFile( bridgeId, ".jar" );
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    IProject project = workspace.getRoot().getProject( "testing" );
    if( !project.exists() ) {
      project.create( null );
      project.open( null );
    }
    IFile bridge = project.getFile( bridgeId + ".jar" );
    if( !bridge.exists() ) {
      bridge.create( new FileInputStream( file ), true, null );
    }
    product.addLibrary( bridge.getFullPath(), false );
  }
}
