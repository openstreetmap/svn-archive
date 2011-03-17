package org.openstreetmap.josm.plugins.turnlanes.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.turnlanes.CollectionUtils;

public class Lane {
	public enum Kind {
		EXTRA_LEFT,
		EXTRA_RIGHT,
		REGULAR;
	}
	
	/**
	 * 
	 * @param road
	 * @param r
	 *          lengths relation
	 * @param w
	 * @return
	 */
	public static List<Lane> load(Road road, Relation left, Relation right, Way w) {
		double extraLengthLeft = left == null ? 0 : Route.load(left).getLengthFrom(w);
		double extraLengthRight = right == null ? 0 : Route.load(right).getLengthFrom(w);
		
		final List<Double> leftLengths = loadLengths(left, Constants.LENGTHS_KEY_LENGTHS_LEFT, extraLengthLeft);
		final List<Double> rightLengths = loadLengths(right, Constants.LENGTHS_KEY_LENGTHS_RIGHT, extraLengthRight);
		
		final List<Lane> result = new ArrayList<Lane>(load(road));
		final int mid = getReverseCount(result);
		result.addAll(mid, map(leftLengths, road, true, extraLengthLeft));
		result.addAll(map(rightLengths, road, false, extraLengthRight));
		
		return result;
	}
	
	private static List<Lane> map(List<Double> lengths, Road road, boolean left, double extraLength) {
		final List<Lane> result = new ArrayList<Lane>(lengths.size());
		
		int index = left ? -lengths.size() : 1;
		for (Double l : (left ? CollectionUtils.reverse(lengths) : lengths)) {
			// TODO road may connect twice with junction => reverse might not always be false
			result.add(new Lane(road, index++, false, left, extraLength, l - extraLength));
		}
		
		return result;
	}
	
	private static int getReverseCount(List<Lane> ls) {
		int result = 0;
		
		for (Lane l : ls) {
			if (l.isReverse()) {
				++result;
			}
		}
		
		return result;
	}
	
	static List<Double> loadLengths(Relation r, String key, double lengthBound) {
		final List<Double> result = new ArrayList<Double>();
		
		if (r != null && r.get(key) != null) {
			for (String s : Constants.SPLIT_PATTERN.split(r.get(key))) {
				// TODO what should the exact input be (there should probably be
				// a unit (m))
				final Double length = Double.parseDouble(s.trim());
				
				if (length > lengthBound) {
					result.add(length);
				}
			}
		}
		
		return result;
	}
	
	private static int getCount(Way w) {
		final String countStr = w.get("lanes");
		
		if (countStr != null) {
			return Integer.parseInt(countStr);
		}
		
		// TODO default lane counts based on "highway" tag
		return 2;
	}
	
	static int getRegularCount(Way w, Node end) {
		final int count = getCount(w);
		
		if (w.hasDirectionKeys()) {
			// TODO check for oneway=-1
			if (w.lastNode().equals(end)) {
				return count;
			} else {
				return 0;
			}
		} else {
			if (w.lastNode().equals(end)) {
				return (count + 1) / 2; // round up in direction of end
			} else {
				return count / 2; // round down in other direction
			}
		}
	}
	
	public static List<Lane> load(Road r) {
		final Route.Segment first = r.getRoute().getFirstSegment();
		final Route.Segment last = r.getRoute().getLastSegment();
		
		final int back = getRegularCount(first.getWay(), first.getStart());
		final int forth = getRegularCount(last.getWay(), last.getEnd());
		
		final List<Lane> result = new ArrayList<Lane>(back + forth);
		
		for (int i = back; i >= 1; --i) {
			result.add(new Lane(r, i, true));
		}
		for (int i = 1; i <= forth; ++i) {
			result.add(new Lane(r, i, false));
		}
		
		return Collections.unmodifiableList(result);
	}
	
	private final Road road;
	private final int index;
	private final Kind kind;
	private final boolean reverse;
	
	/**
	 * If this extra lane extends past the given road, this value equals the length of the way(s)
	 * which come after the end of the road.
	 */
	private final double extraLength;
	
	private double length = -1;
	
	public Lane(Road road, int index, boolean reverse) {
		this.road = road;
		this.index = index;
		this.kind = Kind.REGULAR;
		this.reverse = reverse;
		this.extraLength = -1;
	}
	
	public Lane(Road road, int index, boolean reverse, boolean left, double extraLength, double length) {
		this.road = road;
		this.index = index;
		this.kind = left ? Kind.EXTRA_LEFT : Kind.EXTRA_RIGHT;
		this.reverse = reverse;
		this.extraLength = extraLength;
		this.length = length;
		
		if (length <= 0) {
			throw new IllegalArgumentException("Length must be positive");
		}
		
		if (extraLength < 0) {
			throw new IllegalArgumentException("Extra length must not be negative");
		}
	}
	
	public Road getRoad() {
		return road;
	}
	
	public double getExtraLength() {
		return extraLength;
	}
	
	public Kind getKind() {
		return kind;
	}
	
	public double getLength() {
		return isExtra() ? length : road.getLength();
	}
	
	double getTotalLength() {
		if (!isExtra()) {
			throw new UnsupportedOperationException();
		}
		
		return length + extraLength;
	}
	
	public void setLength(double length) {
		if (!isExtra()) {
			throw new UnsupportedOperationException("Length can only be set for extra lanes.");
		} else if (length <= 0) {
			throw new IllegalArgumentException("Length must positive.");
		}
		
		// TODO if needed, increase length of other lanes
		road.updateLengths();
		
		this.length = length;
	}
	
	public boolean isExtra() {
		return getKind() != Kind.REGULAR;
	}
	
	public int getIndex() {
		return index;
	}
	
	public boolean isReverse() {
		return reverse;
	}
	
	public Junction getOutgoingJunction() {
		return isReverse() ? getRoad().getFromEnd().getJunction() : getRoad().getToEnd().getJunction();
	}
	
	public Junction getIncomingJunction() {
		return isReverse() ? getRoad().getToEnd().getJunction() : getRoad().getFromEnd().getJunction();
	}
	
	public Road.End getOutgoingRoadEnd() {
		return isReverse() ? getRoad().getFromEnd() : getRoad().getToEnd();
	}
	
	public Road.End getIncomingRoadEnd() {
		return isReverse() ? getRoad().getToEnd() : getRoad().getFromEnd();
	}
}
