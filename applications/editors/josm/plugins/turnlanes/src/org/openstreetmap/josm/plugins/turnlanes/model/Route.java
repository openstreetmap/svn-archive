package org.openstreetmap.josm.plugins.turnlanes.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;

public class Route {
    public static final class Segment {
        private final Node start;
        private final Way way;
        private final Node end;
        
        private final List<Node> nodes;
        
        Segment(Node start, Way way, Node end) {
            this.start = start;
            this.way = way;
            this.end = end;
            
            final List<Node> ns = way.getNodes();
            if (way.lastNode().equals(start)) {
                Collections.reverse(ns);
            }
            
            this.nodes = Collections.unmodifiableList(ns);
        }
        
        public Node getStart() {
            return start;
        }
        
        public Way getWay() {
            return way;
        }
        
        public Node getEnd() {
            return end;
        }
        
        public List<Node> getNodes() {
            return nodes;
        }
        
        public double getLength() {
            double length = 0;
            
            Node last = nodes.get(0);
            for (Node n : nodes.subList(1, nodes.size())) {
                length += last.getCoor().greatCircleDistance(n.getCoor());
                last = n;
            }
            
            return length;
        }
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((end == null) ? 0 : end.hashCode());
            result = prime * result + ((start == null) ? 0 : start.hashCode());
            result = prime * result + ((way == null) ? 0 : way.hashCode());
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
            Segment other = (Segment) obj;
            if (end == null) {
                if (other.end != null)
                    return false;
            } else if (!end.equals(other.end))
                return false;
            if (start == null) {
                if (other.start != null)
                    return false;
            } else if (!start.equals(other.start))
                return false;
            if (way == null) {
                if (other.way != null)
                    return false;
            } else if (!way.equals(other.way))
                return false;
            return true;
        }
    }
    
    public static Route load(Relation r) {
        final Node end = Utils.getMemberNode(r, Constants.LENGTHS_ROLE_END);
        final List<Way> ws = Utils.getMemberWays(r, Constants.LENGTHS_ROLE_WAYS);
        
        return create(ws, end);
    }
    
    public static Route load(Relation left, Relation right, Way w) {
        left = left == null ? right : left;
        right = right == null ? left : right;
        
        if (left == null) {
            throw new IllegalArgumentException("At least one relation must not be null.");
        }
        
        final Route leftRoute = load(left);
        final Route rightRoute = load(right);
        
        int iLeft = 0;
        while (!w.equals(leftRoute.getSegments().get(iLeft++).getWay()))
            ;
        
        int iRight = 0;
        while (!w.equals(rightRoute.getSegments().get(iRight++).getWay()))
            ;
        
        final int min = Math.min(iLeft, iRight);
        
        final List<Segment> leftSegments = leftRoute.getSegments().subList(iLeft - min, iLeft);
        final List<Segment> rightSegments = rightRoute.getSegments().subList(iRight - min, iRight);
        
        if (!leftSegments.equals(rightSegments)) {
            throw new IllegalArgumentException("Routes are split across different ways.");
        }
        
        return new Route(iLeft == min ? rightSegments : leftSegments);
    }
    
    public static Route create(List<Way> ws, Node end) {
        final List<Segment> segments = new ArrayList<Segment>(ws.size());
        
        for (Way w : ws) {
            if (!w.isFirstLastNode(end)) {
                throw new IllegalArgumentException("Ways must be ordered.");
            }
            
            final Node start = Utils.getOppositeEnd(w, end);
            segments.add(0, new Segment(start, w, end));
            end = start;
        }
        
        return new Route(segments);
    }
    
    private final List<Segment> segments;
    
    private Route(List<Segment> segments) {
        this.segments = Collections.unmodifiableList(new ArrayList<Segment>(segments));
    }
    
    public List<Segment> getSegments() {
        return segments;
    }
    
    public List<Node> getNodes() {
        final List<Node> ns = new ArrayList<Node>();
        
        ns.add(segments.get(0).getStart());
        for (Segment s : segments) {
            ns.addAll(s.getNodes().subList(1, s.getNodes().size()));
        }
        
        return Collections.unmodifiableList(ns);
    }
    
    public double getLengthFrom(Way w) {
        double length = Double.NEGATIVE_INFINITY;
        
        for (Segment s : getSegments()) {
            length += s.getLength();
            
            if (w.equals(s.getWay())) {
                length = 0;
            }
        }
        
        if (length < 0) {
            throw new IllegalArgumentException("Way must be part of the route.");
        }
        
        return length;
    }
    
    public double getLength() {
        double length = 0;
        
        for (Segment s : getSegments()) {
            length += s.getLength();
        }
        
        return length;
    }
    
    public Node getStart() {
        return getFirstSegment().getStart();
    }
    
    public Node getEnd() {
        return getLastSegment().getEnd();
    }
    
    public Segment getFirstSegment() {
        return getSegments().get(0);
    }
    
    public Segment getLastSegment() {
        return getSegments().get(getSegments().size() - 1);
    }
    
    public Route subRoute(int fromIndex, int toIndex) {
        return new Route(segments.subList(fromIndex, toIndex));
    }
    
    public List<Way> getWays() {
        final List<Way> ws = new ArrayList<Way>();
        
        for (Segment s : segments) {
            ws.add(s.getWay());
        }
        
        return Collections.unmodifiableList(ws);
    }
}
