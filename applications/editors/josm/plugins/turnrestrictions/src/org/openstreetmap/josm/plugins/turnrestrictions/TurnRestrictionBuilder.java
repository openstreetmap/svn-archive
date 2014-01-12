package org.openstreetmap.josm.plugins.turnrestrictions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.turnrestrictions.editor.TurnRestrictionLegRole;
import org.openstreetmap.josm.plugins.turnrestrictions.editor.TurnRestrictionType;
import org.openstreetmap.josm.tools.CheckParameterUtil;

/**
 * TurnRestrictionBuilder creates a turn restriction and initializes it with
 * objects from a selection of OSM primitives, i.e. the current selection
 * in a {@link OsmDataLayer}.
 *
 */
public class TurnRestrictionBuilder {
	
	/**
	 * Replies the angle phi in the polar coordinates (r,phi) representing the first
	 * segment of the way {@code w}, where w is moved such that the start node of {@code w} is
	 * in the origin (0,0).
	 *  
	 * @param w the way.  Must not be null. At least two nodes required. 
	 * @return phi in the polar coordinates 
	 * @throws IllegalArgumentException thrown if w is null
	 * @throws IllegalArgumentException thrown if w is too short (at least two nodes required)
	 */
    static public double phi(Way w) throws IllegalArgumentException{
    	return phi(w, false /* not inverse */);
    }
    
    /**
     * <p>Replies the angle phi in the polar coordinates (r,phi) representing the first
	 * segment of the way {@code w}, where w is moved such that the start node of {@code w} is
	 * in the origin (0,0).</p>
	 * 
	 * <p>If {@code doInvert} is true, computes phi for the way in reversed direction.</p>
	 * 
	 * @param w the way.  Must not be null. At least two nodes required.
	 * @param doInvert if true, computes phi for the reversed way 
	 * @return phi in the polar coordinates 
	 * @throws IllegalArgumentException thrown if w is null
	 * @throws IllegalArgumentException thrown if w is too short (at least two nodes required)
     */
    static public double phi(Way w, boolean doInvert) throws IllegalArgumentException {
    	CheckParameterUtil.ensureParameterNotNull(w, "w");
    	if (w.getNodesCount() < 2) {
    		throw new IllegalArgumentException("can't compute phi for way with less than 2 nodes");
    	}
    	List<Node> nodes = w.getNodes();
    	if (doInvert) Collections.reverse(nodes);
    	Node n0 = nodes.get(0);
    	Node n1 = nodes.get(1);
    	
    	double x = n1.getCoor().getX() - n0.getCoor().getX();
    	double y = n1.getCoor().getY() - n0.getCoor().getY();
    	return Math.atan2(y, x);  	
    }    

    /**
     * Replies the unique common node of two ways, or null, if either no
     * such node or multiple common nodes exist.
     * 
     * @param w1 the first way
     * @param w2 the second way
     * @return the common node or null, if w1 is null, or if w2 is null or if
     * w1 and w2 don't share exactly one node
     */
    static public Node getUniqueCommonNode(Way w1, Way w2) throws IllegalArgumentException{
    	Set<Node> w1Nodes = new HashSet<Node>(w1.getNodes());
    	w1Nodes.retainAll(w2.getNodes());
    	if (w1Nodes.size() != 1) return null;
    	return w1Nodes.iterator().next();
    }   
        
    /**
     * Replies true, if {@code n} is the start node of the way {@code w}.
     * 
     * @param w the way. Must not be null.
     * @param n the node. Must not be null.
     * @return true, if {@code n} is the start node of the way {@code w}.
     */
    static public boolean isStartNode(Way w, Node n) {
    	if (w.getNodesCount() == 0) return false;
    	return w.getNode(0).equals(n);
    }
        
    /**
     * Replies true, if {@code n} is the end node of the way {@code w}.
     * 
     * @param w the way. Must not be null.
     * @param n the node. Must not be null.
     * @return true, if {@code n} is the end node of the way {@code w}.
     */
    static public boolean isEndNode(Way w, Node n){
    	if (w.getNodesCount() == 0) return false;
    	return w.getNode(w.getNodesCount()-1).equals(n);
    }
    
