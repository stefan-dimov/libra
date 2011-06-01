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
package org.eclipse.libra.framework.core;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.IModulePublishHelper;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;


@SuppressWarnings("restriction")
public abstract class OSGIFrameworkInstanceBehaviorDelegate extends ServerBehaviourDelegate implements IModulePublishHelper {

	private static final String ATTR_STOP = "stop-server";
	// the thread used to ping the server to check for startup
	protected transient PingThread ping = null;
	protected transient IDebugEventSetListener processListener;
	
	
	public abstract  String[] getFrameworkProgramArguments(boolean starting) ;
	public abstract  String[] getExcludedFrameworkProgramArguments(boolean starting);
	public abstract  String[] getFrameworkVMArguments();
	public abstract  String getFrameworkClass() ;
	
	protected static int getNextToken(String s, int start) {
		int i = start;
		int length = s.length();
		char lookFor = ' ';

		while (i < length) {
			char c = s.charAt(i);
			if (lookFor == c) {
				if (lookFor == '"')
					return i + 1;
				return i;
			}
			if (c == '"')
				lookFor = '"';
			i++;
		}
		return -1;
	}

	/**
	 * Merge the given arguments into the original argument string, replacing
	 * invalid values if they have been changed. Special handling is provided if
	 * the keepActionLast argument is true and the last vmArg is a simple
	 * string. The vmArgs will be merged such that the last vmArg is guaranteed
	 * to be the last argument in the merged string.
	 * 
	 * @param originalArg
	 *            String of original arguments.
	 * @param vmArgs
	 *            Arguments to merge into the original arguments string
	 * @param excludeArgs
	 *            Arguments to exclude from the original arguments string
	 * @param keepActionLast
	 *            If <b>true</b> the vmArguments are assumed to be Felix program
	 *            arguments, the last of which is the action to perform which
	 *            must remain the last argument. This only has an impact if the
	 *            last vmArg is a simple string argument, like
	 *            &quot;start&quot;.
	 * @return merged argument string
	 */
	public static String mergeArguments(String originalArg, String[] vmArgs,
			String[] excludeArgs, boolean keepActionLast) {
		if (vmArgs == null)
			return originalArg;

		if (originalArg == null)
			originalArg = "";

		// replace and null out all vmargs that already exist
		int size = vmArgs.length;
		for (int i = 0; i < size; i++) {
			int ind = vmArgs[i].indexOf(" ");
			int ind2 = vmArgs[i].indexOf("=");
			if (ind >= 0 && (ind2 == -1 || ind < ind2)) { // -a bc style
				int index = originalArg
						.indexOf(vmArgs[i].substring(0, ind + 1));
				if (index == 0
						|| (index > 0 && Character.isWhitespace(originalArg
								.charAt(index - 1)))) {
					// replace
					String s = originalArg.substring(0, index);
					int index2 = getNextToken(originalArg, index + ind + 1);
					if (index2 >= 0)
						originalArg = s + vmArgs[i]
								+ originalArg.substring(index2);
					else
						originalArg = s + vmArgs[i];
					vmArgs[i] = null;
				}
			} else if (ind2 >= 0) { // a=b style
				int index = originalArg.indexOf(vmArgs[i]
						.substring(0, ind2 + 1));
				if (index == 0
						|| (index > 0 && Character.isWhitespace(originalArg
								.charAt(index - 1)))) {
					// replace
					String s = originalArg.substring(0, index);
					int index2 = getNextToken(originalArg, index);
					if (index2 >= 0)
						originalArg = s + vmArgs[i]
								+ originalArg.substring(index2);
					else
						originalArg = s + vmArgs[i];
					vmArgs[i] = null;
				}
			} else { // abc style
				int index = originalArg.indexOf(vmArgs[i]);
				if (index == 0
						|| (index > 0 && Character.isWhitespace(originalArg
								.charAt(index - 1)))) {
					// replace
					String s = originalArg.substring(0, index);
					int index2 = getNextToken(originalArg, index);
					if (!keepActionLast || i < (size - 1)) {
						if (index2 >= 0)
							originalArg = s + vmArgs[i]
									+ originalArg.substring(index2);
						else
							originalArg = s + vmArgs[i];
						vmArgs[i] = null;
					} else {
						// The last VM argument needs to remain last,
						// remove original arg and append the vmArg later
						if (index2 >= 0)
							originalArg = s + originalArg.substring(index2);
						else
							originalArg = s;
					}
				}
			}
		}

		// remove excluded arguments
		if (excludeArgs != null && excludeArgs.length > 0) {
			for (int i = 0; i < excludeArgs.length; i++) {
				int ind = excludeArgs[i].indexOf(" ");
				int ind2 = excludeArgs[i].indexOf("=");
				if (ind >= 0 && (ind2 == -1 || ind < ind2)) { // -a bc style
					int index = originalArg.indexOf(excludeArgs[i].substring(0,
							ind + 1));
					if (index == 0
							|| (index > 0 && Character.isWhitespace(originalArg
									.charAt(index - 1)))) {
						// remove
						String s = originalArg.substring(0, index);
						int index2 = getNextToken(originalArg, index + ind + 1);
						if (index2 >= 0) {
							// If remainder will become the first argument,
							// remove leading blanks
							while (index2 < originalArg.length()
									&& Character.isWhitespace(originalArg
											.charAt(index2)))
								index2 += 1;
							originalArg = s + originalArg.substring(index2);
						} else
							originalArg = s;
					}
				} else if (ind2 >= 0) { // a=b style
					int index = originalArg.indexOf(excludeArgs[i].substring(0,
							ind2 + 1));
					if (index == 0
							|| (index > 0 && Character.isWhitespace(originalArg
									.charAt(index - 1)))) {
						// remove
						String s = originalArg.substring(0, index);
						int index2 = getNextToken(originalArg, index);
						if (index2 >= 0) {
							// If remainder will become the first argument,
							// remove leading blanks
							while (index2 < originalArg.length()
									&& Character.isWhitespace(originalArg
											.charAt(index2)))
								index2 += 1;
							originalArg = s + originalArg.substring(index2);
						} else
							originalArg = s;
					}
				} else { // abc style
					int index = originalArg.indexOf(excludeArgs[i]);
					if (index == 0
							|| (index > 0 && Character.isWhitespace(originalArg
									.charAt(index - 1)))) {
						// remove
						String s = originalArg.substring(0, index);
						int index2 = getNextToken(originalArg, index);
						if (index2 >= 0) {
							// Remove leading blanks
							while (index2 < originalArg.length()
									&& Character.isWhitespace(originalArg
											.charAt(index2)))
								index2 += 1;
							originalArg = s + originalArg.substring(index2);
						} else
							originalArg = s;
					}
				}
			}
		}

		// add remaining vmargs to the end
		for (int i = 0; i < size; i++) {
			if (vmArgs[i] != null) {
				if (originalArg.length() > 0 && !originalArg.endsWith(" "))
					originalArg += " ";
				originalArg += vmArgs[i];
			}
		}

		return originalArg;
	}

