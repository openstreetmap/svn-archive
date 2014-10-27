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

package org.openstreetmap.josm.plugins.columbusCSV;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * Plugin class to import Columbus V-900 CSV files into JOSM.
 * 
 * @author Oliver Wieland <oliver.wieland@online.de> Wieland
 * 
 */
public class ColumbusCSVPlugin extends Plugin {
    private static PreferenceSetting columbusSettings;
    /*
     * Plugin constructor (adds menu entry to file menu).
     */
    public ColumbusCSVPlugin(PluginInformation info) {
        super(info);
        
        ExtensionFileFilter.importers.add(new ColumbusCSVImporter());
    }
    
    /**
     * Called in the preferences dialog to create a preferences page for the plugin,
     * if any available.
     */
    public PreferenceSetting getPreferenceSetting() {
        if (columbusSettings == null) {
            columbusSettings = new ColumbusCSVPreferences();
        }
        return columbusSettings;
    } 
}
