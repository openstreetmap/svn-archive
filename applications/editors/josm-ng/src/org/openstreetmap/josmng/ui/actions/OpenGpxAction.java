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

import org.openstreetmap.josmng.gpx.GpxLayer;
import org.openstreetmap.josmng.ui.Main;
import org.openstreetmap.josmng.view.Layer;
import org.openstreetmap.josmng.view.MapView;

/**
 *
 * @author nenik
 */
public class OpenGpxAction extends AbstractAction {

    public OpenGpxAction() {
        super("Open GPX File...");
        
    }

    public void actionPerformed(ActionEvent e) {
        doOpen("log-2007-12-31.gpx");
    }

    private void doOpen(String fName) {
        try {
            MapView view = Main.main.getMapView();
            Layer layer = new GpxLayer(view, "GPX", new FileInputStream(fName));
            view.addLayer(layer);
        } catch (IOException ex) {
            System.err.println("open failed:");
            ex.printStackTrace();
        }
    }
}
