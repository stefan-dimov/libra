/*******************************************************************************
 * Copyright (c) 2010, 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dimo Stoilov (SAP AG) - initial API and implementation
 *******************************************************************************/
package org.eclipse.libra.facet.test;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.internal.resources.ProjectDescription;
import org.eclipse.core.internal.resources.ProjectDescriptionReader;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;

@SuppressWarnings("restriction")
public class ProjectUnzipUtil {

	private IPath zipLocation;
	private String[] projectNames;
	private IPath rootLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation();
	private static final String META_PROJECT_NAME = ".project";


	public ProjectUnzipUtil(IPath aZipLocation, String[] aProjectNames) {
		zipLocation = aZipLocation;
		projectNames = aProjectNames;

	}

	public boolean createProjects() throws CoreException {
		IWorkspaceRunnable workspaceRunnable = new IWorkspaceRunnable(){

			public void run(IProgressMonitor monitor) throws CoreException {
				try {
					expandZip();
//					ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
					buildProjects();
				} catch (IOException e) {
					throw new CoreException(new Status(IStatus.ERROR, "org.eclipse.libra.facet.test", e.getCause().getMessage(), e));
				}
			}
			
		};
		
		ResourcesPlugin.getWorkspace().run(workspaceRunnable, new NullProgressMonitor());

		return true;

	}

	private IProgressMonitor getProgessMonitor() {
		return new NullProgressMonitor();
	}

	private void expandZip() throws CoreException, IOException {
		IProgressMonitor monitor = getProgessMonitor();
		ZipFile zipFile = null;
		zipFile = new ZipFile(zipLocation.toFile());
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = (ZipEntry) entries.nextElement();
			monitor.subTask(entry.getName());
			File aFile = computeLocation(entry.getName()).toFile();
			File parentFile = null;
			if (entry.isDirectory()) {
				aFile.mkdirs();
			} else {
				parentFile = aFile.getParentFile();
				if (!parentFile.exists())
					parentFile.mkdirs();
				if (!aFile.exists())
					aFile.createNewFile();
				copy(zipFile.getInputStream(entry), new FileOutputStream(aFile));
				if (entry.getTime() > 0)
					aFile.setLastModified(entry.getTime());
			}
			monitor.worked(1);
		}
	}

	private IPath computeLocation(String name) {
		return rootLocation.append(name);
	}


	public static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		try {
			int n = in.read(buffer);
			while (n > 0) {
				out.write(buffer, 0, n);
				n = in.read(buffer);
			}
		} finally {
			in.close();
			out.close();
		}
	}

	public void setRootLocation(IPath rootLocation) {
		this.rootLocation = rootLocation;
	}

	private void buildProjects() throws IOException, CoreException {
		for (int i = 0; i < projectNames.length; i++) {
			ProjectDescriptionReader pd = new ProjectDescriptionReader();
			IPath projectPath = new Path("/" + projectNames[i] + "/" + META_PROJECT_NAME);
			IPath path = rootLocation.append(projectPath);
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectNames[i]);
			ProjectDescription description;
			description = pd.read(path);
			project.create(description, (getProgessMonitor()));
			project.open(getProgessMonitor());
//			project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
		}
	}



}
