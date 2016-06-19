package org.openstreetmap.josm.plugins.waypointSearch;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.layer.markerlayer.MarkerLayer;
import org.openstreetmap.josm.gui.layer.markerlayer.Marker;

import java.util.*;
import java.util.regex.Pattern;

class Engine {
    
    private Engine() {
        // Utility class
    }

    static List<Marker> searchGpxWaypoints(String waypointSearchPattern) {
        List<Marker> returnList = new ArrayList<>();
        if (gpxLayersExist()) {
            //Loop over marker (waypoint) layers.. it could be more than one
            for (Iterator<MarkerLayer> it = Main.getLayerManager().getLayersOfType(MarkerLayer.class).iterator(); it.hasNext();) {
                //loop over each marker (waypoint)
                for (Iterator<Marker> markerIterator = it.next().data.iterator(); markerIterator.hasNext();) {
                    Marker marker = markerIterator.next();
                    if (Pattern.matches(".*\\Q"+waypointSearchPattern.toLowerCase()+"\\E.*", marker.getText().toLowerCase())) {
                        returnList.add(marker);
                    }
                }               
            }
        } 
        return returnList;
    }   
    
    static boolean gpxLayersExist() {
        return !Main.getLayerManager().getLayersOfType(MarkerLayer.class).isEmpty();
    }
}
