package org.openstreetmap.josm.plugins.tways;

import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Segment;
import org.openstreetmap.josm.data.osm.Way;

/**
 * Parse a set of segments to provide helper functions
 *
 * @author Thomas.Walraet
 */
public class ParsedSegmentSet {

    private SegmentMap outSegments = new SegmentMap();
    private SegmentMap inSegments = new SegmentMap();

    public ParsedSegmentSet(Collection<Segment> segments) {
	for (Segment segment : segments) {
	    if (!segment.deleted) {
		outSegments.putSegment(segment.from, segment);
		inSegments.putSegment(segment.to, segment);
	    }
	}
    }

    public Segment getSegmentFromNode(Node node) {
	return outSegments.getSegment(node);
    }

    public Segment getSegmentToNode(Node node) {
	return inSegments.getSegment(node);
    }

    public boolean isOneInOneOut(Node node) {
	return outSegments.count(node) == 1 && inSegments.count(node) == 1;
    }

    class SegmentMap {
	public HashMap<Node, ArrayList<Segment>> map = new HashMap<Node, ArrayList<Segment>>();

	public void putSegment(Node node, Segment segment) {
	    ArrayList<Segment> segmentList = map.get(node);
	    if (segmentList == null) {
		segmentList = new ArrayList<Segment>();
		map.put(node, segmentList);
	    }
	    segmentList.add(segment);
	}

	public Segment getSegment(Node node) {
	    ArrayList<Segment> segmentList = map.get(node);
	    if (segmentList != null && segmentList.size() == 1) {
		return segmentList.get(0);
	    } else {
		return null;
	    }
	}

	public int count(Node node) {
            return map.get(node) != null ? map.get(node).size() : 0;
	}
    }

}
