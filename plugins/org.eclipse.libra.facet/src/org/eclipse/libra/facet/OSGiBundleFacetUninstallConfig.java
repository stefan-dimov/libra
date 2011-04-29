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
import org.eclipse.core.databinding.observable.value.SelectObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.wst.common.project.facet.core.ActionConfig;

public class OSGiBundleFacetUninstallConfig extends ActionConfig {
	
	private SelectObservableValue strategyValue;
	private WritableValue[] optionValues;
	
	public OSGiBundleFacetUninstallConfig() {
		Realm realm = OSGiBundleFacetRealm.getRealm();
		
		strategyValue = new SelectObservableValue(realm, OSGiBundleFacetUninstallStrategy.class);
		optionValues = new WritableValue[OSGiBundleFacetUninstallStrategy.values().length];
		for (int i = 0; i < optionValues.length; i++) {
			optionValues[i] = new WritableValue(realm, null, Boolean.class);
			strategyValue.addOption(OSGiBundleFacetUninstallStrategy.values()[i], optionValues[i]);
		}
		
		strategyValue.setValue(OSGiBundleFacetUninstallStrategy.defaultStrategy());
	}

	public SelectObservableValue getStrategyValue() {
		return strategyValue;
	}
	
	public OSGiBundleFacetUninstallStrategy getStrategy() {
		return (OSGiBundleFacetUninstallStrategy) strategyValue.getValue();
	}
	
	public void setStrategy(OSGiBundleFacetUninstallStrategy strategy) {
		strategyValue.setValue(strategy);
	}
	
	public WritableValue[] getOptionValues() {
		return optionValues.clone();
	}
	
}
