package org.eclipse.libra.facet.ui.popup.actions;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.libra.facet.ui.popup.actions.messages"; //$NON-NLS-1$
	public static String ConvertProjectsToBundlesAction_NoProjectToConvertDescription;
	public static String ConvertProjectsToBundlesAction_NoProjectToConvertTitle;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
