/**
 *
 */
package at.dallermassl.josm.plugin.openvisible;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.actions.JosmAction;

/**
 * @author cdaller
 *
 */
public class OpenVisiblePlugin extends Plugin {

    public OpenVisiblePlugin() {
        super();
        MainMenu.add(Main.main.menu.fileMenu, new OpenVisibleAction());
    }

}
