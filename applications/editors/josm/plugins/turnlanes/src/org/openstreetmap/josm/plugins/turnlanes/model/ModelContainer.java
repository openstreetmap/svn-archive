package org.openstreetmap.josm.plugins.turnlanes.model;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;

public class ModelContainer {
	public static ModelContainer create(Node n) {
		final ModelContainer container = new ModelContainer(n);
		container.getOrCreateJunction(n);
		return container;
	}
	
	private final Map<Node, Junction> junctions = new HashMap<Node, Junction>();
	private final Map<Way, Road> roads = new HashMap<Way, Road>();
	
	private final Node primary;
	
	private ModelContainer(Node primary) {
		this.primary = primary;
	}
	
	Junction getOrCreateJunction(Node n) {
		final Junction existing = junctions.get(n);
		
		if (existing != null) {
			return existing;
		}
		
		return new Junction(this, n);
	}
	
	public Junction getJunction(Node n) {
		Junction j = junctions.get(n);
		
		if (j == null) {
			throw new IllegalArgumentException();
		}
		
		return j;
	}
	
	Road getRoad(Way w, Junction j) {
		final Road existing = roads.get(w);
		
		if (existing != null && j.equals(existing.getToEnd().getJunction())) {
			return existing;
		}
		
		final Road newRoad = new Road(this, w, j);
		
		for (Route.Segment s : newRoad.getRoute().getSegments()) {
			final Road oldRoad = roads.put(s.getWay(), newRoad);
			
			if (oldRoad != null) {
				return mergeRoads(oldRoad, newRoad);
			}
		}
		
		return newRoad;
	}
	
	private Road mergeRoads(Road a, Road b) {
		throw null; // TODO implement
	}
	
	void register(Junction j) {
		if (junctions.put(j.getNode(), j) != null) {
			throw new IllegalStateException();
		}
	}
	
	Junction getPrimary() {
		return junctions.get(primary);
	}
}
