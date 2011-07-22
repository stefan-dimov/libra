package org.eclipse.libra.facet.test;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.libra.facet.OSGiBundleFacetUtils;
import org.junit.Assert;
import org.junit.Test;

public class PDEvsWTPModelSynchronizationTest {

	private static final String APPEND = "Changed";	
	
	private static final String WAB_PRJ_LOCATION = "resources/TestWAB.zip_";
	private static final String WAB_PRJ_NAME = "TestWAB";
	
	private static final String WAB_PRJ_LOCATION_2 = "resources/TestWAB2.zip_";	
	private static final String WAB_PRJ_NAME_2 = "TestWAB2";
	
	private static final String WAB_PRJ_LOCATION_3 = "resources/TestWAB3.zip_";	
	private static final String WAB_PRJ_NAME_3 = "TestWAB3";	
	
	private static final String WAB_PRJ_LOCATION_4 = "resources/TestWAB4.zip_";	
	private static final String WAB_PRJ_NAME_4 = "TestWAB4";	
	
	
	private static int MAX_ATTEMPTS = 20;
	
	
	@Test
	public void checkPDEChangeLeadsToWTPChange() throws Exception {		
		IProject wabProject = importProjectInWorkspace(WAB_PRJ_LOCATION, WAB_PRJ_NAME);	    	
    	String newPDEWebContextPath = OSGiBundleFacetUtils.getContextRootFromPDEModel(wabProject) + APPEND;
    	OSGiBundleFacetUtils.setContextRootInPDEModel(wabProject, newPDEWebContextPath);
    	checkModels(wabProject);
	}
	
	@Test
	public void checkWTPChangeLeadsToPDEChange() throws Exception {
		IProject wabProject = importProjectInWorkspace(WAB_PRJ_LOCATION_2, WAB_PRJ_NAME_2);	    	
    	String newWTPWebContextPath = OSGiBundleFacetUtils.getContextRootFromWTPModel(wabProject) + APPEND;
    	OSGiBundleFacetUtils.setContextRootInWTPModel(wabProject, newWTPWebContextPath);        	
    	checkModels(wabProject);
	}
	
	@Test
	public void checkSettingsFileChangeLeadsToModelChange() throws Exception {
		IProject wabProject = importProjectInWorkspace(WAB_PRJ_LOCATION_3, WAB_PRJ_NAME_3);	 
		String newWTPWebContextPath = OSGiBundleFacetUtils.getContextRootFromWTPModel(wabProject) + APPEND;
		IFile settingsFile = wabProject.getFile(new Path(".settings/org.eclipse.wst.common.component"));
		Util.changeWebContextRootFromSettings(settingsFile, newWTPWebContextPath);
    	checkModels(wabProject);
	}	
	
	@Test
	public void checkManifetsFileChangeLeadsToModelChange() throws Exception {
		IProject wabProject = importProjectInWorkspace(WAB_PRJ_LOCATION_4, WAB_PRJ_NAME_4);	
		String oldPDEWebContextPath = OSGiBundleFacetUtils.getContextRootFromPDEModel(wabProject);
		String expectedPDEWebContextPath = OSGiBundleFacetUtils.getContextRootFromPDEModel(wabProject) + APPEND;
		IFile manifestFile = wabProject.getFile(new Path("WebContent/META-INF/MANIFEST.MF"));
		Util.changeWebContextRootInManifest(manifestFile, oldPDEWebContextPath, expectedPDEWebContextPath);
		wabProject.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
    	checkModels(wabProject);
	}		
	
	
	// ------------------------------ private helper methods ----------------------------------------------
	
	private IProject importProjectInWorkspace(String projectZipLocation, String projectName) throws IOException, CoreException{
		String absolutePathToMavenProject = System.getProperty("user.dir");
		String localZipPath = absolutePathToMavenProject + IPath.SEPARATOR + projectZipLocation;
		ProjectUnzipUtil util = new ProjectUnzipUtil(new Path(localZipPath), new String[] {projectName});
		Assert.assertTrue(util.createProjects());
		return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
	}
	
	private boolean areModelsEqualtoTheExpectedValue(IProject wabProject) throws CoreException {
		String expectedContextRoot = '/' + wabProject.getName() + APPEND;
		String newPDEWebContextPath = OSGiBundleFacetUtils.getContextRootFromPDEModel(wabProject);
		String newWTPWebContextPath = OSGiBundleFacetUtils.getContextRootFromWTPModel(wabProject);
		boolean equal =  newPDEWebContextPath.equals(newWTPWebContextPath);
		equal = equal && expectedContextRoot.equals(newPDEWebContextPath);
		equal = equal && expectedContextRoot.equals(newWTPWebContextPath);
		return equal;
	}
	
	private void checkModels(IProject wabProject) throws CoreException {
		boolean equal = false;
    	
    	for (int attempt = 0; attempt < MAX_ATTEMPTS && !equal; attempt++) {
    		try {
    			equal = areModelsEqualtoTheExpectedValue(wabProject);
    		} catch (Exception e) {
    			// Sometimes areModelsEqualtoTheExpectedValue(...) throws ResourceException
    			// because the resources are not synched with the file system
    			// This exception is being ignored and iterations continue,
    			// because two rows below the project id being refreshed ...
    		}
    		if (!equal) {
    			// we need to wait for the other model to refresh
    			wabProject.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
    			try {
    				Thread.sleep(1000);// 1 second
    			} catch (InterruptedException iexc) {
    				System.out.println("Interrupted exception caught. Checking once more.");
    				wabProject.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
    				equal = areModelsEqualtoTheExpectedValue(wabProject);
    			}
    		}
    	}  	       	

    	// check that the models are equal to the expected value
    	Assert.assertTrue(equal);
	}

}
