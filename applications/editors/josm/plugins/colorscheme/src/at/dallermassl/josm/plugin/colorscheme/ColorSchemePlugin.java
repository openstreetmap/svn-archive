/**
 * 
 */
package at.dallermassl.josm.plugin.colorscheme;

import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;

/**
 * @author cdaller
 *
 */
public class ColorSchemePlugin extends Plugin {
    
    /**
     * Default Constructor
     */
    public ColorSchemePlugin() {
        
    }

    @Override
    public PreferenceSetting getPreferenceSetting() {
        return new ColorSchemePreference();
    }
    
    

}
