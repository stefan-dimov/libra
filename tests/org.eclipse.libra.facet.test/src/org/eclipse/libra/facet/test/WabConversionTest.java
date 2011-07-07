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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.libra.facet.Activator;
import org.eclipse.libra.facet.OSGiBundleFacetInstallConfig;
import org.eclipse.libra.facet.OSGiBundleFacetUninstallConfig;
import org.eclipse.libra.facet.OSGiBundleFacetUninstallStrategy;
import org.eclipse.libra.facet.OSGiBundleFacetUtils;
import org.eclipse.libra.facet.ui.operations.ConvertProjectsToBundlesOperation;
import org.eclipse.pde.core.project.IBundleClasspathEntry;
import org.eclipse.pde.core.project.IBundleProjectDescription;
import org.eclipse.pde.core.project.IBundleProjectService;
import org.eclipse.pde.core.project.IPackageExportDescription;
import org.eclipse.pde.core.project.IPackageImportDescription;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.natures.PDE;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IFacetedProjectWorkingCopy;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.junit.Assert;
import org.junit.Test;



@SuppressWarnings("restriction")
public class WabConversionTest {
	private static final NullProgressMonitor monitor = new NullProgressMonitor();
	private static final String WEB_PRJ_LOCATION = "resources/testWeb.zip_";
	private static final String JAVA_PRJ_LOCATION = "resources/testJava.zip_";
	private static final String JPA_PRJ_LOCATION = "resources/testJPA.zip_";
	private static final String SIMPLE_PRJ_LOCATION = "resources/testSimple.zip_";
	private static final String WEB_PRJ_NAME = "testWeb";
	private static final String JAVA_PRJ_NAME = "testJava";
	private static final String JPA_PRJ_NAME = "testJPA";
	private static final String SIMPLE_PRJ_NAME = "testSimple";
	private static final String WEB_REFERRING_JAVA_PRJ_LOCATION = "resources/testWebReferringJava.zip_";
	private static final String WEB_REFERRING_JAVA_PRJ_NAME = "testWebReferringJava";
	private static final String JAVA_REFERRED_PRJ_LOCATION = "resources/testJavaReferred.zip_";
	private static final String JAVA_REFERRED_PRJ_NAME = "testJavaReferred";
	private static final String PLUGIN_PRJ_NAME = "testPlugin";
	private static final String PLUGIN_PRJ_LOCATION = "resources/testPlugin.zip_";
	private static final String PLUGIN_PRJ_CUSTOM_HEADERS_PRJ_NAME = "testPluginCustomHeaders";
	private static final String PLUGIN_PRJ_CUSTOM_HEADERS_LOCATION = "resources/testPluginCustomHeaders.zip_";
	private static final String WEB_PRJ_COPY_NAME = "testWebCopy";
	private static final String WEB_PRJ_COPY_LOCATION = "resources/testWebCopy.zip_";
	private static final String WEB_CONVERTED_PRJ_NAME = "testWebConverted";
	private static final String WEB_CONVERTED_PRJ_LOCATION = "resources/testWebConverted.zip_";
	private static final String JAVA_CONVERTED_PRJ_LOCATION = "resources/testJavaConverted.zip_";
	private static final String JAVA_CONVERTED_PRJ_NAME = "testJavaConverted";
	private static final String SIMPLE_CONVERTED_PRJ_LOCATION = "resources/testSimpleConverted.zip_";
	private static final String SIMPLE_CONVERTED_PRJ_NAME = "testSimpleConverted";
	private static final String PLUGIN_CONVERTED_PRJ_LOCATION = "resources/testPluginConverted.zip_";
	private static final String PLUGIN_CONVERTED_PRJ_NAME = "testPluginConverted";
	@SuppressWarnings("unused")
	private static final String SIMPLE_PRJ_COPY_LOCATION = "resources/testSimpleCopy.zip_";
	@SuppressWarnings("unused")
	private static final String SIMPLE_PRJ_COPY_NAME = "testSimpleCopy";
	private static final String JAVA_PRJ_COPY_LOCATION = "resources/testJavaCopy.zip_";
	private static final String JAVA_PRJ_COPY_NAME = "testJavaCopy";


