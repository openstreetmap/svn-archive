/** GPSBlam JOSM Plugin
 * Copyright (C) 2012 Russell Edwards
   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.openstreetmap.josm.plugins.gpsblam;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.net.URL;

import javax.swing.ImageIcon;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class GPSBlamPlugin extends Plugin {

    private IconToggleButton btn;
    private GPSBlamMode mode;

    public GPSBlamPlugin(PluginInformation info) {
        super(info);
        mode = new GPSBlamMode(Main.map, "gpsblam", tr("select gpx points and \"blam!\", find centre and direction of spread"));

        btn = new IconToggleButton(mode);
        btn.setVisible(true);
    }
 
    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        mode.setFrame(newFrame);
        if (oldFrame == null && newFrame != null) {
            if (Main.map != null)
                Main.map.addMapMode(btn);
        }
    }
    public static ImageIcon loadIcon(String name) {
        URL url = GPSBlamPlugin.class.getResource("/images/gpsblam.png");
        return new ImageIcon(url);
    }


}