    /**
     * Replies true, if {@code n} is a node in the way {@code w} but {@code n}
     * is neither the start nor the end node.
     * 
     * @param w the way 
     * @param n the node 
     * @return true if {@code n} is an "inner" node
     */
    static public boolean isInnerNode(Way w, Node n){
    	if (!w.getNodes().contains(n)) return false;
    	if (isStartNode(w, n)) return false;
    	if (isEndNode(w, n)) return false;
    	return true;     	
    }
    
    /**
     * <p>Replies the angle at which way {@code from} and {@code to} are connected
     * at exactly one common node.</p> 
     * 
     * <p>If the result is positive, the way {@code from} bends to the right, if it
     * is negative, the {@code to} bends to the left.</p>
     * 
     * <p>The two ways must not be null and they must be connected at exactly one 
     * common node. They must <strong>not intersect</code> at this node.</p>.
     * 
     * @param from the from way
     * @param to the to way
     * @return the intersection angle 
     * @throws IllegalArgumentException thrown if the two nodes don't have exactly one common
     * node at which they are connected
     * 
     */
    static public double intersectionAngle(Way from, Way to) throws IllegalArgumentException {
	    Node via = getUniqueCommonNode(from, to);
	    if (via == null) 
	    	throw new IllegalArgumentException("the two ways must share exactly one common node"); // no I18n required
	    if (!isStartNode(from, via) && ! isEndNode(from, via)) 
	    	throw new IllegalArgumentException("via node must be start or end node of from-way"); // no I18n required
	    if (!isStartNode(to, via) && ! isEndNode(to, via)) 
	    	throw new IllegalArgumentException("via node must be start or end node of to-way"); // no I18n required
	    double phi1 = phi(from, isStartNode(from, via));
	    double phi2 = phi(to, isEndNode(to, via));		
		return phi1 - phi2;
    }   
        
    static public enum RelativeWayJoinOrientation {
    	LEFT,
    	RIGHT
    }
    /**
     * <p>Determines the orientation in which two ways {@code from} and {@code to}
     * are connected, with respect to the direction of the way {@code from}.</p> 
     * 
     * <p>The following preconditions must be met:
     *   <ul>
     *     <li>{@code from} and {@code to} must not be null</li>
     *     <li>they must have exactly one common node <em>n</em> </li>
     *     <li><em>n</em> must occur exactly once in {@code from} and {@code to}, i.e. the
     *     two ways must not be closed at <em>n</em></li>
     *     <li><em>n</em> must be the start or the end node of both ways </li>
     *   </ul>
     * </p>
     * 
     * <p>Here's a typical configuration:</p>
     * <pre>
     *          to1             to2
     *      -------------> o -------------->
     *                     ^
     *                     | from
     *                     |
     * </pre>
     * 
     * <p>Replies null, if the preconditions aren't met and the method fails to
     *  determine the join orientation.</p>
     * 
     * @param from the "from"-way
     * @param to the "to"-way
     * @return the join orientation or null, if the method fails to compute the
     * join orientation
     */
    public static RelativeWayJoinOrientation determineWayJoinOrientation(Way from, Way to){
    	Node via = getUniqueCommonNode(from, to);
    	if (via == null) return null;
    	if (!isConnectingNode(from, to, via)) return null;
    	// if either w1 or w2 are closed at the via node, we can't determine automatically
    	// whether the connection at "via" is a "left turn" or a "right turn"
    	if (isClosedAt(from, via)) return null;
    	if (isClosedAt(to, via)) return null;
    	
    	double phi = intersectionAngle(from, to);    	
    	if (phi >=0 && phi <= Math.PI) {
    		return RelativeWayJoinOrientation.RIGHT;
    	} else {
    		return RelativeWayJoinOrientation.LEFT;
    	} 
    }
    
