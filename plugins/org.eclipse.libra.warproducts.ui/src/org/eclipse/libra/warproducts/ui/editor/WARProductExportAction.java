/******************************************************************************* 
* Copyright (c) 2010, 2011 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Holger Staudacher - initial API and implementation
*******************************************************************************/
package org.eclipse.libra.warproducts.ui.editor;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.libra.warproducts.ui.exportwizard.ExportWARProductWizard;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.editor.PDEFormEditor;
import org.eclipse.pde.internal.ui.wizards.ResizableWizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class WARProductExportAction extends Action {

  private IProject project;
  private IStructuredSelection selection;

  public WARProductExportAction( final PDEFormEditor editor ) {
    IResource resource = null;
    if( editor != null ) {
      IModel model = ( IModel )editor.getAggregateModel();
      resource = model.getUnderlyingResource();
      if( resource != null ) {
        selection = new StructuredSelection( resource );
      } else {
        selection = new StructuredSelection();
      }
      project = editor.getCommonProject();
    }
  }

  public WARProductExportAction( final IStructuredSelection selection ) {
    this.selection = selection;
    project = null;
  }

  public void run() {
    ExportWARProductWizard wizard = new ExportWARProductWizard( project );
    wizard.init( PlatformUI.getWorkbench(), selection );
    Shell shell = PDEPlugin.getActiveWorkbenchShell();
    WizardDialog wd = new ResizableWizardDialog( shell, wizard );
    wd.create();
    notifyResult( wd.open() == Window.OK );
  }
}
