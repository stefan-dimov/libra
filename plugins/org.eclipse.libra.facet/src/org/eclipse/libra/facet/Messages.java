package org.eclipse.libra.facet;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.libra.facet.messages"; //$NON-NLS-1$
	public static String OSGiBundleFacetInstallConfig_EmptySymbolicName;
	public static String OSGiBundleFacetInstallConfig_EmptyVersion;
	public static String OSGiBundleFacetUninstallStrategy_FacetAndPluginNatureAndManifest;
	public static String OSGiBundleFacetUninstallStrategy_FacetAndPluginNatureButNotManifest;
	public static String OSGiBundleFacetUninstallStrategy_FacetOnly;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
