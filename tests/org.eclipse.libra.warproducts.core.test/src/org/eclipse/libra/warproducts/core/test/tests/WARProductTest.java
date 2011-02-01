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

import java.io.*;

import junit.framework.TestCase;

import org.eclipse.core.runtime.*;
import org.eclipse.libra.warproducts.core.*;
import org.eclipse.pde.internal.core.iproduct.IPluginConfiguration;
import org.eclipse.pde.internal.core.iproduct.IProductPlugin;
import org.eclipse.pde.internal.core.product.ProductPlugin;


public class WARProductTest extends TestCase {
  
  public void testWARProductProxy() {
    WARProductModel model = new WARProductModel();
    WARProductModelFactory factory = new WARProductModelFactory( model );
    IWARProduct product = ( IWARProduct )factory.createProduct();
    assertNotNull( product );
    assertTrue( product instanceof WARProduct );
  }
  
  public void testGetId() {
    WARProductModel model = new WARProductModel();
    WARProductModelFactory factory = new WARProductModelFactory( model );
    IWARProduct product = ( IWARProduct )factory.createProduct();
    String id = "anId";
    product.setId( id );
    assertEquals( id, product.getId() );
  }
  
  public void testAddJARFile() {
    WARProductModel model = new WARProductModel();
    WARProductModelFactory factory = new WARProductModelFactory( model );
    IWARProduct product = ( IWARProduct )factory.createProduct();
    IPath jarPath = new Path( getAbsoluteFilePath( "test", "test.jar" ) );
    product.addLibrary( jarPath, false );
    IPath[] pathes = product.getLibraries();
    assertEquals( jarPath, pathes[ 0 ] );
  }
  
  public void testContainsLibrary() {
    WARProductModel model = new WARProductModel();
    WARProductModelFactory factory = new WARProductModelFactory( model );
    IWARProduct product = ( IWARProduct )factory.createProduct();
    IPath jarPath = new Path( getAbsoluteFilePath( "test", "test.jar" ) );
    product.addLibrary( jarPath, false );
    assertTrue( product.contiansLibrary( jarPath ) );
    IPath newPath = new Path( getAbsoluteFilePath( "test", "test.jar" ) );
    assertTrue( product.contiansLibrary( newPath ) );
  }
  
  public void testAddWebXml() {
    WARProductModel model = new WARProductModel();
    WARProductModelFactory factory = new WARProductModelFactory( model );
    IWARProduct product = ( IWARProduct )factory.createProduct();
    IPath webXmlPath = new Path( getAbsoluteFilePath( "test", "web.xml" ) );
    product.addWebXml( webXmlPath );
    IPath actualPath = product.getWebXml();
    assertEquals( webXmlPath, actualPath );
  }
  
  public void testAddLaunchIni() {
    WARProductModel model = new WARProductModel();
    WARProductModelFactory factory = new WARProductModelFactory( model );
    IWARProduct product = ( IWARProduct )factory.createProduct();
    IPath launchIniPath = new Path( getAbsoluteFilePath( "test", 
                                                         "launch.ini" ) );
    product.addLaunchIni( launchIniPath );
    IPath actualPath = product.getLaunchIni();
    assertEquals( launchIniPath, actualPath );
  }
  
  public void testWrite() {
    String xml = writenXmlFromProduct();
    assertTrue( xml.indexOf( "<warConfiguration webXml=\"/test/web.xml" + "\" " 
                             + "launchIni=\"/test/launch.ini" 
                             + "\">" ) > -1 );
    assertTrue( xml.indexOf( "</warConfiguration>" ) > -1 );
    assertTrue( xml.indexOf( "<libraries>" ) > -1 );
    assertTrue( xml.indexOf( "</libraries>" ) > -1 );
    assertTrue( xml.indexOf( "<library path=\"/test/test.jar\" " +
    		                     "fromTarget=\"false\"/>" ) > -1 );
  }
  
  public void testIncludeLauncher() {
    WARProductModel model = new WARProductModel();
    WARProductModelFactory factory = new WARProductModelFactory( model );
    IWARProduct product = ( IWARProduct )factory.createProduct();
    assertFalse( product.includeLaunchers() );
  }
  
