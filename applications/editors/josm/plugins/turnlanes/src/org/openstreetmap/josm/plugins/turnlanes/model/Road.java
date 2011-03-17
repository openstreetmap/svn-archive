package org.openstreetmap.josm.plugins.turnlanes.model;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.turnlanes.model.Route.Segment;
import org.openstreetmap.josm.tools.Pair;

public class Road {
	public class End {
		private final Junction junction;
		
		private End(Junction junction) {
			this.junction = junction;
		}
		
		public Road getRoad() {
			return Road.this;
		}
		
		public Junction getJunction() {
			return junction;
		}
		
		private boolean isFrom() {
			if (this == fromEnd) {
				return true;
			} else if (this == toEnd) {
				return false;
			}
			
			throw new IllegalStateException();
		}
		
		public boolean isFromEnd() {
			return isFrom();
		}
		
		public boolean isToEnd() {
			return !isFrom();
		}
		
		public void addExtraLane(boolean left) {
			if (!isToEnd()) {
				throw new UnsupportedOperationException();
			}
			
			double length = Double.POSITIVE_INFINITY;
			for (Lane l : getLanes()) {
				if (l.getKind() == (left ? Lane.Kind.EXTRA_LEFT : Lane.Kind.EXTRA_RIGHT)) {
					{
						length = Math.min(length, l.getLength());
					}
				}
			}
			
			if (Double.isInfinite(length)) {
				length = Math.min(20, 3 * getLength() / 4);
			}
			
			addLength(left, length);
		}
	}
	
	public static List<Road> map(ModelContainer container, List<Way> ws, Junction j) {
		final List<Road> result = new ArrayList<Road>();
		
		for (Way w : ws) {
			result.add(container.getRoad(w, j));
		}
		
		return result;
	}
	
	private static Pair<Relation, Relation> getLengthRelations(Way w, Node n) {
		final List<Relation> left = new ArrayList<Relation>();
		final List<Relation> right = new ArrayList<Relation>();
		
		for (OsmPrimitive p : w.getReferrers()) {
			if (p.getType() != OsmPrimitiveType.RELATION) {
				continue;
			}
			
			Relation r = (Relation) p;
			
			if (Constants.TYPE_LENGTHS.equals(r.get("type")) && isRightDirection(r, w, n)) {
				
				if (r.get(Constants.LENGTHS_KEY_LENGTHS_LEFT) != null) {
					left.add(r);
				}
				
				if (r.get(Constants.LENGTHS_KEY_LENGTHS_RIGHT) != null) {
					right.add(r);
				}
			}
		}
		
		if (left.size() > 1) {
			throw new IllegalArgumentException("Way is in " + left.size()
			    + " lengths relations for given direction, both specifying left lane lengths.");
		}
		
		if (right.size() > 1) {
			throw new IllegalArgumentException("Way is in " + right.size()
			    + " lengths relations for given direction, both specifying right lane lengths.");
		}
		
		return new Pair<Relation, Relation>( //
		    left.isEmpty() ? null : left.get(0), //
		    right.isEmpty() ? null : right.get(0) //
		);
	}
	
	/**
	 * @param r
	 *          lengths relation
	 * @param w
	 *          the way to check for
	 * @param n
	 *          first or last node of w, determines the direction
	 * @return whether the turn lane goes into the direction of n
	 */
	private static boolean isRightDirection(Relation r, Way w, Node n) {
		for (Segment s : Route.load(r).getSegments()) {
			if (w.equals(s.getWay())) {
				return n.equals(s.getEnd());
			}
		}
		
		return false;
	}
	
	private final ModelContainer container;
	
	private final Relation lengthsLeft;
	private final Relation lengthsRight;
	private final Route route;
	private final List<Lane> lanes;
	
	private final End fromEnd;
	private final End toEnd;
	
	Road(ModelContainer container, Way w, Junction j) {
		this.container = container;
		this.toEnd = new End(j);
		
		final Node n = j.getNode();
		
		if (!w.isFirstLastNode(n)) {
			throw new IllegalArgumentException("Way must start or end in given node.");
		}
		
		final Pair<Relation, Relation> lr = getLengthRelations(w, n);
		
		this.lengthsLeft = lr.a;
		this.lengthsRight = lr.b;
		this.route = lengthsLeft == null && lengthsRight == null ? Route.create(Arrays.asList(w), n) : Route.load(
		    lengthsLeft, lengthsRight, w);
		this.lanes = lengthsLeft == null && lengthsRight == null ? Lane.load(this) : Lane.load(this, lengthsLeft,
		    lengthsRight, w);
		
		final List<Node> nodes = route.getNodes();
		this.fromEnd = new End(container.getOrCreateJunction(nodes.get(0)));
		
		fromEnd.getJunction().addRoad(this);
		toEnd.getJunction().addRoad(this);
	}
	
