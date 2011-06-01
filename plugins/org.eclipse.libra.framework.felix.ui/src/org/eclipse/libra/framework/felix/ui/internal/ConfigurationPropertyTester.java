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
package org.eclipse.libra.framework.felix.ui.internal;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.libra.framework.core.FrameworkInstanceDelegate;
import org.eclipse.libra.framework.felix.IFelixFrameworkInstance;
import org.eclipse.wst.server.core.IServerAttributes;

/**
 * 
 */
public class ConfigurationPropertyTester extends PropertyTester {
	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		try {
			IServerAttributes server = (IServerAttributes) receiver;
			FrameworkInstanceDelegate felix = (FrameworkInstanceDelegate) server.loadAdapter(IFelixFrameworkInstance.class, null);
			if (felix != null)
				return felix != null;
		} catch (Exception e) {
			// ignore
		}
		return false;
	}
}