/******************************************************************************* 
* Copyright (c) 2010, 2011 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Holger Staudacher - initial API and implementation
*******************************************************************************/
package org.eclipse.libra.warproducts.ui.validation;

import java.util.*;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.libra.warproducts.core.IWARProduct;
import org.eclipse.libra.warproducts.core.validation.*;
import org.eclipse.libra.warproducts.ui.Messages;
import org.eclipse.osgi.service.resolver.ResolverError;
import org.eclipse.osgi.service.resolver.VersionConstraint;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.core.TargetPlatformHelper;
import org.eclipse.pde.internal.core.iproduct.IProductPlugin;
import org.eclipse.pde.internal.launching.launcher.LaunchValidationOperation;
import org.eclipse.pde.internal.launching.launcher.ProductValidationOperation;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEPluginImages;

public class WARProductValidateAction extends Action {

  IWARProduct product;
  List listeners;

  public WARProductValidateAction( final IWARProduct product ) {
    super( Messages.ValidateActionTitle, IAction.AS_PUSH_BUTTON );
    setImageDescriptor( PDEPluginImages.DESC_VALIDATE_TOOL );
    this.product = product;
  }
  
  public void addValidationListener( final IValidationListener listener ) {
    if( listeners == null ) {
      listeners = new ArrayList();
    }
    if( !listeners.contains( listener ) ) {
      listeners.add( listener );
    }
  }

  public void run() {
    HashMap map = new HashMap();
    IProductPlugin[] plugins = product.getPlugins();
    for( int i = 0; i < plugins.length; i++ ) {
      String id = plugins[ i ].getId();
      addBundleIfExisting( map, id );
    }
    validate( map );
  }

  private void addBundleIfExisting( final HashMap map, final String id ) {
    if( id != null && !map.containsKey( id ) ) {
      IPluginModelBase model = PluginRegistry.findModel( id );
      if( bundleExist( id, model ) ) {
        map.put( id, model );
      }
    }
  }
  
  private boolean bundleExist( final String id, final IPluginModelBase model ) {
    boolean result = false;
    if( model != null ) {
      boolean matchesCurrentEnvironment 
      = TargetPlatformHelper.matchesCurrentEnvironment( model );
      if( matchesCurrentEnvironment ) {
        result = true;
      }
    }
    return result;
  }

  private void validate( final HashMap map ) {
    try {
      IPluginModelBase[] baseModel = new IPluginModelBase[ map.size() ];
      Object[] array = map.values().toArray( baseModel );
      IPluginModelBase[] models = ( IPluginModelBase[] )array;
      LaunchValidationOperation operation 
        = new ProductValidationOperation( models );
      operation.run( new NullProgressMonitor() );
      verifyResult( operation );
    } catch( final CoreException e ) {
      PDEPlugin.logException( e );
    }
  }

  private void verifyResult( final LaunchValidationOperation operation )
  {
    Map errors = operation.getInput();
    varifyPDEErrors( errors );
    validateWarContent( errors );
    notifyListeners( errors );
  }

  private void notifyListeners( final Map errors ) {
    if( listeners != null ) {
      for( int i = 0; i < listeners.size(); i++ ) {
        IValidationListener listener 
          = ( IValidationListener )listeners.get( i );
        listener.validationFinished( errors );
      }
    }
  }

  private void varifyPDEErrors( final Map map ) {
    Object[] keys = new Object[ map.size() ];
    map.keySet().toArray( keys );
    for( int i = 0; i < keys.length; i++ ) {
      Object currentKey = keys[ i ];
      ResolverError[] errors = ( ResolverError[] )map.get( currentKey );
      ResolverError[] validErrors = validateErrors( errors );
      map.remove( currentKey );
      if( validErrors.length > 0 ) {
        map.put( currentKey, validErrors );
      }
    }
  }
  
  private ResolverError[] validateErrors( final ResolverError[] errors ) {
    List validErrors = new ArrayList();
    for( int i = 0; i < errors.length; i++ ) {
      ResolverError error = errors[ i ];
      VersionConstraint constraint = error.getUnsatisfiedConstraint();
      if( constraint != null ) {
        String unresolvedBundleId = constraint.getName();
        if( !isBanned( unresolvedBundleId ) ) {
          validErrors.add( error );
        }
      }
    }
    ResolverError[] result = new ResolverError[ validErrors.size() ];
    validErrors.toArray( result );
    return result;
  }

  private boolean isBanned( final String unresolvedBundleId ) {
    boolean result = false;
    String[] bannedBundles = Validator.BANNED_BUNDLES;
    for( int i = 0; i < bannedBundles.length && !result; i++ ) {
      if( unresolvedBundleId.indexOf( bannedBundles[ i ] ) != -1 ) {
        result = true;
      }
    }
    return result;
  }

  private void validateWarContent( final Map map ) {
    Validator validator = new Validator( product );
    Validation validation = validator.validate();
    if( !validation.isValid() ) {
      handleWARValiadtionErrors( map, validation.getErrors() );
    }
  }

  private void handleWARValiadtionErrors( final Map map, 
                                          final ValidationError[] errors ) 
  {
    String key = Messages.ValidateAction;
    map.put( key, errors );
  }
  
}
