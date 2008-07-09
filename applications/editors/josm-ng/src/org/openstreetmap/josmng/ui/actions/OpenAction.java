/*
 *  JOSMng - a Java Open Street Map editor, the next generation.
 * 
 *  Copyright (C) 2008 Petr Nejedly <P.Nejedly@sh.cvut.cz>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */

package org.openstreetmap.josmng.ui.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import org.openstreetmap.josmng.utils.Convertor;
import org.openstreetmap.josmng.ui.Main;
import org.openstreetmap.josmng.utils.MenuPosition;
import org.openstreetmap.josmng.view.Layer;

/**
 *
 * @author nenik
 */
@MenuPosition("File") 
public class OpenAction extends AbstractAction {

    public OpenAction() {
        super("Open File...");
        
    }

    public void actionPerformed(ActionEvent e) {
        JFileChooser jfc = new JFileChooser();
        int returnVal = jfc.showOpenDialog(null);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            open(jfc.getSelectedFile());
        }
    }

    public static void open(String fName) {
        open(new File(fName));
    }

    public static void open(File file) {
        Layer layer = Convertor.<File,Layer>convert(file, Layer.class);
        if (layer != null) Main.main.getMapView().addLayer(layer);
    }

}
