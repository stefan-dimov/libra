/*******************************************************************************
 * Copyright (c) 2010, 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Kaloyan Raev (SAP AG) - initial API and implementation
 *******************************************************************************/
package org.eclipse.libra.facet;

import org.eclipse.core.databinding.observable.Realm;

class OSGiBundleRealm {
	
	private static Realm realm;
	
	public static Realm getRealm() {
		if (realm == null) {
			realm = new SimpleRealm();
		}
		return realm;
	}
	
	private static class SimpleRealm extends Realm {

		@Override
		public boolean isCurrent() {
			return true;
		}
		
	}

}
