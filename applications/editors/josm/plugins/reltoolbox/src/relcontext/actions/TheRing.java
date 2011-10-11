package relcontext.actions;

import java.util.*;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.*;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.tools.Geometry;

/**
 * One ring that contains segments forming an outer way of multipolygon.
 * This class is used in {@link CreateMultipolygonAction#makeManySimpleMultipolygons(java.util.Collection)}.
 * 
 * @author Zverik
 */
public class TheRing {
    private static final String PREF_MULTIPOLY = "reltoolbox.multipolygon.";

    private Way source;
    private List<RingSegment> segments;
    private Relation relation = null;

    public TheRing( Way source ) {
	this.source = source;
	segments = new ArrayList<RingSegment>(1);
	segments.add(new RingSegment(source));
    }
    
    public void collide( TheRing other ) {
	boolean collideNoted = false;
	for( int i = 0; i < segments.size(); i++ ) {
	    if( !segments.get(i).isReference() ) {
		for( int j = 0; j < other.segments.size(); j++ ) {
		    if( !other.segments.get(j).isReference() ) {
			RingSegment seg1 = segments.get(i);
			RingSegment seg2 = other.segments.get(j);
			boolean firstSegmentIsNotARing = !seg1.isRing() && segments.size() == 1;
			if( !seg2.isRing() && other.segments.size() == 1 ) {
			    if( firstSegmentIsNotARing )
				break; // not doing two arcs collision
			    RingSegment tmp = seg1;
			    seg1 = seg2;
			    seg2 = tmp;
			    firstSegmentIsNotARing = true;
			}
			if( firstSegmentIsNotARing ) {
			    if( seg2.getNodes().contains(seg1.getNodes().get(0)) && seg2.getNodes().contains(seg1.getNodes().get(seg1.getNodes().size()-2))) {
				// segment touches a ring
			    
			    }
			} else {
			    // both are segments from rings, find intersection
			}
			
			
			Node[] intersection = getFirstIntersection(segments.get(i), other.segments.get(j));
			if( intersection.length > 1 ) {
			    if( !collideNoted ) {
				System.out.println("Rings for ways " + source.getUniqueId() + " and " + other.source.getUniqueId() + " collide.");
				collideNoted = true;
			    }
			    // now split both ways at control points and remove duplicate parts
			    boolean[] isarc = new boolean[] {
				segments.size() == 1 && !segments.get(0).isRing(),
				other.segments.size() == 1 && !other.segments.get(0).isRing()
			    };
			    RingSegment segment = splitRingAt(i, intersection[0], intersection[1]);
			    RingSegment otherSegment = other.splitRingAt(j, intersection[0], intersection[1]);
			    if( !isarc[0] && !isarc[1] ) {
				if( segments.size() > 2 && other.segments.size() > 2 )
				    segment.makeReference(otherSegment);
				else {
				    System.out.println("Starting complex procedure. Rings: " + this + " and " + other);
				    // this ring was a ring, and we're not sure "segment" is a correct segment
				    // actually, we're not sure always
				    if( segments.size() == 2 )
					segment = segments.get(0);
				    if( other.segments.size() == 2 )
					otherSegment = other.segments.get(0);
				    System.out.println("segment="+segment + ", otherSegment=" + otherSegment);

				    if( areSegmentsEqual(segment, otherSegment) )
					segment.makeReference(otherSegment);
				    else if( segments.size() == 2 && areSegmentsEqual(segments.get(1), otherSegment) )
					segments.get(1).makeReference(otherSegment);
				    else if( areSegmentsEqual(segment, other.segments.get(1)) )
					segment.makeReference(other.segments.get(1));
				    else 
					segments.get(1).makeReference(other.segments.get(1));
				}
			    } else {
				// 1. A ring is an arc. It should have only 2 segments after this
				// 2. But it has one, so add otherSegment as the second.
				// 3. determine which segment!
				if( isarc[0] ) {
				    if( other.segments.size() > 2 )
					segments.add(new RingSegment(otherSegment));
				    else {
					// choose between 2 segments
					int segmentToAdd = whichSegmentIsCloser(segments.get(0), other.segments.get(0), other.segments.get(1));
					segments.add(new RingSegment(other.segments.get(segmentToAdd)));
				    }
				} else {
				    if( segments.size() > 2 )
					other.segments.add(new RingSegment(segment));
				    else {
					// choose between 2 segments
					int segmentToAdd = whichSegmentIsCloser(other.segments.get(0), segments.get(0), segments.get(1));
					other.segments.add(new RingSegment(segments.get(segmentToAdd)));
				    }
				}
			    }
			}
		    }
		    if( segments.get(i).isReference() )
			break;
		}
	    }
	}
    }
    
