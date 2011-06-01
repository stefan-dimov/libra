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
package org.eclipse.libra.framework.core.internal.dependency;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;

public class DependencyUtil {

	public static Object[] getDependencies(BundleDescription bundleDescription) {

		if (bundleDescription == null) {
			return new BundleDescription[0];
		}

		List<Object> c = getDescription(bundleDescription.getRequiredBundles());
		c.addAll(getExportedDescription(bundleDescription,
				bundleDescription.getResolvedImports()));

		if (bundleDescription.getHost() != null) {
			c.add(bundleDescription.getHost().getSupplier());
		}

		return c.toArray(new Object[c.size()]);
	}

	public static List<Object> getDescription(
			BundleSpecification[] specifications) {
		ArrayList<Object> descriptionList = new ArrayList<Object>();
		for (int i = 0; i < specifications.length; i++) {
			BundleSpecification specification = specifications[i];
			if (specification.getSupplier() == null) {
				// We can't get a description, so just keep the bundle
				// specification
				descriptionList.add(specification);
			} else {
				descriptionList.add(specification.getSupplier());
			}
		}
		return descriptionList;
	}

	private static Set<BundleDescription> getExportedDescription(
			BundleDescription element,
			ExportPackageDescription[] exportedPackages) {
		Set<BundleDescription> descriptionList = new HashSet<BundleDescription>();
		for (int i = 0; i < exportedPackages.length; i++) {
			ExportPackageDescription exportedPackage = exportedPackages[i];
			if (!element.getLocation().equals(
					exportedPackage.getExporter().getLocation())) {
				descriptionList.add(exportedPackage.getExporter());
			}
		}
		return descriptionList;
	}
}
