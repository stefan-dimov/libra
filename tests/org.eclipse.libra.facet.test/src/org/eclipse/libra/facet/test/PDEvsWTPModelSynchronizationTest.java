package org.eclipse.libra.facet.test;

import java.io.IOException;

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
	
	
	// ------------------------------ private helper methods ----------------------------------------------
	
	private IProject importProjectInWorkspace(String projectZipLocation, String projectName) throws IOException, CoreException{
		String absolutePathToMavenProject = System.getProperty("user.dir");
		String localZipPath = absolutePathToMavenProject + IPath.SEPARATOR + projectZipLocation;
		ProjectUnzipUtil util = new ProjectUnzipUtil(new Path(localZipPath), new String[] {projectName});
		Assert.assertTrue(util.createProjects());
		return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
	}
	
	private boolean areModelsEqual(IProject wabProject) throws CoreException {
		String newPDEWebContextPath = OSGiBundleFacetUtils.getContextRootFromPDEModel(wabProject);
		String currentWTPWebContextPath = OSGiBundleFacetUtils.getContextRootFromWTPModel(wabProject);
		return newPDEWebContextPath.equals(currentWTPWebContextPath);
	}
	
	private void checkModels(IProject wabProject) throws CoreException {
		boolean equal = false;
    	
    	for (int attempt = 0; attempt < MAX_ATTEMPTS && !equal; attempt++) {    		
    		equal = areModelsEqual(wabProject);
    		if (!equal) {
    			// we need to wait for the other model to refresh
    			wabProject.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
    			try {
    				Thread.sleep(1000);// 1 second
    			} catch (InterruptedException iexc) {
    				System.out.println("Interrupted exception caught. Checking once more.");
    				wabProject.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
    				equal = areModelsEqual(wabProject);
    			}
    		}
    	}  	       	

    	// check that the models are equal
    	Assert.assertTrue(equal);

    	// check that both models are set to the new context root
		String expectedContextRoot = '/' + wabProject.getName() + APPEND;
    	Assert.assertEquals(expectedContextRoot, OSGiBundleFacetUtils.getContextRootFromPDEModel(wabProject));
    	Assert.assertEquals(expectedContextRoot, OSGiBundleFacetUtils.getContextRootFromWTPModel(wabProject));
	}

}
