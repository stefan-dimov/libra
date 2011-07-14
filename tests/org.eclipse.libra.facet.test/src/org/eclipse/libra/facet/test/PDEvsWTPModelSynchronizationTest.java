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
	
	private static final String WAB_PRJ_LOCATION = "resources/TestWAB.zip_";
	private static final String WAB_PRJ_NAME = "TestWAB";	
	
	private static final String WAB_PRJ_LOCATION_2 = "resources/TestWAB2.zip_";	
	private static final String WAB_PRJ_NAME_2 = "TestWAB2";
	
	private static final String APPEND = "Changed";	
	private static int MAX_ATTEMPTS = 20;
	
	
	@Test
	public void checkPDEChangeLeadsToWTPChange() throws Exception {		
		
		IProject wabProject = importProjectInWorkspace(WAB_PRJ_LOCATION, WAB_PRJ_NAME);	    	
    	String newPDEWebContextPath = OSGiBundleFacetUtils.getContextRootFromPDEModel(wabProject) + APPEND;
    	OSGiBundleFacetUtils.setContextRootInPDEModel(wabProject, newPDEWebContextPath);
    	    	
    	boolean equals = false;
    	
    	for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
    		
    		newPDEWebContextPath = OSGiBundleFacetUtils.getContextRootFromPDEModel(wabProject);
    		String currentWTPWebContextPath = OSGiBundleFacetUtils.getContextRootFromWTPModel(wabProject);
    		equals = newPDEWebContextPath.equals(currentWTPWebContextPath);
    		  			
    		if (!equals) {
    			wabProject.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
    			try {
    				Thread.sleep(1*1000);
    			} catch (InterruptedException iexc) {
    				System.out.println("Interrupted exception caught. Checking once more.");
    				
    				
    				newPDEWebContextPath = OSGiBundleFacetUtils.getContextRootFromPDEModel(wabProject);
    	    		currentWTPWebContextPath = OSGiBundleFacetUtils.getContextRootFromWTPModel(wabProject);
    	    		equals = newPDEWebContextPath.equals(currentWTPWebContextPath);
    				
    				Assert.assertTrue(equals);
    				return;
    			}
    		} else {
    			break;
    		}
    		
    	}  	    	
    			
		Assert.assertTrue(equals);
		
	}
	
	@Test
	public void checkWTPChangeLeadsToPDEChange() throws Exception {
		
		
		IProject wabProject2 = importProjectInWorkspace(WAB_PRJ_LOCATION_2, WAB_PRJ_NAME_2);	    	
    	String newWTPWebContextPath = OSGiBundleFacetUtils.getContextRootFromWTPModel(wabProject2) + APPEND;
    	OSGiBundleFacetUtils.setContextRootInWTPModel(wabProject2, newWTPWebContextPath);        	
    	    	
    	boolean equals = false;
    	
    	for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {    		
    		
    		newWTPWebContextPath = OSGiBundleFacetUtils.getContextRootFromWTPModel(wabProject2);
    		String currentPDEWebContextPath = OSGiBundleFacetUtils.getContextRootFromPDEModel(wabProject2);	
    		equals = newWTPWebContextPath.equals(currentPDEWebContextPath);
  			
    		if (!equals) {
    			wabProject2.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
    			try {
    				Thread.sleep(1*1000);
    			} catch (InterruptedException iexc) {
    				System.out.println("Interrupted exception caught. Checking once more.");
    				
    				newWTPWebContextPath = OSGiBundleFacetUtils.getContextRootFromWTPModel(wabProject2);
    	    		currentPDEWebContextPath = OSGiBundleFacetUtils.getContextRootFromPDEModel(wabProject2);	
    	    		equals = newWTPWebContextPath.equals(currentPDEWebContextPath);
    				Assert.assertTrue(equals);
    				
    				return;
    			}
    		} else {
    			break;
    		}
    	}  	       	
    	Assert.assertTrue(equals);
		
	}
	
	
	// ------------------------------ private helper methods ----------------------------------------------
	
	private IProject importProjectInWorkspace(String projectZipLocation, String projectName) throws IOException, CoreException{
		String absolutePathToMavenProject = System.getProperty("user.dir");
		String localZipPath = absolutePathToMavenProject + IPath.SEPARATOR + projectZipLocation;
		ProjectUnzipUtil util = new ProjectUnzipUtil(new Path(localZipPath), new String[] {projectName});
		Assert.assertTrue(util.createProjects());
		return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
	}

}
