package org.openstreetmap.josm.plugins.turnlanes.model;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.turnlanes.CollectionUtils;

public final class Turn {
    static Set<Turn> load(ModelContainer c, String role, OsmPrimitive primitive) {
        final Set<Turn> result = new HashSet<Turn>();
        
        for (Relation r : OsmPrimitive.getFilteredList(primitive.getReferrers(), Relation.class)) {
            if (!r.isUsable() || !r.get("type").equals(Constants.TYPE_TURNS)) {
                continue;
            }
            
            for (RelationMember m : r.getMembers()) {
                if (m.getRole().equals(role) && m.getMember().equals(primitive)) {
                    result.addAll(load(c, r));
                }
            }
        }
        
        return result;
    }
    
    static Set<Turn> load(ModelContainer c, Relation r) {
        for (RelationMember m : r.getMembers()) {
            if (m.getRole().equals(Constants.TURN_ROLE_VIA)) {
                if (m.isNode()) {
                    return loadWithViaNode(c, r);
                } else if (m.isWay()) {
                    return loadWithViaWays(c, r);
                }
            }
        }
        
        throw new IllegalArgumentException("No via node or way(s).");
    }
    
    private static Set<Turn> loadWithViaWays(ModelContainer c, Relation r) {
        final Way from = Utils.getMemberWay(r, Constants.TURN_ROLE_FROM);
        final Way to = Utils.getMemberWay(r, Constants.TURN_ROLE_TO);
        
        if (!c.hasRoad(from) || !c.hasRoad(to)) {
            return Collections.emptySet();
        }
        
        final List<Way> tmp = Utils.getMemberWays(r, Constants.TURN_ROLE_VIA);
        final LinkedList<Road> via = new LinkedList<Road>();
        
        final Road.End fromRoadEnd = c.getJunction(Utils.lineUp(from, tmp.get(0))).getRoadEnd(from);
        
        Node n = fromRoadEnd.getJunction().getNode();
        final Iterator<Way> it = tmp.iterator();
        while (it.hasNext()) {
            final Way w = it.next();
            if (!c.hasRoad(w)) {
                return Collections.emptySet();
            }
            
            final Road v = c.getRoad(w);
            via.add(v);
            n = Utils.getOppositeEnd(w, n);
            
            if (!v.isPrimary()) {
                throw new IllegalStateException("The road is not part of the junction.");
            }
            
            final Iterator<Route.Segment> it2 = (v.getRoute().getFirstSegment().getWay().equals(w) ? v.getRoute()
                    .getSegments() : CollectionUtils.reverse(v.getRoute().getSegments())).iterator();
            it2.next(); // first is done
            
            while (it2.hasNext()) {
                final Way w2 = it2.next().getWay();
                n = Utils.getOppositeEnd(w2, n);
                
                if (!it.hasNext() || !w2.equals(it.next())) {
                    throw new IllegalStateException("The via ways of the relation do not form a road.");
                }
            }
        }
        final Road.End toRoadEnd = c.getJunction(n).getRoadEnd(to);
        n = Utils.getOppositeEnd(to, n);
        
        final Set<Turn> result = new HashSet<Turn>();
        for (int i : indices(r, Constants.TURN_KEY_LANES)) {
            result.add(new Turn(r, fromRoadEnd.getLane(Lane.Kind.REGULAR, i), via, toRoadEnd));
        }
        for (int i : indices(r, Constants.TURN_KEY_EXTRA_LANES)) {
            result.add(new Turn(r, fromRoadEnd.getExtraLane(i), via, toRoadEnd));
        }
        return result;
    }
    
    static List<Integer> indices(Relation r, String key) {
        final String joined = r.get(key);
        
        if (joined == null) {
            return new ArrayList<Integer>(1);
        }
        
        final List<Integer> result = new ArrayList<Integer>();
        for (String lane : Constants.SPLIT_PATTERN.split(joined)) {
            result.add(Integer.parseInt(lane));
        }
        
        return result;
    }
    
