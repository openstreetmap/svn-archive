package org.openstreetmap.josm.plugins.turnrestrictions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.turnrestrictions.editor.TurnRestrictionType;
import org.openstreetmap.josm.tools.CheckParameterUtil;

/**
 * TurnRestrictionBuilder creates a turn restriction and initializes it with
 * objects from a selection of OSM primitives, i.e. the current selection
 * in a {@link OsmDataLayer}.
 *
 */
public class TurnRestrictionBuilder {
    
    public TurnRestrictionBuilder(){
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
     * Tries to initialize a No-U-Turn restriction from the primitives in 
     * <code>primitives</code>. If successful, replies true, otherwise false.
     * 
     * @param primitives the primitives 
     * @return true, if we can propose a U-turn restriction for the primitives
     * in <code>primitives</code>
     */
    protected Relation initNoUTurnRestriction(List<OsmPrimitive> primitives) {
    	if (primitives.size() != 2) return null;
    	    	
    	// we need exactly one node and one way in the selection ...
    	List<Node> nodes = OsmPrimitive.getFilteredList(primitives, Node.class);
    	List<Way> ways = OsmPrimitive.getFilteredList(primitives, Way.class);
    	if (nodes.size() != 1 || ways.size() != 1) return null;
    	
    	// .. and the node has to be the start or the node of the way
    	Way way = ways.get(0);
    	Node node = nodes.get(0);
    	List<Node> wayNodes = way.getNodes();
    	if (wayNodes.size() < 2) return null; // shouldn't happen - just in case
    	if (! (wayNodes.get(0).equals(node) ||wayNodes.get(wayNodes.size()-1).equals(node))) return null;

    	Relation tr = new Relation();
    	tr.put("type", "restriction");
    	tr.addMember(new RelationMember("from", way));
    	tr.addMember(new RelationMember("to", way));
    	tr.addMember(new RelationMember("via", node));
    	tr.put("restriction", TurnRestrictionType.NO_U_TURN.getTagValue());
    	return tr;    	
    }

    /**
     * Replies the unique common node of two ways, or null, if either no
     * such node or multiple common nodes exist.
     * 
     * @param w1 the first way
     * @param w2 the second way
     * @return the common node
     */
    protected Node getUniqueCommonNode(Way w1, Way w2){
    	List<Node> w1Nodes = w1.getNodes();
    	w1Nodes.retainAll(w2.getNodes());
    	if (w1Nodes.size() != 1) return null;
    	return w1Nodes.get(0);
    }    
    
    /**
     * Replies true, if {@code n} is the start node of the way {@code w}.
     * 
     * @param w the way 
     * @param n the node 
     * @return true, if {@code n} is the start node of the way {@code w}.
     */
    protected boolean isStartNode(Way w, Node n) {
    	if (w.getNodesCount() == 0) return false;
    	return w.getNode(0).equals(n);
    }
    
    
    /**
     * Replies true, if {@code n} is the end node of the way {@code w}.
     * 
     * @param w the way 
     * @param n the node 
     * @return true, if {@code n} is the end node of the way {@code w}.
     */
    protected boolean isEndNode(Way w, Node n){
    	if (w.getNodesCount() == 0) return false;
    	return w.getNode(w.getNodesCount()-1).equals(n);
    }
    
    /**
     * <p>Replies true, if the ways {@code w1} and {@code w2} are connected
     * at the node {@code n}.</p>
     * 
     * <p>If {@code w1} and {@code w2} <em>intersect</em> at the node {@code n},
     * this method replies false.</p>
     * 
     * @param w1 the first way
     * @param w2 the second way 
     * @param n the node 
     * @return
     */
    protected boolean isConnectingNode(Way w1, Way w2, Node n){
    	if (isStartNode(w1, n)) {
    		return isStartNode(w2, n)  | isEndNode(w2, n);
    	} else if (isEndNode(w1, n)){
    		return isStartNode(w2, n)  | isEndNode(w2, n);
    	}
    	return false;
    }
    
    /**
     * Replies true, if the way {@code w} is closed at the node {@code n}.
     * 
     * @param w the way
     * @param n the node 
     * @return true, if the way {@code w} is closed at the node {@code n}.
     */
    protected boolean isClosedAt(Way w, Node n){
    	List<Node> nodes = w.getNodes();
    	nodes.retainAll(Collections.singletonList(n));
    	return nodes.size() >= 2;
    }
    
    protected double phi(Way w) {
    	return phi(w, false /* not inverse */);
    }
    
    protected double phi(Way w, boolean doInvert) {
    	double x1 = w.getNode(0).getCoor().getX();
    	double y1 = w.getNode(0).getCoor().getY();
    	double x2 = w.getNode(w.getNodesCount()-1).getCoor().getX();
    	double y2 = w.getNode(w.getNodesCount()-1).getCoor().getY();
    	if (doInvert){
    		double t = x1; x1 = x2; x2 = t;
    		t = y1; y1 = y2; y2 = t;
    	}
    	x2-=x1;
    	y2-=y1;
    	return phi(x2,y2);    	
    }
    
    protected double phi(double x, double y) {
    	return Math.atan2(y, x);
    }   
    
    /**
     * <p>Determines the standard turn restriction between from way {@code w1} to
     * way {@code w2}.</p>
     * 
     * <p>Replies {@link TurnRestrictionType#NO_LEFT_TURN no_left_turn} or 
     * {@link TurnRestrictionType#NO_RIGHT_TURN no_right_turn}, if applicable. Or
     * null, if neither of these restrictions is applicable, for instance because
     * the passed in via node {@code via} isn't a node where the two ways are
     * connected.</p>
     * 
     * @param w1 the "from"-way
     * @param w2 the "to"-way
     * @param via the via node
     * @return an applicable turn restriction, or null, if no turn restriction is
     * applicable
     */
    protected String determineRestriction(Way w1, Way w2, Node via){
    	if (via == null) return null;
    	if (!isConnectingNode(w1, w2, via)) return null;
    	// if either w1 or w2 are closed at the via node, we can't determine automatically
    	// whether the connection at "via" is a "left turn" or a "right turn"
    	if (isClosedAt(w1, via)) return null;
    	if (isClosedAt(w2, via)) return null;
    	
    	double phi1 = 0, phi2 = 0;
    	if (isEndNode(w1, via)){
    		if (isStartNode(w2, via)) {
	    		phi1 = phi(w1);
	    		phi2 = phi(w2);
    		} else if (isEndNode(w2, via)){
    			phi1 = phi(w1);
    			phi2 = phi(w2, true /* reverse it */);
    		} else {
    			assert false: "Unexpected state: via node is expected to be a start or and end node";
    		}	    		
    	} else if (isStartNode(w1,via)) {
    		if (isStartNode(w2, via)) {
	    		phi1 = phi(w1, true /* reverse it */);
	    		phi2 = phi(w2);
    		} else if (isEndNode(w2, via)){
    			phi1 = phi(w1, true /* reverse it */);
    			phi2 = phi(w2, true /* reverse it */);
    		} else {
    			assert false: "Unexpected state: via node is expected to be a start or and end node";
    		}	    
    	} else {
    		assert false: "Unexpected state: via node is expected to be a start or and end node of w1";    		
    	}
    	
    	double phi = phi1-phi2;
    	if (phi >=0 && phi <= Math.PI) {
    		// looks like a right turn  
    		return TurnRestrictionType.NO_RIGHT_TURN.getTagValue();
    	} else {
    		// looks like a left turn 
    		return TurnRestrictionType.NO_LEFT_TURN.getTagValue();
    	} 
    }
    
    protected Relation initTurnRestrictionFromTwoWays(List<OsmPrimitive> primitives) {
    	Way w1 = null;
    	Way w2 = null;
    	Node via = null;
    	if (primitives.size() == 2) {
    		// if we have exactly two selected primitives, we expect two ways. 
    		// See initNoUTurnRestriction() for the case where we have a selected way
    		// and a selected node
    		List<Way> selWays = OsmPrimitive.getFilteredList(primitives, Way.class);
    		if (selWays.size() != 2) return null;
    		w1 = selWays.get(0);
    		w2 = selWays.get(1);
    		via = getUniqueCommonNode(w1, w2);    		
    	} else if (primitives.size() == 3){
    		// if we have exactly three selected primitives, we need two ways and a 
    		// node, which should be an acceptable via node 
    		List<Way> selWays = OsmPrimitive.getFilteredList(primitives, Way.class);
    		List<Node> selNodes = OsmPrimitive.getFilteredList(primitives, Node.class);
    		if (selWays.size() != 2) return null;
    		if (selNodes.size() != 1) return null;
    		w1 = selWays.get(0);
    		w2 = selWays.get(1);
    		via = selNodes.get(0);
    		if (! w1.getNodes().contains(via) || ! w2.getNodes().contains(via)){
    			// the selected node is not an acceptable via node
    			via = null;
    		}
    	} else {
    		// the selection doesn't consists of primitives for which we can build
    		// a turn restriction 
    		return null;
    	}
    	
    	// if we get here, we know the two "legs" of the turn restriction. We may
    	// or may not know a via node, though
    	assert w1 != null;
    	assert w2 != null;
    	
    	Relation tr = new Relation();
    	tr.put("type", "restriction");
    	tr.addMember(new RelationMember("from", w1));
    	tr.addMember(new RelationMember("to", w2));
    	
    	if (via != null){
    		tr.addMember(new RelationMember("via", via));
    		String restriction = determineRestriction(w1, w2, via); 
    		if (restriction != null){
    			tr.put("restriction", restriction);
    		}
    	}
    	return tr;
    }
       
    protected Relation initEmptyTurnRestriction() {
	   Relation tr = new Relation();
       tr.put("type", "restriction");
       return tr;
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
        if (primitives == null || primitives.isEmpty()) {
        	return initEmptyTurnRestriction();
        }
        Relation tr;
        switch(primitives.size()){
        // case 0 already handled 
        case 1: 
        	tr = initEmptyTurnRestriction();
        	if (OsmPrimitive.getFilteredList(primitives, Way.class).size() == 1) {     
        		// we have exactly one selected way? -> init the "from" leg
        		// of the turn restriction with it
        		tr.addMember(new RelationMember("from", primitives.get(0)));
        	}
        	return tr;
        	
        case 2:
        	tr = initNoUTurnRestriction(primitives);
        	if (tr != null) return tr;
        	tr = initTurnRestrictionFromTwoWays(primitives);
        	if (tr != null) return tr;
        	return initEmptyTurnRestriction();       
        	
        default:        	
        	tr = initTurnRestrictionFromTwoWays(primitives);
        	if (tr != null) return tr;
        	return initEmptyTurnRestriction();       
        }
    }       
}