	@Test
	public void convertSimpleProject() throws Exception {
		IProject simpleProject = importProjectInWorkspace(SIMPLE_PRJ_LOCATION, SIMPLE_PRJ_NAME);
    	new ConvertProjectsToBundlesOperation(new IProject[]{simpleProject}).run(monitor);
    	
    	IBundleProjectService bundleProjectService = Activator.getDefault().getBundleProjectService();
    	IBundleProjectDescription description = bundleProjectService.getDescription(simpleProject);
		
    	checkSimpleProject(SIMPLE_PRJ_NAME, "TestSimple", null, "1.0.0.qualifier", description);
	}
	
	@Test
	public void convertJavaProject() throws Exception {

		IProject javaProject = importProjectInWorkspace(JAVA_PRJ_LOCATION, JAVA_PRJ_NAME);
    	new ConvertProjectsToBundlesOperation(new IProject[]{javaProject}).run(monitor);
    	
    	IBundleProjectService bundleProjectService = Activator.getDefault().getBundleProjectService();
    	IBundleProjectDescription description = bundleProjectService.getDescription(javaProject);
		
    	checkJavaProject(javaProject, JAVA_PRJ_NAME, "TestJava", null, "1.0.0.qualifier", new String[] {"javapack", "javapack1"}, description);
	}

	@Test
	public void convertJavaProjectCustomHeaders() throws Exception {

		IProject javaProject = importProjectInWorkspace(JAVA_PRJ_COPY_LOCATION, JAVA_PRJ_COPY_NAME);
    	OSGiBundleFacetInstallConfig osgiBundleFacetInstallConfig = setupOSGiBundleFacetInstallConfig("customSymbolicName", "CustomBundleName", "customVendor", "1.0.1.qualifier");
		IFacetedProject fproj = ProjectFacetsManager.create(javaProject, true, monitor);
		fproj.installProjectFacet(OSGiBundleFacetUtils.OSGI_BUNDLE_FACET_42, osgiBundleFacetInstallConfig, monitor);
    	
    	IBundleProjectService bundleProjectService = Activator.getDefault().getBundleProjectService();
    	IBundleProjectDescription description = bundleProjectService.getDescription(javaProject);
		
    	checkJavaProject(javaProject, "customSymbolicName", "CustomBundleName", "customVendor", "1.0.1.qualifier", new String[] {"javapack", "javapack1"}, description);
	}
	
	@Test
	public void convertPluginProject() throws Exception {
		IProject pluginProject = importProjectInWorkspace(PLUGIN_PRJ_LOCATION, PLUGIN_PRJ_NAME);
    	new ConvertProjectsToBundlesOperation(new IProject[]{pluginProject}).run(monitor);
    	
    	IBundleProjectService bundleProjectService = Activator.getDefault().getBundleProjectService();
    	IBundleProjectDescription description = bundleProjectService.getDescription(pluginProject);
		
    	checkJavaProject(pluginProject, PLUGIN_PRJ_NAME, "TestPlugin", null, "1.0.0.qualifier", new String[] {"javapack", "javapack1", "testplugin"}, description);

    	IPackageImportDescription[] packageImports = description.getPackageImports();
    	Assert.assertNotNull(packageImports);
		List<String> packageImportStrings = new ArrayList<String>();
		for (IPackageImportDescription currPackageImportDescription : packageImports) {
			packageImportStrings.add(currPackageImportDescription.getName());
		}
    	Assert.assertTrue(packageImportStrings.contains("org.osgi.framework"));
    	
	}

	@Test
	public void convertPluginProjectCustomHeaders() throws Exception {
		IProject pluginProject = importProjectInWorkspace(PLUGIN_PRJ_CUSTOM_HEADERS_LOCATION, PLUGIN_PRJ_CUSTOM_HEADERS_PRJ_NAME);
    	new ConvertProjectsToBundlesOperation(new IProject[]{pluginProject}).run(monitor);
    	
    	IBundleProjectService bundleProjectService = Activator.getDefault().getBundleProjectService();
    	IBundleProjectDescription description = bundleProjectService.getDescription(pluginProject);

    	IPackageImportDescription[] packageImports = description.getPackageImports();
    	Assert.assertNotNull(packageImports);
		List<String> packageImportStrings = new ArrayList<String>();
		for (IPackageImportDescription currPackageImportDescription : packageImports) {
			packageImportStrings.add(currPackageImportDescription.getName());
		}
		Assert.assertTrue(packageImportStrings.contains("org.osgi.framework"));

    	checkJavaProject(pluginProject, "customSymbolicName", "CustomBundleName", "CustomProvider", "1.0.1.qualifier", new String[] {"javapack", "javapack1", "testplugincustomheaders"}, description);
    	
	}
	