    private static Set<Turn> loadWithViaNode(ModelContainer c, Relation r) {
        final Way from = Utils.getMemberWay(r, Constants.TURN_ROLE_FROM);
        final Node via = Utils.getMemberNode(r, Constants.TURN_ROLE_VIA);
        final Way to = Utils.getMemberWay(r, Constants.TURN_ROLE_TO);
        
        if (!c.hasRoad(from) || !c.hasJunction(via) || !c.hasRoad(to)) {
            return Collections.emptySet();
        }
        
        final Junction j = c.getJunction(via);
        
        final Road.End fromRoadEnd = j.getRoadEnd(from);
        final Road.End toRoadEnd = j.getRoadEnd(to);
        
        final Set<Turn> result = new HashSet<Turn>();
        for (int i : indices(r, Constants.TURN_KEY_LANES)) {
            result.add(new Turn(r, fromRoadEnd.getLane(Lane.Kind.REGULAR, i), Collections.<Road> emptyList(), toRoadEnd));
        }
        for (int i : indices(r, Constants.TURN_KEY_EXTRA_LANES)) {
            result.add(new Turn(r, fromRoadEnd.getExtraLane(i), Collections.<Road> emptyList(), toRoadEnd));
        }
        return result;
    }
    
    static String join(List<Integer> list) {
        if (list.isEmpty()) {
            return null;
        }
        
        final StringBuilder builder = new StringBuilder(list.size() * (2 + Constants.SEPARATOR.length()));
        
        for (int e : list) {
            builder.append(e).append(Constants.SEPARATOR);
        }
        
        builder.setLength(builder.length() - Constants.SEPARATOR.length());
        return builder.toString();
    }
    
    private final Relation relation;
    
    private final Lane from;
    private final List<Road> via;
    private final Road.End to;
    
    public Turn(Relation relation, Lane from, List<Road> via, Road.End to) {
        this.relation = relation;
        this.from = from;
        this.via = via;
        this.to = to;
    }
    
    public Lane getFrom() {
        return from;
    }
    
    public List<Road> getVia() {
        return via;
    }
    
    public Road.End getTo() {
        return to;
    }
    
    Relation getRelation() {
        return relation;
    }
    
    public void remove() {
        final GenericCommand cmd = new GenericCommand(relation.getDataSet(), tr("Delete turn."));
        
        remove(cmd);
        
        Main.main.undoRedo.add(cmd);
    }
    
    void remove(GenericCommand cmd) {
        final List<Integer> lanes = indices(relation, Constants.TURN_KEY_LANES);
        final List<Integer> extraLanes = indices(relation, Constants.TURN_KEY_EXTRA_LANES);
        
        // TODO understand & document
        if (lanes.size() + extraLanes.size() == 1 && (from.isExtra() ^ !lanes.isEmpty())) {
            cmd.backup(relation).setDeleted(true);
            // relation.getDataSet().removePrimitive(relation.getPrimitiveId());
        } else if (from.isExtra()) {
            extraLanes.remove(Integer.valueOf(from.getIndex()));
        } else {
            lanes.remove(Integer.valueOf(from.getIndex()));
        }
        
        cmd.backup(relation).put(Constants.TURN_KEY_LANES, lanes.isEmpty() ? null : join(lanes));
        cmd.backup(relation).put(Constants.TURN_KEY_EXTRA_LANES, extraLanes.isEmpty() ? null : join(extraLanes));
    }
    
    void fixReferences(GenericCommand cmd, boolean left, int index) {
        final List<Integer> fixed = new ArrayList<Integer>();
        for (int i : indices(relation, Constants.TURN_KEY_EXTRA_LANES)) {
            if (left ? i < index : i > index) {
                fixed.add(left ? i + 1 : i - 1);
            } else {
                fixed.add(i);
            }
        }
        
        cmd.backup(relation).put(Constants.TURN_KEY_EXTRA_LANES, join(fixed));
    }
}
