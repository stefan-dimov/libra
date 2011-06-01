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
package org.eclipse.libra.framework.felix;

import org.eclipse.osgi.util.NLS;
/**
 * Translated messages.
 */
public class Messages extends NLS {

	public static String errorInstallDirTrailingSlash = null;
	public static String errorJRE;
	public static String warningCantReadConfig;
	public static String warningCantReadBundle;
	public static String errorNotBundle;
	public static String errorOSGiBundlesOnly;
	public static String errorNoRuntime;
	
	public static String publishServerTask;
	public static String errorConfigurationProjectClosed;
	public static String errorNoConfiguration;
	public static String loadingTask;
	public static String errorCouldNotLoadConfiguration;
	public static String configurationEditorActionAddOsgiModule;

	static {
		NLS.initializeMessages(FelixPlugin.PLUGIN_ID + ".Messages", Messages.class);
	}
}