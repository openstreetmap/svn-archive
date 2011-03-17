package org.openstreetmap.josm.plugins.turnlanes.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;

public class Turn {
	public static Set<Turn> load(Junction junction) {
		final Set<Turn> result = new HashSet<Turn>();

		for (OsmPrimitive p : junction.getNode().getReferrers()) {
			if (p.getType() != OsmPrimitiveType.RELATION) {
				continue;
			}

			Relation r = (Relation) p;

			if (!r.get("type").equals(Constants.TYPE_TURNS)) {
				continue;
			}

			result.addAll(Turn.load(junction, r));
		}

		return result;
	}

	private static Set<Turn> load(Junction junction, Relation turns) {
		if (!Constants.TYPE_TURNS.equals(turns.get("type"))) {
			throw new IllegalArgumentException("Relation must be a of type \""
					+ Constants.TYPE_TURNS + "\".");
		}

		Way fromWay = null;
		Way toWay = null;
		Node via = null;
		final List<Integer> lanes = split(turns.get(Constants.TURN_KEY_LANES));
		final List<Integer> extraLanes = split(turns
				.get(Constants.TURN_KEY_EXTRA_LANES));

		for (RelationMember m : turns.getMembers()) {
			final String r = m.getRole();
			if (Constants.TURN_ROLE_VIA.equals(r)) {
				if (via != null) {
					throw new IllegalArgumentException("More than one \""
							+ Constants.TURN_ROLE_VIA + "\" members.");
				}

				via = m.getNode();
			} else if (Constants.TURN_ROLE_FROM.equals(r)) {
				if (fromWay != null) {
					throw new IllegalArgumentException("More than one \""
							+ Constants.TURN_ROLE_FROM + "\" members.");
				}

				fromWay = m.getWay();
			} else if (Constants.TURN_ROLE_TO.equals(r)) {
				if (toWay != null) {
					throw new IllegalArgumentException("More than one \""
							+ Constants.TURN_ROLE_TO + "\" members.");
				}

				toWay = m.getWay();
			}
		}

		if (!via.equals(junction.getNode())) {
			throw new IllegalArgumentException(
					"Turn is for a different junction.");
		}

		final Road.End to = junction.getRoadEnd(toWay);

		final Set<Turn> result = new HashSet<Turn>();
		for (Road r : junction.getRoads()) {
			final boolean reverse;
			if (r.getRoute().getFirstSegment().getWay().equals(fromWay)
					&& r.getRoute().getFirstSegment().getStart().equals(via)) {
				reverse = true;
			} else if (r.getRoute().getLastSegment().getWay().equals(fromWay)
					&& r.getRoute().getLastSegment().getEnd().equals(via)) {
				reverse = false;
			} else {
				continue;
			}

			for (int l : lanes) {
				result.add(new Turn(turns, r.getLane(reverse, l), junction, to,
						fromWay, toWay));
			}
			for (int l : extraLanes) {
				result.add(new Turn(turns, r.getExtraLane(reverse, l),
						junction, to, fromWay, toWay));
			}
		}

		return result;
	}

	static List<Integer> split(String lanes) {
		if (lanes == null) {
			return new ArrayList<Integer>(1);
		}

		final List<Integer> result = new ArrayList<Integer>();
		for (String lane : Constants.SPLIT_PATTERN.split(lanes)) {
			result.add(Integer.parseInt(lane));
		}

		return result;
	}

	static String join(List<Integer> list) {
		if (list.isEmpty()) {
			return null;
		}

		final StringBuilder builder = new StringBuilder(list.size()
				* (2 + Constants.SEPARATOR.length()));

		for (int e : list) {
			builder.append(e).append(Constants.SEPARATOR);
		}

		builder.setLength(builder.length() - Constants.SEPARATOR.length());
		return builder.toString();
	}

	private final Relation relation;

	private final Lane from;
	private final Junction via;
	private final Road.End to;

	private final Way fromWay;
	private final Way toWay;

	public Turn(Relation relation, Lane from, Junction via, Road.End to,
			Way fromWay, Way toWay) {
		this.relation = relation;
		this.from = from;
		this.via = via;
		this.to = to;
		this.fromWay = fromWay;
		this.toWay = toWay;
	}

	public Lane getFrom() {
		return from;
	}

	public Junction getVia() {
		return via;
	}

	public Road.End getTo() {
		return to;
	}

	Relation getRelation() {
		return relation;
	}

	Way getFromWay() {
		return fromWay;
	}

	Way getToWay() {
		return toWay;
	}

	void remove() {
		final List<Integer> lanes = split(relation
				.get(Constants.TURN_KEY_LANES));
		final List<Integer> extraLanes = split(relation
				.get(Constants.TURN_KEY_EXTRA_LANES));

		if (lanes.size() + extraLanes.size() == 1
				&& (from.isExtra() ^ !lanes.isEmpty())) {
			relation.getDataSet().removePrimitive(relation.getPrimitiveId());
		} else if (from.isExtra()) {
			extraLanes.remove(Integer.valueOf(from.getIndex()));
		} else {
			lanes.remove(Integer.valueOf(from.getIndex()));
		}

		relation.put(Constants.TURN_KEY_LANES, join(lanes));
		relation.put(Constants.TURN_KEY_EXTRA_LANES, join(extraLanes));
	}
}
