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
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.tools.Shortcut;

/**
 *
 * @author subhodip, ax
 */
public class UploadDataGuiPlugin extends Plugin {
    
    UploadAction openaction;

    public UploadDataGuiPlugin(PluginInformation info) {
        super(info);
        openaction = new UploadAction();
        Main.main.menu.toolsMenu.add(openaction);
    }

    class UploadAction extends JosmAction {
        
        public UploadAction(){
            super(tr("Upload Traces"), "UploadAction", tr("Uploads traces to openstreetmap.org"),
                Shortcut.registerShortcut("tools:uploadtraces", tr("Tool: {0}", tr("Upload Traces")),
                KeyEvent.VK_G, Shortcut.CTRL), false);
        }
        
        public void actionPerformed(ActionEvent e) {
            UploadDataGui go = new UploadDataGui();
            go.setVisible(true);
        }

        // because LayerListDialog doesn't provide a way to hook into "layer selection changed"
        // but the layer selection (*not* activation) is how we determine the layer to be uploaded
        // we have to let the upload trace menu always be enabled
//        @Override
//        protected void updateEnabledState() {
//            // enable button if ... @see autoSelectTrace()
//            if (UploadOsmConnection.getInstance().autoSelectTrace() == null) {
//                setEnabled(false);
//            } else {
//                setEnabled(true);
//            }
//        }
    }
}
