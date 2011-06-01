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
package org.eclipse.libra.framework.equinox;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.libra.framework.core.Trace;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.core.plugin.TargetPlatform;
import org.eclipse.wst.server.core.IModule;
import org.osgi.framework.Version;


public class EquinoxHandler implements IEquinoxVersionHandler {

	public IStatus verifyInstallPath(IPath location) {
		boolean isFound = false;
		if (location.toFile().exists()) {
			File[] files = location.toFile().listFiles();
			for (File file : files) {
				if (file.getName().indexOf("org.eclipse.osgi") > -1) {
					isFound = true;
					break;
				}
			}
			if (isFound) {
				return Status.OK_STATUS;
			}
		}
		return new Status(IStatus.ERROR, EquinoxPlugin.PLUGIN_ID, 0,
				Messages.warningCantReadConfig, null);
	}

	public String getFrameworkClass() {
		return "org.eclipse.core.runtime.adaptor.EclipseStarter";
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List getFrameworkClasspath(IPath installPath, IPath configPath) {

		List cp = new ArrayList();
		if (installPath.toFile().exists()) {
			File[] files = installPath.toFile().listFiles();
			for (File file : files) {
				if (file.getName().indexOf("org.eclipse.osgi") > -1) {
					IPath path = installPath
							.append(file.getName());
					cp.add(JavaRuntime.newArchiveRuntimeClasspathEntry(path));
				}
			}
		}

		return cp;
	}

	public String[] getFrameworkProgramArguments(IPath configPath, boolean debug,
			boolean starting) {

		ArrayList<String> programArgs = new ArrayList<String>();
		programArgs.add("-dev");
		programArgs.add(configPath.makeAbsolute().toOSString()
				+ "/dev.properties"); //$NON-NLS-1$
		programArgs.add("-configuration");
		programArgs.add(configPath.makeAbsolute().toOSString()); //$NON-NLS-1$
		if (debug) {
			programArgs.add("-debug"); //$NON-NLS-1$
		}
		programArgs.add("-os"); //$NON-NLS-1$
		programArgs.add(TargetPlatform.getOS());
		programArgs.add("-ws"); //$NON-NLS-1$
		programArgs.add(TargetPlatform.getWS());
		programArgs.add("-arch"); //$NON-NLS-1$
		programArgs.add(TargetPlatform.getOSArch());
		programArgs.add("-console"); //$NON-NLS-1$

		return (String[]) programArgs.toArray(new String[programArgs.size()]);
	}

	public String[] getExcludedFrameworkProgramArguments(boolean debug,
			boolean starting) {
		return null;
	}

	public String[] getFrameworkVMArguments(IPath installPath, IPath configPath,
			IPath deployPath, boolean isTestEnv) {
		// TODO Murat
		String configPathStr = deployPath.makeAbsolute().toOSString();
		String vmArgs = "-D32 -Declipse.ignoreApp=true -Dosgi.noShutdown=true"; //$NON-NLS-1$ //$NON-NLS-2$

		return new String[] { vmArgs };
	}

	public IStatus canAddModule(IModule module) {
		String id = module.getModuleType().getId();
		// String version = module.getModuleType().getVersion();
		if ("osgi.bundle".equals(id))
			return Status.OK_STATUS;

		return new Status(IStatus.ERROR, EquinoxPlugin.PLUGIN_ID, 0,
				Messages.errorNotBundle, null);
	}

	public IStatus prepareFrameworkInstanceDirectory(IPath baseDir) {
		return Status.OK_STATUS;// TomcatVersionHelper.createCatalinaInstanceDirectory(baseDir);
	}

	public IStatus prepareDeployDirectory(IPath deployPath) {

		if (Trace.isTraceEnabled())
			Trace.trace(Trace.FINER, "Creating runtime directory at "
					+ deployPath.toOSString());

		// TODO Murat
		// Prepare a felix directory structure
		File temp = deployPath.append("plugins").toFile();
		if (!temp.exists())
			temp.mkdirs();
		temp = deployPath.append("auto").toFile();
		if (!temp.exists())
			temp.mkdirs();
		temp = deployPath.append("cache").toFile();
		if (!temp.exists())
			temp.mkdirs();

		return Status.OK_STATUS;
	}

	public boolean supportsServeModulesWithoutPublish() {
		return true;
	}

	private String getEquinoxJar(String path) {
		File file = null;
		file = new File(path, "");
		String[] children = file.list();
		for (String string : children) {
			if (string.lastIndexOf("org.eclipse.osgi") > -1) {
				System.out.println(string);
				return "/" + string;
			}
		}
		return null;
	}

	public void prepareFrameworkConfigurationFile(IPath configPath,
			String workspaceBundles, String kernelBundles2) {
		Properties properties = new Properties();
		properties.setProperty(
				"osgi.instance.area.default",
				"file:"
						+ configPath.toPortableString().substring(
								0,
								configPath.toPortableString().indexOf(
										".metadata")));

		String[] krBundles = kernelBundles2.split(" ");

		properties.put("osgi.framework", "file:" + krBundles[0]);
		properties.setProperty("osgi.configuration.cascaded", "false"); //$NON-NLS-1$ //$NON-NLS-2$
		int start = 4;
		properties.put(
				"osgi.bundles.defaultStartLevel", Integer.toString(start)); //$NON-NLS-1$
		properties.setProperty(
				"org.eclipse.equinox.simpleconfigurator.configUrl", "file:"
						+ configPath.toPortableString() + "/bundles.info");
		String[] bundless = workspaceBundles.split(" ");
		String propertyInstall = "";
		try {
			// Create file
			FileWriter fstream = new FileWriter(configPath.toPortableString()
					+ "/bundles.info");
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("#version=1\n");
			for (String bundle : bundless) {
				if (bundle.indexOf("@") != -1)
					bundle = bundle.substring(0, bundle.indexOf("@"));
				IPluginModelBase[] models = PluginRegistry.getWorkspaceModels();
				String modelId = "";
				for (IPluginModelBase iPluginModelBase : models) {
					if (bundle
							.indexOf(iPluginModelBase.getPluginBase().getId()) > -1) {
						modelId = iPluginModelBase.getPluginBase().getId();
						Version version = iPluginModelBase
								.getBundleDescription().getVersion();
						out.write(modelId + "," + version + ",file:"
								+ iPluginModelBase.getInstallLocation()
								+ ",4,true\n");
					}
				}
			}
			out.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}

//		IPluginModelBase model = PluginRegistry
//				.findModel("org.eclipse.equinox.simpleconfigurator");
//		propertyInstall = model.getInstallLocation() + "@1:start,";
//
//		// IPluginModelBase modeltest = PluginRegistry.findModel("osgi.cmpn");
//
//		krBundles[0] = null;
		for (String string : krBundles) {
			if (string != null && !(string.trim().equalsIgnoreCase(""))) {
				File file = new File(string.substring(string.indexOf("/")));
				if (file.isFile()){
					if (string.indexOf(".jar") > -1) {
						propertyInstall += string + "@2:start, ";

					}
				}else{
				for (String string2 : file.list()) {
					if (string2.indexOf(".jar") > -1) {
						propertyInstall += string + string2 + "@2:start, ";

					}
				}
				}
			}

		}

		properties.setProperty("osgi.bundles", propertyInstall);

		properties.put("eclipse.ignoreApp", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		properties.put("osgi.noShutdown", "true"); //$NON-NLS-1$ //$NON-NLS-2$

		try {
			properties.store(
					new FileOutputStream(configPath.append("config.ini")
							.makeAbsolute().toFile()), "## AUTO GENERATED ##");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
