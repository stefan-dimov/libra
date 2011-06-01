/*******************************************************************************
 *   Copyright (c) 2010 Eteration A.S. and others.
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *  
 *   Contributors:
 *      Naci Dai and Murat Yener, Eteration A.S. - Initial API and implementation
 *******************************************************************************/
package org.eclipse.libra.framework.felix.internal;

import org.eclipse.libra.framework.core.OSGIFrameworkLocatorDelegate;

public class FelixRuntimeInstanceLocator extends OSGIFrameworkLocatorDelegate {

	protected static final String[] runtimeTypes = new String[] {
	"org.eclipse.libra.framework.felix.runtimeType.20x"};
	
	public static String[] getRuntimeTypes() {
			return runtimeTypes;
	}

}
