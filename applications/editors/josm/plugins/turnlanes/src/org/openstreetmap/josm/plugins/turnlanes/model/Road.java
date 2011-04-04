package org.openstreetmap.josm.plugins.turnlanes.model;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.turnlanes.CollectionUtils;
import org.openstreetmap.josm.plugins.turnlanes.model.Route.Segment;
import org.openstreetmap.josm.tools.Pair;

public class Road {
	public class End {
		private final boolean from;
		private final Junction junction;
		
		private final Relation lengthsLeft;
		private final Relation lengthsRight;
		
		private final double extraLengthLeft;
		private final double extraLengthRight;
		
		private final List<Lane> lanes;
		
		private End(boolean from, Junction junction, Relation lengthsLeft, Relation lengthsRight) {
			this.from = from;
			this.junction = junction;
			this.lengthsLeft = lengthsLeft;
			this.lengthsRight = lengthsRight;
			this.extraLengthLeft = lengthsLeft == null ? 0 : Route.load(lengthsLeft).getLengthFrom(getWay());
			this.extraLengthRight = lengthsRight == null ? 0 : Route.load(lengthsRight).getLengthFrom(getWay());
			this.lanes = Lane.load(this);
			
			junction.addRoad(getWay());
		}
		
		private End(boolean from, Junction junction) {
			this.from = from;
			this.junction = junction;
			this.lengthsLeft = null;
			this.lengthsRight = null;
			this.extraLengthLeft = 0;
			this.extraLengthRight = 0;
			this.lanes = Lane.load(this);
			
			junction.addRoad(getWay());
		}
		
		public Road getRoad() {
			return Road.this;
		}
		
		public Way getWay() {
			return isFromEnd() ? getRoute().getFirstSegment().getWay() : getRoute().getLastSegment().getWay();
		}
		
		public Junction getJunction() {
			return junction;
		}
		
		public boolean isFromEnd() {
			return from;
		}
		
		public boolean isToEnd() {
			return !isFromEnd();
		}
		
		public End getOppositeEnd() {
			return isFromEnd() ? toEnd : fromEnd;
		}
		
		/**
		 * @return the turns <em>onto</em> this road at this end
		 */
		public Set<Turn> getTurns() {
			return Turn.load(getContainer(), Constants.TURN_ROLE_TO, getWay());
		}
		
		public void addLane(Lane.Kind kind) {
			if (kind == Lane.Kind.REGULAR) {
				throw new IllegalArgumentException("Only extra lanes can be added.");
			}
			
			double length = Double.POSITIVE_INFINITY;
			for (Lane l : lanes) {
				if (l.getKind() == kind) {
					length = Math.max(0, Math.min(length, l.getLength() - 1));
				}
			}
			
			if (Double.isInfinite(length)) {
				length = Math.min(20, 3 * getLength() / 4);
			}
			
			addLane(kind, length);
		}
		
