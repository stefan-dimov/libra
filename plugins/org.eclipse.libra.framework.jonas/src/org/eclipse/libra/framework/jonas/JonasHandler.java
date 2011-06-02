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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.libra.framework.jonas.internal.util.ZipUtil;
import org.eclipse.wst.server.core.IModule;

public class JonasHandler implements IJonasVersionHandler {

	public IStatus verifyInstallPath(IPath location) {

		File f = location.append("conf/jonas.properties").toFile();
		if (f == null || !f.exists())
			return new Status(IStatus.ERROR, JonasPlugin.PLUGIN_ID, 0,
					Messages.warningCantReadConfig, null);
		File[] conf = f.listFiles();
		if (conf != null) {
			int size = conf.length;
			for (int i = 0; i < size; i++) {
				if (!f.canRead())
					return new Status(IStatus.WARNING, JonasPlugin.PLUGIN_ID,
							0, Messages.warningCantReadConfig, null);
			}
		}

		return Status.OK_STATUS;
	}

	public String getFrameworkClass() {
		return "org.ow2.jonas.commands.admin.ClientAdmin";
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List getFrameworkClasspath(IPath installPath, IPath configPath) {

		List cp = new ArrayList();

		IPath binPath = installPath.append("/lib/bootstrap");
		if (binPath.toFile().exists()) {
			IPath path = binPath.append("/felix-launcher.jar");
			cp.add(JavaRuntime.newArchiveRuntimeClasspathEntry(path));
			IPath path2 = binPath.append("/jonas-commands.jar");
			cp.add(JavaRuntime.newArchiveRuntimeClasspathEntry(path2));
			IPath path3 = binPath.append("/jonas-version.jar");
			cp.add(JavaRuntime.newArchiveRuntimeClasspathEntry(path3));
		}

		return cp;
	}

	public String[] getFrameworkProgramArguments(IPath configPath, boolean debug,
			boolean starting) {
		return new String[] { "-start" };
	}

	public String[] getExcludedFrameworkProgramArguments(boolean debug,
			boolean starting) {
		return null;
	}

	public String[] getFrameworkVMArguments(IPath installPath, IPath configPath,
			IPath deployPath, boolean isTestEnv) {

		String configPathStr = deployPath.makeAbsolute().toOSString();
		String vmArgs = "-Dfelix.config.properties=file:" + configPathStr + "/config.properties"; //$NON-NLS-1$ //$NON-NLS-2$
		String jonasRoot = installPath.toOSString();
		String jonasBase = deployPath.append("/jonasbase").toOSString();
		return new String[] {
				"-Djonas.root=" + jonasRoot,
				"-Djonas.base=" + jonasBase,
				"-Dipojo.log.level=ERROR",
				"-Djava.security.policy=" + jonasBase + "/conf/java.policy",
				"-Djava.security.auth.login.config=" + jonasBase
						+ "/conf/jaas.config",
				"-Djava.endorsed.dirs=" + jonasRoot + "/lib/endorsed",
				"-Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB",
				"-Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton",
				"-Dorg.omg.PortableInterceptor.ORBInitializerClass.standard_init=org.jacorb.orb.standardInterceptors.IORInterceptorInitializer",
				"-Dcom.sun.CORBA.ORBDynamicStubFactoryFactoryClass=com.sun.corba.se.impl.presentation.rmi.StubFactoryFactoryStaticImpl",
				"-Djavax.xml.soap.SOAPConnectionFactory=com.sun.xml.messaging.saaj.client.p2p.HttpSOAPConnectionFactory",
				"-Djavax.xml.soap.SOAPFactory=com.sun.xml.messaging.saaj.soap.ver1_1.SOAPFactory1_1Impl",
				"-Djavax.xml.soap.MetaFactory=com.sun.xml.messaging.saaj.soap.SAAJMetaFactoryImpl",
				"-Djavax.xml.soap.MessageFactory=com.sun.xml.messaging.saaj.soap.ver1_1.SOAPMessageFactory1_1Impl",
				"-Djonas.felix.tui.enabled=true", "-Djonas.cache.clean=true" };
	}

	public IStatus canAddModule(IModule module) {
		String id = module.getModuleType().getId();
		// String version = module.getModuleType().getVersion();
		if ("osgi.bundle".equals(id))
			return Status.OK_STATUS;

		return new Status(IStatus.ERROR, JonasPlugin.PLUGIN_ID, 0,
				Messages.errorNotBundle, null);
	}

	public IStatus prepareFrameworkInstanceDirectory(IPath baseDir) {
		return Status.OK_STATUS;// TomcatVersionHelper.createCatalinaInstanceDirectory(baseDir);
	}

	public IStatus prepareDeployDirectory(IPath deployPath) {

		// if (Trace.isTraceEnabled())
		// Trace.trace(Trace.FINER, "Creating runtime directory at "
		// + deployPath.toOSString());
		//
		// // Prepare a felix directory structure
		// File temp = deployPath.append("plugins").toFile();
		// if (!temp.exists())
		// temp.mkdirs();
		// temp = deployPath.append("auto").toFile();
		// if (!temp.exists())
		// temp.mkdirs();
		// temp = deployPath.append("cache").toFile();
		// if (!temp.exists())
		// temp.mkdirs();

		return Status.OK_STATUS;
	}

	public boolean supportsServeModulesWithoutPublish() {
		return true;
	}

	public void prepareFrameworkConfigurationFile(IPath configPath,
			String workspaceBundles, String kernelBundles) {
		// Properties properties = new Properties();
		//
		// properties.setProperty("felix.auto.deploy.dir",
		// configPath.append("auto").makeAbsolute().toPortableString());
		// properties.setProperty("felix.auto.deploy.action", "install,start");
		// properties.setProperty("org.osgi.framework.startlevel.beginning",
		// "2");
		// properties.setProperty("felix.auto.install.1", kernelBundles);
		// properties.setProperty("felix.auto.start.1", kernelBundles);
		// properties.setProperty("felix.auto.install.2", workspaceBundles);
		// properties.setProperty("felix.auto.start.2", workspaceBundles);
		// properties.setProperty("org.osgi.framework.storage", "file:"
		// + configPath.append("auto").makeAbsolute().toPortableString());
		// properties.setProperty("org.osgi.framework.storage.clean",
		// "onFirstInit");
		//
		// try {
		// properties.store(
		// new FileOutputStream(configPath.append("config.properties")
		// .makeAbsolute().toFile()), "## AUTO GENERATED ##");
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
	}

	final int BUFFER = 2048;

	public void createJonasBase(IPath location, String instanceDirectory) {
		try {
			ZipUtil.unzip(
					this.getClass().getResourceAsStream("/jonasbase.zip"),
					new File(instanceDirectory));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
