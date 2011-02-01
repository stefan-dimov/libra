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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.libra.warproducts.core.IWARProduct;
import org.eclipse.libra.warproducts.core.WARProductModel;
import org.eclipse.pde.internal.core.iproduct.IProduct;


public class WARProductModelTest extends TestCase {
  
  public void testLoad() throws CoreException {
    WARProductModel model = new WARProductModel();
    String separator = "/";
    ClassLoader classLoader = getClass().getClassLoader();
    String fileName = separator + "test.warproduct";
    model.load( classLoader.getResourceAsStream( fileName ), false );
    IWARProduct product = ( IWARProduct )model.getProduct();
    String webXmlPath = product.getWebXml().toString();
    assertEquals( separator + "test.rap" + separator + "WEB-INF" 
                  + separator + "web.xml", webXmlPath );
    String launchIniPath = product.getLaunchIni().toString();
    assertEquals( separator + "test.rap" + separator
                  + "WEB-INF" + separator + "eclipse" 
                  + separator + "launch.ini", launchIniPath );
    String libJarPath = product.getLibraries()[ 0 ].toString();
    assertEquals( separator + "test.rap" + separator + "lib.jar", libJarPath );
  }
  
  public void testWrite() throws Exception {
    WARProductModel model = new WARProductModel();
    String separator = File.separator;
    ClassLoader classLoader = getClass().getClassLoader();
    InputStream stream 
      = classLoader.getResourceAsStream( separator + "test.warproduct" );
    String xml = readStream( stream );
    InputStream stream2 
      = classLoader.getResourceAsStream( separator + "test.warproduct" );
    model.load( stream2, false );
    IProduct product = model.getProduct();
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter( stringWriter );
    product.write( "", writer );
    stringWriter.close();
    String actualXml = stringWriter.toString();
    assertEquals( xml.replaceAll( " ", "" ), actualXml.replaceAll( " ", "" ) );
  }
  
  public void testLoadWindowsFile() throws Exception {
    setUpProject();
    WARProductModel model = new WARProductModel();
    String separator = File.separator;
    String fileName = separator + "testWin.warproduct";
    ClassLoader classLoader = getClass().getClassLoader();
    model.load( classLoader.getResourceAsStream( fileName ), false );
    IWARProduct product = ( IWARProduct )model.getProduct();
    IPath webXml = product.getWebXml();
    IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
    IPath absolutWebXmlPath = wsRoot.getLocation().append( webXml );
    File file = new File( absolutWebXmlPath.toOSString() );
    assertTrue( file.exists() );
  }

  private void setUpProject() throws Exception  {
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IProject project = root.getProject( "test.rap" );
    if( !project.exists() ) {
      project.create( null );
      project.open( null );
    }
    IFolder webInf = project.getFolder( "WEB-INF" );
    if( !webInf.exists() ) {
      webInf.create( true, true, null );
    }
    IFile file = webInf.getFile( "web.xml" );
    if( !file.exists() ) {
      File tempFile = File.createTempFile( "test", ".xml" );
      FileInputStream stream = new FileInputStream( tempFile );
      file.create( stream, true, null );
    }
  }

  private String readStream( final InputStream stream ) throws IOException {
    InputStreamReader streamReader = new InputStreamReader( stream );
    BufferedReader reader = new BufferedReader( streamReader );
    StringBuffer webxmlContent = new StringBuffer();
    int c;
    while( ( c = reader.read() ) != -1 ) {
      webxmlContent.append( ( char ) c );
    }
    reader.close();
    return webxmlContent.toString();
  }
  
}
