/******************************************************************************* 
* Copyright (c) 2010 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
*******************************************************************************/ 
package org.eclipse.libra.warproducts.ui.newwizard;

import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.libra.warproducts.core.*;
import org.eclipse.libra.warproducts.ui.WARProductConstants;
import org.eclipse.pde.core.plugin.TargetPlatform;
import org.eclipse.pde.internal.core.iproduct.*;
import org.eclipse.pde.internal.core.iproduct.IProduct;
import org.eclipse.pde.internal.launching.launcher.BundleLauncherHelper;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.launching.IPDELauncherConstants;


public class WARProductFromConfigOperation 
  extends BaseWARProductCreationOperation 
{

  private IContainer productParent;
  private ILaunchConfiguration launchConfig;

  public WARProductFromConfigOperation( final IFile file, 
                                        final ILaunchConfiguration config )
  {
    super( file );  
    productParent = file.getParent();
    this.launchConfig = config;
  }
  
  protected void initializeProduct( final IProduct product ) {
    internalInitializeProduct( product );
    InfrastructureCreator creator = new InfrastructureCreator( productParent );
    createWebInfContent( creator );
    if( product instanceof WARProduct ) {
      WARProduct warProduct = ( WARProduct )product;
      warProduct.addLaunchIni( creator.getLaunchIniPath() );
      warProduct.addWebXml( creator.getWebXmlPath() );
      WARProductInitializer initializer 
        = new WARProductInitializer( warProduct );
      initializer.initialize();
    }
  }

  protected void internalInitializeProduct( final IProduct product ) {
    if( launchConfig != null ) {
      try {
        IProductModelFactory factory = product.getModel().getFactory();
        handleApplication( product, factory );
        handleJRE( product );
        handlePluginModel( product, factory );
        handleVMArguments( product, factory );
      } catch( final CoreException e ) {
        PDEPlugin.logException( e );
      }
    }
  }
  
  private void handleApplication( final IProduct product,
                                  final IProductModelFactory factory )
  throws CoreException
  {
    boolean useProduct 
      = launchConfig.getAttribute( IPDELauncherConstants.USE_PRODUCT, false );
    if( useProduct ) {
      String id 
        = launchConfig.getAttribute( IPDELauncherConstants.PRODUCT, "" ); //$NON-NLS-1$
      if( !id.equals( "" ) ) { //$NON-NLS-1$
        initializeProductInfo( factory, product, id );
      }
    } else {
      String application = IPDELauncherConstants.APPLICATION;
      String defaultApplication = TargetPlatform.getDefaultApplication();
      String appName 
        = launchConfig.getAttribute( application, defaultApplication );
      product.setApplication( appName );
    }
  }
  
  private void handleJRE( final IProduct product ) throws CoreException {
    // Set JRE info from information from the launch configuration
    String JreContainerPathName 
      = IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH;
    String jreString = launchConfig.getAttribute( JreContainerPathName, "" ); //$NON-NLS-1$
    if( !jreString.equals( "" ) ) { //$NON-NLS-1$
      IPath jreContainerPath = new Path( jreString );
      IJREInfo jreInfo = product.getJREInfo();
      if( jreInfo == null ) {
        jreInfo = product.getModel().getFactory().createJVMInfo();
      }
      jreInfo.setJREContainerPath( TargetPlatform.getOS(), jreContainerPath );
      product.setJREInfo( jreInfo );
    }
  }

  private void handlePluginModel( final IProduct product,
                                  final IProductModelFactory factory )
    throws CoreException
  {
    // fetch the plug-ins models
    String workspaceId = IPDELauncherConstants.SELECTED_WORKSPACE_PLUGINS;
    String targetId = IPDELauncherConstants.SELECTED_TARGET_PLUGINS;
    String configType = launchConfig.getType().getIdentifier();
    if( configType.equals( IPDELauncherConstants.OSGI_CONFIGURATION_TYPE )
        || configType.equals( WARProductConstants.RAP_LAUNCH_CONFIG_TYPE ) )
    {
      workspaceId = IPDELauncherConstants.WORKSPACE_BUNDLES;
      targetId = IPDELauncherConstants.TARGET_BUNDLES;
    }
    Set set = new HashSet();
    Map map = BundleLauncherHelper.getWorkspaceBundleMap( launchConfig,
                                                          set,
                                                          workspaceId );
    map.putAll( BundleLauncherHelper.getTargetBundleMap( launchConfig,
                                                         set,
                                                         targetId ) );
    addPlugins( factory, product, map );
    handleDefaultConfig( product, factory );
  }

  private void handleDefaultConfig( final IProduct product,
                                    final IProductModelFactory factory )
    throws CoreException
  {
    String configDefault = IPDELauncherConstants.CONFIG_GENERATE_DEFAULT;
    boolean useDefault = launchConfig.getAttribute( configDefault, true );
    if( useDefault ) {
      super.initializeProduct( product );
    } else {
      handleTemplatePath( product, factory );
    }
  }

  private void handleTemplatePath( final IProduct product,
                                   final IProductModelFactory factory )
    throws CoreException
  {
    String templateLocation = IPDELauncherConstants.CONFIG_TEMPLATE_LOCATION;
    String path = launchConfig.getAttribute( templateLocation, "/" );  //$NON-NLS-1$
    IWorkspace workspace = PDEPlugin.getWorkspace();
    IWorkspaceRoot root = workspace.getRoot();
    IContainer container = root.getContainerForLocation( new Path( path ) );
    if( container != null ) {
      IConfigurationFileInfo info = factory.createConfigFileInfo();
      info.setUse( null, "custom" ); //$NON-NLS-1$
      info.setPath( null, container.getFullPath().toString() );
      product.setConfigurationFileInfo( info );
    } else {
      super.initializeProduct( product );
    }
  }

  private void handleVMArguments( final IProduct product,
                                  final IProductModelFactory factory )
  throws CoreException
  {
    // set vm and program arguments from the launch configuration
    String vmArgumentsName 
      = IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS;
    String vmargs = launchConfig.getAttribute( vmArgumentsName, "" ); //$NON-NLS-1$
    String programArgumentsName 
      = IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS;
    String programArgs = launchConfig.getAttribute( programArgumentsName, "" ); //$NON-NLS-1$
    if( !vmargs.equals( "" ) || !programArgs.equals( "" ) ) { //$NON-NLS-1$ //$NON-NLS-2$
      IArgumentsInfo arguments = product.getLauncherArguments();
      if( arguments != null ) {
        arguments = factory.createLauncherArguments();
        if( !vmargs.equals( "" ) ) { //$NON-NLS-1$
          arguments.setVMArguments( vmargs, IArgumentsInfo.L_ARGS_ALL );
        }
        if( !programArgs.equals( "" ) ) { //$NON-NLS-1$
          arguments.setProgramArguments( programArgs, IArgumentsInfo.L_ARGS_ALL );
        }
        product.setLauncherArguments( arguments );
      }
    }
  }
  
}
