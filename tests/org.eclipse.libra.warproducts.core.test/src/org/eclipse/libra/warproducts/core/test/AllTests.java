/******************************************************************************* 
* Copyright (c) 2010 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
*******************************************************************************/ 
package org.eclipse.libra.warproducts.core.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.libra.warproducts.core.test.tests.WARProductExportOperationTest;


public class AllTests {
  
  public static Test suite() {
    TestSuite suite = new TestSuite( "All WAR product tests" );
    suite.addTest( QuickTests.suite() );
    TestSuite longSuite = new TestSuite( "Long running WAR product tests" );
    suite.addTest( longSuite );
    longSuite.addTestSuite( WARProductExportOperationTest.class );
    
    return suite;
  }
  
  
}