    /**
     * <p>Selects either of the two ways resulting from the split of a way
     * in the role {@link TurnRestrictionLegRole#TO TO}.</p>
     * 
     * <p>This methods operates on three ways for which the following
     * preconditions must be met:
     * <ul>
     *   <li>{@code t1} and {@code t2} are connected at a common node <em>n</em></li>
     *   <li>{@code from} is also connected to the node <em>n</em>. <em>n</em> occurs
     *   exactly once in {@code from} and is either the start or the end node of {@code from}.</li>
     * </ul>
     * </p>
     * 
     * <p>Here's a typical configuration:</p>
     * <pre>
     *          to1             to2
     *      -------------> o -------------->
     *                     ^
     *                     | from
     *                     |
     * </pre>
     * 
     * <p>Depending on {@code restrictionType}, this method either returns {@code to1}
     * or {@code to2}. If {@code restrictionType} indicates that our context is a 
     * "left turn", {@code to1} is replied. If our context is a "right turn", {@code to2}
     * is returned.</p>
     * 
     * <p>Replies null, if the expected preconditions aren't met or if we can't infer
     * from {@code restrictionType} whether our context is a "left turn" or a "right turn".</p>
     * 
     * @param from the from-way
     * @param to1 the first part of the split to-way
     * @param to2 the second part of the split to-way
     * @param restrictionType the restriction type
     * @return either {@code to1}, {@code to2}, or {@code null}.
     */
    static public Way selectToWayAfterSplit(Way from, Way to1, Way to2, TurnRestrictionType restrictionType){
    	if (restrictionType == null) return null;
    	Node cn1 = TurnRestrictionBuilder.getUniqueCommonNode(from, to1);
    	if (cn1 == null) return null;
    	Node cn2 = TurnRestrictionBuilder.getUniqueCommonNode(from, to2);
    	if (cn2 == null) return null;
    	if (cn1 != cn2) return null;        	
    	
    	if (! isStartNode(from, cn1) && ! isEndNode(from, cn1)) {
    		/*
    		 * the now split to-way still *intersects* the from-way. We
    		 * can't adjust the split decisions. 
    		 */
    		return null;
    	}
    	
    	RelativeWayJoinOrientation o1 = determineWayJoinOrientation(from, to1);
    	RelativeWayJoinOrientation o2 = determineWayJoinOrientation(from, to2);
    	
        switch(restrictionType){
        case NO_LEFT_TURN:
        case ONLY_LEFT_TURN:
        	if (RelativeWayJoinOrientation.LEFT.equals(o1)) return to1;
        	else if (RelativeWayJoinOrientation.LEFT.equals(o2)) return to2;
        	else return null;
        	
        case NO_RIGHT_TURN:
        case ONLY_RIGHT_TURN:
        	if (RelativeWayJoinOrientation.RIGHT.equals(o1)) return to1;
        	else if (RelativeWayJoinOrientation.RIGHT.equals(o2)) return to2;
        	else return null;
        	
        default:
	       	 /*
	       	  * For restriction types like NO_U_TURN, NO_STRAIGHT_ON, etc. we
	       	  * can select a "left" or "right" way after splitting.
	       	  */
        	return null;
        }
    }
    
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
    public static boolean isConnectingNode(Way w1, Way w2, Node n){
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
    public static boolean isClosedAt(Way w, Node n){
    	List<Node> nodes = w.getNodes();
    	nodes.retainAll(Collections.singletonList(n));
    	return nodes.size() >= 2;
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
    		RelativeWayJoinOrientation orientation = determineWayJoinOrientation(w1, w2);
    		if (orientation != null){
    			switch(orientation){
    			case LEFT:
    				tr.put("restriction", TurnRestrictionType.NO_LEFT_TURN.getTagValue());
    				break;
    			case RIGHT:
    				tr.put("restriction", TurnRestrictionType.NO_RIGHT_TURN.getTagValue());
    				break;    				
    			}
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
