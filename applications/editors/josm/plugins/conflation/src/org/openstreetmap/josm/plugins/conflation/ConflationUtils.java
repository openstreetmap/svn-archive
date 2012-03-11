package org.openstreetmap.josm.plugins.conflation;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.tools.StringMetrics;

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
    public static double calcCost(OsmPrimitive referenceObject, OsmPrimitive subjectObject, ConflationSettings settings) {
        double cost;

        if (referenceObject==subjectObject) {
            return MAX_COST;
        }

        double distance = 0;
        double stringCost = 1.0;
        if (settings.distanceWeight != 0) {
            distance = getCenter(referenceObject).distance(getCenter(subjectObject));
        }
        if (settings.stringWeight != 0) {
            String referenceString = referenceObject.getKeys().get(settings.keyString);
            String subjectString = subjectObject.getKeys().get(settings.keyString);
            
            if (referenceString == null ? subjectString == null : referenceString.equals(subjectString))
                stringCost = 0.0;
            else if (referenceString == null || subjectString == null)
                stringCost = 1.0;
            else
                stringCost = 1.0 - StringMetrics.getByName("levenshtein").getSimilarity(subjectString, referenceString);
        }
        
        if (distance > settings.distanceCutoff || stringCost > settings.stringCutoff)
            cost = MAX_COST;
        else
            cost = distance * settings.distanceWeight + stringCost * settings.stringWeight;

        return cost;
    }
}
