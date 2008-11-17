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
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.Plugin;
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

class UploadAction extends JosmAction {
      
        public UploadAction(){
            super(tr("Upload Traces"), "UploadAction", tr("Uploads traces to openstreetmap.org"), KeyEvent.VK_G, KeyEvent.CTRL_MASK,
            false);
        }
        public void actionPerformed(ActionEvent e) {
            UploadDataGui go = new UploadDataGui();
            go.setVisible(true);
     
            }
     }
     
}