		private void addLane(Lane.Kind kind, double length) {
			assert kind == Lane.Kind.EXTRA_LEFT || kind == Lane.Kind.EXTRA_RIGHT;
			
			final boolean left = kind == Lane.Kind.EXTRA_LEFT;
			final Relation rel = left ? lengthsLeft : lengthsRight;
			final Relation other = left ? lengthsRight : lengthsLeft;
			final Node n = getJunction().getNode();
			
			final String lengthStr = toLengthString(length);
			final Relation target;
			if (rel == null) {
				if (other == null || !Utils.getMemberNode(other, "end").equals(n)) {
					target = createLengthsRelation();
				} else {
					target = other;
				}
			} else {
				target = rel;
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
			final Node n = getJunction().getNode();
			
			final Relation r = new Relation();
			r.put("type", Constants.TYPE_LENGTHS);
			
			r.addMember(new RelationMember(Constants.LENGTHS_ROLE_END, n));
			for (Route.Segment s : isFromEnd() ? route.getSegments() : CollectionUtils.reverse(route.getSegments())) {
				r.addMember(new RelationMember(Constants.LENGTHS_ROLE_WAYS, s.getWay()));
			}
			
			n.getDataSet().addPrimitive(r);
			
			return r;
		}
		
		void updateLengths() {
			for (final boolean left : Arrays.asList(true, false)) {
				final Lane.Kind kind = left ? Lane.Kind.EXTRA_LEFT : Lane.Kind.EXTRA_RIGHT;
				final Relation r = left ? lengthsLeft : lengthsRight;
				final double extra = left ? extraLengthLeft : extraLengthRight;
				
				if (r == null) {
					continue;
				}
				
				final StringBuilder lengths = new StringBuilder(32);
				for (Lane l : left ? CollectionUtils.reverse(lanes) : lanes) {
					if (l.getKind() == kind) {
						lengths.append(toLengthString(extra + l.getLength())).append(Constants.SEPARATOR);
					}
				}
				
				lengths.setLength(lengths.length() - Constants.SEPARATOR.length());
				r.put(left ? Constants.LENGTHS_KEY_LENGTHS_LEFT : Constants.LENGTHS_KEY_LENGTHS_RIGHT, lengths.toString());
			}
		}
		
		public List<Lane> getLanes() {
			return lanes;
		}
		
		public Lane getLane(Lane.Kind kind, int index) {
			for (Lane l : lanes) {
				if (l.getKind() == kind && l.getIndex() == index) {
					return l;
				}
			}
			
			throw new IllegalArgumentException("No such lane.");
		}
		
		public Lane getExtraLane(int index) {
			return index < 0 ? getLane(Lane.Kind.EXTRA_LEFT, index) : getLane(Lane.Kind.EXTRA_RIGHT, index);
		}
		
		public boolean isExtendable() {
			final End o = getOppositeEnd();
			return (lengthsLeft == null && lengthsRight == null) && (o.lengthsLeft != null || o.lengthsRight != null);
		}
		
		public void extend(Way way) {
			if (!isExtendable()) {
				throw new IllegalStateException();
			}
			
			final End o = getOppositeEnd();
			if (o.lengthsLeft != null) {
				o.lengthsLeft.addMember(new RelationMember(Constants.LENGTHS_ROLE_WAYS, way));
			}
			if (o.lengthsRight != null) {
				o.lengthsRight.addMember(new RelationMember(Constants.LENGTHS_ROLE_WAYS, way));
			}
		}
		
		public List<Double> getLengths(Lane.Kind kind) {
			switch (kind) {
				case EXTRA_LEFT:
					return Lane.loadLengths(lengthsLeft, Constants.LENGTHS_KEY_LENGTHS_LEFT, extraLengthLeft);
				case EXTRA_RIGHT:
					return Lane.loadLengths(lengthsRight, Constants.LENGTHS_KEY_LENGTHS_RIGHT, extraLengthRight);
				default:
					throw new IllegalArgumentException(String.valueOf(kind));
			}
		}
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
	private final Route route;
	private final End fromEnd;
	private final End toEnd;
	
	Road(ModelContainer container, Way w, Junction j) {
		final Node n = j.getNode();
		if (!w.isFirstLastNode(n)) {
			throw new IllegalArgumentException("Way must start or end in given node.");
		}
		final Pair<Relation, Relation> lengthsRelations = getLengthRelations(w, n);
		
		this.container = container;
		this.route = lengthsRelations.a == null && lengthsRelations.b == null ? Route.create(Arrays.asList(w), n) : Route
		    .load(lengthsRelations.a, lengthsRelations.b, w);
		this.fromEnd = new End(true, container.getOrCreateJunction(route.getFirstSegment().getStart()));
		this.toEnd = new End(false, j, lengthsRelations.a, lengthsRelations.b);
	}
	
	Road(ModelContainer container, Route route) {
		this.container = container;
		this.route = route;
		this.fromEnd = new End(true, container.getJunction(route.getStart()));
		this.toEnd = new End(false, container.getJunction(route.getEnd()));
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
	
	public double getLength() {
		return route.getLength();
	}
	
	private String toLengthString(double length) {
		final DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();
		dfs.setDecimalSeparator('.');
		final DecimalFormat nf = new DecimalFormat("0.0", dfs);
		nf.setRoundingMode(RoundingMode.HALF_UP);
		return nf.format(length);
	}
	
	public ModelContainer getContainer() {
		return container;
	}
	
	public boolean isPrimary() {
		return getContainer().isPrimary(this);
	}
}
