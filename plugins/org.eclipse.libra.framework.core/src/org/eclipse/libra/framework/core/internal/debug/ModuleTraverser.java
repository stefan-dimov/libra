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
package org.eclipse.libra.framework.core.internal.debug;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.libra.framework.core.Trace;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.server.core.IModule;



public class ModuleTraverser {

	/**
     * Scans the module using the specified visitor.
     * 
     * @param module module to traverse
     * @param visitor visitor to handle resources
     * @param monitor a progress monitor
     * @throws CoreException
     */
    public static void traverse(IModule module, IModuleVisitor visitor,
            IProgressMonitor monitor) throws CoreException {
        if (module == null || module.getModuleType() == null)
            return;

        //String typeId = module.getModuleType().getId();
        IVirtualComponent component = ComponentCore.createComponent(module.getProject());

        if (component == null) {
            // can happen if project has been closed
            Trace.trace(Trace.WARNING, "Unable to create component for module "
                    + module.getName());
            return;
        }

        traverseOSGIComponent(component, visitor, monitor);
        
    }

 

    private static void traverseOSGIComponent(IVirtualComponent component,
            IModuleVisitor visitor, IProgressMonitor monitor)
            throws CoreException {

        visitor.visitWebComponent(component);

        visitor.endVisitWebComponent(component);
    }
}