    private Node[] getFirstIntersection( RingSegment seg1, RingSegment seg2 ) {
	List<Node> intersectionNodes = new ArrayList<Node>();
	boolean colliding = false;
	List<Node> nodes1 = seg1.getNodes();
	List<Node> nodes2 = seg2.getNodes();
	for( int ni = 0; ni < nodes2.size(); ni++ ) {
	    if( nodes1.contains(nodes2.get(ni)) != colliding ) {
		intersectionNodes.add(nodes2.get(colliding ? ni - 1 : ni));
		colliding = !colliding;
	    }
	}
	if( colliding )
	    intersectionNodes.add(nodes2.get(nodes2.size() - 1));
	// when an intersection of two rings spans a ring's beginning
	if( seg1.isRing() && seg2.isRing() && intersectionNodes.contains(nodes2.get(0)) && intersectionNodes.contains(nodes2.get(nodes2.size() - 1)) ) {
	    intersectionNodes.remove(0);
	    intersectionNodes.remove(intersectionNodes.size() - 1);
	    intersectionNodes.add(intersectionNodes.get(0));
	    intersectionNodes.remove(0);
	}
	System.out.print("Intersection nodes for segments " + seg1 + " and " + seg2 + ": ");
	for( Node inode : intersectionNodes )
	    System.out.print(inode.getUniqueId() + ",");
	System.out.println();
	// unclosed ways produce duplicate nodes
	int ni = 1;
	while( ni < intersectionNodes.size() ) {
	    if( intersectionNodes.get(ni - 1).equals(intersectionNodes.get(ni)) )
		intersectionNodes.remove(ni - 1);
	    else
		ni++;
	}
	return intersectionNodes.toArray(new Node[2]);
    }
    
    private int whichSegmentIsCloser( RingSegment base, RingSegment test0, RingSegment test1 ) {
	List<Node> testRing = new ArrayList<Node>(base.getNodes());
	closePolygon(testRing, test1.getNodes());
	return segmentInsidePolygon(test1.getNodes().get(0), test1.getNodes().get(1), testRing) ? 1 : 0;
    }
    
    private boolean areSegmentsEqual( RingSegment seg1, RingSegment seg2 ) {
	List<Node> nodes1 = seg1.getNodes();
	List<Node> nodes2 = seg2.getNodes();
	int size = nodes1.size();
	if( size != nodes2.size() )
	    return false;
	boolean reverse = size > 1 && !nodes1.get(0).equals(nodes2.get(0));
	for( int i = 0; i < size; i++ )
	    if( !nodes1.get(i).equals(nodes2.get(reverse ? size-1-i : i)) )
		return false;
	return true;
    }

    /**
     * Split the segment in this ring at those nodes.
     * @return The segment between nodes.
     */
    private RingSegment splitRingAt( int segmentIndex, Node n1, Node n2 ) {
	if( n1.equals(n2) )
	    throw new IllegalArgumentException("Both nodes are equal, id=" + n1.getUniqueId());
	RingSegment segment = segments.get(segmentIndex);
	boolean isRing = segment.isRing();
	System.out.println("Split segment " + segment + " at nodes " + n1.getUniqueId() + " and " + n2.getUniqueId());
	boolean reversed = segment.getNodes().indexOf(n2) < segment.getNodes().indexOf(n1);
	if( reversed && !isRing ) {
	    // order nodes
	    Node tmp = n1;
	    n1 = n2;
	    n2 = tmp;
	}
	RingSegment secondPart = isRing ? segment.split(n1, n2) : segment.split(n1);
	// if secondPart == null, then n1 == firstNode
	RingSegment thirdPart = isRing ? null : secondPart == null ? segment.split(n2) : secondPart.split(n2);
	// if secondPart == null, then thirdPart is between n1 and n2
	// otherwise, thirdPart is between n2 and lastNode
	// if thirdPart == null, then n2 == lastNode
	int pos = segmentIndex + 1;
	if( secondPart != null )
	    segments.add(pos++, secondPart);
	if( thirdPart != null )
	    segments.add(pos++, thirdPart);
	RingSegment result = isRing || secondPart == null ? segment : secondPart;
//	System.out.println("Returning segment " + result);
	return result;
    }