	/**
	 * Replace the current JRE container classpath with the given entry.
	 * 
	 * @param cp
	 * @param entry
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void replaceJREContainer(List cp, IRuntimeClasspathEntry entry) {
		int size = cp.size();
		for (int i = 0; i < size; i++) {
			IRuntimeClasspathEntry entry2 = (IRuntimeClasspathEntry) cp.get(i);
			if (entry2.getPath().uptoSegment(2).isPrefixOf(entry.getPath())) {
				cp.set(i, entry);
				return;
			}
		}

		cp.add(0, entry);
	}

	/**
	 * Merge a single classpath entry into the classpath list.
	 * 
	 * @param cp
	 * @param entry
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void mergeClasspath(List cp, IRuntimeClasspathEntry entry) {
		Iterator iterator = cp.iterator();
		while (iterator.hasNext()) {
			IRuntimeClasspathEntry entry2 = (IRuntimeClasspathEntry) iterator
					.next();

			if (entry2.getPath().equals(entry.getPath()))
				return;
		}

		cp.add(entry);
	}

	public OSGIFrameworkInstanceBehaviorDelegate() {
		super();
	}

	public void setServerStarted() {
		setServerState(IServer.STATE_STARTED);
	}

	public void stopImpl() {
		if (ping != null) {
			ping.stop();
			ping = null;
		}
		if (processListener != null) {
			DebugPlugin.getDefault().removeDebugEventListener(processListener);
			processListener = null;
		}
		setServerState(IServer.STATE_STOPPED);
	}

	/**
	 * Cleanly shuts down and terminates the server.
	 * 
	 * @param force
	 *            <code>true</code> to kill the server
	 */
	public void stop(boolean force) {
		if (force) {
			terminate();
			return;
		}
		int state = getServer().getServerState();
		// If stopped or stopping, no need to run stop command again
		if (state == IServer.STATE_STOPPED || state == IServer.STATE_STOPPING)
			return;
		else if (state == IServer.STATE_STARTING) {
			terminate();
			return;
		}

		try {
			if (Trace.isTraceEnabled())
				Trace.trace(Trace.FINER, "Stopping Felix");
			if (state != IServer.STATE_STOPPED)
				setServerState(IServer.STATE_STOPPING);

			// ILaunchConfiguration launchConfig = ((Server)
			// getServer()).getLaunchConfiguration(true, null);
			// ILaunchConfigurationWorkingCopy wc =
			// launchConfig.getWorkingCopy();
			//
			// String args =
			// renderCommandLine(getRuntimeProgramArguments(false)," ");
			// wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,args);
			// wc.setAttribute("org.eclipse.debug.ui.private", true);
			// wc.setAttribute(ATTR_STOP, "true");
			// wc.launch(ILaunchManager.RUN_MODE, new NullProgressMonitor());
			this.terminate();

		} catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Error stopping Felix", e);
		}
	}

	/**
	 * Terminates the server.
	 */
	protected void terminate() {
		if (getServer().getServerState() == IServer.STATE_STOPPED)
			return;

		try {
			setServerState(IServer.STATE_STOPPING);
			if (Trace.isTraceEnabled())
				Trace.trace(Trace.FINER, "Killing the Felix process");
			ILaunch launch = getServer().getLaunch();
			if (launch != null) {
				launch.terminate();
				stopImpl();
			}
		} catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Error killing the process", e);
		}
	}

	public IPath getTempDirectory() {
		return super.getTempDirectory(false);
	}

	public IPath getBaseDirectory() {

		IPath confDir = getTempDirectory(true);
		String instancePathStr = getFrameworkInstance().getInstanceDirectory();
		if (instancePathStr != null) {
			IPath instanceDir = new Path(getFrameworkInstance()
					.getInstanceDirectory());
			if (instanceDir != null)
				confDir = instanceDir;
		}

		return confDir;
	}

	public void initialize(IProgressMonitor monitor) {
		// do nothing
	}

	public IOSGIFramework getFramework() {
		if (getServer().getRuntime() == null)
			return null;

		return (IOSGIFramework) getServer().getRuntime().loadAdapter(
				IOSGIFramework.class, null);
	}

	public IOSGIFrameworkInstance getFrameworkInstance() {
		return (IOSGIFrameworkInstance) getServer().loadAdapter(
				IOSGIFrameworkInstance.class, null);
	}

	public void addProcessListener(final IProcess newProcess) {
		if (processListener != null || newProcess == null)
			return;

		processListener = new IDebugEventSetListener() {
			public void handleDebugEvents(DebugEvent[] events) {
				if (events != null) {
					int size = events.length;
					for (int i = 0; i < size; i++) {
						if (newProcess != null
								&& newProcess.equals(events[i].getSource())
								&& events[i].getKind() == DebugEvent.TERMINATE) {
							stopImpl();
						}
					}
				}
			}
		};
		DebugPlugin.getDefault().addDebugEventListener(processListener);
	}

	/**
	 * Setup for starting the server.
	 * 
	 * @param launch
	 *            ILaunch
	 * @param launchMode
	 *            String
	 * @param monitor
	 *            IProgressMonitor
	 * @throws CoreException
	 *             if anything goes wrong
	 */
	public void setupLaunch(ILaunch launch, String launchMode,
			IProgressMonitor monitor) throws CoreException {
		if ("true".equals(launch.getLaunchConfiguration().getAttribute(
				ATTR_STOP, "false")))
			return;
		// if (getFelixRuntime() == null)
		// throw new CoreException(Status.);

		IStatus status = getFramework().validate();
		if (status != null && status.getSeverity() == IStatus.ERROR)
			throw new CoreException(status);

		setServerRestartState(false);
		setServerState(IServer.STATE_STARTING);
		setMode(launchMode);

		// ping server to check for startup
		try {
			String url = "http://" + getServer().getHost();
			int port = 8888;
			if (port != 80)
				url += ":" + port;
			ping = new PingThread(getServer(), url, -1, this);
		} catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Can't ping for Felix startup.");
		}
	}




	/**
	 * Return a string representation of this object.
	 * 
	 * @return java.lang.String
	 */
	public String toString() {
		return "FelixRuntimeInstance";
	}

	public void setupLaunchConfiguration(
			ILaunchConfigurationWorkingCopy workingCopy,
			IProgressMonitor monitor) throws CoreException {
		String existingProgArgs = workingCopy.getAttribute(
				IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
				(String) null);
		workingCopy.setAttribute(
				IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
				mergeArguments(existingProgArgs,
						getFrameworkProgramArguments(true),
						getExcludedFrameworkProgramArguments(true), true));

		String existingVMArgs = workingCopy.getAttribute(
				IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,
				(String) null);
		String[] parsedVMArgs = null;
		if (null != existingVMArgs) {
			parsedVMArgs = DebugPlugin.parseArguments(existingVMArgs);
		}
		String[] configVMArgs = getFrameworkVMArguments();

		workingCopy.setAttribute(
				IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,
				mergeArguments(existingVMArgs, configVMArgs, null, false));

		IOSGIFramework runtime = getFramework();
		IVMInstall vmInstall = runtime.getVMInstall();
		if (vmInstall != null)
			workingCopy.setAttribute(
					IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH,
					JavaRuntime.newJREContainerPath(vmInstall)
							.toPortableString());

		// update classpath
		IRuntimeClasspathEntry[] originalClasspath = JavaRuntime
				.computeUnresolvedRuntimeClasspath(workingCopy);
		int size = originalClasspath.length;
		List oldCp = new ArrayList(originalClasspath.length + 2);
		for (int i = 0; i < size; i++)
			oldCp.add(originalClasspath[i]);

		List cp2 = runtime.getFrameworkClasspath(null);
		Iterator iterator = cp2.iterator();
		while (iterator.hasNext()) {
			IRuntimeClasspathEntry entry = (IRuntimeClasspathEntry) iterator
					.next();
			mergeClasspath(oldCp, entry);
		}

		if (vmInstall != null) {
			try {
				String typeId = vmInstall.getVMInstallType().getId();
				replaceJREContainer(oldCp,
						JavaRuntime.newRuntimeContainerClasspathEntry(new Path(
								JavaRuntime.JRE_CONTAINER).append(typeId)
								.append(vmInstall.getName()),
								IRuntimeClasspathEntry.BOOTSTRAP_CLASSES));
			} catch (Exception e) {
				// ignore
			}

			IPath jrePath = new Path(vmInstall.getInstallLocation()
					.getAbsolutePath());
			if (jrePath != null) {
				IPath toolsPath = jrePath.append("lib").append("tools.jar");
				if (toolsPath.toFile().exists()) {
					IRuntimeClasspathEntry toolsJar = JavaRuntime
							.newArchiveRuntimeClasspathEntry(toolsPath);
					// Search for index to any existing tools.jar entry
					int toolsIndex;
					for (toolsIndex = 0; toolsIndex < oldCp.size(); toolsIndex++) {
						IRuntimeClasspathEntry entry = (IRuntimeClasspathEntry) oldCp
								.get(toolsIndex);
						if (entry.getType() == IRuntimeClasspathEntry.ARCHIVE
								&& entry.getPath().lastSegment()
										.equals("tools.jar")) {
							break;
						}
					}
					// If existing tools.jar found, replace in case it's
					// different. Otherwise add.
					if (toolsIndex < oldCp.size())
						oldCp.set(toolsIndex, toolsJar);
					else
						mergeClasspath(oldCp, toolsJar);
				}
			}
		}

		iterator = oldCp.iterator();
		List list = new ArrayList();
		while (iterator.hasNext()) {
			IRuntimeClasspathEntry entry = (IRuntimeClasspathEntry) iterator
					.next();
			try {
				list.add(entry.getMemento());
			} catch (Exception e) {
				Trace.trace(Trace.SEVERE, "Could not resolve classpath entry: "
						+ entry, e);
			}
		}

		workingCopy.setAttribute(
				IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, list);
		workingCopy
				.setAttribute(
						IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH,
						false);
	}

	protected IModuleResource[] getResources(IModule[] module) {
		return super.getResources(module);
	}

	protected IModuleResourceDelta[] getPublishedResourceDelta(IModule[] module) {
		return super.getPublishedResourceDelta(module);
	}

	/**
	 * @see ServerBehaviourDelegate#handleResourceChange()
	 */
	public void handleResourceChange() {
		if (getServer().getServerRestartState())
			return;

		Iterator iterator = getAllModules().iterator();
		while (iterator.hasNext()) {
			IModule[] module = (IModule[]) iterator.next();
			IModuleResourceDelta[] delta = getPublishedResourceDelta(module);
			if (delta == null || delta.length == 0)
				continue;

			if (containsNonResourceChange(delta)) {
				setServerRestartState(true);
				return;
			}
		}
	}

	protected boolean containsNonResourceChange(IModuleResourceDelta[] delta) {
		int size = delta.length;
		for (int i = 0; i < size; i++) {
			IModuleResourceDelta d = delta[i];
			if (d.getModuleRelativePath().segmentCount() == 0) {
				if ("WEB-INF".equals(d.getModuleResource().getName())) {
					return containsNonResourceChange(d.getAffectedChildren());
				}
				continue;
			}
			if (d.getModuleResource() instanceof IModuleFile)
				return true;

			boolean b = containsNonAddChange(d.getAffectedChildren());
			if (b)
				return true;
		}
		return false;
	}

	protected boolean containsNonAddChange(IModuleResourceDelta[] delta) {
		if (delta == null)
			return false;
		int size = delta.length;
		for (int i = 0; i < size; i++) {
			IModuleResourceDelta d = delta[i];
			if (d.getModuleResource() instanceof IModuleFile) {
				if (d.getKind() != IModuleResourceDelta.ADDED)
					return true;
			}

			boolean b = containsNonAddChange(d.getAffectedChildren());
			if (b)
				return true;
		}
		return false;
	}

	/**
	 * Cleans the entire work directory for this server. This involves deleting
	 * all subdirectories of the server's work directory.
	 * 
	 * @param monitor
	 *            a progress monitor
	 * @return results of the clean operation
	 * @throws CoreException
	 */
	public IStatus cleanFrameworkInstanceWorkDir(IProgressMonitor monitor)
			throws CoreException {
		return Status.OK_STATUS;
	}

	public IPath getPublishDirectory(IModule[] module) {
		return getServerDeployDirectory();
	}

	/**
	 * Gets the directory to which modules should be deployed for this server.
	 * 
	 * @return full path to deployment directory for the server
	 */
	public IPath getServerDeployDirectory() {
		return new Path(getFrameworkInstance().getInstanceDirectory());
	}

	/**
	 * Gets the directory to which to deploy a module's web application.
	 * 
	 * @param module
	 *            a module
	 * @return full path to deployment directory for the module
	 */
	public IPath getModuleDeployDirectory(IModule module) {
		return getServerDeployDirectory().append(module.getName());
	}

	public void setModulePublishState2(IModule[] module, int state) {
		setModulePublishState(module, state);
	}

	public Properties loadModulePublishLocations() {
		Properties p = new Properties();
		IPath path = getTempDirectory().append("publish.txt");
		FileInputStream fin = null;
		try {
			fin = new FileInputStream(path.toFile());
			p.load(fin);
		} catch (Exception e) {
			// ignore
		} finally {
			try {
				fin.close();
			} catch (Exception ex) {
				// ignore
			}
		}
		return p;
	}

	public void saveModulePublishLocations(Properties p) {
		IPath path = getTempDirectory().append("publish.txt");
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(path.toFile());
			p.store(fout, "Felix publish data");
		} catch (Exception e) {
			// ignore
		} finally {
			try {
				fout.close();
			} catch (Exception ex) {
				// ignore
			}
		}
	}


}