/*
 *  Copyright by Subhodip Biswas
 *  This program is free software and licensed under GPL.
 *
 */

package org.openstreetmap.josm.plugins.DirectUpload;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.tools.Shortcut;
/**
 *
 * @author subhodip
 */
public class UploadDataGuiPlugin extends Plugin{
    UploadAction openaction;

    public UploadDataGuiPlugin() {
        openaction = new UploadAction();
        Main.main.menu.toolsMenu.add(openaction);
    }

    class UploadAction extends JosmAction{
        public UploadAction(){
            super(tr("Upload Traces"), "UploadAction", tr("Uploads traces to openstreetmap.org"),
            Shortcut.registerShortcut("tools:uploadtraces", tr("Tool: {0}", tr("Upload Traces")),
            KeyEvent.VK_G, Shortcut.GROUP_MENU), false);
        }
        public void actionPerformed(ActionEvent e) {
            UploadDataGui go = new UploadDataGui();
            go.setVisible(true);
        }

        @Override
		protected void updateEnabledState() {
            if(Main.map == null
                    || Main.map.mapView == null
                    || Main.map.mapView.getActiveLayer() == null
                    || !(Main.map.mapView.getActiveLayer() instanceof GpxLayer)) {                
                setEnabled(false);
            } else {
            	setEnabled(true);
            }

		}		
    }
}