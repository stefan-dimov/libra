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
package org.eclipse.libra.framework.core.launching;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.ExecutionArguments;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jst.server.core.ServerProfilerDelegate;
import org.eclipse.libra.framework.core.FrameworkCorePlugin;
import org.eclipse.libra.framework.core.IOSGIFrameworkInstance;
import org.eclipse.libra.framework.core.OSGIFrameworkInstanceBehaviorDelegate;
import org.eclipse.libra.framework.core.Trace;
import org.eclipse.libra.framework.core.internal.command.AddOsgiModuleCommand;
import org.eclipse.libra.tools.model.composite.schema.composite.Bundle;
import org.eclipse.libra.tools.model.composite.schema.composite.Group;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.launching.PDELaunchingPlugin;
import org.eclipse.pde.internal.launching.launcher.OSGiFrameworkManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.ui.internal.wizard.RunOnServerWizard;


/**
 * 
 */
public class LaunchConfigurationDelegate extends
		AbstractJavaLaunchConfigurationDelegate {

	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		IServer server = ServerUtil.getServer(configuration);
		
		if (server == null) {
			Trace.trace(Trace.FINEST,
					"Launch configuration could not find runtime instance");

			server = getBundleInfoFromRuntimes(configuration, mode, monitor);
		}

		if (server.shouldPublish() && ServerCore.isAutoPublishing())
			server.publish(IServer.PUBLISH_INCREMENTAL, monitor);

		OSGIFrameworkInstanceBehaviorDelegate oribd = (OSGIFrameworkInstanceBehaviorDelegate) server
				.loadAdapter(OSGIFrameworkInstanceBehaviorDelegate.class, null);

		String mainTypeName = oribd.getFrameworkClass();

		IVMInstall vm = verifyVMInstall(configuration);

		IVMRunner runner = vm.getVMRunner(mode);
		if (runner == null)
			runner = vm.getVMRunner(ILaunchManager.RUN_MODE);

		File workingDir = verifyWorkingDirectory(configuration);
		String workingDirName = null;
		if (workingDir != null)
			workingDirName = workingDir.getAbsolutePath();

		// Program & VM args
		String pgmArgs = getProgramArguments(configuration);
		String vmArgs = getVMArguments(configuration);
		String[] envp = getEnvironment(configuration);

		ExecutionArguments execArgs = new ExecutionArguments(vmArgs, pgmArgs);

		// VM-specific attributes
		Map vmAttributesMap = getVMSpecificAttributesMap(configuration);

		// Classpath
		String[] classpath = getClasspath(configuration);

		// Create VM config
		VMRunnerConfiguration runConfig = new VMRunnerConfiguration(
				mainTypeName, classpath);
		runConfig.setProgramArguments(execArgs.getProgramArgumentsArray());
		runConfig.setVMArguments(execArgs.getVMArgumentsArray());
		runConfig.setWorkingDirectory(workingDirName);
		runConfig.setEnvironment(envp);
		runConfig.setVMSpecificAttributesMap(vmAttributesMap);

		// Bootpath
		String[] bootpath = getBootpath(configuration);
		if (bootpath != null && bootpath.length > 0)
			runConfig.setBootClassPath(bootpath);

		setDefaultSourceLocator(launch, configuration);

		if (ILaunchManager.PROFILE_MODE.equals(mode)) {
			try {
				ServerProfilerDelegate.configureProfiling(launch, vm,
						runConfig, monitor);
			} catch (CoreException ce) {
				oribd.stopImpl();
				throw ce;
			}
		}

		// Launch the configuration
		oribd.setupLaunch(launch, mode, monitor);
		try {
			runner.run(runConfig, launch, monitor);
			oribd.addProcessListener(launch.getProcesses()[0]);
		} catch (Exception e) {
			// Ensure we don't continue to think the server is starting
			oribd.stopImpl();
		}
	}
	
	IServer iserver;

	@SuppressWarnings("restriction")
	private IServer getBundleInfoFromRuntimes(
			ILaunchConfiguration configuration, final String mode, final IProgressMonitor monitor)
			throws CoreException {
		IServer server=null;
		String bundleStr=configuration.getAttribute("workspace_bundles", "");
		String[] bundlesStr=bundleStr.split(",");  
		Set<IServer> matchedServers=new HashSet<IServer>();
		Set<Bundle> matchedBundles=new HashSet<Bundle>();

		for (IServer iServer : ServerCore.getServers()) {
			IOSGIFrameworkInstance iori = (IOSGIFrameworkInstance) iServer.loadAdapter(IOSGIFrameworkInstance.class, null);
			EList<Group> groups=(iori.getFrameworkInstanceConfiguration().getComposite()).getGroup1();
			for (Group group : groups) {
				for (Bundle bundle : group.getBundle()) {
					for (String bund : bundlesStr) {
						if (bund.contains(bundle.getId())){
							matchedBundles.add(bundle);
							matchedServers.add(iServer);
						}
					}
				}
			}

		}
		
		if (matchedServers.size()!=1){
			final IModule module=null;
			 Display.getDefault().syncExec( new Runnable() {  public void run() { 
				 Shell shell = FrameworkCorePlugin.getDefault().getWorkbench().getDisplay().getShells()[0];
				 
				 RunOnServerWizard wizard = new RunOnServerWizard(module, mode, null);
				 WizardDialog dialog = new WizardDialog(shell, wizard);
				 if (dialog.open() == Window.CANCEL) {
					 if (monitor != null)
						 monitor.setCanceled(true);
				 }
				 
				 try {
					 Job.getJobManager().join("org.eclipse.wst.server.ui.family", null);
				 } catch (Exception e) {
					 Trace.trace(Trace.WARNING, "Error waiting for job", e);
				 }
				 
				 iserver = wizard.getServer();
			 }});
			 IPluginModelBase[] plugins= PluginRegistry.getActiveModels();
			 IOSGIFrameworkInstance iori = (IOSGIFrameworkInstance) iserver.loadAdapter(IOSGIFrameworkInstance.class, null);
			 for (IPluginModelBase iPluginModelBase : plugins) {
				for (Bundle bundle : matchedBundles) {
					if (bundle.getId().equalsIgnoreCase(iPluginModelBase.getBundleDescription().getName())){
						try {
							new AddOsgiModuleCommand(iori.getFrameworkInstanceConfiguration(), iPluginModelBase).execute(monitor, null);
						} catch (ExecutionException e) {
							Trace.trace(Trace.WARNING, "Error starting "+iPluginModelBase.getBundleDescription().getName(), e);
						}
					}
				}
			}
			 iori.getFrameworkInstanceConfiguration().saveComposite();
			 
			 server=iserver;
		}else{
			server=((IServer)matchedServers.toArray()[0]);
		}
		ILaunchConfigurationWorkingCopy wcConfiguration=configuration.getWorkingCopy();
		wcConfiguration.setAttribute(Server.ATTR_SERVER_ID, server.getId());
		
		
		wcConfiguration.doSave();
		
		return server;
	}
}