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
      String absoluteBridgePath = getServletBridgeAbsolutePath();
      if( absoluteBridgePath != null ) {
        if( absoluteBridgePath.indexOf( libPath.toPortableString() ) != -1 ) {
          result = new Path( absoluteBridgePath );
        }
      }
    } else {
      IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
      IFile lib = root.getFile( libPath );
      result = lib.getLocation();
    }
    return result;
  }
  
  public static void addServletBridgeFromTarget( final IWARProduct product ) {
    String path = getServletBridgeAbsolutePath();
    if( path != null ) {
      IPath absolutePath = new Path( path );
      IPath relativePath = new Path( absolutePath.lastSegment() );
      product.addLibrary( relativePath, true );
    }
  }

  private static String getServletBridgeAbsolutePath() {
    String result = null;
    ModelEntry entry = PluginRegistry.findEntry( Validator.SERVLET_BRIDGE_ID );
    if( entry != null ) {
      IPluginModelBase[] targetModels = entry.getExternalModels();
      for( int i = 0; i < targetModels.length && result == null; i++ ) {
        IPluginModelBase bridgeModel = targetModels[ i ];
        String libLocation = bridgeModel.getInstallLocation();
        if(    libLocation != null 
            && libLocation.toLowerCase().indexOf( ".jar" ) != -1 ) //$NON-NLS-1$
        {
          result = libLocation;
        }
      }
    }
    return result;
  }
  
}
