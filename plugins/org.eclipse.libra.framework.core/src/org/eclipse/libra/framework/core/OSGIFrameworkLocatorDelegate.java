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

import java.io.File;
import java.io.FileFilter;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.model.RuntimeLocatorDelegate;

/**
 * 
 */
public abstract class OSGIFrameworkLocatorDelegate extends RuntimeLocatorDelegate {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.wst.server.core.model.IRuntimeFactoryDelegate#getKnownRuntimes
	 * ()
	 */
	public void searchForRuntimes(IPath path, IRuntimeSearchListener listener,
			IProgressMonitor monitor) {
		searchForRuntimes2(path, listener, monitor);
	}

	protected static void searchForRuntimes2(IPath path,
			IRuntimeSearchListener listener, IProgressMonitor monitor) {
		File[] files = null;
		if (path != null) {
			File f = path.toFile();
			if (f.exists())
				files = f.listFiles();
			else
				return;
		} else
			files = File.listRoots();

		if (files != null) {
			int size = files.length;
			int work = 100 / size;
			int workLeft = 100 - (work * size);
			for (int i = 0; i < size; i++) {
				if (monitor.isCanceled())
					return;
				if (files[i] != null && files[i].isDirectory())
					searchDir(listener, files[i], 4, monitor);
				monitor.worked(work);
			}
			monitor.worked(workLeft);
		} else
			monitor.worked(100);
	}

	protected static void searchDir(IRuntimeSearchListener listener, File dir,
			int depth, IProgressMonitor monitor) {
		if ("conf".equals(dir.getName())) {
			IRuntimeWorkingCopy runtime = getRuntimeFromDir(
					dir.getParentFile(), monitor);
			if (runtime != null) {
				listener.runtimeFound(runtime);
				return;
			}
		}

		if (depth == 0)
			return;

		File[] files = dir.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory();
			}
		});
		if (files != null) {
			int size = files.length;
			for (int i = 0; i < size; i++) {
				if (monitor.isCanceled())
					return;
				searchDir(listener, files[i], depth - 1, monitor);
			}
		}
	}

	protected static IRuntimeWorkingCopy getRuntimeFromDir(File dir, IProgressMonitor monitor) {
		return getRuntimeFromDir(dir, null, monitor);
	}

	protected static IRuntimeWorkingCopy getRuntimeFromDir(File dir,
			String runtimeTypes[], IProgressMonitor monitor) {

		for (int i = 0; i < runtimeTypes.length; i++) {
			try {
				IRuntimeType runtimeType = ServerCore
						.findRuntimeType(runtimeTypes[i]);
				String absolutePath = dir.getAbsolutePath();
				String id = absolutePath.replace(File.separatorChar, '_')
						.replace(':', '-');
				IRuntimeWorkingCopy runtime = runtimeType.createRuntime(id,
						monitor);
				runtime.setName(dir.getName());
				runtime.setLocation(new Path(absolutePath));
				IOSGIFrameworkWorkingCopy wc = (IOSGIFrameworkWorkingCopy) runtime
						.loadAdapter(IOSGIFrameworkWorkingCopy.class, null);
				wc.setVMInstall(JavaRuntime.getDefaultVMInstall());
				IStatus status = runtime.validate(monitor);
				if (status == null || status.getSeverity() != IStatus.ERROR)
					return runtime;

				Trace.trace(Trace.FINER,
						"False runtime found at " + dir.getAbsolutePath()
								+ ": " + status.getMessage());
			} catch (Exception e) {
				Trace.trace(Trace.SEVERE, "Could not find runtime", e);
			}
		}
		return null;
	}
	
	public static String[] getRuntimeTypes(){
		return new String[]{};
	}

}
