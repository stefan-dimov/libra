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
import org.eclipse.core.runtime.IPath;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;

/**
 * Visitor interface to process module components
 */
public interface IModuleVisitor {

	/**
	 * Process web component
	 * @param component web component to process
	 * @throws CoreException
	 */
	void visitWebComponent(IVirtualComponent component) throws CoreException;

	/**
	 * Post process web component
	 * @param component web component to process
	 * @throws CoreException
	 */
	void endVisitWebComponent(IVirtualComponent component) throws CoreException;

	/**
	 * Process archive component.
	 * @param runtimePath path for component at runtime
	 * @param workspacePath path to component in workspace
	 */
	void visitArchiveComponent(IPath runtimePath, IPath workspacePath);

	/**
	 * Process dependent component.
	 * @param runtimePath path for component at runtime
	 * @param workspacePath path to component in workspace
	 */
	void visitDependentComponent(IPath runtimePath, IPath workspacePath);

	/**
	 * Process web resource.
	 * @param runtimePath path for resource at runtime
	 * @param workspacePath path to resource in workspace
	 */
	void visitWebResource(IPath runtimePath, IPath workspacePath);

	/**
	 * Process a content resource from dependent component.
	 * @param runtimePath path for resource at runtime
	 * @param workspacePath path to resource in workspace
	 */
	void visitDependentContentResource(IPath runtimePath, IPath workspacePath);

	/**
	 * Process EAR resource.
	 * @param runtimePath path for resource at runtime
	 * @param workspacePath path to resource in workspace
	 */
	void visitEarResource(IPath runtimePath, IPath workspacePath);

	/**
	 * Post process EAR resource.
	 * @param component EAR component to process
	 * @throws CoreException 
	 */
	void endVisitEarComponent(IVirtualComponent component) throws CoreException;
}
