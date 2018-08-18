// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.commandline;

import java.awt.Point;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.MainApplication;

public class NodeAction extends AbstractOsmAction<Node> {

    public NodeAction(CommandLine parentPlugin) {
        super(parentPlugin, "joinnode");
    }

    @Override
    protected Node getNearest(Point mousePos) {
        return MainApplication.getMap().mapView.getNearestNode(mousePos, OsmPrimitive::isUsable);
    }
}
