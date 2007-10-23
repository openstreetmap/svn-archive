/**
 * 
 */
package at.dallermassl.josm.plugin.openvisible;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.Plugin;

/**
 * @author cdaller
 *
 */
public class OpenVisiblePlugin extends Plugin {
    
    public OpenVisiblePlugin() {
        super();
        JMenu fileMenu = Main.main.menu.fileMenu;
        //JMenu navigatorMenu = new JMenu("Open Visible");
        JMenuItem menuItem = new JMenuItem(new OpenVisibleAction());
        
        fileMenu.add(menuItem);

    }

}
