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
package org.eclipse.libra.framework.jonas;

import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.wst.server.core.IModule;

public interface IJonasVersionHandler {

	public abstract boolean supportsServeModulesWithoutPublish();

	public abstract IStatus prepareDeployDirectory(IPath deployPath);

	public abstract IStatus prepareFrameworkInstanceDirectory(IPath baseDir);

	public abstract IStatus canAddModule(IModule module);

	public abstract String[] getFrameworkVMArguments(IPath installPath, IPath configPath,
			IPath deployPath, boolean isTestEnv);

	public abstract String[] getExcludedFrameworkProgramArguments(boolean debug,
			boolean starting);

	public abstract String[] getFrameworkProgramArguments(IPath configPath,
			boolean debug, boolean starting);

	@SuppressWarnings("rawtypes")
	public abstract List getFrameworkClasspath(IPath installPath, IPath configPath);

	public abstract String getFrameworkClass();

	public abstract IStatus verifyInstallPath(IPath location);

	public abstract void prepareFrameworkConfigurationFile(IPath confDir,String workspaceBundles, String kernelBundles);

	public abstract void createJonasBase(IPath location, String instanceDirectory);

}
