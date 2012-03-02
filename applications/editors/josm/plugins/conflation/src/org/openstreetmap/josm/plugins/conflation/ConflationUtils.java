package org.openstreetmap.josm.plugins.conflation;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

public final class ConflationUtils {
    private final static double MAX_COST = Double.MAX_VALUE;
        
    public static EastNorth getCenter(OsmPrimitive prim) {
            LatLon center = prim.getBBox().getTopLeft().getCenter(prim.getBBox().getBottomRight());
            return Main.map.mapView.getProjection().latlon2eastNorth(center);
    }
    
    /**
     * Calculate the cost of a pair of <code>OsmPrimitive</code>'s. A
     * simple cost consisting of the Euclidean distance is used
     * now, later we can also use dissimilarity between tags.
     *
     * @param   referenceObject      the reference <code>OsmPrimitive</code>.
     * @param   subjectObject   the non-reference <code>OsmPrimitive</code>.
     */
    public static double calcCost(OsmPrimitive referenceObject, OsmPrimitive subjectObject) {
        if (referenceObject==subjectObject) {
            return MAX_COST;
        }
        
        try {
            return getCenter(referenceObject).distance(getCenter(subjectObject));
        } catch (Exception e) {
            return MAX_COST;
        }

        // TODO: use other "distance" measures, i.e. matching tags
    }
}
