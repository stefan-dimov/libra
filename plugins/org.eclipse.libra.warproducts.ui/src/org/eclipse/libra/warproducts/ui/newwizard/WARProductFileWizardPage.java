/*******************************************************************************
 * Copyright (c) 2010 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors:
 * EclipseSource - initial API and implementation
 *******************************************************************************/
package org.eclipse.libra.warproducts.ui.newwizard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.*;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.libra.warproducts.ui.Messages;
import org.eclipse.libra.warproducts.ui.WARProductConstants;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.wizards.PDEWizardNewFileCreationPage;
import org.eclipse.pde.launching.IPDELauncherConstants;
import org.eclipse.pde.ui.launcher.EclipseLaunchShortcut;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.IWorkbenchHelpSystem;

public class WARProductFileWizardPage extends PDEWizardNewFileCreationPage {

  public final static int USE_DEFAULT = 0;
  public final static int USE_LAUNCH_CONFIG = 1;
  private static final String FILE_EXTENSION = "warproduct"; //$NON-NLS-1$   
  
  private Button basicButton;
  private Button launchConfigButton;
  private Combo launchConfigCombo;
  private Group group;

  public WARProductFileWizardPage( final String pageName,
                                   final IStructuredSelection selection )
  {
    super( pageName, selection );
    setDescription( Messages.FileWizardPageNewFile );
    setTitle( Messages.FileWizardPageTitle );
    setFileExtension( FILE_EXTENSION );
  }