	@Test
	public void convertWebProject() throws Exception {
		IProject webProject = importProjectInWorkspace(WEB_PRJ_LOCATION, WEB_PRJ_NAME);
    	new ConvertProjectsToBundlesOperation(new IProject[]{webProject}).run(monitor);
    	
    	IBundleProjectService bundleProjectService = Activator.getDefault().getBundleProjectService();
    	IBundleProjectDescription description = bundleProjectService.getDescription(webProject);
		
    	checkWebProject(webProject, WEB_PRJ_NAME, "TestWeb", null, "1.0.0.qualifier", new String[] {"test", "test1"}, "/" + WEB_PRJ_NAME, description);
	}
	
	@Test
	public void convertJPAProject() throws Exception {
		IProject jpaProject = importProjectInWorkspace(JPA_PRJ_LOCATION, JPA_PRJ_NAME);
    	new ConvertProjectsToBundlesOperation(new IProject[]{jpaProject}).run(monitor);
    	
    	IBundleProjectService bundleProjectService = Activator.getDefault().getBundleProjectService();
    	IBundleProjectDescription description = bundleProjectService.getDescription(jpaProject);
		
    	checkJPAProject(jpaProject, JPA_PRJ_NAME, "TestJPA", null, "1.0.0.qualifier", new String[] {"test"}, description);
	}

	@Test
	public void convertWebProjectCustomHeders() throws Exception {
		IProject webProject = importProjectInWorkspace(WEB_PRJ_COPY_LOCATION, WEB_PRJ_COPY_NAME);
		
    	OSGiBundleFacetInstallConfig osgiBundleFacetInstallConfig = setupOSGiBundleFacetInstallConfig("customSymbolicName", "CustomBundleName", "customVendor", "1.0.1.qualifier");
		IFacetedProject fproj = ProjectFacetsManager.create(webProject, true, monitor);
		fproj.installProjectFacet(OSGiBundleFacetUtils.OSGI_BUNDLE_FACET_42, osgiBundleFacetInstallConfig, monitor);
    	
    	IBundleProjectService bundleProjectService = Activator.getDefault().getBundleProjectService();
    	IBundleProjectDescription description = bundleProjectService.getDescription(webProject);
		
    	checkWebProject(webProject, "customSymbolicName", "CustomBundleName", "customVendor", "1.0.1.qualifier", new String[] {"test", "test1"}, "/customWebContext", description);
	}
	
	@Test
	public void convertWebReferringJavaProjects() throws Exception {
		IProject webProject = importProjectInWorkspace(WEB_REFERRING_JAVA_PRJ_LOCATION, WEB_REFERRING_JAVA_PRJ_NAME);
		IProject javaProject = importProjectInWorkspace(JAVA_REFERRED_PRJ_LOCATION, JAVA_REFERRED_PRJ_NAME);
    	new ConvertProjectsToBundlesOperation(new IProject[]{webProject, javaProject}).run(monitor);
    	
    	IBundleProjectService bundleProjectService = Activator.getDefault().getBundleProjectService();
    	IBundleProjectDescription webPrjDescription = bundleProjectService.getDescription(webProject);
    	IBundleProjectDescription javaPrjDescription = bundleProjectService.getDescription(javaProject);
		
    	checkWebProject(webProject, WEB_REFERRING_JAVA_PRJ_NAME, "TestWebReferringJava", null, "1.0.0.qualifier", new String[] {"test", "test1"}, "/" + WEB_REFERRING_JAVA_PRJ_NAME, webPrjDescription);
    	checkJavaProject(javaProject, JAVA_REFERRED_PRJ_NAME, "TestJavaReferred", null, "1.0.0.qualifier", new String[] {"javapack", "javapack1"}, javaPrjDescription);
    	
    	//the web projects uses a class from the java project. 
    	//Check that the corresponding package from the java project is available as imported package in the web project
    	boolean hasPackageImportForTheReferredJavaClass = false;
    	IPackageImportDescription[] packageImports = webPrjDescription.getPackageImports();
    	for (int i = 0; i < packageImports.length; i++) {
			IPackageImportDescription currPackageImportDescription = packageImports[i];
			if (currPackageImportDescription.getName().equals("javapack")) {
				hasPackageImportForTheReferredJavaClass = true;
				break;
			}
		}
    	Assert.assertTrue(hasPackageImportForTheReferredJavaClass);
	}

