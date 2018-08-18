// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.commandline;

import java.awt.Point;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;

public class WayAction extends AbstractOsmAction<Way> {

    public WayAction(CommandLine parentPlugin) {
        super(parentPlugin, "joinway");
    }

    @Override
    protected Way getNearest(Point mousePos) {
        return MainApplication.getMap().mapView.getNearestWay(mousePos, OsmPrimitive::isUsable);
    }
}
