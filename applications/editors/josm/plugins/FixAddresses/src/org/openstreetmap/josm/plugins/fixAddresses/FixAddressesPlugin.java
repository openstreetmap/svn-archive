/**
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.openstreetmap.josm.plugins.fixAddresses;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.fixAddresses.gui.IncompleteAddressesDialog;
import org.openstreetmap.josm.plugins.fixAddresses.gui.actions.SelectIncompleteAddressesAction;

/**
 * The Class FixAddressesPlugin is the main entry point for the plugin.
 */
public class FixAddressesPlugin extends Plugin {
	private static IncompleteAddressesDialog incompleteAddrDlg;
	private static FixAddressesPreferences preferences;

	/**
	 * Constructor for the AddressEdit plugin. Called by JOSM when loading the plugin.
	 * @param info Context information of the plugin.
	 */
	public FixAddressesPlugin(PluginInformation info) {
		super(info);

		// Create actions...
		FixUnresolvedStreetsAction action = new FixUnresolvedStreetsAction();
		SelectIncompleteAddressesAction incAddrAction = new SelectIncompleteAddressesAction();

		// ... and add them to the tools menu in main
		Main.main.menu.toolsMenu.addSeparator();
		Main.main.menu.toolsMenu.add(action);
		Main.main.menu.toolsMenu.add(incAddrAction);

		// create preferences instance
		preferences = (FixAddressesPreferences) new FixAddressesPreferences.Factory().createPreferenceSetting();
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.Plugin#mapFrameInitialized(org.openstreetmap.josm.gui.MapFrame, org.openstreetmap.josm.gui.MapFrame)
	 */
	@Override
	public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
		// TODO Auto-generated method stub
		super.mapFrameInitialized(oldFrame, newFrame);

		if (newFrame != null) {
			incompleteAddrDlg = new IncompleteAddressesDialog();
			FixAddressesMapMode faMode = new FixAddressesMapMode(Main.map);
			IconToggleButton faModeButton = new IconToggleButton(faMode);
			faModeButton.setVisible(true);
			newFrame.addToggleDialog(incompleteAddrDlg);
		}
	}

	@Override
	public PreferenceSetting getPreferenceSetting() {
		// TODO Auto-generated method stub
		return getPreferences();
	}

	/**
	 * @return the incompleteAddrDlg
	 */
	protected static IncompleteAddressesDialog getIncompleteAddrDlg() {
		return incompleteAddrDlg;
	}

	/**
	 * Gets the preferences instance for this plugin.
	 *
	 * @return the preferences
	 */
	public static FixAddressesPreferences getPreferences() {
		return preferences;
	}
}
