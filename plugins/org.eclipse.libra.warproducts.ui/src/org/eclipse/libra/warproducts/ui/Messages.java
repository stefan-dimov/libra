/*******************************************************************************
 * Copyright (c) 2010 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors:
 * EclipseSource - initial API and implementation
 *******************************************************************************/
package org.eclipse.libra.warproducts.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

  private static final String BUNDLE_NAME = "org.eclipse.libra.warproducts.ui.messages"; //$NON-NLS-1$
  public static String BaseWARProductCreationOperation;
  public static String BuildEditor_ClasspathSection_jarsTitle;
  public static String ConfigurationPageTitle;
  public static String DestinationGroupBrowe;
  public static String DestinationGroupFile;
  public static String DestinationGroupWarning;
  public static String DestinationGroupWarningDirectory;
  public static String editorExport;
  public static String LibrarySectionAddRequired;
  public static String LibrarySectionLibraries;
  public static String LibrarySectionListLibraries;
  public static String LibrarySectionSelectJar;
  public static String LibrarySectionServletBridge;
  public static String noProblems;
  public static String PluginSection_remove;
  public static String Product_PluginSection_add;
  public static String ValidateAction;
  public static String ValidateActionTitle;
  public static String Validation;
  public static String ValidationPageDesc;
  public static String ValidationPageTitle;
  public static String EditorExportSection;
  public static String ExportPage;
  public static String ExportPageDescription;
  public static String ExportPageTitle;
  public static String FileWizardPageCreate;
  public static String FileWizardPageError;
  public static String FileWizardPageInit;
  public static String FileWizardPageLaunchConfig;
  public static String FileWizardPageNewFile;
  public static String FileWizardPageTitle;
  public static String NewWarProductFileWizard;
  public static String SelectionPageSelect;
  public static String SelectionPageTitle;
  public static String NewWARProductError;
  public static String LoadWARProductError;
  public static String OutlinePluginsTitle;
  static {
    // initialize resource bundle
    NLS.initializeMessages( BUNDLE_NAME, Messages.class );
  }

  private Messages() {
  }
}
