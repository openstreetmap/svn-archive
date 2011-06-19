package org.openstreetmap.josm.plugins.turnlanes.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.turnlanes.CollectionUtils;
import org.openstreetmap.josm.tools.Pair;

public class ModelContainer {
    public static ModelContainer create(Iterable<Node> primaryNodes, Iterable<Way> primaryWays) {
        final Set<Node> closedNodes = new HashSet<Node>(CollectionUtils.toList(primaryNodes));
        final Set<Way> closedWays = new HashSet<Way>(CollectionUtils.toList(primaryWays));
        
        close(closedNodes, closedWays);
        
        return new ModelContainer(closedNodes, closedWays);
    }
    
    private static void close(Set<Node> closedNodes, Set<Way> closedWays) {
        boolean closed = false;
        
        while (!closed) {
            closed = true;
            
            for (Node n : closedNodes) {
                for (Way w : Utils.filterRoads(n.getReferrers())) {
                    if (w.isFirstLastNode(n)) {
                        closed &= close(closedNodes, closedWays, w, Constants.TURN_ROLE_FROM);
                        closed &= close(closedNodes, closedWays, w, Constants.TURN_ROLE_TO);
                    }
                }
                
                for (Way w : closedWays) {
                    closed &= close(closedNodes, closedWays, w, Constants.TURN_ROLE_VIA);
                }
            }
        }
    }
    
    private static boolean close(Set<Node> closedNodes, Set<Way> closedWays, Way w, String role) {
        boolean closed = true;
        
        for (Relation r : OsmPrimitive.getFilteredList(w.getReferrers(), Relation.class)) {
            if (!r.get("type").equals(Constants.TYPE_TURNS)) {
                continue;
            }
            
            for (RelationMember m : r.getMembers()) {
                if (m.getRole().equals(role) && m.getMember().equals(w)) {
                    closed &= close(closedNodes, closedWays, r);
                }
            }
        }
        
        return closed;
    }
    
    private static boolean close(Set<Node> closedNodes, Set<Way> closedWays, Relation r) {
        boolean closed = true;
        
        final List<Way> via = new ArrayList<Way>();
        for (RelationMember m : Utils.getMembers(r, Constants.TURN_ROLE_VIA)) {
            if (m.isWay()) {
                closed &= !closedWays.add(m.getWay());
                via.add(m.getWay());
            } else if (m.isNode()) {
                closed &= !closedNodes.add(m.getNode());
            }
        }
        
        if (!via.isEmpty()) {
            final Way from = Utils.getMemberWay(r, Constants.TURN_ROLE_FROM);
            final Way to = Utils.getMemberWay(r, Constants.TURN_ROLE_TO);
            
            closed &= !closedNodes.add(Utils.lineUp(from, via.get(0)));
            closed &= !closedNodes.add(Utils.lineUp(via.get(via.size() - 1), to));
        }
        
        return closed;
    }
    
    private final Map<Node, Junction> junctions = new HashMap<Node, Junction>();
    private final Map<Way, Road> roads = new HashMap<Way, Road>();
    
    private final Set<Node> primaryNodes;
    private final Set<Way> primaryWays;
    
    private ModelContainer(Set<Node> primaryNodes, Set<Way> primaryWays) {
        this.primaryNodes = Collections.unmodifiableSet(new HashSet<Node>(primaryNodes));
        this.primaryWays = Collections.unmodifiableSet(new HashSet<Way>(primaryWays));
        
        final Set<Pair<Way, Junction>> ws = new HashSet<Pair<Way, Junction>>();
        for (Node n : primaryNodes) {
            final Junction j = getOrCreateJunction(n);
            
            for (Way w : Utils.filterRoads(n.getReferrers())) {
                if (w.isFirstLastNode(n)) {
                    ws.add(new Pair<Way, Junction>(w, j));
                }
            }
        }
        
        final List<Route> rs = Utils.orderWays(primaryWays, primaryNodes);
        for (Route r : rs) {
            addRoad(new Road(this, r));
        }
        
        for (Pair<Way, Junction> w : ws) {
            if (!primaryWays.contains(w.a)) {
                addRoad(new Road(this, w.a, w.b));
            }
        }
    }
    
    Junction getOrCreateJunction(Node n) {
        final Junction existing = junctions.get(n);
        
        if (existing != null) {
            return existing;
        }
        
        return new Junction(this, n);
    }
    
    public Junction getJunction(Node n) {
        Junction j = junctions.get(n);
        
        if (j == null) {
            throw new IllegalArgumentException();
        }
        
        return j;
    }
    
    Road getRoad(Way w) {
        final Road r = roads.get(w);
        
        if (r == null) {
            throw new IllegalArgumentException("There is no road containing the given way.");
        }
        
        return r;
    }
    
    private void addRoad(Road newRoad, Road mergedA, Road mergedB) {
        assert (mergedA == null) == (mergedB == null);
        
        for (Route.Segment s : newRoad.getRoute().getSegments()) {
            final Road oldRoad = roads.put(s.getWay(), newRoad);
            
            if (oldRoad != null) {
                if (mergedA == null) {
                    final Road mergedRoad = mergeRoads(oldRoad, newRoad);
                    addRoad(mergedRoad, oldRoad, newRoad);
                } else if (!oldRoad.equals(mergedA) && !oldRoad.equals(mergedB)) {
                    throw new RuntimeException("A road can't be connected to more than two junctions.");
                }
            }
        }
    }
    
    private void addRoad(Road newRoad) {
        addRoad(newRoad, null, null);
    }
    
    private Road mergeRoads(Road a, Road b) {
        final String ERR_ILLEGAL_ARGS = "The given roads can not be merged into one.";
        
        final List<Way> ws = new ArrayList<Way>(CollectionUtils.toList(CollectionUtils.reverse(a.getRoute().getWays())));
        final List<Way> bws = b.getRoute().getWays();
        
        int i = -1;
        for (Way w : ws) {
            if (w.equals(bws.get(i + 1))) {
                ++i;
            } else if (i >= 0) {
                throw new IllegalArgumentException(ERR_ILLEGAL_ARGS);
            }
        }
        
        if (i < 0) {
            throw new IllegalArgumentException(ERR_ILLEGAL_ARGS);
        }
        ws.addAll(bws.subList(i + 1, bws.size()));
        
        final Route mergedRoute = Route.create(ws, a.getRoute().getLastSegment().getEnd());
        return new Road(this, mergedRoute);
    }
    
    void register(Junction j) {
        if (junctions.put(j.getNode(), j) != null) {
            throw new IllegalStateException();
        }
    }
    
    public Set<Junction> getPrimaryJunctions() {
        final Set<Junction> pjs = new HashSet<Junction>();
        
        for (Node n : primaryNodes) {
            pjs.add(getOrCreateJunction(n));
        }
        
        return pjs;
    }
    
    public Set<Road> getPrimaryRoads() {
        final Set<Road> prs = new HashSet<Road>();
        
        for (Way w : primaryWays) {
            prs.add(roads.get(w));
        }
        
        return prs;
    }
    
    public ModelContainer recalculate() {
        return new ModelContainer(primaryNodes, primaryWays);
    }
    
    public boolean isPrimary(Junction j) {
        return primaryNodes.contains(j.getNode());
    }
    
    public boolean isPrimary(Road r) {
        return primaryWays.contains(r.getRoute().getFirstSegment().getWay());
    }
}
