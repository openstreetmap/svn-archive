package org.openstreetmap.josm.plugins.turnlanes.model;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.turnlanes.model.Issue.QuickFix;

public class Validator {
	private static final class IncomingLanes {
		private static final class Key {
			final Node junction;
			final Way from;
			
			public Key(Node junction, Way from) {
				this.junction = junction;
				this.from = from;
			}
			
			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + ((from == null) ? 0 : from.hashCode());
				result = prime * result + ((junction == null) ? 0 : junction.hashCode());
				return result;
			}
			
			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				Key other = (Key) obj;
				if (from == null) {
					if (other.from != null)
						return false;
				} else if (!from.equals(other.from))
					return false;
				if (junction == null) {
					if (other.junction != null)
						return false;
				} else if (!junction.equals(other.junction))
					return false;
				return true;
			}
		}
		
		final Key key;
		private final int extraLeft;
		private final int regular;
		private final int extraRight;
		private final BitSet bitset;
		
		public IncomingLanes(Key key, int extraLeft, int regular, int extraRight) {
			this.key = key;
			this.extraLeft = extraLeft;
			this.regular = regular;
			this.extraRight = extraRight;
			this.bitset = new BitSet(extraLeft + regular + extraRight);
		}
		
		public boolean existsRegular(int l) {
			if (l > 0 && l <= regular) {
				bitset.set(extraLeft + l - 1);
				return true;
			}
			
			return false;
		}
		
		public boolean existsExtra(int l) {
			if (l < 0 && Math.abs(l) <= extraLeft) {
				bitset.set(Math.abs(l) - 1);
				return true;
			} else if (l > 0 && l <= extraRight) {
				bitset.set(extraLeft + regular + l - 1);
				return true;
			}
			return false;
		}
		
		public int unreferenced() {
			return extraLeft + regular + extraRight - bitset.cardinality();
		}
	}
	
	public List<Issue> validate(DataSet dataSet) {
		final List<Relation> lenghts = new ArrayList<Relation>();
		final List<Relation> turns = new ArrayList<Relation>();
		
		for (OsmPrimitive p : dataSet.allPrimitives()) {
			if (p.getType() != OsmPrimitiveType.RELATION) {
				continue;
			}
			
			final Relation r = (Relation) p;
			final String type = p.get("type");
			
			if (Constants.TYPE_LENGTHS.equals(type)) {
				lenghts.add(r);
			} else if (Constants.TYPE_TURNS.equals(type)) {
				turns.add(r);
			}
		}
		
		final List<Issue> issues = new ArrayList<Issue>();
		
		final Map<IncomingLanes.Key, IncomingLanes> incomingLanes = new HashMap<IncomingLanes.Key, IncomingLanes>();
		issues.addAll(validateLengths(lenghts, incomingLanes));
		issues.addAll(validateTurns(turns, incomingLanes));
		
		for (IncomingLanes lanes : incomingLanes.values()) {
			if (lanes.unreferenced() > 0) {
				issues.add(Issue.newWarning(Arrays.asList(lanes.key.junction, lanes.key.from),
				    tr("{0} lanes are not referenced in any turn-relation.", lanes.unreferenced())));
			}
		}
		
		return issues;
	}
	
	private List<Issue> validateLengths(List<Relation> lenghts, Map<IncomingLanes.Key, IncomingLanes> incomingLanes) {
		final List<Issue> issues = new ArrayList<Issue>();
		
		for (Relation r : lenghts) {
			issues.addAll(validateLengths(r, incomingLanes));
		}
		
		return issues;
	}
	
	private List<Issue> validateLengths(Relation r, Map<IncomingLanes.Key, IncomingLanes> incomingLanes) {
		final List<Issue> issues = new ArrayList<Issue>();
		
		final Node end = validateLengthsEnd(r, issues);
		
		if (end == null) {
			return issues;
		}
		
		final Route route = validateLengthsWays(r, end, issues);
		
		if (route == null) {
			return issues;
		}
		
		final List<Double> left = Lane.loadLengths(r, Constants.LENGTHS_KEY_LENGTHS_LEFT, 0);
		final List<Double> right = Lane.loadLengths(r, Constants.LENGTHS_KEY_LENGTHS_RIGHT, 0);
		
		int tooLong = 0;
		for (Double l : left) {
			if (l > route.getLength()) {
				++tooLong;
			}
		}
		for (Double l : right) {
			if (l > route.getLength()) {
				++tooLong;
			}
		}
		
		if (tooLong > 0) {
			issues.add(Issue.newError(r, end, "The lengths-relation specifies " + tooLong
			    + " extra-lanes which are longer than its ways."));
		}
		
		putIncomingLanes(route, left, right, incomingLanes);
		
		return issues;
	}
	
	private void putIncomingLanes(Route route, List<Double> left, List<Double> right,
	    Map<IncomingLanes.Key, IncomingLanes> incomingLanes) {
		final Node end = route.getLastSegment().getEnd();
		final Way way = route.getLastSegment().getWay();
		
		final IncomingLanes.Key key = new IncomingLanes.Key(end, way);
		final IncomingLanes lanes = new IncomingLanes(key, left.size(), Lane.getRegularCount(way, end), right.size());
		final IncomingLanes old = incomingLanes.put(key, lanes);
		
		if (old != null) {
			incomingLanes.put(
			    key,
			    new IncomingLanes(key, Math.max(lanes.extraLeft, old.extraLeft), Math.max(lanes.regular, old.regular), Math
			        .max(lanes.extraRight, old.extraRight)));
		}
		
		final double length = route.getLastSegment().getLength();
		final List<Double> newLeft = reduceLengths(left, length);
		final List<Double> newRight = new ArrayList<Double>(right.size());
		
		if (route.getSegments().size() > 1) {
			final Route subroute = route.subRoute(0, route.getSegments().size() - 1);
			putIncomingLanes(subroute, newLeft, newRight, incomingLanes);
		}
	}
	
	private List<Double> reduceLengths(List<Double> lengths, double length) {
		final List<Double> newLengths = new ArrayList<Double>(lengths.size());
		
		for (double l : lengths) {
			if (l > length) {
				newLengths.add(l - length);
			}
		}
		
		return newLengths;
	}
	
	private Route validateLengthsWays(Relation r, Node end, List<Issue> issues) {
		final List<Way> ways = Utils.getMemberWays(r, Constants.LENGTHS_ROLE_WAYS);
		
		if (ways.isEmpty()) {
			issues.add(Issue.newError(r, "A lengths-relation requires at least one member-way with role \""
			    + Constants.LENGTHS_ROLE_WAYS + "\"."));
			return null;
		}
		
		Node current = end;
		for (Way w : ways) {
			if (!w.isFirstLastNode(current)) {
				return orderWays(r, ways, current, issues);
			}
			
			current = Utils.getOppositeEnd(w, current);
		}
		
		return Route.create(ways, end);
	}
	
	private Route orderWays(final Relation r, List<Way> ways, Node end, List<Issue> issues) {
		final List<Way> unordered = new ArrayList<Way>(ways);
		final List<Way> ordered = new ArrayList<Way>(ways.size());
		final Set<Node> ends = new HashSet<Node>(); // to find cycles
		
		Node current = end;
		findNext: while (!unordered.isEmpty()) {
			if (!ends.add(current)) {
				issues.add(Issue.newError(r, ways, "The ways of the lengths-relation are unordered (and contain cycles)."));
				return null;
			}
			
			Iterator<Way> it = unordered.iterator();
			while (it.hasNext()) {
				final Way w = it.next();
				
				if (w.isFirstLastNode(current)) {
					it.remove();
					ordered.add(w);
					current = Utils.getOppositeEnd(w, current);
					continue findNext;
				}
			}
			
			issues.add(Issue.newError(r, ways, "The ways of the lengths-relation are disconnected."));
			return null;
		}
		
		final QuickFix quickFix = new QuickFix(tr("Put the ways in order.")) {
			@Override
			public boolean perform() {
				for (int i = r.getMembersCount() - 1; i >= 0; --i) {
					final RelationMember m = r.getMember(i);
					
					if (m.isWay() && Constants.LENGTHS_ROLE_WAYS.equals(m.getRole())) {
						r.removeMember(i);
					}
				}
				
				for (Way w : ordered) {
					r.addMember(new RelationMember(Constants.LENGTHS_ROLE_WAYS, w));
				}
				
				return true;
			}
		};
		
		issues.add(Issue.newError(r, ways, "The ways of the lengths-relation are unordered.", quickFix));
		
		return Route.create(ordered, end);
	}
	
	private Node validateLengthsEnd(Relation r, List<Issue> issues) {
		final List<Node> endNodes = Utils.getMemberNodes(r, Constants.LENGTHS_ROLE_END);
		
		if (endNodes.size() != 1) {
			issues.add(Issue.newError(r, endNodes, "A lengths-relation requires exactly one member-node with role \""
			    + Constants.LENGTHS_ROLE_END + "\"."));
			return null;
		}
		
		return endNodes.get(0);
	}
	
	private List<Issue> validateTurns(List<Relation> turns, Map<IncomingLanes.Key, IncomingLanes> incomingLanes) {
		final List<Issue> issues = new ArrayList<Issue>();
		
		for (Relation r : turns) {
			issues.addAll(validateTurns(r, incomingLanes));
		}
		
		return issues;
	}
	
	private List<Issue> validateTurns(Relation r, Map<IncomingLanes.Key, IncomingLanes> incomingLanes) {
		final List<Issue> issues = new ArrayList<Issue>();
		
		final List<Way> fromWays = Utils.getMemberWays(r, Constants.TURN_ROLE_FROM);
		final List<Node> viaNodes = Utils.getMemberNodes(r, Constants.TURN_ROLE_VIA);
		final List<Way> toWays = Utils.getMemberWays(r, Constants.TURN_ROLE_TO);
		
		if (fromWays.size() != 1) {
			issues.add(Issue.newError(r, fromWays, "A turns-relation requires exactly one member-way with role \""
			    + Constants.TURN_ROLE_FROM + "\"."));
		}
		if (viaNodes.size() != 1) {
			issues.add(Issue.newError(r, viaNodes, "A turns-relation requires exactly one member-node with role \""
			    + Constants.TURN_ROLE_VIA + "\"."));
		}
		if (toWays.size() != 1) {
			issues.add(Issue.newError(r, toWays, "A turns-relation requires exactly one member-way with role \""
			    + Constants.TURN_ROLE_TO + "\"."));
		}
		
		if (!issues.isEmpty()) {
			return issues;
		}
		
		final Way from = fromWays.get(0);
		final Node via = viaNodes.get(0);
		final Way to = toWays.get(0);
		
		if (!from.isFirstLastNode(via)) {
			issues.add(Issue.newError(r, from, "The from-way does not start or end at the via-node."));
		} else if (from.firstNode().equals(from.lastNode())) {
			issues.add(Issue.newError(r, from, "The from-way both starts as well as ends at the via-node."));
		}
		if (!to.isFirstLastNode(via)) {
			issues.add(Issue.newError(r, to, "The to-way does not start or end at the via-node."));
		} else if (to.firstNode().equals(to.lastNode())) {
			issues.add(Issue.newError(r, to, "The to-way both starts as well as ends at the via-node."));
		}
		
		if (!issues.isEmpty()) {
			return issues;
		}
		
		final IncomingLanes lanes = get(incomingLanes, via, from);
		
		for (int l : splitInts(r, Constants.TURN_KEY_LANES, issues)) {
			if (!lanes.existsRegular(l)) {
				issues.add(Issue.newError(r, tr("Relation references non-existent (regular) lane {0}", l)));
			}
		}
		
		for (int l : splitInts(r, Constants.TURN_KEY_EXTRA_LANES, issues)) {
			if (!lanes.existsExtra(l)) {
				issues.add(Issue.newError(r, tr("Relation references non-existent extra lane {0}", l)));
			}
		}
		
		return issues;
	}
	
	private List<Integer> splitInts(Relation r, String key, List<Issue> issues) {
		final String ints = r.get(key);
		
		if (ints == null) {
			return Collections.emptyList();
		}
		
		final List<Integer> result = new ArrayList<Integer>();
		
		for (String s : Constants.SPLIT_PATTERN.split(ints)) {
			try {
				int i = Integer.parseInt(s.trim());
				result.add(Integer.valueOf(i));
			} catch (NumberFormatException e) {
				issues.add(Issue.newError(r, tr("Integer list \"{0}\" contains unexpected values.", key)));
			}
		}
		
		return result;
	}
	
	private IncomingLanes get(Map<IncomingLanes.Key, IncomingLanes> incomingLanes, Node via, Way from) {
		final IncomingLanes.Key key = new IncomingLanes.Key(via, from);
		final IncomingLanes lanes = incomingLanes.get(key);
		
		if (lanes == null) {
			final IncomingLanes newLanes = new IncomingLanes(key, 0, Lane.getRegularCount(from, via), 0);
			incomingLanes.put(key, newLanes);
			return newLanes;
		} else {
			return lanes;
		}
	}
}
