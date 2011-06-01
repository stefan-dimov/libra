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
package org.eclipse.libra.framework.knopflerfish.internal;

import org.eclipse.core.runtime.IPath;
import org.eclipse.libra.framework.core.OSGIFrameworkInstanceBehaviorDelegate;
import org.eclipse.libra.framework.core.publish.PublishHelper;



public class KnopflerfishConfigurationPublishHelper extends PublishHelper{
	public OSGIFrameworkInstanceBehaviorDelegate behavior;

	public KnopflerfishConfigurationPublishHelper(OSGIFrameworkInstanceBehaviorDelegate behavior) {
		this.behavior = behavior;
	}
	
	protected IPath getPublishFolder() {
		IPath configPath = behavior.getBaseDirectory();
		configPath = configPath.append("plugins").makeAbsolute();
		return configPath;
	}


}