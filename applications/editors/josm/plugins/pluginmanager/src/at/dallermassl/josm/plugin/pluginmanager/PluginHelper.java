/**
 * 
 */
package at.dallermassl.josm.plugin.pluginmanager;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JMenu;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.PluginProxy;

/**
 * @author cdaller
 *
 */
public class PluginHelper {
    
    private static PluginHelper INSTANCE = new PluginHelper();
    private Map<String, JMenu>menus;
    private VariableHelper variableHelper;
    
    /**
     * Private constructor
     */
    private PluginHelper() {
        menus = new HashMap<String, JMenu>();
        registerMenu("file", Main.main.menu.fileMenu);
        registerMenu("view", Main.main.menu.viewMenu);
        registerMenu("edit", Main.main.menu.editMenu);
        registerMenu("tools", Main.main.menu.toolsMenu);
//        registerMenu("connection", Main.main.menu.connectionMenu);
//        registerMenu("layer", Main.main.menu.layerMenu);
        registerMenu("help", Main.main.menu.helpMenu);
        
        variableHelper = new VariableHelper();
        variableHelper.addAll(Main.pref.getAllPrefix(""));
        variableHelper.add("josm.user.dir", Main.pref.getPreferencesDir());
    }
    
    /**
     * Returns the singleton instance of this helper.
     * @return the singleton instance of this helper.
     */
    public static PluginHelper getInstance() {
        return INSTANCE;
    }
    
    /**
     * Adds a menu to the main menu of JOSM and registers it under the given id.
     * @param menuId the id to register the menu, so other plugins can retrieve
     * the menu with {@link #getMenu(String)}
     * @param menu the menu to add.
     */
    public void addMainMenu(String menuId, JMenu menu) {
        Main.main.menu.add(menu);
        registerMenu(menuId, menu);
    }
    
    /**
     * Register the menu under the given id so other plugins can retrieve
     * the menu with {@link #getMenu(String)}. 
     * @param menuId
     * @param menu
     */
    public void registerMenu(String menuId, JMenu menu) {
        menus.put(menuId, menu);
    }
    
    /**
     * Returns the main menu that was registered with the given id or <code>null</code> 
     * if no menu was registered with that id. The default menus have the ids:
     * <code>file</code>, <code>view</code>, <code>edit</code>, <code>tools</code>,
     * <code>connection</code>, <code>layer</code>, <code>help</code>,
     * @return the main menu.
     */
    public JMenu getMenu(String menuId) {
        return menus.get(menuId);
    }

    /**
     * @return the variableHelper
     */
    public VariableHelper getVariableHelper() {
        return this.variableHelper;
    }
    
    /**
     * Returns the plugin with the given id or <code>null</code>
     * if the plugin is not installed.
     * @param pluginName the name of the plugin
     * @returnthe plugin with the given name or <code>null</code>
     * if the plugin is not installed.
     */
    public PluginInformation getPluginInfo(String pluginId) {
        for(PluginProxy plugin : Main.plugins) {
            System.out.println("compare id " + pluginId + " with " + plugin.info.name);
            if(pluginId.equals(plugin.info.name)) {
                return plugin.info;
            }
        }
        return null;
    }
    

    

}
