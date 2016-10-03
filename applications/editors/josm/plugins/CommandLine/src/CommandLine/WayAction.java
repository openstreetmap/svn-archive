// License: GPL. For details, see LICENSE file.
package CommandLine;

import java.awt.Point;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MapFrame;

public class WayAction extends AbstractOsmAction<Way> {

    public WayAction(MapFrame mapFrame, CommandLine parentPlugin) {
        super(mapFrame, parentPlugin, "joinway");
    }

    @Override
    protected Way getNearest(Point mousePos) {
        return Main.map.mapView.getNearestWay(mousePos, OsmPrimitive::isUsable);
    }
}
