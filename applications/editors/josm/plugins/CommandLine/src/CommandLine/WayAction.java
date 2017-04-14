// License: GPL. For details, see LICENSE file.
package CommandLine;

import java.awt.Point;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;

public class WayAction extends AbstractOsmAction<Way> {

    public WayAction(CommandLine parentPlugin) {
        super(parentPlugin, "joinway");
    }

    @Override
    protected Way getNearest(Point mousePos) {
        return Main.map.mapView.getNearestWay(mousePos, OsmPrimitive::isUsable);
    }
}
