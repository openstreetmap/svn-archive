package org.openstreetmap.josm.plugins.waypointSearch;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.layer.markerlayer.MarkerLayer;
import org.openstreetmap.josm.gui.layer.markerlayer.Marker;

import java.util.*;
import java.util.regex.Pattern;

public class Engine {
	
	public List<Marker> searchGpxWaypoints(String waypointSearchPattern) {
		List<Marker> returnList = new ArrayList<Marker>();
		if (gpxLayersExist()) {
			//Loop over marker (waypoint) layers.. it could be more than one
			for (Iterator<MarkerLayer> layerIterator = Main.map.mapView.getLayersOfType(MarkerLayer.class).iterator(); layerIterator.hasNext();) {
				//loop over each marker (waypoint)
				for (Iterator<Marker> markerIterator = layerIterator.next().data.iterator(); markerIterator.hasNext();) {
					Marker marker = markerIterator.next();
					if (Pattern.matches(".*\\Q"+waypointSearchPattern.toLowerCase()+"\\E.*", marker.getText().toLowerCase())) {
						returnList.add(marker);
					}
				}				
			}
		} 
		return returnList;
	}	
		
		
		
	

	
	
	
    public boolean gpxLayersExist() {
  	  boolean rv = false;
  	  if (Main.map != null) {
  		  if (Main.map.mapView.hasLayers()) {
  			  if (Main.map.mapView.getLayersOfType(MarkerLayer.class).size()>0) {
  				  rv = true;
  			  }
  		  }
  	  }
  	  return rv;
    }
	
	
	
}