	@Test
	public void uninstallOSGiFacetFromWebProject() throws Exception {
		IProject webProject = importProjectInWorkspace(WEB_CONVERTED_PRJ_LOCATION, WEB_CONVERTED_PRJ_NAME);
		IFacetedProject fproj = ProjectFacetsManager.create(webProject, true, monitor);
		OSGiBundleFacetUninstallConfig config = new OSGiBundleFacetUninstallConfig();
		config.setStrategy(OSGiBundleFacetUninstallStrategy.FACET_AND_PLUGIN_NATURE_AND_MANIFEST);
		fproj.uninstallProjectFacet(OSGiBundleFacetUtils.OSGI_BUNDLE_FACET_42, config, monitor);
		Assert.assertFalse(webProject.hasNature(IBundleProjectDescription.PLUGIN_NATURE));
		Assert.assertFalse(hasBuildSpec(webProject, PDE.MANIFEST_BUILDER_ID));
		Assert.assertFalse(hasBuildSpec(webProject, PDE.SCHEMA_BUILDER_ID));
		Assert.assertFalse(hasPluginDependenciesCP(webProject));
		IFile buildPropertiesFile = webProject.getFile("WebContent/" + OSGiBundleFacetUtils.BUILD_PROPERTIES);
		Assert.assertFalse(buildPropertiesFile.exists());
	}

	@Test
	public void uninstallOSGiFacetFromJavaProject() throws Exception {
		IProject javaProject = importProjectInWorkspace(JAVA_CONVERTED_PRJ_LOCATION, JAVA_CONVERTED_PRJ_NAME);
		IFacetedProject fproj = ProjectFacetsManager.create(javaProject, true, monitor);
		OSGiBundleFacetUninstallConfig config = new OSGiBundleFacetUninstallConfig();
		config.setStrategy(OSGiBundleFacetUninstallStrategy.FACET_AND_PLUGIN_NATURE_AND_MANIFEST);
		fproj.uninstallProjectFacet(OSGiBundleFacetUtils.OSGI_BUNDLE_FACET_42, config, monitor);
		Assert.assertFalse(javaProject.hasNature(IBundleProjectDescription.PLUGIN_NATURE));
		Assert.assertFalse(hasBuildSpec(javaProject, PDE.MANIFEST_BUILDER_ID));
		Assert.assertFalse(hasBuildSpec(javaProject, PDE.SCHEMA_BUILDER_ID));
		Assert.assertFalse(hasPluginDependenciesCP(javaProject));
		IFile buildPropertiesFile = javaProject.getFile(OSGiBundleFacetUtils.BUILD_PROPERTIES);
		Assert.assertFalse(buildPropertiesFile.exists());
	}

	@Test
	public void uninstallOSGiFacetFromPluginProject() throws Exception {
		IProject pluginProject = importProjectInWorkspace(PLUGIN_CONVERTED_PRJ_LOCATION, PLUGIN_CONVERTED_PRJ_NAME);
		IFacetedProject fproj = ProjectFacetsManager.create(pluginProject, true, monitor);
		OSGiBundleFacetUninstallConfig config = new OSGiBundleFacetUninstallConfig();
		config.setStrategy(OSGiBundleFacetUninstallStrategy.FACET_ONLY);
		fproj.uninstallProjectFacet(OSGiBundleFacetUtils.OSGI_BUNDLE_FACET_42, config, monitor);
		Assert.assertTrue(pluginProject.hasNature(IBundleProjectDescription.PLUGIN_NATURE));
		Assert.assertTrue(hasPluginDependenciesCP(pluginProject));
		IFile buildPropertiesFile = pluginProject.getFile(OSGiBundleFacetUtils.BUILD_PROPERTIES);
		Assert.assertTrue(buildPropertiesFile.exists());
	}
	