    /**
     * Tries to arrange segments in order for each ring to have at least one.
     * Also, sets source way for all rings.
     * 
     * This method should be called, even if there is just one ring.
     */
    public static void redistributeSegments( List<TheRing> rings ) {
	// build segments map
	Map<RingSegment, TheRing> segmentMap = new HashMap<RingSegment, TheRing>();
	for( TheRing ring : rings )
	    for( RingSegment seg : ring.segments )
		if( !seg.isReference() )
		    segmentMap.put(seg, ring);

	// rearrange references
	for( int i = 0; i < rings.size(); i++ ) {
	    TheRing ring = rings.get(i);
	    if( ring.countNonReferenceSegments() == 0 ) {
		// need to find one non-reference segment
		for( RingSegment seg : ring.segments ) {
		    TheRing otherRing = segmentMap.get(seg.references);
		    if( otherRing.countNonReferenceSegments() > 1 ) {
			// we could check for >0, but it is prone to deadlocking
			seg.swapReference();
		    }
		}
	    }
	}

	// initializing source way for each ring
	for( TheRing ring : rings )
	    ring.putSourceWayFirst();
    }

    private int countNonReferenceSegments() {
	int count = 0;
	for( RingSegment seg : segments )
	    if( !seg.isReference() )
		count++;
	return count;
    }

    private void putSourceWayFirst() {
	for( RingSegment seg : segments ) {
	    if( !seg.isReference() ) {
		seg.overrideWay(source);
		return;
	    }
	}
    }

    /**
     * Returns a list of commands to make a new relation and all newly created ways.
     * The first way is copied from the source one, ChangeCommand is issued in this case.
     */
    public List<Command> getCommands() {
	System.out.println("Making ring " + this);
	Collection<String> linearTags = Main.pref.getCollection(PREF_MULTIPOLY + "lineartags", CreateMultipolygonAction.DEFAULT_LINEAR_TAGS);
	relation = new Relation();
	relation.put("type", "multipolygon");
	Way sourceCopy = new Way(source);
	for( String key : sourceCopy.keySet() ) {
	    if( !linearTags.contains(key) ) {
		relation.put(key, sourceCopy.get(key));
		sourceCopy.remove(key);
	    }
	}

	// build a map of referencing relations
	Map<Relation, Integer> referencingRelations = new HashMap<Relation, Integer>();
	List<Command> relationCommands = new ArrayList<Command>();
	for( OsmPrimitive p : source.getReferrers() ) {
	    if( p instanceof Relation ) {
		Relation rel = new Relation((Relation)p);
		relationCommands.add(new ChangeCommand((Relation)p, rel));
		for( int i = 0; i < rel.getMembersCount(); i++ )
		    if( rel.getMember(i).getMember().equals(source) )
			referencingRelations.put(rel, Integer.valueOf(i));
	    }
	}

	List<Command> commands = new ArrayList<Command>();
	boolean foundOwnWay = false;
	for( RingSegment seg : segments ) {
	    boolean needAdding = !seg.isWayConstructed();
	    Way w = seg.constructWay(seg.isReference() ? null : sourceCopy);
	    if( needAdding )
		commands.add(new AddCommand(w));
	    if( w.equals(source) ) {
		if( segments.size() == 1 ) {
		    // one segment means that it is a ring
		    List<Node> segnodes = seg.getNodes();
		    segnodes.add(segnodes.get(0));
		    sourceCopy.setNodes(segnodes);
		} else
		    sourceCopy.setNodes(seg.getNodes());
		commands.add(new ChangeCommand(source, sourceCopy));
		foundOwnWay = true;
	    } else {
		for( Relation rel : referencingRelations.keySet() ) {
		    int relIndex = referencingRelations.get(rel);
		    rel.addMember(new RelationMember(rel.getMember(relIndex).getRole(), w));
		}
	    }
	    relation.addMember(new RelationMember("outer", w));
	}
	if( !foundOwnWay )
	    commands.add(new DeleteCommand(source));
	commands.addAll(relationCommands);
	commands.add(new AddCommand(relation));
	return commands;
    }

