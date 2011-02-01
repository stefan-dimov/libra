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

import org.eclipse.core.runtime.IPath;
import org.eclipse.pde.internal.core.iproduct.IProduct;


public interface IWARProduct extends IProduct {

  void addLibrary( final IPath absolutePath, final boolean fromTarget );
  IPath[] getLibraries();
  boolean isLibraryFromTarget( final IPath libraryPath );
  boolean contiansLibrary( final IPath relativeWorkspacePath );

  void addWebXml( final IPath relativeWorkspacePath );
  IPath getWebXml();

  void addLaunchIni( final IPath relativeWorkspacePath );
  IPath getLaunchIni();

  void removeLibrary( final IPath libraryPath );
  void removeLibraries( final IPath[] pathes );
}
