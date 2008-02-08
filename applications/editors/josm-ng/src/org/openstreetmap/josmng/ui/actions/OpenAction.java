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
import java.io.FileInputStream;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import org.openstreetmap.josmng.osm.DataSet;
import org.openstreetmap.josmng.osm.io.OsmReader;
import org.openstreetmap.josmng.ui.Main;
import org.openstreetmap.josmng.view.Layer;
import org.openstreetmap.josmng.view.MapView;
import org.openstreetmap.josmng.view.osm.OsmLayer;

/**
 *
 * @author nenik
 */
public class OpenAction extends AbstractAction {

    public OpenAction() {
        super("Open File...");
        
    }

    public void actionPerformed(ActionEvent e) {
        JFileChooser jfc = new JFileChooser();
        int returnVal = jfc.showOpenDialog(null);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            doOpen(jfc.getSelectedFile().getAbsolutePath());
        }
    }

    private void doOpen(String fName) {
        try {
            DataSet ds = OsmReader.parse(new FileInputStream(fName));
            MapView view = Main.main.getMapView();
            Layer layer = new OsmLayer(view, "fName", ds);
            view.addLayer(layer);
        } catch (IOException ex) {
            System.err.println("open failed:");
            ex.printStackTrace();
        }
    }
}