	@Test
	public void uninstallOSGiFacetFromSimpleProject() throws Exception {
		IProject simpleProject = importProjectInWorkspace(SIMPLE_CONVERTED_PRJ_LOCATION, SIMPLE_CONVERTED_PRJ_NAME);
		IFacetedProject fproj = ProjectFacetsManager.create(simpleProject, true, monitor);
		OSGiBundleFacetUninstallConfig config = new OSGiBundleFacetUninstallConfig();
		config.setStrategy(OSGiBundleFacetUninstallStrategy.FACET_AND_PLUGIN_NATURE_AND_MANIFEST);
		fproj.uninstallProjectFacet(OSGiBundleFacetUtils.OSGI_BUNDLE_FACET_42, config, monitor);
		Assert.assertFalse(simpleProject.hasNature(IBundleProjectDescription.PLUGIN_NATURE));
		Assert.assertFalse(hasBuildSpec(simpleProject, PDE.MANIFEST_BUILDER_ID));
		Assert.assertFalse(hasBuildSpec(simpleProject, PDE.SCHEMA_BUILDER_ID));
		IFile buildPropertiesFile = simpleProject.getFile(OSGiBundleFacetUtils.BUILD_PROPERTIES);
		Assert.assertFalse(buildPropertiesFile.exists());
	}
	
	private void checkWebProject(IProject project, String expectedSymbolicName, String expectedBundleName, String expectedVendor, String expectedVersion, String[] expectedPackageExports, String expectedWebContextPath, IBundleProjectDescription description) throws JavaModelException {
		checkJavaProject(project, expectedSymbolicName, expectedBundleName, expectedVendor, expectedVersion, expectedPackageExports, description);
		checkWebPackageImports(description);
		Assert.assertEquals("2", description.getHeader("Bundle-ManifestVersion"));
		checkLaunchShortcuts(description);
		Assert.assertEquals(expectedWebContextPath, description.getHeader("Web-ContextPath"));
		IPath[] binIncludes = description.getBinIncludes();
		Assert.assertTrue(binIncludes.length == 1);
		Assert.assertEquals("WEB-INF/", binIncludes[0].toString());
		IBundleClasspathEntry[] bundleClasspath = description.getBundleClasspath();
		Assert.assertEquals(1, bundleClasspath.length);
		Assert.assertNull(bundleClasspath[0].getBinaryPath());
		Assert.assertEquals("src/", bundleClasspath[0].getSourcePath().toPortableString());
		Assert.assertEquals("WEB-INF/classes/", bundleClasspath[0].getLibrary().toPortableString());
	}
		
	private void checkJPAProject(IProject project, String expectedSymbolicName, String expectedBundleName, String expectedVendor, String expectedVersion, String[] expectedPackageExports, IBundleProjectDescription description) throws JavaModelException {
		checkJavaProject(project, expectedSymbolicName, expectedBundleName, expectedVendor, expectedVersion, expectedPackageExports, description);
		checkJPAPackageImports(description);
		Assert.assertEquals("2", description.getHeader("Bundle-ManifestVersion"));
		IBundleClasspathEntry[] bundleClasspath = description.getBundleClasspath();
		Assert.assertEquals(1, bundleClasspath.length);
		Assert.assertNull(bundleClasspath[0].getBinaryPath());
		Assert.assertEquals("src/", bundleClasspath[0].getSourcePath().toPortableString());
		IPath outputFolder = description.getDefaultOutputFolder();
		Assert.assertEquals("build/classes", outputFolder.toString());
	}

	private void checkJavaProject(IProject project, String expectedSymbolicName, String expectedBundleName, String expectedVendor, String expectedVersion, String[] expectedPackageExports, IBundleProjectDescription description) throws JavaModelException {
		checkSimpleProject(expectedSymbolicName, expectedBundleName, expectedVendor, expectedVersion, description);
		Assert.assertTrue(hasPluginDependenciesCP(project));
		checkPackageExports(expectedPackageExports, description);
	}

	private boolean hasPluginDependenciesCP(IProject project) throws JavaModelException {
		IJavaProject javaProject = JavaCore.create(project);
		IClasspathEntry[] entries = javaProject.getRawClasspath();
		boolean hasPluginDependeciesCP = false;
		for (int i = 0; i < entries.length; i++) {
			IClasspathEntry entry = entries[i];
			if (PDECore.REQUIRED_PLUGINS_CONTAINER_PATH.equals(entry.getPath())) {
				hasPluginDependeciesCP = true;
				break;
			}
		}
		return hasPluginDependeciesCP;
	}

	private void checkSimpleProject(String expectedSymbolicName, String expectedBundleName, String expectedVendor, String expectedVersion, IBundleProjectDescription description) {
		Assert.assertEquals(expectedSymbolicName, description.getSymbolicName());
		Assert.assertEquals(expectedBundleName, description.getBundleName());
		Assert.assertEquals(expectedVersion, description.getBundleVersion().toString());
		Assert.assertEquals(expectedVendor, description.getBundleVendor());
		String[] natureIds = description.getNatureIds();
		Assert.assertTrue(Arrays.asList(natureIds).contains(IBundleProjectDescription.PLUGIN_NATURE));
	}

