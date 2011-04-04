package org.openstreetmap.josm.plugins.turnlanes.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;

public class Junction {
	private final ModelContainer container;
	
	private final Node node;
	private final Set<Way> roads = new HashSet<Way>();
	
	Junction(ModelContainer container, Node n) {
		this.container = container;
		this.node = n;
		
		container.register(this);
		
		if (isPrimary()) {
			// if turn data is invalid, this will force an exception now, not later during painting
			// getTurns(); TODO force this again
		}
	}
	
	public boolean isPrimary() {
		return getContainer().isPrimary(this);
	}
	
	public Node getNode() {
		return node;
	}
	
	public List<Road> getRoads() {
		final List<Road> result = new ArrayList<Road>(roads.size());
		
		for (Way w : roads) {
			result.add(container.getRoad(w));
		}
		
		return result;
	}
	
	public List<Road.End> getRoadEnds() {
		final List<Road.End> result = new ArrayList<Road.End>(roads.size());
		
		for (Way w : roads) {
			result.add(getRoadEnd(w));
		}
		
		return result;
	}
	
	void addRoad(Way w) {
		roads.add(w);
	}
	
	Road.End getRoadEnd(Way w) {
		final Road r = getContainer().getRoad(w);
		
		if (r.getRoute().getSegments().size() == 1) {
			final boolean starts = r.getRoute().getStart().equals(node);
			final boolean ends = r.getRoute().getEnd().equals(node);
			
			if (starts && ends) {
				throw new IllegalArgumentException("Ambiguous: The way starts and ends at the junction node.");
			} else if (starts) {
				return r.getFromEnd();
			} else if (ends) {
				return r.getToEnd();
			}
		} else if (r.getRoute().getFirstSegment().getWay().equals(w)) {
			return r.getFromEnd();
		} else if (r.getRoute().getLastSegment().getWay().equals(w)) {
			return r.getToEnd();
		}
		
		throw new IllegalArgumentException("While there exists a road for the given way, the way neither "
		    + "starts nor ends at the junction node.");
	}
	
	public ModelContainer getContainer() {
		return container;
	}
}
