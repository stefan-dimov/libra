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

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.libra.warproducts.core.validation.Validator;
import org.eclipse.pde.core.plugin.*;


public class WARProductUtil {
  
  public static IPath getAbsolutLibraryPath( final IPath libPath, 
                                             final IWARProduct product ) 
  {
    IPath result = null;
    boolean fromTarget = product.isLibraryFromTarget( libPath );
    if( fromTarget ) {
      String targetString = TargetPlatform.getLocation();
      IPath targetPath = new Path( targetString );
      result = targetPath.append( libPath );
    } else {
      IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
      IFile lib = root.getFile( libPath );
      result = lib.getLocation();
    }
    return result;
  }
  
  public static void addServletBridgeFromTarget( final IWARProduct product ) {
    ModelEntry entry = PluginRegistry.findEntry( Validator.SERVLET_BRIDGE_ID );
    if( entry != null ) {
      IPluginModelBase[] targetModels = entry.getExternalModels();
      boolean foundBridge = false;
      for( int i = 0; i < targetModels.length && !foundBridge; i++ ) {
        IPluginModelBase bridgeModel = targetModels[ i ];
        String libLocation = bridgeModel.getInstallLocation();
        if(    libLocation != null 
            && libLocation.toLowerCase().indexOf( ".jar" ) != -1 ) //$NON-NLS-1$
        {
          String targetLocation = TargetPlatform.getLocation();
          int targetLength = targetLocation.length();
          int liblength = libLocation.length();
          String pathToAdd = libLocation.substring( targetLength, liblength );
          IPath bridgePath = new Path( pathToAdd );
          product.addLibrary( bridgePath, true );
          foundBridge = true;
        }
      }
    }
  }
  
}
