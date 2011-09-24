package org.openstreetmap.josm.plugins.turnlanes.model;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;

public class Lane {
    public enum Kind {
        EXTRA_LEFT,
        EXTRA_RIGHT,
        REGULAR;
        
        public boolean isExtra() {
            return this == EXTRA_LEFT || this == EXTRA_RIGHT;
        }
    }
    
    static List<Lane> load(Road.End roadEnd) {
        final List<Lane> result = new ArrayList<Lane>();
        int i;
        
        i = 0;
        for (double l : roadEnd.getLengths(Kind.EXTRA_LEFT)) {
            result.add(new Lane(roadEnd, --i, Kind.EXTRA_LEFT, l));
        }
        Collections.reverse(result);
        
        final int regulars = getRegularCount(roadEnd.getWay(), roadEnd.getJunction().getNode());
        for (i = 1; i <= regulars; ++i) {
            result.add(new Lane(roadEnd, i));
        }
        
        i = 0;
        for (double l : roadEnd.getLengths(Kind.EXTRA_RIGHT)) {
            result.add(new Lane(roadEnd, ++i, Kind.EXTRA_RIGHT, l));
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
                
                if (length >= lengthBound) {
                    result.add(length);
                }
            }
        }
        
        return result;
    }
    
    static int getRegularCount(Way w, Node end) {
        final int count = Utils.parseIntTag(w, "lanes");
        final boolean forward = w.lastNode().equals(end);
        
        if (w.hasDirectionKeys()) {
            return getRegularCountOneWay(w, forward, count);
        } else {
            return getRegularCountTwoWay(w, forward, count);
        }
    }
    
    private static int getRegularCountOneWay(Way w, boolean forward, final int count) {
        if (forward ^ "-1".equals(w.get("oneway"))) {
            return count;
        } else {
            return 0;
        }
    }
    
    private static int getRegularCountTwoWay(Way w, boolean forward, final int count) {
        if (w.get("lanes:backward") != null) {
            final int backwardCount = Utils.parseIntTag(w, "lanes:backward");
            return forward ? count - backwardCount : backwardCount;
        }
        
        if (w.get("lanes:forward") != null) {
            final int forwardCount = Utils.parseIntTag(w, "lanes:forward");
            return forward ? forwardCount : count - forwardCount;
        }
        
        // default: round up in forward direction...
        return forward ? (count + 1) / 2 : count / 2;
    }
    
    private final Road.End roadEnd;
    private final int index;
    private final Kind kind;
    
    private Set<Turn> turns;
    private double length = -1;
    
    public Lane(Road.End roadEnd, int index) {
        this.roadEnd = roadEnd;
        this.index = index;
        this.kind = Kind.REGULAR;
    }
    
    public Lane(Road.End roadEnd, int index, Kind kind, double length) {
        assert kind == Kind.EXTRA_LEFT || kind == Kind.EXTRA_RIGHT;
        
        this.roadEnd = roadEnd;
        this.index = index;
        this.kind = kind;
        this.length = length;
        
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }
    }
    
    public Road getRoad() {
        return roadEnd.getRoad();
    }
    
    public Kind getKind() {
        return kind;
    }
    
    public double getLength() {
        return isExtra() ? length : getRoad().getLength();
    }
    
    public void setLength(double length) {
        if (!isExtra()) {
            throw new UnsupportedOperationException("Length can only be set for extra lanes.");
        } else if (length <= 0) {
            throw new IllegalArgumentException("Length must positive.");
        }
        
        this.length = length;
        
        // TODO if needed, increase length of other lanes
        getOutgoingRoadEnd().updateLengths();
    }
    
    public boolean isExtra() {
        return getKind() != Kind.REGULAR;
    }
    
    public int getIndex() {
        return index;
    }
    
    public Junction getOutgoingJunction() {
        return getOutgoingRoadEnd().getJunction();
    }
    
    public Junction getIncomingJunction() {
        return getIncomingRoadEnd().getJunction();
    }
    
    public Road.End getOutgoingRoadEnd() {
        return roadEnd;
    }
    
    public Road.End getIncomingRoadEnd() {
        return roadEnd.getOppositeEnd();
    }
    
    public ModelContainer getContainer() {
        return getRoad().getContainer();
    }
    
    public void addTurn(List<Road> via, Road.End to) {
        final GenericCommand cmd = new GenericCommand(getOutgoingJunction().getNode().getDataSet(), tr("Add turn"));
        
        Relation existing = null;
        for (Turn t : to.getTurns()) {
            if (t.getFrom().getOutgoingRoadEnd().equals(getOutgoingRoadEnd()) && t.getVia().equals(via)) {
                if (t.getFrom().equals(this)) {
                    // was already added
                    return;
                }
                
                existing = t.getRelation();
            }
        }
        
        final Relation r;
        if (existing == null) {
            r = new Relation();
            r.put("type", Constants.TYPE_TURNS);
            
            r.addMember(new RelationMember(Constants.TURN_ROLE_FROM, getOutgoingRoadEnd().getWay()));
            if (via.isEmpty()) {
                r.addMember(new RelationMember(Constants.TURN_ROLE_VIA, getOutgoingJunction().getNode()));
            } else {
                for (Way w : Utils.flattenVia(getOutgoingJunction().getNode(), via, to.getJunction().getNode())) {
                    r.addMember(new RelationMember(Constants.TURN_ROLE_VIA, w));
                }
            }
            r.addMember(new RelationMember(Constants.TURN_ROLE_TO, to.getWay()));
            
            cmd.add(r);
        } else {
            r = existing;
        }
        
        final String key = isExtra() ? Constants.TURN_KEY_EXTRA_LANES : Constants.TURN_KEY_LANES;
        final List<Integer> lanes = Turn.indices(r, key);
        lanes.add(getIndex());
        cmd.backup(r).put(key, Turn.join(lanes));
        
        Main.main.undoRedo.add(cmd);
    }
    
    public Set<Turn> getTurns() {
        return turns;
    }
    
    public void remove() {
        if (!isExtra()) {
            throw new UnsupportedOperationException();
        }
        
        final GenericCommand cmd = new GenericCommand(getOutgoingJunction().getNode().getDataSet(), tr("Delete lane."));
        
        for (Turn t : getTurns()) {
            t.remove(cmd);
        }
        
        getOutgoingRoadEnd().removeLane(cmd, this);
        
        Main.main.undoRedo.add(cmd);
    }
    
    void initialize() {
        final Set<Turn> turns = Turn.load(getContainer(), Constants.TURN_ROLE_FROM, getOutgoingRoadEnd().getWay());
        
        final Iterator<Turn> it = turns.iterator();
        while (it.hasNext()) {
            final Turn t = it.next();
            
            if (!t.getFrom().equals(this)) {
                it.remove();
            }
        }
        
        this.turns = Collections.unmodifiableSet(turns);
    }
}
