// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.commandline;

import java.awt.Point;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.MainApplication;

public class AnyAction extends AbstractOsmAction<OsmPrimitive> {

    public AnyAction(CommandLine parentPlugin) {
        super(parentPlugin, "joinnode");
    }

    @Override
    protected OsmPrimitive getNearest(Point mousePos) {
        return MainApplication.getMap().mapView.getNearestNodeOrWay(mousePos, OsmPrimitive::isUsable, false);
    }
}
