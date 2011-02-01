/******************************************************************************* 
* Copyright (c) 2010, 2011 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Holger Staudacher - initial API and implementation
*******************************************************************************/ 
package org.eclipse.libra.warproducts.core.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.libra.warproducts.core.test.tests.*;


public class QuickTests {
  
  public static Test suite() {
    TestSuite suite = new TestSuite( "Quick WAR product tests" );
    suite.addTestSuite( InfrastructureCreatorTest.class );
    suite.addTestSuite( WARProductTest.class );
    suite.addTestSuite( WARProductModelTest.class );
    suite.addTestSuite( ValidatorTest.class );
    suite.addTestSuite( WARProductInitializerTest.class );
    return suite;
  }
  
}
