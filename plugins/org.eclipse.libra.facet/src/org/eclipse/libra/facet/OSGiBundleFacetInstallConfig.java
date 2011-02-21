/*******************************************************************************
 * Copyright (c) 2010, 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Kaloyan Raev (SAP AG) - initial API and implementation
 *******************************************************************************/
package org.eclipse.libra.facet;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.pde.core.project.IBundleProjectDescription;
import org.eclipse.pde.core.project.IBundleProjectService;
import org.eclipse.wst.common.project.facet.core.ActionConfig;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IFacetedProjectWorkingCopy;
import org.eclipse.wst.common.project.facet.core.events.IFacetedProjectEvent;
import org.eclipse.wst.common.project.facet.core.events.IFacetedProjectListener;
import org.osgi.framework.Version;


public class OSGiBundleFacetInstallConfig extends ActionConfig implements IFacetedProjectListener {
	
	private static final String QUALIFIER = "qualifier";

	private IObservableValue symbolicNameValue;
	private IObservableValue versionValue;
	private IObservableValue nameValue;
	private IObservableValue vendorValue;
	
	public OSGiBundleFacetInstallConfig() {
		Realm realm = OSGiBundleRealm.getRealm();
		
		symbolicNameValue = new WritableValue(realm, getDefaultSymbolicName(), String.class);
		versionValue = new WritableValue(realm, getDefaultVersion(), String.class);
		nameValue = new WritableValue(realm, getDefaultSymbolicName(), String.class);
		vendorValue = new WritableValue(realm, getDefaultVendor(), String.class);
	}
	
	public IObservableValue getSymbolicNameValue() {
		return symbolicNameValue;
	}

	public IObservableValue getVersionValue() {
		return versionValue;
	}

	public IObservableValue getNameValue() {
		return nameValue;
	}

	public IObservableValue getVendorValue() {
		return vendorValue;
	}

	public String getSymbolicName() {
		return (String) getSymbolicNameValue().getValue();
	}

	public Version getVersion() {
		return Version.parseVersion((String) getVersionValue().getValue());
	}

	public String getName() {
		return (String) getNameValue().getValue();
	}

	public String getVendor() {
		return (String) getVendorValue().getValue();
	}

	@Override
	public void setFacetedProjectWorkingCopy(IFacetedProjectWorkingCopy fpjwc) {
		super.setFacetedProjectWorkingCopy(fpjwc);
		fpjwc.addListener(this, IFacetedProjectEvent.Type.PROJECT_NAME_CHANGED);
		updateDefaultValues();
	}

	public void handleEvent(IFacetedProjectEvent event) {
		if (event.getType() == IFacetedProjectEvent.Type.PROJECT_NAME_CHANGED) {
			updateDefaultNameValues();
		}
	}

	private void updateDefaultNameValues() {
		symbolicNameValue.setValue(getDefaultSymbolicName());
		nameValue.setValue(getDefaultName());
	}
	
	private void updateDefaultValues() {
		symbolicNameValue.setValue(getDefaultSymbolicName());
		versionValue.setValue(getDefaultVersion());
		nameValue.setValue(getDefaultName());
		vendorValue.setValue(getDefaultVendor());
	}

	private Object getDefaultSymbolicName() {
		String symbolicName = null;
		
		IFacetedProjectWorkingCopy fpjwc = getFacetedProjectWorkingCopy();
		if (fpjwc != null) {
			// check if there is a bundle model already available
			IBundleProjectDescription bundleProjectDescription = getBundleProjectDescription();
			if (bundleProjectDescription != null) {
				// there is a bundle model available - return the already available symbolic name
				symbolicName = bundleProjectDescription.getSymbolicName();
			}
			
			if (symbolicName == null) {
				// no bundle model available - return the project name as a default symbolic name
				symbolicName = fpjwc.getProjectName();
			}
		}
		
		return symbolicName;
	}
	
	private String getDefaultVersion() {
		Version version = new Version(1, 0, 0, QUALIFIER);
		
		// check if there is a bundle model already available
		IBundleProjectDescription bundleProjectDescription = getBundleProjectDescription();
		if (bundleProjectDescription != null) {
			// there is a bundle model available - return the already available vendor
			Version v = bundleProjectDescription.getBundleVersion();
			if (v != null) {
				version = v;
			}
		}
		
		return version.toString();
	}

	private Object getDefaultName() {
		String bundleName = null;
		
		IFacetedProjectWorkingCopy fpjwc = getFacetedProjectWorkingCopy();
		if (fpjwc != null) {
			// check if there is a bundle model already available
			IBundleProjectDescription bundleProjectDescription = getBundleProjectDescription();
			if (bundleProjectDescription != null) {
				// there is a bundle model available - return the already available bundle name
				bundleName = bundleProjectDescription.getBundleName();
			}
			
			if (bundleName == null) {
				// no bundle model available - return the capitalized project name as a default bundle name
				bundleName = fpjwc.getProjectName();
				// capitalize the first letter
				if (bundleName != null && bundleName.length() > 0 && !Character.isTitleCase(bundleName.charAt(0))) {
					StringBuilder builder = new StringBuilder(bundleName);
					builder.replace(0, 1, String.valueOf(Character.toTitleCase(bundleName.charAt(0))));
					bundleName = builder.toString();
				}				
			}
		}
		
		return bundleName;
	}

	private Object getDefaultVendor() {
		String vendor = null;
		
		// check if there is a bundle model already available
		IBundleProjectDescription bundleProjectDescription = getBundleProjectDescription();
		if (bundleProjectDescription != null) {
			// there is a bundle model available - return the already available vendor
			vendor = bundleProjectDescription.getBundleVendor();
		}
		
		return vendor;
	}
	
	private IBundleProjectDescription getBundleProjectDescription() {
		IFacetedProjectWorkingCopy fpjwc = getFacetedProjectWorkingCopy();
		if (fpjwc == null) 
			return null;
		
		IFacetedProject fproj = fpjwc.getFacetedProject();
		if (fproj == null)
			return null;
		
		IProject project = fproj.getProject(); 
		IBundleProjectService bundleProjectService = Activator.getDefault().getBundleProjectService();
		try {
			return bundleProjectService.getDescription(project);
		} catch (CoreException e) {
			return null;
		}
	}

	public static class SymbolicNameValidator implements IValidator {

		public IStatus validate(Object value) {
			String symbolicName = (String) value;
			
			if (symbolicName == null || symbolicName.trim().length() == 0) {
				return ValidationStatus.error("Symbolic name cannot be empty.");
			}
			
			return ValidationStatus.ok();
		}
		
	}
	
	public static class VersionValidator implements IValidator {

		public IStatus validate(Object value) {
			String version = (String) value;
			
			if (version == null || version.trim().length() == 0) {
				return ValidationStatus.error("Version cannot be empty.");
			}
			
			return ValidationStatus.ok();
		}

	}
	
}