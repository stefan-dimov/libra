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
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.libra.tools.model.composite.schema.composite.Bundle;
import org.eclipse.libra.tools.model.composite.schema.composite.Composite;
import org.eclipse.libra.tools.model.composite.schema.composite.CompositeFactory;
import org.eclipse.libra.tools.model.composite.schema.composite.CompositePackage;
import org.eclipse.libra.tools.model.composite.schema.composite.DocumentRoot;
import org.eclipse.libra.tools.model.composite.schema.composite.Group;
import org.eclipse.libra.tools.model.composite.schema.composite.util.CompositeResourceFactoryImpl;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.target.TargetPlatformService;
import org.eclipse.pde.internal.core.target.provisional.IBundleContainer;
import org.eclipse.pde.internal.core.target.provisional.ITargetDefinition;
import org.eclipse.pde.internal.core.target.provisional.ITargetHandle;


@SuppressWarnings("restriction")
public class FrameworkInstanceConfiguration {

	public static final String ADD_BUNDLE = "ADD_BUNDLE";
	public static final String REMOVE_BUNDLE = "REMOVE_BUNDLE";
	
	
	protected IFolder configPath;
	protected ITargetDefinition targetDefinition;
	protected Composite composite;
	protected FrameworkInstanceDelegate runtimeInstance;
	private transient List<PropertyChangeListener> propertyListeners;

	
	public FrameworkInstanceConfiguration(IFolder path, FrameworkInstanceDelegate runtimeInstance) {
		super();
		this.configPath = path;
		this.runtimeInstance = runtimeInstance;
	}


	public Composite getComposite() {
		return composite;
	}

	public void setComposite(Composite composite) {
		this.composite = composite;
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

		IPath ws = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		File compositeFile = ws.append(folder.getFullPath()).append("felix.composite")
				.makeAbsolute().toFile();
		if (!compositeFile.exists()) {
			createDefaultComposite(compositeFile);
		} else {
			loadDefaultComposite(compositeFile);
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

	public void saveComposite() {
		IPath ws = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		File compositeFile = ws.append(getFolder().getFullPath()).append("felix.composite")
				.makeAbsolute().toFile();

		writeCompositeFile(compositeFile);
	}

	private void writeCompositeFile(File compositeFile) {
		try {
			// Create a resource set to hold the resources.
			//
			ResourceSet resourceSet = new ResourceSetImpl();

			// Register the appropriate resource factory to handle all file
			// extensions.
			//
			resourceSet
					.getResourceFactoryRegistry()
					.getExtensionToFactoryMap()
					.put(Resource.Factory.Registry.DEFAULT_EXTENSION,
							new CompositeResourceFactoryImpl());

			resourceSet.getPackageRegistry().put(CompositePackage.eNS_URI,CompositePackage.eINSTANCE);
			Resource resource = resourceSet.createResource(URI.createURI("http:///My.composite"));
			DocumentRoot documentRoot = CompositeFactory.eINSTANCE.createDocumentRoot();
			documentRoot.setComposite(composite);
			resource.getContents().add(documentRoot);
			FileOutputStream fos = new FileOutputStream(compositeFile);
			//resource.save(System.out, null);
			resource.save(fos, null);
			
			IPath tf = new Path(compositeFile.getAbsolutePath());
			tf.makeAbsolute();
			IPath wsp =  ResourcesPlugin.getWorkspace().getRoot().getLocation().makeAbsolute();
			tf = tf.removeFirstSegments(tf.matchingFirstSegments(wsp)).makeAbsolute();
			tf = tf.removeLastSegments(1);
			IResource r = ResourcesPlugin.getWorkspace().getRoot().findMember(tf);
			if(r!= null && r.getProject() != null)
				r.getProject().refreshLocal(IResource.DEPTH_INFINITE,new NullProgressMonitor());
		} catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Cannot write composite file",e);
		}
	}
	
	private void createDefaultComposite(File targetFile) {

			Composite root = CompositeFactory.eINSTANCE.createComposite();
			root.setId(runtimeInstance.getServer().getId());
			root.setName(runtimeInstance.getServer().getName());
			root.setVersion("1.0");
			Group g1 = CompositeFactory.eINSTANCE.createGroup();
			root.getGroup1().add(g1);
			composite = root;
			writeCompositeFile(targetFile);
	}

	private void loadDefaultComposite(File compositeFile) {
		// Create a resource set to hold the resources.
		//
		ResourceSet resourceSet = new ResourceSetImpl();

		// Register the appropriate resource factory to handle all file
		// extensions.
		//
		resourceSet
				.getResourceFactoryRegistry()
				.getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION,
						new CompositeResourceFactoryImpl());

		// Register the package to ensure it is available during loading.
		//
		resourceSet.getPackageRegistry().put(CompositePackage.eNS_URI,
				CompositePackage.eINSTANCE);

		URI uri = URI.createFileURI(compositeFile.getAbsolutePath());

		Resource resource = resourceSet.getResource(uri, true);
		DocumentRoot documentRoot = (DocumentRoot) resource.getContents()
				.get(0);
		composite = documentRoot.getComposite();

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
		return "FelixConfiguration[" + getFolder() + "]";
	}

	public void addOsgiBundle(IPluginModelBase module) {
		Group group =  composite.getGroup1().get(0);
		boolean found = false;
		
		for(Bundle b: group.getBundle())
		{
			if(b.getId().equals(module.getPluginBase().getId())){
				found = true;
				break;
			}
		}
		if(!found){
			Bundle b = CompositeFactory.eINSTANCE.createBundle();
			b.setId(module.getPluginBase().getId());
			b.setName(module.getPluginBase().getName());
			b.setVersion(module.getPluginBase().getVersion());
			group.getBundle().add(b);
			firePropertyChangeEvent(ADD_BUNDLE, null, b);
			this.saveComposite();
		}
		
	}

	public void removeBundle(IPluginModelBase module) {
		Group group =  composite.getGroup1().get(0);
		boolean found = false;
		Bundle fb = null;
		for(Bundle b: group.getBundle())
		{
			if(b.getId().equals(module.getPluginBase().getId())){
				found = true;
				fb = b;
				break;
			}
		}
		if(found){
			group.getBundle().remove(fb);
			firePropertyChangeEvent(REMOVE_BUNDLE, fb, null);
			this.saveComposite();
		}
	
	}
	
	
	

}