    /**
     * Returns the relation created in {@link #getCommands()}.
     */
    public Relation getRelation() {
	return relation;
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder("TheRing@");
	sb.append(this.hashCode()).append('[').append("wayId: ").append(source == null ? "null" : source.getUniqueId()).append("; segments: ");
	if( segments.isEmpty() )
	    sb.append("empty");
	else {
	    sb.append(segments.get(0));
	    for( int i = 1; i < segments.size(); i++ )
		sb.append(", ").append(segments.get(i));
	}
	return sb.append(']').toString();
    }

    /**
     * Appends "append" to "base" so the closed polygon forms.
     */
    private static void closePolygon( List<Node> base, List<Node> append ) {
	if( append.get(0).equals(base.get(0)) && append.get(append.size() - 1).equals(base.get(base.size() - 1)) ) {
	    List<Node> ap2 = new ArrayList<Node>(append);
	    Collections.reverse(ap2);
	    append = ap2;
	}
	base.remove(base.size() - 1);
	base.addAll(append);
    }

    /**
     * Checks if a middle point between two nodes is inside a polygon. Useful to check if the way is inside.
     */
    private static boolean segmentInsidePolygon( Node n1, Node n2, List<Node> polygon ) {
	EastNorth en1 = n1.getEastNorth();
	EastNorth en2 = n2.getEastNorth();
	Node testNode = new Node(new EastNorth((en1.east() + en2.east()) / 2.0, (en1.north() + en2.north()) / 2.0));
	return Geometry.nodeInsidePolygon(testNode, polygon);
    }
    
    /**
     * Copies segment from {@code ring} to close a multipolygon containing {@code segment}.
     * @param segment Unclosed segment.
     * @param ring Closed ring.
     * @return Missing way.
     */
    private Way makeIntersectionLine( Way segment, Way ring ) {
	List<Node> nodes = new ArrayList<Node>(ring.getNodes());
	nodes.remove(nodes.size() - 1);
	int index1 = nodes.indexOf(segment.firstNode());
	int index2 = nodes.indexOf(segment.lastNode());
	if( index1 == index2 || index1 < 0 || index2 < 0 )
	    return null;

	// split ring
	List<List<Node>> chunks = new ArrayList<List<Node>>(2);
	chunks.add(new ArrayList<Node>());
	chunks.add(new ArrayList<Node>());
	int chunk = 0, i = index1;
	boolean madeCircle = false;
	while( i != index1 || !madeCircle ) {
	    chunks.get(chunk).add(nodes.get(i));
	    if( i == index2 ) {
		chunk = 1 - chunk;
		chunks.get(chunk).add(nodes.get(i));
	    }
	    if( ++i >= nodes.size() )
		i = 0;
	    madeCircle = true;
	}
	chunks.get(chunk).add(nodes.get(i));

	// check which segment to add
	List<Node> testRing = new ArrayList<Node>(segment.getNodes());
	closePolygon(testRing, chunks.get(0));
	chunk = segmentInsidePolygon(chunks.get(1).get(0), chunks.get(1).get(1), testRing) ? 1 : 0;

	// create way
	Way w = new Way();
	w.setKeys(segment.getKeys());
	w.setNodes(chunks.get(chunk));
	return w;
    }

    private static class RingSegment {
	private List<Node> nodes;
	private RingSegment references;
	private Way resultingWay = null;
	private boolean wasTemplateApplied = false;
	private boolean isRing;

	private RingSegment() {
	}

	public RingSegment( Way w ) {
	    this(w.getNodes());
	}

	public RingSegment( List<Node> nodes ) {
	    this.nodes = nodes;
	    isRing = nodes.size() > 1 && nodes.get(0).equals(nodes.get(nodes.size() - 1));
	    if( isRing )
		nodes.remove(nodes.size() - 1);
	    references = null;
	}

	public RingSegment( RingSegment ref ) {
	    this.nodes = null;
	    this.references = ref;
	}

