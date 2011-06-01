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

package org.eclipse.libra.framework.ui;


/**
 * Constant ids for context help.
 */
public interface ContextIds {
	public static final String FRAMEWORK_INSTANCE_EDITOR = FrameworkUIPlugin.PLUGIN_ID + ".teig0000";
	public static final String FRAMEWORK_INSTANCE_EDITOR_TEST_ENVIRONMENT = FrameworkUIPlugin.PLUGIN_ID + ".teig0002";
	public static final String FRAMEWORK_INSTANCE_EDITOR_SECURE = FrameworkUIPlugin.PLUGIN_ID + ".teig0004";
	public static final String FRAMEWORK_INSTANCE_EDITOR_DEBUG_MODE = FrameworkUIPlugin.PLUGIN_ID + ".teig0006";

	public static final String CONFIGURATION_EDITOR_MODULES = FrameworkUIPlugin.PLUGIN_ID + ".tecw0000";
	public static final String CONFIGURATION_EDITOR_MODULES_LIST = FrameworkUIPlugin.PLUGIN_ID + ".tecw0002";
	public static final String CONFIGURATION_EDITOR_MODULES_ADD_PROJECT = FrameworkUIPlugin.PLUGIN_ID + ".tecw0004";
	public static final String CONFIGURATION_EDITOR_MODULES_ADD_EXTERNAL = FrameworkUIPlugin.PLUGIN_ID + ".tecw0006";
	public static final String CONFIGURATION_EDITOR_MODULES_EDIT = FrameworkUIPlugin.PLUGIN_ID + ".tecw0008";
	public static final String CONFIGURATION_EDITOR_MODULES_REMOVE = FrameworkUIPlugin.PLUGIN_ID + ".tecw0010";

	public static final String CONFIGURATION_EDITOR_MODULE_DIALOG = FrameworkUIPlugin.PLUGIN_ID + ".tdwm0000";
	public static final String CONFIGURATION_EDITOR_MODULE_DIALOG_PROJECT = FrameworkUIPlugin.PLUGIN_ID + ".tdpr0002";
	public static final String CONFIGURATION_EDITOR_MODULE_DIALOG_PATH = FrameworkUIPlugin.PLUGIN_ID + ".tdpr0004";
	public static final String CONFIGURATION_EDITOR_MODULE_DIALOG_DOCBASE = FrameworkUIPlugin.PLUGIN_ID + ".tdpr0006";
	public static final String CONFIGURATION_EDITOR_MODULE_DIALOG_RELOAD = FrameworkUIPlugin.PLUGIN_ID + ".tdpr0008";

	public static final String CONFIGURATION_EDITOR_MAPPINGS = FrameworkUIPlugin.PLUGIN_ID + ".tecm0000";
	public static final String CONFIGURATION_EDITOR_MAPPINGS_LIST = FrameworkUIPlugin.PLUGIN_ID + ".tecm0002";
	public static final String CONFIGURATION_EDITOR_MAPPINGS_ADD = FrameworkUIPlugin.PLUGIN_ID + ".tecm0004";
	public static final String CONFIGURATION_EDITOR_MAPPINGS_EDIT = FrameworkUIPlugin.PLUGIN_ID + ".tecm0006";
	public static final String CONFIGURATION_EDITOR_MAPPINGS_REMOVE = FrameworkUIPlugin.PLUGIN_ID + ".tecm0008";

	public static final String CONFIGURATION_EDITOR_MAPPING_DIALOG = FrameworkUIPlugin.PLUGIN_ID + ".tdmm0000";
	public static final String CONFIGURATION_EDITOR_MAPPING_DIALOG_TYPE = FrameworkUIPlugin.PLUGIN_ID + ".tdmm0002";
	public static final String CONFIGURATION_EDITOR_MAPPING_DIALOG_EXTENSION = FrameworkUIPlugin.PLUGIN_ID + ".tdmm0004";

	public static final String CONFIGURATION_EDITOR_PORTS = FrameworkUIPlugin.PLUGIN_ID + ".tecp0000";
	public static final String CONFIGURATION_EDITOR_PORTS_LIST = FrameworkUIPlugin.PLUGIN_ID + ".tecp0002";
		
	public static final String FRAMEWORK_COMPOSITE = FrameworkUIPlugin.PLUGIN_ID + ".twnr0000";

	public static final String FRAMEWORK_INSTANCE_CLEAN_WORK_DIR = FrameworkUIPlugin.PLUGIN_ID + ".tvcp0000";
	public static final String FRAMEWORK_INSTANCE_CLEAN_WORK_DIR_TERMINATE = FrameworkUIPlugin.PLUGIN_ID + ".tvcp0001";
}
