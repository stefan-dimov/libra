/*******************************************************************************
 *    Copyright (c) 2010 Eteration A.S. and others.
 *    All rights reserved. This program and the accompanying materials
 *    are made available under the terms of the Eclipse Public License v1.0
 *    which accompanies this distribution, and is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 *    
 *     Contributors:
 *        IBM Corporation - initial API and implementation
 *           - This code is based on WTP SDK frameworks and Tomcat Server Adapters
 *           org.eclipse.jst.server.core
 *           org.eclipse.jst.server.ui
 *           
 *       Naci Dai and Murat Yener, Eteration A.S. 
 *******************************************************************************/
package org.eclipse.libra.framework.ui.internal;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.libra.framework.core.FrameworkInstanceDelegate;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.core.model.PublisherDelegate;


public class ModulePublisherDelegate extends PublisherDelegate {
	
	public IStatus execute(int kind, IProgressMonitor monitor, IAdaptable info) throws CoreException {
		// this publisher only runs when there is a UI
		if (info == null)
			return Status.OK_STATUS;
		
		final Shell shell = (Shell) info.getAdapter(Shell.class);
		if (shell == null)
			return Status.OK_STATUS;
		
		IServer server = (IServer) getTaskModel().getObject(TaskModel.TASK_SERVER);
		FrameworkInstanceDelegate runtimeInstance = (FrameworkInstanceDelegate) server.loadAdapter(FrameworkInstanceDelegate.class, monitor);
		
		List modules = (List) getTaskModel().getObject(TaskModel.TASK_MODULES);
		int size = modules.size();
		for (int i = 0; i < size; i++) {
			IModule[] module = (IModule[]) modules.get(i);
			final IModule m = module[module.length - 1];
			if (m != null) {
				// TODO OSAMI  Module Publish Starts Here
			}
		}
		
		return Status.OK_STATUS;
	}


}