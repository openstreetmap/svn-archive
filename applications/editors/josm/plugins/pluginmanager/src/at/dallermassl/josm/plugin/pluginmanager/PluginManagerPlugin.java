/**
 *
 */
package at.dallermassl.josm.plugin.pluginmanager;

import javax.swing.JMenu;

import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;

/**
 * @author cdaller
 *
 */
public class PluginManagerPlugin extends Plugin {

    public PluginManagerPlugin() {
//        JMenu menu = PluginHelper.getInstance().getMenu("tools");
//        menu.addSeparator();
//        menu.add(new PluginManagerAction());
    }

    @Override
    public PreferenceSetting getPreferenceSetting() {
        return new PluginManagerPreference();
    }

}