  public void testParse() throws CoreException {
    String xml = writenXmlFromProduct();
    WARProductModel model = new WARProductModel();
    InputStream stream = new ByteArrayInputStream( xml.getBytes() );
    model.load( stream, false );
    IWARProduct product = ( IWARProduct )model.getProduct();
    IPath launchIniPath = new Path( getAbsoluteFilePath( "test", "launch.ini" ) );
    assertEquals( launchIniPath, product.getLaunchIni() );
    IPath webXmlPath = new Path( getAbsoluteFilePath( "test", "web.xml" ) );
    assertEquals( webXmlPath, product.getWebXml() );
    IPath jarPath = new Path( getAbsoluteFilePath( "test", "test.jar" ) );
    assertEquals( jarPath, product.getLibraries()[ 0 ] );
  }
  
  public void testPluginConfiguration() throws CoreException {
    WARProductModel model = new WARProductModel();
    WARProductModelFactory factory = new WARProductModelFactory( model );
    IWARProduct product = ( IWARProduct )factory.createProduct();
    IProductPlugin[] plugins = new IProductPlugin[ 5 ];
    for( int i = 0; i < plugins.length; i++ ) {
      plugins[ i ] = new ProductPlugin( model );
      plugins[ i ].setId( "a.bundle.id." + i );
    }
    product.addPlugins( plugins );
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter( stringWriter );
    product.write( "", writer );
    String serializedProduct = stringWriter.getBuffer().toString();
    model = new WARProductModel();
    ByteArrayInputStream stream 
      = new ByteArrayInputStream( serializedProduct.getBytes() );
    model.load( stream, false );
    product = ( IWARProduct )model.getProduct();
    IPluginConfiguration[] pluginConfigurations 
      = product.getPluginConfigurations();
    assertEquals( 5, pluginConfigurations.length );
    for( int i = 0; i < pluginConfigurations.length; i++ ) {
      IPluginConfiguration conf = pluginConfigurations[ i ];
      assertEquals( "a.bundle.id." + i , conf.getId() );
      assertTrue( conf.isAutoStart() );
    }
  }

  private String writenXmlFromProduct() {
    WARProductModel model = new WARProductModel();
    WARProductModelFactory factory = new WARProductModelFactory( model );
    IWARProduct product = ( IWARProduct )factory.createProduct();
    IPath launchIniPath = new Path( getAbsoluteFilePath( "test", "launch.ini" ) );
    product.addLaunchIni( launchIniPath );
    IPath webXmlPath = new Path( getAbsoluteFilePath( "test", "web.xml" ) );
    product.addWebXml( webXmlPath );
    IPath jarPath = new Path( getAbsoluteFilePath( "test", "test.jar" ) );
    product.addLibrary( jarPath, false );
    StringWriter writer = new StringWriter();
    PrintWriter printWriter = new PrintWriter( writer );
    product.write( "", printWriter );
    writer.flush();
    StringBuffer buffer = writer.getBuffer();
    String xml = buffer.toString();
    return xml;
  }
  
  public void testReset() {
    WARProductModel model = new WARProductModel();
    WARProductModelFactory factory = new WARProductModelFactory( model );
    IWARProduct product = ( IWARProduct )factory.createProduct();
    IPath launchIniPath = new Path( getAbsoluteFilePath( "test", "launch.ini" ) );
    product.addLaunchIni( launchIniPath );
    IPath webXmlPath = new Path( getAbsoluteFilePath( "test", "web.xml" ) );
    product.addWebXml( webXmlPath );
    IPath jarPath = new Path( getAbsoluteFilePath( "test", "test.jar" ) );
    product.addLibrary( jarPath, false );
    product.reset();
    assertNull( product.getWebXml() );
    assertNull( product.getLaunchIni() );
    assertEquals( 0, product.getLibraries().length );
  }
  
  public void testIsLibFromTarget() {
    WARProductModel model = new WARProductModel();
    WARProductModelFactory factory = new WARProductModelFactory( model );
    IWARProduct product = ( IWARProduct )factory.createProduct();
    IPath jarPath = new Path( getAbsoluteFilePath( "test", "test.jar" ) );
    IPath jarPath2 = new Path( getAbsoluteFilePath( "test", "test2.jar" ) );
    product.addLibrary( jarPath, true );
    product.addLibrary( jarPath2, false );
    assertTrue( product.isLibraryFromTarget( jarPath ) );
    assertFalse( product.isLibraryFromTarget( jarPath2 ) );
  }
  
  private String getAbsoluteFilePath( final String folder, final String file ) {
    return File.separator + folder + File.separator + file;
  }
  
}
