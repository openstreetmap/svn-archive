package org.openstreetmap.josm.plugins.turnlanes.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.turnlanes.CollectionUtils;

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
        for (double l : CollectionUtils.reverse(roadEnd.getLengths(Kind.EXTRA_LEFT))) {
            result.add(new Lane(roadEnd, --i, Kind.EXTRA_LEFT, l));
        }
        
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
            try {
                return Integer.parseInt(countStr);
            } catch (NumberFormatException e) {
                throw UnexpectedDataException.Kind.INVALID_TAG_FORMAT.chuck("lanes", countStr);
            }
        }
        
        throw UnexpectedDataException.Kind.MISSING_TAG.chuck("lanes");
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
    
    private final Road.End roadEnd;
    private final int index;
    private final Kind kind;
    
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
        
        // TODO if needed, increase length of other lanes
        getOutgoingRoadEnd().updateLengths();
        
        this.length = length;
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
        assert equals(to.getJunction());
        
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
            
            getOutgoingJunction().getNode().getDataSet().addPrimitive(r);
        } else {
            r = existing;
        }
        
        final String key = isExtra() ? Constants.TURN_KEY_EXTRA_LANES : Constants.TURN_KEY_LANES;
        final List<Integer> lanes = Turn.indices(r, key);
        lanes.add(getIndex());
        r.put(key, Turn.join(lanes));
    }
    
    public Set<Turn> getTurns() {
        return Turn.load(getContainer(), Constants.TURN_ROLE_FROM, getOutgoingRoadEnd().getWay());
    }
}
