/**
 * This plugin leverages JOSM to import TangoGPS files.
 */
package org.openstreetmap.josm.plugins;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.io.TangoGPS;

public class DataImport extends Plugin {

	/**
	 * Add new File import filter into open dialog
	 */
	public DataImport() {
		super();
		ExtensionFileFilter.importers.add(new TangoGPS());
	}

}
