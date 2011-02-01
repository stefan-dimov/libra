/******************************************************************************* 
* Copyright (c) 2010 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
*******************************************************************************/ 
package org.eclipse.libra.warproducts.core.test.tests;

import java.io.*;

import junit.framework.TestCase;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.Path;
import org.eclipse.libra.warproducts.core.*;
import org.eclipse.libra.warproducts.core.validation.*;
import org.eclipse.pde.internal.core.iproduct.IProductPlugin;
import org.eclipse.pde.internal.core.product.ProductPlugin;

public class ValidatorTest extends TestCase {
  
  private static final String EQUINOX_HTTP_SERVLETBRIDGE 
    = "org.eclipse.equinox.http.servletbridge";
  private static final String EQUINOX_HTTP_SERVLET 
    = "org.eclipse.equinox.http.servlet";
  private static final String EQUINOX_REGISTRY = "org.eclipse.equinox.registry";
  private static final String EQUINOX_HTTP_REGISTRY 
    = "org.eclipse.equinox.http.registry";
  private static final String SERVLETBRIDGE_EXTENSIONBUNDLE 
    = "org.eclipse.equinox.servletbridge.extensionbundle";
  private static final String UPDATE_CONFIGURATOR 
    = "org.eclipse.update.configurator";
  private static final String JAVAX_SERVLET = "javax.servlet";
  private static final String SERVLETBRIDGE 
    = "org.eclipse.equinox.servletbridge";
  private static final String SERVLETBRIDGE_JAR 
    = "org.eclipse.equinox.servletbridge.jar";

  public void testServletBridgeLibraryIsMissing() {
    IWARProduct product = createBasicProduct();
    Validator validator = new Validator( product );
    Validation validation = validator.validate();
    assertFalse( validation.isValid() );
    ValidationError[] errors = validation.getErrors();
    boolean foundServletBridgeMissing = false;
    for( int i = 0; i < errors.length; i++ ) {
      ValidationError error = errors[ i ];
      String message = error.getMessage();
      if( error.getType() == ValidationError.LIBRARY_MISSING
          && message.indexOf( SERVLETBRIDGE ) != -1 ) 
      {
        foundServletBridgeMissing = true;
      }
    }
    assertTrue( foundServletBridgeMissing );
  }
  
  public void testContainsServletBridgeLibrary() throws Exception {
    IWARProduct product = createBasicProduct();
    IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
    IProject project = wsRoot.getProject( "warProduct" );
    if( !project.exists() ) {
      project.create( null );
      project.open( null );
    }
    IFile jar = project.getFile( SERVLETBRIDGE + ".jar" );
    if( !jar.exists() ) {
      File bridge 
        = File.createTempFile( SERVLETBRIDGE, ".jar" );
      FileInputStream stream = new FileInputStream( bridge );
      jar.create( stream, true, null );
    }
    product.addLibrary( jar.getFullPath(), false );
    Validator validator = new Validator( product );
    Validation validation = validator.validate();
    assertTrue( validation.isValid() );
  }
  
  public void testLibrariesDoesntExist() {
    IWARProduct product = createBasicProduct();
    String projectPath = File.separator + "test";
    Path path = new Path( projectPath + File.separator + "test.jar" );
    product.addLibrary( path, false );
    Path servletBridgePath 
      = new Path( projectPath + File.separator + SERVLETBRIDGE_JAR );
    product.addLibrary( servletBridgePath, false );
    Validator validator = new Validator( product );
    Validation validation = validator.validate();
    assertFalse( validation.isValid() );
    ValidationError[] errors = validation.getErrors();
    boolean testJarIsMissing = false;
    for( int i = 0; i < errors.length; i++ ) {
      ValidationError error = errors[ i ];
      String message = error.getMessage();
      if( error.getType() == ValidationError.LIBRARY_DOESNT_EXIST
          && message.indexOf( "test.jar" ) != -1 ) 
      {
        testJarIsMissing = true;
      }
    }
    assertTrue( testJarIsMissing );
  }
  
  public void testLibrariesExist() throws Exception {
    IWARProduct product = createBasicProductWithLibraries();
    Validator validator = new Validator( product );
    Validation validation = validator.validate();
    assertTrue( validation.isValid() );
  }
  
  public void testJavaxServletIsExcluded() throws Exception {
    String id = JAVAX_SERVLET;
    checkForBannedBundle( id );
  }
  
  public void testUpdateConfiguratorIsExcluded() throws Exception {
    String id = UPDATE_CONFIGURATOR;
    checkForBannedBundle( id );
  }

