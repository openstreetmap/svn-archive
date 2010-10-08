package org.openstreetmap.josm.plugins.turnrestrictions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.tools.CheckParameterUtil;

/**
 * TurnRestrictionBuilder creates a turn restriction and initializes it with
 * objects from a selection of OSM primitives, i.e. the current selection
 * in a {@link OsmDataLayer}.
 *
 */
public class TurnRestrictionBuilder {

    private Way from;
    private Way to;
    private final ArrayList<OsmPrimitive> vias = new ArrayList<OsmPrimitive>();
    
    public TurnRestrictionBuilder(){
    }
    
    /**
     * Initializes the 'from' leg. Proposes the  first element
     * in {@code primitives} as 'from' leg if this element is a
     * non-deleted, visible way.
     * 
     * @param primitives
     */
    protected void initFromLeg(List<OsmPrimitive> primitives){
        if (primitives == null || primitives.isEmpty()) return;
        OsmPrimitive p = primitives.get(0);
        if (! (p instanceof Way)) return;
        Way fromLeg = (Way)p;
        if (fromLeg.isDeleted() || ! fromLeg.isVisible()) return;
        this.from = fromLeg;
    }

    /**
     * Initializes the 'to' leg. Proposes the last element 
     * in {@code primitives} as 'to' leg if this element is a
     * non-deleted, visible way.
     *
     * @param primitives
     */
    protected void initToLeg(List<OsmPrimitive> primitives){
        if (primitives == null || primitives.isEmpty()) return;
        if (primitives.size() < 2) return;
        OsmPrimitive p = primitives.get(primitives.size()-1);
        if (! (p instanceof Way)) return;
        Way toLeg = (Way)p;
        if (toLeg.isDeleted() || ! toLeg.isVisible()) return;
        this.to = toLeg;
    }
    
    /**
     * Initializes the vias from the two turn restriction legs. The two
     * legs have to be defined, otherwise no via is proposed. This methods
     * proposes exactly one node as via, if the two turn restriction
     * legs intersect at exactly one node. 
     */
    protected void initViaFromLegs(){
        if (from == null || to == null) return;     
        // check whether 'from' and 'to' have exactly one intersecting 
        // node. This node is proposed as via node. The turn restriction
        // node will also provide functionality to split either or both
        // of 'from' and 'to' way if they aren't connected from tail to
        // head
        //
        HashSet<Node> nodes = new HashSet<Node>();
        nodes.addAll(from.getNodes());
        nodes.retainAll(to.getNodes());
        if (nodes.size() == 1){
            vias.add(nodes.iterator().next());
        }       
    }
    
    /**
     * Initializes the vias with the primitives (1..size-2), provided
     * these primitives aren't relations and they are visible and non-deleted.
     * 
     * @param primitives
     */
    protected void initViasFromPrimitives(List<OsmPrimitive> primitives) {
        if (primitives == null || primitives.size() <=2) return;
        // if we didn't find a from or a to way, we don't propose via objects
        // either
        if (from == null || to == null) return;
        for(int i=1; i< primitives.size() -2;i++){
            OsmPrimitive p = primitives.get(i);
            if (p == null) continue;
            if (p instanceof Relation) continue;
            if (p.isDeleted() || ! p.isVisible()) continue;
            vias.add(p);
        }
    }
    
    /**
     * Resets the builder 
     */
    protected void reset() {
        this.from = null;
        this.to = null;
        this.vias.clear();
    }
    
    /**
     * Creates and initializes a new turn restriction based on the primitives
     * currently selected in layer {@code layer}.
     *  
     * @param layer the layer. Must not be null.
     * @return the new initialized turn restriction. The turn restriction isn't
     * added to the layer yet.
     * @throws IllegalArgumentException thrown if layer is null
     */
    public synchronized Relation buildFromSelection(OsmDataLayer layer) {
        CheckParameterUtil.ensureParameterNotNull(layer, "layer");
        List<OsmPrimitive> selection = new ArrayList<OsmPrimitive>(layer.data.getSelected());
        return build(selection);
    }

    /**
     * Creates and initializes a new turn restriction based on primitives 
     * in {@code primitives}.
     * 
     * @param primitives the primitives 
     * @return the new initialized turn restriction. The turn restriction isn't
     * added to the layer yet.
     */
    public synchronized Relation build(List<OsmPrimitive> primitives){
        Relation tr = new Relation();
        tr.put("type", "restriction");
        if (primitives == null || primitives.isEmpty()) return tr;
        if (primitives.size() <=2){
            initFromLeg(primitives);
            initToLeg(primitives);
            initViaFromLegs();
        } else if (primitives.size() > 2) {
            initFromLeg(primitives);
            initToLeg(primitives);
            initViasFromPrimitives(primitives);
        }
        
        if (from != null){
            tr.addMember(new RelationMember("from", from));
        }
        if (to != null){
            tr.addMember(new RelationMember("to", to));
        }
        for(OsmPrimitive via: vias){
            tr.addMember(new RelationMember("via", via));
        }
        return tr;
    }       
}