	/**
	 * Splits this segment at node n. Retains nodes 0..n and moves
	 * nodes n..N to a separate segment that is returned.
	 * @param n node at which to split.
	 * @return new segment, {@code null} if splitting is unnecessary.
	 */
	public RingSegment split( Node n ) {
	    if( nodes == null )
		throw new IllegalArgumentException("Cannot split segment: it is a reference");
	    int pos = nodes.indexOf(n);
	    if( pos <= 0 || pos >= nodes.size() - 1 )
		return null;
	    List<Node> newNodes = new ArrayList<Node>(nodes.subList(pos, nodes.size()));
	    nodes.subList(pos + 1, nodes.size()).clear();
	    return new RingSegment(newNodes);
	}

	/**
	 * Split this segment as a way at two nodes. If one of them is null or at the end,
	 * split as an arc. Note: order of nodes is important.
	 * @return A new segment from n2 to n1.
	 */
	public RingSegment split( Node n1, Node n2 ) {
	    if( nodes == null )
		throw new IllegalArgumentException("Cannot split segment: it is a reference");
	    if( !isRing ) {
		if( n1 == null || nodes.get(0).equals(n1) || nodes.get(nodes.size() - 1).equals(n1) )
		    return split(n2);
		if( n2 == null || nodes.get(0).equals(n2) || nodes.get(nodes.size() - 1).equals(n2) )
		    return split(n1);
		throw new IllegalArgumentException("Split for two nodes is called for not-ring: " + this);
	    }
	    int pos1 = nodes.indexOf(n1);
	    int pos2 = nodes.indexOf(n2);
	    if( pos1 == pos2 )
		return null;

	    List<Node> newNodes = new ArrayList<Node>();
	    if( pos2 > pos1 ) {
		newNodes.addAll(nodes.subList(pos2, nodes.size()));
		newNodes.addAll(nodes.subList(0, pos1 + 1));
		if( pos2 + 1 < nodes.size() )
		    nodes.subList(pos2 + 1, nodes.size()).clear();
		if( pos1 > 0 )
		    nodes.subList(0, pos1).clear();
	    } else {
		newNodes.addAll(nodes.subList(pos2, pos1 + 1));
		nodes.addAll(new ArrayList<Node>(nodes.subList(0, pos2 + 1)));
		nodes.subList(0, pos1).clear();
	    }
	    isRing = false;
	    return new RingSegment(newNodes);
	}

	public List<Node> getNodes() {
	    return nodes == null ? references.nodes : nodes;
	}

	public boolean isReference() {
	    return nodes == null;
	}

	public boolean isRing() {
	    return isRing;
	}

	public void makeReference( RingSegment segment ) {
	    System.out.println(this + " was made a reference to " + segment);
	    this.nodes = null;
	    this.references = segment;
	}

	public void swapReference() {
	    this.nodes = references.nodes;
	    references.nodes = null;
	    references.references = this;
	    this.references = null;
	}

	public boolean isWayConstructed() {
	    return isReference() ? references.isWayConstructed() : resultingWay != null;
	}

	public Way constructWay( Way template ) {
	    if( isReference() )
		return references.constructWay(template);
	    if( resultingWay == null ) {
		resultingWay = new Way();
		resultingWay.setNodes(nodes);
	    }
	    if( template != null && !wasTemplateApplied ) {
		resultingWay.setKeys(template.getKeys());
		wasTemplateApplied = true;
	    }
	    return resultingWay;
	}

	public void overrideWay( Way source ) {
	    if( isReference() )
		references.overrideWay(source);
	    else {
		resultingWay = source;
		wasTemplateApplied = true;
	    }
	}

	/**
	 * Compares two segments with respect to referencing.
	 * @return true if ways are equals, or one references another.
	 */
	public boolean isReferencingEqual( RingSegment other ) {
	    return this.equals(other) || (other.isReference() && other.references == this) || (isReference() && references == other);
	}

	@Override
	public String toString() {
	    StringBuilder sb = new StringBuilder("RingSegment@");
	    sb.append(this.hashCode()).append('[');
	    if( isReference() )
		sb.append("references ").append(references.hashCode());
	    else if( nodes.isEmpty() )
		sb.append("empty");
	    else {
		if( isRing )
		    sb.append("ring:");
		sb.append(nodes.get(0).getUniqueId());
		for( int i = 1; i < nodes.size(); i++ )
		    sb.append(',').append(nodes.get(i).getUniqueId());
	    }
	    return sb.append(']').toString();
	}
    }
}
