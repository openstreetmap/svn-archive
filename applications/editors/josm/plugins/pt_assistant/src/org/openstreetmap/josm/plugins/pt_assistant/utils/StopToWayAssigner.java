package org.openstreetmap.josm.plugins.pt_assistant.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;

public final class StopToWayAssigner {

	private static HashMap<Long, Way> stopToWay = new HashMap<>();

	private StopToWayAssigner(Relation r) {
		// Hide default constructor for utils classes
	}

	public static Way getWay(OsmPrimitive stop, Relation route) {
		if (stopToWay.containsKey(stop.getId())) {
			return stopToWay.get(stop.getId());
		}
		

		
		
		if (stop.getType().equals(OsmPrimitiveType.NODE)) {
			List<OsmPrimitive> referrers = stop.getReferrers();
			List<Way> referredWays = new ArrayList<>();
			for (OsmPrimitive referrer: referrers) {
				if (referrer.getType().equals(OsmPrimitiveType.WAY)) {
					referredWays.add((Way)referrer);
				}
			}
			if (stop.hasTag("public_transport", "stop_position")) {
				// TODO
				Node n = (Node) stop;
			}
		}

		// TODO: algorithm with growing bounding boxes
		// TODO: if found, add to
		return null;
	}
	
	/**
	 * Remove a map entry
	 * @param stopId
	 */
	public static void removeStopKey(long stopId) {
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
