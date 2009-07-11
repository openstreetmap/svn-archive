package org.openstreetmap.josm.plugins.routes;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;


public class ConvertedWay {
	
	public class WayEnd {
		private Node end;
		
		public WayEnd(Node end) {
			this.end = end;
		}
		
		@Override
		public boolean equals(Object o) {
			if (o instanceof WayEnd) {
				WayEnd otherEnd = (WayEnd)o;
				return end.equals(otherEnd.end) && routes.equals(otherEnd.getRoutes());
			} else {
				return false;
			}
		}
		
		@Override
		public int hashCode() {
			return end.hashCode() + routes.hashCode();
		}
		
		public BitSet getRoutes() {
			return routes;
		}
		
		public ConvertedWay getWay() {
			return ConvertedWay.this;
		}
	}
	
	private List<Node> nodes = new ArrayList<Node>();
	private BitSet routes;
	
	public ConvertedWay(BitSet routes, Way way) {
		this.routes = routes;
		nodes.addAll(way.nodes);
	}
	
	public WayEnd getStart() {
		return new WayEnd(nodes.get(0));
	}
	
	public WayEnd getStop() {
		return new WayEnd(nodes.get(nodes.size() - 1));
	}
	
	/**
	 * Connects way to this way. Other ways internal representation is destroyed!!!
	 * @param way
	 */
	public void connect(ConvertedWay way) {
		for (int i=0; i<2; i++) {			
			if (way.nodes.get(0).equals(nodes.get(nodes.size() - 1))) {
				way.nodes.remove(0);
				nodes.addAll(way.nodes);
				return;
			}
			
			if (way.nodes.get(way.nodes.size() - 1).equals(nodes.get(0))) {
				nodes.remove(0);
				way.nodes.addAll(nodes);
				nodes = way.nodes;
				return;
			}
			Collections.reverse(nodes);
		}
	}

	public List<Node> getNodes() {
		return nodes;
	}

	public BitSet getRoutes() {
		return routes;
	}


}