	public End getFromEnd() {
		return fromEnd;
	}
	
	public End getToEnd() {
		return toEnd;
	}
	
	public Route getRoute() {
		return route;
	}
	
	public List<Lane> getLanes() {
		return lanes;
	}
	
	public double getLength() {
		return route.getLength();
	}
	
	void updateLengths() {
		if (lengthsLeft != null) { // TODO only forward lanes at this point
			String lengths = "";
			for (int i = lanes.size() - 1; i >= 0; --i) {
				final Lane l = lanes.get(i);
				if (l.getKind() == Lane.Kind.EXTRA_LEFT) {
					lengths += toLengthString(l.getTotalLength()) + Constants.SEPARATOR;
				}
			}
			lengthsLeft.put(Constants.LENGTHS_KEY_LENGTHS_LEFT,
			    lengths.substring(0, lengths.length() - Constants.SEPARATOR.length()));
		}
		if (lengthsRight != null) {
			String lengths = "";
			for (Lane l : lanes) {
				if (l.getKind() == Lane.Kind.EXTRA_RIGHT) {
					lengths += toLengthString(l.getTotalLength()) + Constants.SEPARATOR;
				}
			}
			lengthsRight.put(Constants.LENGTHS_KEY_LENGTHS_RIGHT,
			    lengths.substring(0, lengths.length() - Constants.SEPARATOR.length()));
		}
	}
	
	private String toLengthString(double length) {
		final DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();
		dfs.setDecimalSeparator('.');
		final DecimalFormat nf = new DecimalFormat("0.0", dfs);
		nf.setRoundingMode(RoundingMode.HALF_UP);
		return nf.format(length);
	}
	
	// TODO create a special Lanes class which deals with this, misplaced here
	public Lane getLane(boolean reverse, int index) {
		for (Lane l : lanes) {
			if (!l.isExtra() && l.isReverse() == reverse && l.getIndex() == index) {
				return l;
			}
		}
		
		throw new IllegalArgumentException("No such lane.");
	}
	
	public Lane getExtraLane(boolean reverse, int index) {
		for (Lane l : lanes) {
			if (l.isExtra() && l.isReverse() == reverse && l.getIndex() == index) {
				return l;
			}
		}
		
		throw new IllegalArgumentException("No such lane.");
	}
	
	public ModelContainer getContainer() {
		return container;
	}
	
	private void addLength(boolean left, double length) {
		final Relation l = lengthsLeft;
		final Relation r = lengthsRight;
		final Node n = toEnd.getJunction().getNode();
		
		final String lengthStr = toLengthString(length);
		final Relation target;
		if (left) {
			if (l == null) {
				if (r == null || !Utils.getMemberNode(r, "end").equals(n)) {
					target = createLengthsRelation();
				} else {
					target = r;
				}
			} else {
				target = l;
			}
		} else {
			if (r == null) {
				if (l == null || !Utils.getMemberNode(l, "end").equals(n)) {
					target = createLengthsRelation();
				} else {
					target = l;
				}
			} else {
				target = r;
			}
		}
		
		final String key = left ? Constants.LENGTHS_KEY_LENGTHS_LEFT : Constants.LENGTHS_KEY_LENGTHS_RIGHT;
		final String old = target.get(key);
		if (old == null) {
			target.put(key, lengthStr);
		} else {
			target.put(key, old + Constants.SEPARATOR + lengthStr);
		}
	}
	
	private Relation createLengthsRelation() {
		final Node n = toEnd.getJunction().getNode();
		
		final Relation r = new Relation();
		r.put("type", Constants.TYPE_LENGTHS);
		
		r.addMember(new RelationMember(Constants.LENGTHS_ROLE_END, n));
		for (Route.Segment s : route.getSegments()) {
			r.addMember(1, new RelationMember(Constants.LENGTHS_ROLE_WAYS, s.getWay()));
		}
		
		n.getDataSet().addPrimitive(r);
		
		return r;
	}
	
	public boolean isExtendable() {
		return lengthsLeft != null || lengthsRight != null;
	}
	
	public void extend(Way way) {
		if (lengthsLeft != null) {
			lengthsLeft.addMember(new RelationMember(Constants.LENGTHS_ROLE_WAYS, way));
		}
		if (lengthsRight != null) {
			lengthsRight.addMember(new RelationMember(Constants.LENGTHS_ROLE_WAYS, way));
		}
	}
}