  protected void createAdvancedControls( final Composite parent ) {
    group = new Group( parent, SWT.NONE );
    group.setText( Messages.FileWizardPageInit );
    group.setLayout( new GridLayout( 2, false ) );
    group.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
    basicButton = new Button( group, SWT.RADIO );
    GridData gd = new GridData();
    gd.horizontalSpan = 2;
    basicButton.setLayoutData( gd );
    basicButton.setText( Messages.FileWizardPageCreate );
    launchConfigButton = new Button( group, SWT.RADIO );
    String buttonText = Messages.FileWizardPageLaunchConfig;
    launchConfigButton.setText( buttonText );
    launchConfigButton.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( final SelectionEvent e ) {
        launchConfigCombo.setEnabled( launchConfigButton.getSelection() );
      }
    } );
    launchConfigCombo = new Combo( group, SWT.SINGLE | SWT.READ_ONLY );
    launchConfigCombo.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
    launchConfigCombo.setItems( getLaunchConfigurations() );
    initializeState();
  }

  private void initializeState() {
    launchConfigCombo.setEnabled( false );
    if( launchConfigCombo.getItemCount() > 0 ) {
      launchConfigCombo.setText( launchConfigCombo.getItem( 0 ) );
    }
    basicButton.setSelection( true );
  }

  private String[] getLaunchConfigurations() {
    ArrayList list = new ArrayList();
    try {
      String eclipseConf = EclipseLaunchShortcut.CONFIGURATION_TYPE;
      addLaunchConfigToListFromType( list, eclipseConf  );
      // add osgi launch configs to the list
      String osgiConf = IPDELauncherConstants.OSGI_CONFIGURATION_TYPE;
      addLaunchConfigToListFromType( list, osgiConf  );
      // add RAP launch configs to the list
      String rapConf = WARProductConstants.RAP_LAUNCH_CONFIG_TYPE;
      addLaunchConfigToListFromType( list, rapConf );
    } catch( final CoreException e ) {
      PDEPlugin.logException( e );
    }
    String[] launchConfigArray = new String[ list.size() ];
    return ( String[] )list.toArray( launchConfigArray );
  }
  
  private void addLaunchConfigToListFromType( final List list, 
                                              final String type ) 
    throws CoreException 
  {
    ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
    ILaunchConfigurationType configType 
      = manager.getLaunchConfigurationType( type );
    ILaunchConfiguration[] configs 
      = manager.getLaunchConfigurations( configType );
    for( int i = 0; i < configs.length; i++ ) {
      if( !DebugUITools.isPrivate( configs[ i ] ) ) {
        list.add( configs[ i ].getName() );
      }
    }
  }

  public ILaunchConfiguration getSelectedLaunchConfiguration() {
    ILaunchConfiguration result = null;
    if( launchConfigButton.getSelection() ) {
      String configName = launchConfigCombo.getText();
      try {
        ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
        String eclipseConf = EclipseLaunchShortcut.CONFIGURATION_TYPE;
        ILaunchConfigurationType type 
          = manager.getLaunchConfigurationType( eclipseConf );
        String osgiConf = IPDELauncherConstants.OSGI_CONFIGURATION_TYPE;
        ILaunchConfigurationType type2 
          = manager.getLaunchConfigurationType( osgiConf );
        String rapCong = WARProductConstants.RAP_LAUNCH_CONFIG_TYPE;
        ILaunchConfigurationType type3 
          = manager.getLaunchConfigurationType( rapCong );
        ILaunchConfiguration[] configs 
          = manager.getLaunchConfigurations( type );
        ILaunchConfiguration[] configs2 
          = manager.getLaunchConfigurations( type2 );
        ILaunchConfiguration[] configs3 
          = manager.getLaunchConfigurations( type3 );
        ILaunchConfiguration[] configurations 
          = new ILaunchConfiguration[   configs.length
                                      + configs2.length 
                                      + configs3.length ];
        System.arraycopy( configs, 0, configurations, 0, configs.length );
        System.arraycopy( configs2,
                          0,
                          configurations,
                          configs.length,
                          configs2.length );
        System.arraycopy( configs3,
                          0,
                          configurations,
                          configs2.length,
                          configs3.length );
        for( int i = 0; i < configurations.length && result == null; i++ ) {
          if( configurations[ i ].getName().equals( configName )
              && !DebugUITools.isPrivate( configurations[ i ] ) ) 
          {
            result = configurations[ i ];
          }
        }
      } catch( final CoreException e ) {
        PDEPlugin.logException( e );
      }
    }
    return result;
  }

  public int getInitializationOption() {
    int result = USE_LAUNCH_CONFIG;
    if( basicButton.getSelection() ) {
      result = USE_DEFAULT;
    }
    return result;
  }

  public void createControl( final Composite parent ) {
    super.createControl( parent );
    Dialog.applyDialogFont( group );
    IWorkbenchHelpSystem helpSystem = PlatformUI.getWorkbench().getHelpSystem();
    String contextId = WARProductConstants.HELP_CONTEXT_NEWFILE_PAGE;
    helpSystem.setHelp( getControl(), contextId );
  }
  
  protected boolean validatePage() {
    boolean valid =  super.validatePage();
    if( valid ) {
      IPath containerPath = getContainerFullPath();
      valid = validateWarProductLocation( valid, containerPath );
    }
    return valid;
  }

  private boolean validateWarProductLocation( final boolean valid, 
                                              final IPath containerPath )
  {
    boolean result = valid;
    IContainer container = getSelectedContainer( containerPath );
    try {
      IResource[] members = container.members();
      for( int i = 0; i < members.length && valid; i++ ) {
        IResource member = members[ i ];
        String extension = member.getFileExtension();
        if( extension != null 
            && extension.equals( WARProductConstants.FILE_EXTENSION ) ) 
        {
          result = false;
          setErrorMessage( Messages.FileWizardPageError );
        }
      }
    } catch( final CoreException e ) {
      MessageDialog.openError( getShell(), 
                               PDEUIMessages.ProductExportWizard_error, 
                               e.getMessage() );
    }
    return result;
  }

  private IContainer getSelectedContainer( final IPath containerPath ) {
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IContainer container;
    if( containerPath.segmentCount() > 1 ) {
      container = root.getFolder( containerPath );
    } else {
      container = root.getProject( containerPath.toOSString() );
    }
    return container;
  }
  
}
