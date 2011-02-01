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

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

  private static final String BUNDLE_NAME 
    = "org.eclipse.libra.warproducts.core.messages"; //$NON-NLS-1$
  public static String creatorCouldntCopy;
  public static String validatorForbiddenBundle;
  public static String validatorLibraryNotExist;
  public static String validatorMissingBundle;
  public static String validatorMissingLibrary;  
  public static String FeatureBasedExportOperation_ProblemDuringExport;
  public static String FeatureExportOperation_CompilationErrors;
  static {
    // initialize resource bundle
    NLS.initializeMessages( BUNDLE_NAME, Messages.class );
  }

  private Messages() {
  }
}