	private void checkLaunchShortcuts(IBundleProjectDescription description) {
		String[] launchShortcuts = description.getLaunchShortcuts();
		List<String> launchShortcutsList = Arrays.asList(launchShortcuts);
		Assert.assertTrue(launchShortcutsList.contains("org.eclipse.pde.ui.EquinoxLaunchShortcut"));
		Assert.assertTrue(launchShortcutsList.contains("org.eclipse.wst.server.launchShortcut"));
		Assert.assertEquals(2, launchShortcuts.length);
	}

	private void checkPackageExports(String[] expectedPackageExports, IBundleProjectDescription description) {
		IPackageExportDescription[] packageExports = description.getPackageExports();
		Assert.assertNotNull(packageExports);
		List<String> packageExportStrings = new ArrayList<String>();
		for (IPackageExportDescription currPackageExportDescription : packageExports) {
			packageExportStrings.add(currPackageExportDescription.getName());
		}
		Assert.assertEquals(expectedPackageExports.length, packageExports.length);
		List<String> expectedPackageExportList = Arrays.asList(expectedPackageExports);
		Assert.assertTrue(packageExportStrings.containsAll(expectedPackageExportList));
	}

	private void checkWebPackageImports(IBundleProjectDescription description) {
		IPackageImportDescription[] packageImports = description.getPackageImports();
		List<String> packageImportStrings = new ArrayList<String>();
		for (IPackageImportDescription currPackageImportDescription : packageImports) {
			packageImportStrings.add(currPackageImportDescription.getName());
		}
		Assert.assertTrue(packageImportStrings.contains("javax.servlet"));
		Assert.assertTrue(packageImportStrings.contains("javax.servlet.http"));
		Assert.assertTrue(packageImportStrings.contains("javax.servlet.jsp"));
		Assert.assertTrue(packageImportStrings.contains("javax.servlet.jsp.el"));
		Assert.assertTrue(packageImportStrings.contains("javax.servlet.jsp.tagext"));
		Assert.assertTrue(packageImportStrings.contains("javax.el"));
	}
	
	private void checkJPAPackageImports(IBundleProjectDescription description) {
		IPackageImportDescription[] packageImports = description.getPackageImports();
		List<String> packageImportStrings = new ArrayList<String>();
		for (IPackageImportDescription currPackageImportDescription : packageImports) {
			packageImportStrings.add(currPackageImportDescription.getName());
		}
		Assert.assertTrue(packageImportStrings.contains("javax.persistence"));
	}
	
	private OSGiBundleFacetInstallConfig setupOSGiBundleFacetInstallConfig(String symbolicName, String bundleName, String vendor, String version) throws CoreException {
		
		OSGiBundleFacetInstallConfig osgiBundleFacetInstallConfig = new OSGiBundleFacetInstallConfig() {
			
			@Override
			public void setFacetedProjectWorkingCopy(IFacetedProjectWorkingCopy fpjwc) {
				//do nothing
			}
			
		};
    	osgiBundleFacetInstallConfig.getSymbolicNameValue().setValue(symbolicName);
    	osgiBundleFacetInstallConfig.getNameValue().setValue(bundleName);
    	osgiBundleFacetInstallConfig.getVendorValue().setValue(vendor);
    	osgiBundleFacetInstallConfig.getVersionValue().setValue(version);
    	
		return osgiBundleFacetInstallConfig;
		
	}
	
	private IProject importProjectInWorkspace(String projectZipLocation, String projectName) throws IOException, CoreException{
		String absolutePathToMavenProject = System.getProperty("user.dir");
		String localZipPath = absolutePathToMavenProject + IPath.SEPARATOR + projectZipLocation;
		ProjectUnzipUtil util = new ProjectUnzipUtil(new Path(localZipPath), new String[] {projectName});
		Assert.assertTrue(util.createProjects());
		return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
	}
	
	private boolean hasBuildSpec(IProject project, String builderId) throws CoreException {
		ICommand[] commands = project.getDescription().getBuildSpec();
		for (ICommand command : commands) {
			if (command.getBuilderName().equals(builderId)) {
				return true;
			}
		}
		return false;
	}
	
}
