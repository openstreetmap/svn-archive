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
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * The Class FixAddressesPlugin is the main entry point for the plugin.
 */
public class FixAddressesPlugin extends Plugin {

	/**
	 * Constructor for the AddressEdit plugin. Called by JOSM when loading the plugin.
	 * @param info Context information of the plugin.
	 */
	public FixAddressesPlugin(PluginInformation info) {
		super(info);
		
		// Create action for edit...
		FixUnresolvedStreetsAction action = new FixUnresolvedStreetsAction();
		SelectIncompleteAddressesAction incAddrAction = new SelectIncompleteAddressesAction();
		// ... and add it to the tools menu in main
		Main.main.menu.toolsMenu.addSeparator();
        Main.main.menu.toolsMenu.add(action);
        Main.main.menu.toolsMenu.add(incAddrAction);
	}

}
