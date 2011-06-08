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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.internal.core.target.TargetPlatformService;
import org.eclipse.pde.internal.core.target.provisional.ITargetDefinition;
import org.eclipse.pde.internal.core.target.provisional.ITargetHandle;


@SuppressWarnings("restriction")
public class FrameworkInstanceConfiguration {

	public static final String ADD_BUNDLE = "ADD_BUNDLE";
	public static final String REMOVE_BUNDLE = "REMOVE_BUNDLE";
	
	
	protected IFolder configPath;
	protected ITargetDefinition targetDefinition;
	protected FrameworkInstanceDelegate runtimeInstance;
	private transient List<PropertyChangeListener> propertyListeners;

	
	public FrameworkInstanceConfiguration(IFolder path, FrameworkInstanceDelegate runtimeInstance) {
		super();
		this.configPath = path;
		this.runtimeInstance = runtimeInstance;
	}



	public ITargetDefinition getTargetDefinition() {
		try {
			loadTarget();
		} catch (CoreException e) {
		}
		return targetDefinition;
	}

	public void setTargetDefinition(ITargetDefinition targetDefinition) {
		this.targetDefinition = targetDefinition;
	}

	protected IFolder getFolder() {
		return configPath;
	}

	public void load(IFolder folder, IProgressMonitor monitor)
			throws CoreException {

		targetDefinition = loadTarget();
		if (targetDefinition == null) {
			createDefaultTarget();
		} else {
			loadTarget();
		}



	}

	private ITargetDefinition loadTarget() throws CoreException {

		targetDefinition = null;
		ITargetHandle[] targets = TargetPlatformService.getDefault()
				.getTargets(new NullProgressMonitor());
		String name = runtimeInstance.getServer().getName();
		for (ITargetHandle handle : targets) {
			if (name.equals(handle.getTargetDefinition().getName())) {
				targetDefinition = handle.getTargetDefinition();
			}
		}
		return targetDefinition;
	}

	private void createDefaultTarget() throws CoreException {
		
		targetDefinition = runtimeInstance.createDefaultTarget();
		

	}

	


	protected void firePropertyChangeEvent(String propertyName,
			Object oldValue, Object newValue) {
		if (propertyListeners == null)
			return;

		PropertyChangeEvent event = new PropertyChangeEvent(this, propertyName,
				oldValue, newValue);
		try {
			for (PropertyChangeListener listener : propertyListeners) {
				try {
					listener.propertyChange(event);
				} catch (Exception e) {
					Trace.trace(Trace.SEVERE,
							"Error firing property change event", e);
				}
			}
		} catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Error in property event", e);
		}
	}

	/**
	 * Adds a property change listener to this server.
	 * 
	 * @param listener
	 *            java.beans.PropertyChangeListener
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		if (propertyListeners == null)
			propertyListeners = new ArrayList<PropertyChangeListener>();
		propertyListeners.add(listener);
	}

	/**
	 * Removes a property change listener from this server.
	 * 
	 * @param listener
	 *            java.beans.PropertyChangeListener
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		if (propertyListeners != null)
			propertyListeners.remove(listener);
	}

	public void importFromPath(IPath path, boolean isTestEnv,
			IProgressMonitor monitor) throws CoreException {
		load(path, monitor);
	}

	public void load(IPath path, IProgressMonitor monitor) throws CoreException {
		try {
			monitor = ProgressUtil.getMonitorFor(monitor);
			monitor.beginTask(Messages.loadingTask, 7);

			// InputStream in = new
			// FileInputStream(path.append("catalina.policy")
			// .toFile());
			// in.read();
			// in.close();
			monitor.worked(1);
			monitor.worked(1);

			if (monitor.isCanceled())
				return;
			monitor.done();
		} catch (Exception e) {
			Trace.trace(
					Trace.WARNING,
					"Could not load Felix configuration from "
							+ path.toOSString() + ": " + e.getMessage());
			throw new CoreException(new Status(IStatus.ERROR,
					FrameworkCorePlugin.PLUGIN_ID, 0, NLS.bind(
							Messages.errorCouldNotLoadConfiguration,
							path.toOSString()), e));
		}

	}

	/**
	 * Save to the given directory.
	 * 
	 * @param path
	 *            a path
	 * @param forceDirty
	 *            boolean
	 * @param monitor
	 *            a progress monitor
	 * @exception CoreException
	 */
	protected void save(IPath path, boolean forceDirty, IProgressMonitor monitor)
			throws CoreException {

	}

	/**
	 * Save to the given directory. All configuration files are forced to be
	 * saved.
	 * 
	 * @param path
	 *            Desination path for the configuration files.
	 * @param monitor
	 *            A progress monitor
	 * @exception CoreException
	 */
	public void save(IPath path, IProgressMonitor monitor) throws CoreException {
		save(path, true, monitor);
	}

	/**
	 * Save the information held by this object to the given directory.
	 * 
	 * @param folder
	 *            a folder
	 * @param monitor
	 *            a progress monitor
	 * @throws CoreException
	 */
	public void save(IFolder folder, IProgressMonitor monitor)
			throws CoreException {
	}

	/**
	 * Return a string representation of this object.
	 * 
	 * @return java.lang.String
	 */
	public String toString() {
		return "FrameworkInstanceConfiguration[" + getFolder() + "]";
	}
	
}
