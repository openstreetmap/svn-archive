package org.openstreetmap.josm.plugins.pt_assistant.utils;

import java.util.HashMap;
import java.util.List;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTStop;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTWay;

/**
 * Assigns stops to ways in following steps: (1) checks if the stop is in the
 * list of already assigned stops, (2) checks if the stop has a stop position,
 * (3) calculates it using proximity / growing bounding boxes
 * 
 * @author darya
 *
 */
public class StopToWayAssigner {

	/* contains assigned stops */
	private static HashMap<PTStop, PTWay> stopToWay = new HashMap<>();
	
	/* contains all PTWays of the route relation for which this assigner was created */
	private List<PTWay> ptways;
	
	public StopToWayAssigner(List<PTWay> ptways) {
		this.ptways = ptways;
	}

	public PTWay get(PTStop stop) {
		
		// 1) Search if this stop has already been assigned:
		if (stopToWay.containsKey(stop)) {
			return stopToWay.get(stop);
		}
		
		// 2) Search if the stop has a stop position:
		Node stopPosition = stop.getStopPosition();
		if (stopPosition != null) {
			
			// search in the referrers:
			List<OsmPrimitive> referrers = stopPosition.getReferrers();
			for (OsmPrimitive referredPrimitive: referrers) {
				if (referredPrimitive.getType().equals(OsmPrimitiveType.WAY)) {
					Way referredWay = (Way) referredPrimitive;
					for (PTWay ptway: ptways) {
						if (ptway.getWays().contains(referredWay)) {
							stopToWay.put(stop, ptway);
							return ptway;
						}
					}
				}
			}
			
		}
		
	   // 3) Run the growing-bounding-boxes algorithm:
		// TODO

		return null;
	}

	/**
	 * Remove a map entry
	 * FIXME: keys should be PTStop
	 * @param stopId
	 */
	public static void removeStopKey(Long stopId) {
		Long id = new Long(stopId);
		if (stopToWay.containsKey(id)) {
			stopToWay.remove(id);
		}
	}

	/**
	 * May be needed if the correspondence between stops and ways has changed
	 * significantly
	 */
	public static void reinitiate() {
		stopToWay = new HashMap<>();
	}

}
