/**
 * 
 */
package org.openstreetmap.josm.plugins.sumoconvert;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * @author ignacio_palermo
 *
 */
public class SumoExportAction extends JosmAction {
	
	public SumoExportAction(){
        super(tr("OSM Export"), "images/dialogs/logo-sumo.png",
        tr("Export traffic data to SUMO network file."),
        Shortcut.registerShortcut("menu:sumoexport", tr("Menu: {0}", tr("SUMO Export")),
        KeyEvent.VK_G, Shortcut.ALT_CTRL), false);
    }

	@Override
	public void actionPerformed(ActionEvent e) {
        MainApplication.worker.execute(new ExportTask());
	}
}
