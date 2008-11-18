/**
 * 
 */
package at.dallermassl.josm.plugin.openvisible;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.actions.JosmAction;

/**
 * @author cdaller
 *
 */
public class OpenVisiblePlugin extends Plugin {
    
    public OpenVisiblePlugin() {
        super();
        JMenu fileMenu = Main.main.menu.fileMenu;
        JosmAction openVisible = new OpenVisibleAction();

        //JMenu navigatorMenu = new JMenu("Open Visible");
        JMenuItem menuItem = new JMenuItem(openVisible);
        
        fileMenu.add(menuItem,2);
        menuItem.setAccelerator(openVisible.shortcut);
    }

}
