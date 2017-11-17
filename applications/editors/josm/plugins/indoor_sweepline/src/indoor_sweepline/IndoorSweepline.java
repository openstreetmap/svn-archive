// License: GPL. For details, see LICENSE file.
package indoor_sweepline;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class IndoorSweepline extends Plugin {
    /**
     * Will be invoked by JOSM to bootstrap the plugin
     *
     * @param info  information about the plugin and its local installation
     */
    public IndoorSweepline(PluginInformation info) {
        super(info);
        refreshMenu();
    }

    public static void refreshMenu() {
        JMenu menu = MainApplication.getMenu().moreToolsMenu;
        if (menu.isVisible())
            menu.addSeparator();
        else
            menu.setVisible(true);
        menu.add(new JMenuItem(new IndoorSweeplineWizardAction()));
    }
}
