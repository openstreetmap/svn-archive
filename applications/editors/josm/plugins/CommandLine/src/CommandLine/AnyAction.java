// License: GPL. For details, see LICENSE file.
package CommandLine;

import java.awt.Point;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

public class AnyAction extends AbstractOsmAction<OsmPrimitive> {

    public AnyAction(CommandLine parentPlugin) {
        super(parentPlugin, "joinnode");
    }

    @Override
    protected OsmPrimitive getNearest(Point mousePos) {
        return Main.map.mapView.getNearestNodeOrWay(mousePos, OsmPrimitive::isUsable, false);
    }
}
