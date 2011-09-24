package org.openstreetmap.josm.plugins.turnlanes.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.turnlanes.CollectionUtils;

public class Utils {
    private static final Set<String> ROAD_HIGHWAY_VALUES = Collections.unmodifiableSet(new HashSet<String>(Arrays
            .asList("motorway", "motorway_link", "trunk", "trunk_link", "primary", "primary_link", "secondary",
                    "secondary_link", "tertiary", "residential", "unclassified", "road", "living_street", "service",
                    "track", "pedestrian", "raceway", "services")));
    
    public static boolean isRoad(Way w) {
        return ROAD_HIGHWAY_VALUES.contains(w.get("highway"));
    }
    
    public static final List<Way> filterRoads(List<OsmPrimitive> of) {
        final List<Way> result = new ArrayList<Way>();
        
        for (OsmPrimitive p : of) {
            if (p.getType() == OsmPrimitiveType.WAY && Utils.isRoad((Way) p)) {
                result.add((Way) p);
            }
        }
        
        return result;
    }
    
    public static Node getMemberNode(Relation r, String role) {
        return getMember(r, role, OsmPrimitiveType.NODE).getNode();
    }
    
    public static Way getMemberWay(Relation r, String role) {
        return getMember(r, role, OsmPrimitiveType.WAY).getWay();
    }
    
    public static RelationMember getMember(Relation r, String role, OsmPrimitiveType type) {
        final List<RelationMember> candidates = getMembers(r, role, type);
        if (candidates.isEmpty()) {
            throw UnexpectedDataException.Kind.NO_MEMBER.chuck(role);
        } else if (candidates.size() > 1) {
            throw UnexpectedDataException.Kind.MULTIPLE_MEMBERS.chuck(role);
        }
        return candidates.get(0);
    }
    
    public static List<RelationMember> getMembers(Relation r, String role, OsmPrimitiveType type) {
        final List<RelationMember> result = getMembers(r, role);
        for (RelationMember m : getMembers(r, role)) {
            if (m.getType() != type) {
                throw UnexpectedDataException.Kind.WRONG_MEMBER_TYPE.chuck(role, m.getType(), type);
            }
        }
        return result;
    }
    
    public static List<RelationMember> getMembers(Relation r, String role) {
        final List<RelationMember> result = new ArrayList<RelationMember>();
        for (RelationMember m : r.getMembers()) {
            if (m.getRole().equals(role)) {
                result.add(m);
            }
        }
        return result;
    }
    
    public static List<Node> getMemberNodes(Relation r, String role) {
        return mapMembers(getMembers(r, role, OsmPrimitiveType.NODE), Node.class);
    }
    
    public static List<Way> getMemberWays(Relation r, String role) {
        return mapMembers(getMembers(r, role, OsmPrimitiveType.WAY), Way.class);
    }
    
    private static <T> List<T> mapMembers(List<RelationMember> ms, Class<T> t) {
        final List<T> result = new ArrayList<T>(ms.size());
        for (RelationMember m : ms) {
            result.add(t.cast(m.getMember()));
        }
        return result;
    }
    
    /**
     * 
     * @param a
     * @param b
     * @return the node at which {@code a} and {@code b} are connected
     */
    public static Node lineUp(Way a, Way b) {
        final Set<Node> s = new HashSet<Node>(Arrays.asList(a.firstNode(), a.lastNode(), b.firstNode(), b.lastNode()));
        if (a.firstNode() == a.lastNode() || b.firstNode().equals(b.lastNode()) || s.size() == 2) {
            throw new IllegalArgumentException("Cycles are not allowed.");
        } else if (s.size() == 4) {
            throw new IllegalArgumentException("Ways are not connected (at their first and last nodes).");
        }
        
        if (a.firstNode() == b.firstNode() || a.lastNode() == b.firstNode()) {
            return b.firstNode();
        } else if (a.firstNode() == b.lastNode() || a.lastNode() == b.lastNode()) {
            return b.lastNode();
        } else {
            throw new AssertionError();
        }
    }
    
