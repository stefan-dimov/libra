/*******************************************************************************
 * Copyright (c) 2010 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors:
 * EclipseSource - initial API and implementation
 *******************************************************************************/
package org.eclipse.libra.warproducts.ui.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.libra.warproducts.core.validation.Validator;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.HostSpecification;
import org.eclipse.pde.internal.core.TargetPlatformHelper;
import org.eclipse.pde.internal.core.iproduct.IProduct;
import org.eclipse.pde.internal.core.iproduct.IProductModel;
import org.eclipse.pde.internal.core.iproduct.IProductModelFactory;
import org.eclipse.pde.internal.core.iproduct.IProductPlugin;
import org.eclipse.pde.internal.ui.editor.PDEFormEditor;
import org.eclipse.pde.internal.ui.editor.PDEFormPage;
import org.eclipse.pde.internal.ui.editor.product.PluginSection;
import org.eclipse.pde.internal.ui.search.dependencies.DependencyCalculator;
import org.eclipse.swt.widgets.Composite;

public class PluginSectionExtended extends PluginSection {

  private static final int BUTTON_ADD_REQUIRED = 2;

  public PluginSectionExtended( final PDEFormPage formPage,
                                final Composite parent )
  {
    super( formPage, parent );
  }

  protected void buttonSelected( int index ) {
    if( index == BUTTON_ADD_REQUIRED ) {
      PDEFormEditor pdeEditor = getPage().getPDEEditor();
      IProductModel model = ( IProductModel )pdeEditor.getAggregateModel();
      validHandleAddRequired( model.getProduct().getPlugins(), 
                              includeOptionalDependencies() );
    } else {
      super.buttonSelected( index );
    }
  }

  private void validHandleAddRequired( final IProductPlugin[] plugins,
                                       final boolean includeOptional )
  {
    if( plugins.length > 0 ) {
      ArrayList list = new ArrayList( plugins.length );
      for( int i = 0; i < plugins.length; i++ ) {
        list.add( TargetPlatformHelper.getState()
          .getBundle( plugins[ i ].getId(), null ) );
      }
      DependencyCalculator calculator 
        = new DependencyCalculator( includeOptional );
      calculator.findDependencies( list.toArray() );
      BundleDescription[] bundles 
        = TargetPlatformHelper.getState().getBundles();
      for( int i = 0; i < bundles.length; i++ ) {
        HostSpecification host = bundles[ i ].getHost();
        String compatibilityId = "org.eclipse.ui.workbench.compatibility"; //$NON-NLS-1$
        String bundleName = bundles[ i ].getSymbolicName();
        boolean compatible = compatibilityId.equals( bundleName );
        if( host != null ) {
          String hostName = host.getName();
          boolean containsPluginId = calculator.containsPluginId( hostName );
          if( !compatible && containsPluginId ) {
            calculator.findDependency( bundles[ i ] );
          }
        }
      }
      List dependencies = validateBundleIds( calculator.getBundleIDs() );
      validateBundleIds( dependencies );
      
      IProduct product = plugins[ 0 ].getProduct();
      IProductModelFactory factory = product.getModel().getFactory();
      IProductPlugin[] requiredPlugins 
        = new IProductPlugin[ dependencies.size() ];
      for( int i = 0; i < requiredPlugins.length; i++ ) {
        String id = ( String )dependencies.get( i );
        IProductPlugin plugin = factory.createPlugin();
        plugin.setId( id );
        requiredPlugins[ i ] = plugin;
      }
      product.addPlugins( requiredPlugins );
    }
  }

  private List validateBundleIds( final Collection bundleIDs ) {
    List validBundles = new ArrayList();
    copyBundleIds( bundleIDs, validBundles );
    handleBannedBundles( validBundles );
    handleRequiredBundles( validBundles );
    return validBundles;
  }

  private void copyBundleIds( final Collection bundleIDs, 
                              final List validBundles ) 
  {
    Iterator iter = bundleIDs.iterator();
    while( iter.hasNext() ) {
      validBundles.add( iter.next() );
    }
  }

  private void handleRequiredBundles( final Collection bundleIDs ) {
    String[] requiredBundles = Validator.REQUIRED_BUNDLES;
    for( int i = 0; i < requiredBundles.length; i++ ) {
      String requiredId = requiredBundles[ i ];
      if( !bundleIDs.contains( requiredId ) ) {
        bundleIDs.add( requiredId );
      }
    }
  }

  private void handleBannedBundles( final Collection bundleIDs ) {
    String[] bannedBundles = Validator.BANNED_BUNDLES;
    for( int i = 0; i < bannedBundles.length; i++ ) {
      String bannedId = bannedBundles[ i ];
      if( bundleIDs.contains( bannedId ) ) {
        bundleIDs.remove( bannedId );
      }
    }
  }
}
