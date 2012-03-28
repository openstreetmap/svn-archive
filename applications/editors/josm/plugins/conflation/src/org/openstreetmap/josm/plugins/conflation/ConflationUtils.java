package org.openstreetmap.josm.plugins.conflation;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

public final class ConflationUtils {

    public static EastNorth getCenter(OsmPrimitive prim) {
        LatLon center = prim.getBBox().getTopLeft().getCenter(prim.getBBox().getBottomRight());
        return Main.map.mapView.getProjection().latlon2eastNorth(center);
    }
}
