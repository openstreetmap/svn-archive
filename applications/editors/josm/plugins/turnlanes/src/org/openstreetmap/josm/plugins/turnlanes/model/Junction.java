package org.openstreetmap.josm.plugins.turnlanes.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;

public class Junction {
	private static final List<Way> filterHighways(List<OsmPrimitive> of) {
		final List<Way> result = new ArrayList<Way>();
		
		for (OsmPrimitive p : of) {
			if (p.getType() == OsmPrimitiveType.WAY && Utils.isRoad((Way) p)) {
				result.add((Way) p);
			}
		}
		
		return result;
	}
	
	private static List<Way> filterBeginsOrEndsAt(List<Way> ways, Node node) {
		final List<Way> result = new ArrayList<Way>();
		
		for (Way w : ways) {
			if (w.isFirstLastNode(node)) {
				result.add(w);
			}
		}
		
		return result;
	}
	
	private static List<Road> loadRoads(ModelContainer container, Junction j) {
		final List<Way> ways = filterBeginsOrEndsAt(filterHighways(j.getNode().getReferrers()), j.getNode());
		
		return Road.map(container, ways, j);
	}
	
	private final ModelContainer container;
	
	private final Node node;
	private final List<Road> roads = new ArrayList<Road>();
	
	Junction(ModelContainer container, Node n) {
		this.container = container;
		this.node = n;
		
		container.register(this);
		
		if (isPrimary()) {
			loadRoads(container, this);
			// if turn data is invalid, this will force an exception now, not later during painting
			getTurns();
		}
	}
	
	boolean isPrimary() {
		return container.getPrimary().equals(this);
	}
	
	public Node getNode() {
		return node;
	}
	
	public List<Road> getRoads() {
		return roads;
	}
	
	void addRoad(Road r) {
		roads.add(r);
	}
	
	public void addTurn(Lane from, Road.End to) {
		assert equals(from.getOutgoingJunction());
		assert equals(to.getJunction());
		
		final Way fromWay = from.isReverse() ? from.getRoad().getRoute().getFirstSegment().getWay() : from.getRoad()
		    .getRoute().getLastSegment().getWay();
		final Way toWay = to.isFromEnd() ? to.getRoad().getRoute().getFirstSegment().getWay() : to.getRoad().getRoute()
		    .getLastSegment().getWay();
		
		Relation existing = null;
		for (Turn t : getTurns()) {
			if ((from.isReverse() ? from.getRoad().getRoute().getFirstSegment() : from.getRoad().getRoute().getLastSegment())
			    .getWay().equals(t.getFromWay()) && t.getTo().equals(to)) {
				if (t.getFrom().isExtra() == from.isExtra() && t.getFrom().getIndex() == from.getIndex()) {
					// was already added
					return;
				}
				
				existing = t.getRelation();
			}
		}
		
		final Relation r = existing == null ? new Relation() : existing;
		
		final String key = from.isExtra() ? Constants.TURN_KEY_EXTRA_LANES : Constants.TURN_KEY_LANES;
		final List<Integer> lanes = Turn.split(r.get(key));
		lanes.add(from.getIndex());
		r.put(key, Turn.join(lanes));
		
		if (existing == null) {
			r.put("type", Constants.TYPE_TURNS);
			
			r.addMember(new RelationMember(Constants.TURN_ROLE_VIA, node));
			r.addMember(new RelationMember(Constants.TURN_ROLE_FROM, fromWay));
			r.addMember(new RelationMember(Constants.TURN_ROLE_TO, toWay));
			
			node.getDataSet().addPrimitive(r);
		}
	}
	
	public Set<Turn> getTurns() {
		return Turn.load(this);
	}
	
	Road.End getRoadEnd(Way way) {
		final List<Road.End> candidates = new ArrayList<Road.End>();
		
		for (Road r : getRoads()) {
			if (r.getFromEnd().getJunction().equals(this)) {
				if (r.getRoute().getSegments().get(0).getWay().equals(way)) {
					candidates.add(r.getFromEnd());
				}
			}
			
			if (r.getToEnd().getJunction().equals(this)) {
				if (r.getRoute().getSegments().get(r.getRoute().getSegments().size() - 1).getWay().equals(way)) {
					candidates.add(r.getToEnd());
				}
			}
		}
		
		if (candidates.isEmpty()) {
			throw new IllegalArgumentException("No such road end.");
		} else if (candidates.size() > 1) {
			throw new IllegalArgumentException("There are " + candidates.size()
			    + " road ends at this junction for the given way.");
		}
		
		return candidates.get(0);
	}
	
	public void removeTurn(Turn turn) {
		turn.remove();
	}
}