    public static Node getOppositeEnd(Way w, Node n) {
        final boolean first = n.equals(w.firstNode());
        final boolean last = n.equals(w.lastNode());
        
        if (first && last) {
            throw new IllegalArgumentException("Way starts as well as ends at the given node.");
        } else if (first) {
            return w.lastNode();
        } else if (last) {
            return w.firstNode();
        } else {
            throw new IllegalArgumentException("Way neither starts nor ends at given node.");
        }
    }
    
    /**
     * Orders the {@code ways} such that the combined ways out of each returned list form a path (in
     * order) from one node out of {@code nodes} to another out of {@code nodes}.
     * 
     * <ul>
     * <li>Each way is used exactly once.</li>
     * <li>Paths contain no {@code nodes} excepting the first and last nodes.</li>
     * <li>Paths contain no loops w.r.t. the ways' first and last nodes</li>
     * </ul>
     * 
     * @param ways
     *            ways to be ordered
     * @param nodes
     *            start/end nodes
     * @return
     * @throws IllegalArgumentException
     *             if the ways can't be ordered
     */
    public static List<Route> orderWays(Iterable<Way> ways, Iterable<Node> nodes) {
        final List<Way> ws = new LinkedList<Way>(CollectionUtils.toList(ways));
        final Set<Node> ns = new HashSet<Node>(CollectionUtils.toList(nodes));
        
        final List<Route> result = new ArrayList<Route>();
        
        while (!ws.isEmpty()) {
            result.add(findPath(ws, ns));
        }
        
        return result;
    }
    
    private static Route findPath(List<Way> ws, Set<Node> ns) {
        final Way w = findPathSegment(ws, ns);
        final boolean first = ns.contains(w.firstNode());
        final boolean last = ns.contains(w.lastNode());
        
        if (first && last) {
            return Route.create(Arrays.asList(w), w.firstNode());
        } else if (!first && !last) {
            throw new AssertionError();
        }
        
        final List<Way> result = new ArrayList<Way>();
        result.add(w);
        Node n = first ? w.lastNode() : w.firstNode();
        while (true) {
            final Way next = findPathSegment(ws, Arrays.asList(n));
            result.add(next);
            n = getOppositeEnd(next, n);
            
            if (ns.contains(n)) {
                return Route.create(result, first ? w.firstNode() : w.lastNode());
            }
        }
    }
    
    private static Way findPathSegment(List<Way> ws, Collection<Node> ns) {
        final Iterator<Way> it = ws.iterator();
        
        while (it.hasNext()) {
            final Way w = it.next();
            
            if (ns.contains(w.firstNode()) || ns.contains(w.lastNode())) {
                it.remove();
                return w;
            }
        }
        
        throw new IllegalArgumentException("Ways can't be ordered.");
    }
    
    public static Iterable<Way> flattenVia(Node start, List<Road> via, Node end) {
        final List<Way> result = new ArrayList<Way>();
        
        Node n = start;
        for (Road r : via) {
            final Iterable<Route.Segment> segments = r.getRoute().getFirstSegment().getWay().isFirstLastNode(n) ? r
                    .getRoute().getSegments() : CollectionUtils.reverse(r.getRoute().getSegments());
            
            for (Route.Segment s : segments) {
                result.add(s.getWay());
                n = Utils.getOppositeEnd(s.getWay(), n);
            }
        }
        if (!end.equals(n)) {
            throw new IllegalArgumentException("The given via ways don't end at the given node.");
        }
        
        return result;
    }
    
    public static int parseIntTag(OsmPrimitive primitive, String tag) {
        final String value = primitive.get(tag);
        
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw UnexpectedDataException.Kind.INVALID_TAG_FORMAT.chuck(tag, value);
            }
        }
        
        throw UnexpectedDataException.Kind.MISSING_TAG.chuck(tag);
    }
}
