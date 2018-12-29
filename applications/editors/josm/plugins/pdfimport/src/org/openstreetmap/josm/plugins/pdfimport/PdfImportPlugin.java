// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pdfimport;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * A plugin to import a PDF file.
 */

public class PdfImportPlugin extends Plugin {


	public PdfImportPlugin(PluginInformation info) {
		super(info);
		MainMenu.add(MainApplication.getMenu().imagerySubMenu, new PdfImportAction());
		new Preferences(getPluginInformation().name);
	}

	@Override
	public PreferenceSetting getPreferenceSetting() {
		return new PreferenceSettings();
		}

}
