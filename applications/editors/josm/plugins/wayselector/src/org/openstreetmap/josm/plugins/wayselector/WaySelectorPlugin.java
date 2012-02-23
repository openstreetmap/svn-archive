package org.openstreetmap.josm.plugins.wayselector;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Plugin class for the Way Selector plugin
 *
 * @author Marko Mäkelä
 */
public class WaySelectorPlugin extends Plugin {
    /**
     * Creates the plugin at JOSM startup
     *
     * @param info the plugin information describing the plugin.
     */
    public WaySelectorPlugin(PluginInformation info) {
        super(info);

        MainMenu.add(Main.main.menu.toolsMenu, new WaySelectAction());
    }

    private class WaySelectAction extends JosmAction {
        /** Set up the action (text appearing on the menu, keyboard shortcut etc */
        public WaySelectAction() {
            super(tr("Way Select"),
                  "way-select",
                  tr("Select non-branching sequences of ways"),
                  Shortcut.registerShortcut("wayselector:wayselect", tr("Way Select"), KeyEvent.VK_W, Shortcut.SHIFT),
                  true);
        }

        @Override
		public void actionPerformed(ActionEvent ev) {
        DataSet ds = Main.main.getCurrentDataSet();
        WaySelection ws = new WaySelection(ds.getSelectedWays());
        ws.extend(ds);
        }

        /**
         * Update the enabled state of the action when something in
         * the JOSM state changes, i.e. when a layer is removed or
         * added.
         */
        @Override
        protected void updateEnabledState() {
            setEnabled(Main.main.getCurrentDataSet() != null);
        }
    }
}
