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

public enum OSGiBundleFacetUninstallStrategy {
	
	FACET_ONLY("Uninstall the facet only and keep the plugin nature"), 
	FACET_AND_PLUGIN_NATURE_BUT_NOT_MANIFEST("Uninstall the facet and the plugin nature, but keep the MANIFEST.MF"), 
	FACET_AND_PLUGIN_NATURE_AND_MANIFEST("Uninstall the facet and the plugin nature, and remove the MANIFEST.MF");
	
	private final String description;
	
	OSGiBundleFacetUninstallStrategy(String description) {
		this.description = description;
	}
	
	public String description() {
		return description;
	}
	
	public static OSGiBundleFacetUninstallStrategy defaultStrategy() {
		return FACET_ONLY;
	}

}
