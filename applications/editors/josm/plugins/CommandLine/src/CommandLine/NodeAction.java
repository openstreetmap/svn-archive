// License: GPL. For details, see LICENSE file.
package CommandLine;

import java.awt.Point;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

public class NodeAction extends AbstractOsmAction<Node> {

    public NodeAction(CommandLine parentPlugin) {
        super(parentPlugin, "joinnode");
    }

    @Override
    protected Node getNearest(Point mousePos) {
        return Main.map.mapView.getNearestNode(mousePos, OsmPrimitive::isUsable);
    }
}
