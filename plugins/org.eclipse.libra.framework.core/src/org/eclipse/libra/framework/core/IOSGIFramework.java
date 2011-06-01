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
package org.eclipse.libra.framework.core;

import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.launching.IVMInstall;

public interface IOSGIFramework {
	/**
	 * Return the VM install (installed JRE) that this runtime is using.
	 * 
	 * @return the current VM install
	 */
	public IVMInstall getVMInstall();

	/**
	 * Returns <code>true</code> if the runtime is using the default JRE.
	 * 
	 * @return <code>true</code> if the runtime is using the default JRE,
	 *    and <code>false</code> otherwise
	 * @since 2.0
	 */
	public boolean isUsingDefaultJRE();
	
	/**
	 * Returns the runtime classpath that is used by this runtime.
	 * 
	 * @return the runtime classpath
	 */
	@SuppressWarnings("rawtypes")
	public List getFrameworkClasspath(IPath configPath);
	
	public IStatus validate() ;
}
