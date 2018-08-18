// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.columbusCSV;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * Plugin class to import Columbus V-900 CSV files into JOSM.
 * 
 * @author Oliver Wieland &lt;oliver.wieland@online.de&gt; Wieland
 * 
 */
public class ColumbusCSVPlugin extends Plugin {
    private static PreferenceSetting columbusSettings;

    /**
     * Plugin constructor (adds menu entry to file menu).
     * @param info plugin information
     */
    public ColumbusCSVPlugin(PluginInformation info) {
        super(info);
        
        ExtensionFileFilter.addImporter(new ColumbusCSVImporter());
    }
    
    /**
     * Called in the preferences dialog to create a preferences page for the plugin,
     * if any available.
     */
    @Override
    public PreferenceSetting getPreferenceSetting() {
        if (columbusSettings == null) {
            columbusSettings = new ColumbusCSVPreferences();
        }
        return columbusSettings;
    } 
}