  private void checkForBannedBundle( final String id ) throws Exception 
  {
    IWARProduct product = createBasicProductWithLibraries();
    IProductPlugin plugin = new ProductPlugin( product.getModel() );
    plugin.setId( id );
    IProductPlugin[] plugins = new IProductPlugin[] { plugin };
    product.addPlugins( plugins );
    Validator validator = new Validator( product );
    Validation validation = validator.validate();
    assertFalse( validation.isValid() );
    ValidationError[] errors = validation.getErrors();
    boolean foundBannedBundle = false;
    for( int i = 0; i < errors.length && !foundBannedBundle; i++ ) {
      ValidationError error = errors[ i ];
      if( error.getType() == ValidationError.BUNDLE_BANNED ) {
        IProductPlugin bannedPlugin 
          = ( IProductPlugin )error.getCausingObject();
        String message = error.getMessage();
        if( bannedPlugin.getId().equals( id ) && message.indexOf( id ) != -1 ) {
          foundBannedBundle = true;
        }
      }
    }
    assertTrue( foundBannedBundle );
  }
  
  public void testSerlvetExtensionBundleIsIncluded() throws IOException {
    String id = SERVLETBRIDGE_EXTENSIONBUNDLE;
    checkForMissingRequiredBundle( id );
  }
  
  public void testEquinoxHTTPRegistryBundleIsIncluded() throws IOException {
    String id = EQUINOX_HTTP_REGISTRY;
    checkForMissingRequiredBundle( id );
  }
  
  public void testEquinoxRegistryBundleIsIncluded() throws IOException {
    String id = EQUINOX_REGISTRY;
    checkForMissingRequiredBundle( id );
  }
  
  public void testEquinoxHTTPServletBundleIsIncluded() throws IOException {
    String id = EQUINOX_HTTP_SERVLET;
    checkForMissingRequiredBundle( id );
  }
  
  public void testEquinoxHTTPServletBridgeBundleIsIncluded() 
    throws IOException 
  {
    String id = EQUINOX_HTTP_SERVLETBRIDGE;
    checkForMissingRequiredBundle( id );
  }
  
  private void checkForMissingRequiredBundle( final String id ) 
    throws IOException 
  {
    IWARProduct product = createPlainProducttWithLibraries();
    Validator validator = new Validator( product );
    Validation validation = validator.validate();
    assertFalse( validation.isValid() );
    ValidationError[] errors = validation.getErrors();
    boolean foundMissingBundle = false;
    for( int i = 0; i < errors.length && !foundMissingBundle; i++ ) {
      ValidationError error = errors[ i ];
      if( error.getType() == ValidationError.BUNDLE_MISSING ) {
        IProductPlugin missingBundle 
          = ( IProductPlugin )error.getCausingObject();
        String message = error.getMessage();
        if( missingBundle.getId().equals( id ) 
            && message.indexOf( id ) != -1 ) 
        {
          foundMissingBundle = true;
        }
      }
    }
    assertTrue( foundMissingBundle );
  }

  private IWARProduct createBasicProductWithLibraries() throws Exception {
    IWARProduct product = createBasicProduct();
    IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
    IProject project = wsRoot.getProject( "warProduct" );
    if( !project.exists() ) {
      project.create( null );
      project.open( null );
    }
    IFile jar = project.getFile( "test.jar" );
    if( !jar.exists() ) {
      File testJar = File.createTempFile( "test", ".jar" );
      FileInputStream stream = new FileInputStream( testJar );
      jar.create( stream, true, null );
    }
    product.addLibrary( jar.getFullPath(), false );
    IFile bridge = project.getFile( SERVLETBRIDGE + ".jar" );
    if( !bridge.exists() ) {
      File bridgeJar 
        = File.createTempFile( SERVLETBRIDGE, ".jar" );
      FileInputStream stream = new FileInputStream( bridgeJar );
      bridge.create( stream, true, null );
    }
    product.addLibrary( bridge.getFullPath(), false );
    return product;
  }

  private IWARProduct createBasicProduct() {
    WARProductModel model = new WARProductModel();
    WARProductModelFactory factory = new WARProductModelFactory( model );
    String requiredBundles[] = Validator.REQUIRED_BUNDLES;
    IWARProduct product = ( IWARProduct )factory.createProduct();
    for( int i = 0; i < requiredBundles.length; i++ ) {
      addBundleToProduct( factory, requiredBundles[ i ], product );
    }
    return product;
  }
  
  private IWARProduct createPlainProducttWithLibraries() throws IOException {
    IWARProduct product = createPlainProduct();
    File testJar = File.createTempFile( "test", ".jar" );
    Path testPath = new Path( testJar.getAbsolutePath() );
    product.addLibrary( testPath, false );
    File bridgeJar 
      = File.createTempFile( SERVLETBRIDGE, ".jar" );
    Path servletBridgePath = new Path( bridgeJar.getAbsolutePath() );
    product.addLibrary( servletBridgePath, false );
    return product;
  }
  
  private IWARProduct createPlainProduct() {
    WARProductModel model = new WARProductModel();
    WARProductModelFactory factory = new WARProductModelFactory( model );
    IWARProduct product = ( IWARProduct )factory.createProduct();
    return product;
  }

  private void addBundleToProduct( final WARProductModelFactory factory, 
                                   final String id, 
                                   final IWARProduct product ) 
  {
    IProductPlugin bundle = factory.createPlugin();
    bundle.setId( id );
    bundle.setVersion( "0.0.0" );
    product.addPlugins( new IProductPlugin[] { bundle } );
  }
  
}
