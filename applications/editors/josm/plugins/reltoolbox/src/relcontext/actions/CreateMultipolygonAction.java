package relcontext.actions;

import java.awt.Dialog.ModalityType;
import java.awt.GridBagLayout;
import static org.openstreetmap.josm.tools.I18n.tr;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.*;
import javax.swing.*;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.actions.SplitWayAction;
import org.openstreetmap.josm.command.*;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.data.osm.MultipolygonCreate.JoinedPolygon;
import org.openstreetmap.josm.data.osm.visitor.paint.relations.Multipolygon.JoinedWay;
import org.openstreetmap.josm.gui.DefaultNameFormatter;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.Geometry;
import org.openstreetmap.josm.tools.Geometry.PolygonIntersection;
import org.openstreetmap.josm.tools.Shortcut;
import relcontext.ChosenRelation;

/**
 * Creates new multipolygon from selected ways.
 * Choose relation afterwards.
 *
 * @author Zverik
 */
public class CreateMultipolygonAction extends JosmAction {
    private static final String ACTION_NAME = "Create relation";
    private static final String PREF_MULTIPOLY = "reltoolbox.multipolygon.";
    protected ChosenRelation chRel;

    public CreateMultipolygonAction( ChosenRelation chRel ) {
	super("Multi", "data/multipolygon", tr("Create a multipolygon from selected objects"),
		Shortcut.registerShortcut("reltoolbox:multipolygon", tr("Relation Toolbox: {0}", tr("Create multipolygon")),
		KeyEvent.VK_B, Shortcut.GROUP_HOTKEY), true);
	this.chRel = chRel;
	updateEnabledState();
    }

    public CreateMultipolygonAction() {
	this(null);
    }

    public static boolean getDefaultPropertyValue( String property ) {
	if( property.equals("boundary") )
	    return false;
	else if( property.equals("boundaryways") )
	    return true;
	else if( property.equals("tags") )
	    return true;
	else if( property.equals("alltags") )
	    return false;
	else if( property.equals("single") )
	    return true;
	throw new IllegalArgumentException(property);
    }

    private boolean getPref( String property ) {
	return Main.pref.getBoolean(PREF_MULTIPOLY + property, getDefaultPropertyValue(property));
    }

    public void actionPerformed( ActionEvent e ) {
	boolean isBoundary = getPref("boundary");
	Collection<Way> selectedWays = getCurrentDataSet().getSelectedWays();
	if( !isBoundary && getPref("tags") ) {
	    if( selectedWays.size() == 1 && !selectedWays.iterator().next().isClosed() ) {
		Relation newRelation = tryToCloseOneWay(selectedWays.iterator().next());
		if( newRelation != null ) {
		    if( chRel != null )
			chRel.set(newRelation);
		    return;
		}
	    }
	    if( areAllOfThoseRings(getCurrentDataSet().getSelectedWays()) ) {
		List<Relation> rels = makeManySimpleMultipolygons(getCurrentDataSet().getSelectedWays());
		if( chRel != null )
		    chRel.set(rels.size() == 1 ? rels.get(0) : null);
		return;
	    }
	}

	// for now, just copying standard action
	MultipolygonCreate mpc = new MultipolygonCreate();
	String error = mpc.makeFromWays(getCurrentDataSet().getSelectedWays());
	if( error != null ) {
	    JOptionPane.showMessageDialog(Main.parent, error);
	    return;
	}
	Relation rel = new Relation();
	if( isBoundary ) {
	    rel.put("type", "boundary");
	    rel.put("boundary", "administrative");
	} else
	    rel.put("type", "multipolygon");
	for( MultipolygonCreate.JoinedPolygon poly : mpc.outerWays )
	    for( Way w : poly.ways )
		rel.addMember(new RelationMember("outer", w));
	for( MultipolygonCreate.JoinedPolygon poly : mpc.innerWays )
	    for( Way w : poly.ways )
		rel.addMember(new RelationMember("inner", w));
	List<Command> list = removeTagsFromInnerWays(rel);
	if( !list.isEmpty() && isBoundary ) {
	    Main.main.undoRedo.add(new SequenceCommand(tr("Move tags from ways to relation"), list));
	    list = new ArrayList<Command>();
	}
	if( isBoundary ) {
	    if( !askForAdminLevelAndName(rel) )
		return;
	    addBoundaryMembers(rel);
	    if( getPref("boundaryways") )
		list.addAll(fixWayTagsForBoundary(rel));
	}
	list.add(new AddCommand(rel));
	Main.main.undoRedo.add(new SequenceCommand(tr("Create multipolygon"), list));

	if( chRel != null )
	    chRel.set(rel);

	getCurrentDataSet().setSelected(rel);
    }

    @Override
    protected void updateEnabledState() {
	if( getCurrentDataSet() == null ) {
	    setEnabled(false);
	} else {
	    updateEnabledState(getCurrentDataSet().getSelected());
	}
    }

    @Override
    protected void updateEnabledState( Collection<? extends OsmPrimitive> selection ) {
	boolean isEnabled = true;
	if( selection == null || selection.isEmpty() )
	    isEnabled = false;
	else {
	    if( !getPref("boundary") ) {
		for( OsmPrimitive p : selection ) {
		    if( !(p instanceof Way) ) {
			isEnabled = false;
			break;
		    }
		}
	    }
	}
	setEnabled(isEnabled);
    }

    /**
     * Add selected nodes and relations with corresponding roles.
     */
    private void addBoundaryMembers( Relation rel ) {
	for( OsmPrimitive p : getCurrentDataSet().getSelected() ) {
	    String role = null;
	    if( p.getType().equals(OsmPrimitiveType.RELATION) ) {
		role = "subarea";
	    } else if( p.getType().equals(OsmPrimitiveType.NODE) ) {
		Node n = (Node)p;
		if( !n.isIncomplete() ) {
		    if( n.hasKey("place") )
			role = "admin_centre";
		    else
			role = "label";
		}
	    }
	    if( role != null )
		rel.addMember(new RelationMember(role, p));
	}
    }

    /**
     * For all untagged ways in relation, add tags boundary and admin_level.
     */
    private List<Command> fixWayTagsForBoundary( Relation rel ) {
	List<Command> commands = new ArrayList<Command>();
	if( !rel.hasKey("boundary") || !rel.hasKey("admin_level") )
	    return commands;
	String adminLevelStr = rel.get("admin_level");
	int adminLevel = 0;
	try {
	    adminLevel = Integer.parseInt(adminLevelStr);
	} catch( NumberFormatException e ) {
	    return commands;
	}
	Set<OsmPrimitive> waysBoundary = new HashSet<OsmPrimitive>();
	Set<OsmPrimitive> waysAdminLevel = new HashSet<OsmPrimitive>();
	for( OsmPrimitive p : rel.getMemberPrimitives() ) {
	    if( p instanceof Way ) {
		int count = 0;
		if( p.hasKey("boundary") && p.get("boundary").equals("administrative") )
		    count++;
		if( p.hasKey("admin_level") )
		    count++;
		if( p.keySet().size() - count == 0 ) {
		    if( !p.hasKey("boundary") )
			waysBoundary.add(p);
		    if( !p.hasKey("admin_level") ) {
			waysAdminLevel.add(p);
		    } else {
			try {
			    int oldAdminLevel = Integer.parseInt(p.get("admin_level"));
			    if( oldAdminLevel > adminLevel )
				waysAdminLevel.add(p);
			} catch( NumberFormatException e ) {
			    waysAdminLevel.add(p); // some garbage, replace it
			}
		    }
		}
	    }
	}
	if( !waysBoundary.isEmpty() )
	    commands.add(new ChangePropertyCommand(waysBoundary, "boundary", "administrative"));
	if( !waysAdminLevel.isEmpty() )
	    commands.add(new ChangePropertyCommand(waysAdminLevel, "admin_level", adminLevelStr));
	return commands;
    }
    static public final List<String> DEFAULT_LINEAR_TAGS = Arrays.asList(new String[] {"barrier", "source"});
    private static final Set<String> REMOVE_FROM_BOUNDARY_TAGS = new TreeSet<String>(Arrays.asList(new String[] {
		"boundary", "boundary_type", "type", "admin_level"
	    }));

    /**
     * This method removes tags/value pairs from inner ways that are present in relation or outer ways.
     * It was copypasted from the standard {@link org.openstreetmap.josm.actions.CreateMultipolygonAction}.
     * Todo: rewrite it.
     */
    private List<Command> removeTagsFromInnerWays( Relation relation ) {
	Map<String, String> values = new HashMap<String, String>();

	if( relation.hasKeys() ) {
	    for( String key : relation.keySet() ) {
		values.put(key, relation.get(key));
	    }
	}

	List<Way> innerWays = new ArrayList<Way>();
	List<Way> outerWays = new ArrayList<Way>();

	Set<String> conflictingKeys = new TreeSet<String>();

	for( RelationMember m : relation.getMembers() ) {

	    if( m.hasRole() && "inner".equals(m.getRole()) && m.isWay() && m.getWay().hasKeys() ) {
		innerWays.add(m.getWay());
	    }

	    if( m.hasRole() && "outer".equals(m.getRole()) && m.isWay() && m.getWay().hasKeys() ) {
		Way way = m.getWay();
		outerWays.add(way);
		for( String key : way.keySet() ) {
		    if( !values.containsKey(key) ) { //relation values take precedence
			values.put(key, way.get(key));
		    } else if( !relation.hasKey(key) && !values.get(key).equals(way.get(key)) ) {
			conflictingKeys.add(key);
		    }
		}
	    }
	}

	// filter out empty key conflicts - we need second iteration
	boolean isBoundary = getPref("boundary");
	if( isBoundary || !getPref("alltags") )
	    for( RelationMember m : relation.getMembers() )
		if( m.hasRole() && m.getRole().equals("outer") && m.isWay() )
		    for( String key : values.keySet() )
			if( !m.getWay().hasKey(key) && !relation.hasKey(key) )
			    conflictingKeys.add(key);

	for( String key : conflictingKeys )
	    values.remove(key);

	for( String linearTag : Main.pref.getCollection(PREF_MULTIPOLY + "lineartags", DEFAULT_LINEAR_TAGS) )
	    values.remove(linearTag);

	if( values.containsKey("natural") && values.get("natural").equals("coastline") )
	    values.remove("natural");

	String name = values.get("name");
	if( isBoundary ) {
	    Set<String> keySet = new TreeSet<String>(values.keySet());
	    for( String key : keySet )
		if( !REMOVE_FROM_BOUNDARY_TAGS.contains(key) )
		    values.remove(key);
	}

	values.put("area", "yes");

	List<Command> commands = new ArrayList<Command>();
	boolean moveTags = getPref("tags");

	for( String key : values.keySet() ) {
	    List<OsmPrimitive> affectedWays = new ArrayList<OsmPrimitive>();
	    String value = values.get(key);

	    for( Way way : innerWays ) {
		if( way.hasKey(key) && (isBoundary || value.equals(way.get(key))) ) {
		    affectedWays.add(way);
		}
	    }

	    if( moveTags ) {
		// remove duplicated tags from outer ways
		for( Way way : outerWays ) {
		    if( way.hasKey(key) ) {
			affectedWays.add(way);
		    }
		}
	    }

	    if( affectedWays.size() > 0 ) {
		commands.add(new ChangePropertyCommand(affectedWays, key, null));
	    }
	}

	if( moveTags ) {
	    // add those tag values to the relation
	    if( isBoundary )
		values.put("name", name);
	    boolean fixed = false;
	    Relation r2 = new Relation(relation);
	    for( String key : values.keySet() ) {
		if( !r2.hasKey(key) && !key.equals("area")
			&& (!isBoundary || key.equals("admin_level") || key.equals("name")) ) {
		    if( relation.isNew() )
			relation.put(key, values.get(key));
		    else
			r2.put(key, values.get(key));
		    fixed = true;
		}
	    }
	    if( fixed && !relation.isNew() )
		commands.add(new ChangeCommand(relation, r2));
	}

	return commands;
    }

    /**
     *
     * @param rel
     * @return false if user pressed "cancel".
     */
    private boolean askForAdminLevelAndName( Relation rel ) {
	String relAL = rel.get("admin_level");
	String relName = rel.get("name");
	if( relAL != null && relName != null )
	    return true;

	JPanel panel = new JPanel(new GridBagLayout());
	panel.add(new JLabel(tr("Enter admin level and name for the border relation:")), GBC.eol().insets(0, 0, 0, 5));

	final JTextField admin = new JTextField();
	admin.setText(relAL != null ? relAL : Main.pref.get(PREF_MULTIPOLY + "lastadmin", ""));
	panel.add(new JLabel(tr("Admin level")), GBC.std());
	panel.add(Box.createHorizontalStrut(10), GBC.std());
	panel.add(admin, GBC.eol().fill(GBC.HORIZONTAL).insets(0, 0, 0, 5));

	final JTextField name = new JTextField();
	if( relName != null )
	    name.setText(relName);
	panel.add(new JLabel(tr("Name")), GBC.std());
	panel.add(Box.createHorizontalStrut(10), GBC.std());
	panel.add(name, GBC.eol().fill(GBC.HORIZONTAL));

	final JOptionPane optionPane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION) {
	    @Override
	    public void selectInitialValue() {
		admin.requestFocusInWindow();
		admin.selectAll();
	    }
	};
	final JDialog dlg = optionPane.createDialog(Main.parent, tr("Create a new relation"));
	dlg.setModalityType(ModalityType.DOCUMENT_MODAL);

	name.addActionListener(new ActionListener() {
	    public void actionPerformed( ActionEvent e ) {
		dlg.setVisible(false);
		optionPane.setValue(JOptionPane.OK_OPTION);
	    }
	});

	dlg.setVisible(true);

	Object answer = optionPane.getValue();
	if( answer == null || answer == JOptionPane.UNINITIALIZED_VALUE
		|| (answer instanceof Integer && (Integer)answer != JOptionPane.OK_OPTION) ) {
	    return false;
	}

	String admin_level = admin.getText().trim();
	String new_name = name.getText().trim();
	if( admin_level.equals("10") || (admin_level.length() == 1 && Character.isDigit(admin_level.charAt(0))) ) {
	    rel.put("admin_level", admin_level);
	    Main.pref.put(PREF_MULTIPOLY + "lastadmin", admin_level);
	}
	if( new_name.length() > 0 )
	    rel.put("name", new_name);
	return true;
    }

    private boolean areAllOfThoseRings( Collection<Way> ways ) {
	List<Way> rings = new ArrayList<Way>();
	List<Way> otherWays = new ArrayList<Way>();
	for( Way way : ways ) {
	    if( way.isClosed() )
		rings.add(way);
	    else
		otherWays.add(way);
	}
	if( rings.isEmpty() || ways.size() == 1 )
	    return false; // todo: for one ring, attach it to neares multipolygons

	// check that every segment touches just one ring
	for( Way segment : otherWays ) {
	    boolean found = false;
	    for( Way ring : rings ) {
		System.out.println("segment " + segment.getId() + ", ring " + ring.getId());
		System.out.println("ring.containsNode(segment.firstNode()) = " + ring.containsNode(segment.firstNode()));
		System.out.println("ring.containsNode(segment.lastNode() = " + ring.containsNode(segment.lastNode()));
		System.out.println("segmentInsidePolygon(segment.getNode(0), segment.getNode(1), ring.getNodes()) = " + segmentInsidePolygon(segment.getNode(0), segment.getNode(1), ring.getNodes()));
		if( ring.containsNode(segment.firstNode()) && ring.containsNode(segment.lastNode())
			&& !segmentInsidePolygon(segment.getNode(0), segment.getNode(1), ring.getNodes()) )
		    found = true;
	    }
	    if( !found )
		return false;
	}

	// check for non-containment of rings
	for( int i = 0; i < rings.size() - 1; i++ ) {
	    for( int j = i + 1; j < rings.size(); j++ ) {
		PolygonIntersection intersection = Geometry.polygonIntersection(rings.get(i).getNodes(), rings.get(j).getNodes());
		if( intersection == PolygonIntersection.FIRST_INSIDE_SECOND || intersection == PolygonIntersection.SECOND_INSIDE_FIRST )
		    return false;
	    }
	}

	return true;
    }

    /**
     * Creates ALOT of Multipolygons and pets him gently.
     * @return list of new relations.
     */
    private List<Relation> makeManySimpleMultipolygons( Collection<Way> selection ) {
	List<Command> commands = new ArrayList<Command>();
	List<Way> ways = new ArrayList<Way>(selection.size());
	Map<Way, Way> wayDiff = new HashMap<Way, Way>(selection.size());
	List<Relation> relations = new ArrayList<Relation>(ways.size());
	Collection<String> linearTags = Main.pref.getCollection(PREF_MULTIPOLY + "lineartags", DEFAULT_LINEAR_TAGS);
	for( Way w : selection ) {
	    Way newWay = new Way(w);
	    wayDiff.put(w, newWay);
	    commands.add(new ChangeCommand(w, newWay));
	    ways.add(newWay);
	    Relation r = new Relation();
	    r.put("type", "multipolygon");
	    r.addMember(new RelationMember("outer", w));
	    // move tags to relations
	    for( String key : newWay.keySet() ) {
		if( !linearTags.contains(key) ) {
		    r.put(key, newWay.get(key));
		    newWay.remove(key);
		}
	    }
	    if( !w.isClosed() ) {
		Way ring = null;
		for( Way tring : selection )
		    if( tring.containsNode(newWay.firstNode()) && tring.containsNode(newWay.lastNode())
			    && !segmentInsidePolygon(newWay.getNode(0), newWay.getNode(1), tring.getNodes()) )
			ring = tring;
		Way intersection = makeIntersectionLine(newWay, ring);
		commands.add(new AddCommand(intersection));
		r.addMember(new RelationMember("outer", intersection));
	    }
	    relations.add(r);
	}

	for( int i = 0; i < relations.size() - 1; i++ )
	    for( int j = i + 1; j < relations.size(); j++ )
		collideMultipolygons(relations.get(i), relations.get(j), commands, wayDiff);

	for( Relation r : relations )
	    commands.add(new AddCommand(r));
	Main.main.undoRedo.add(new SequenceCommand(tr("Create multipolygons from rings"), commands));
	return relations;
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

    /**
     * Appends "append" to "base" so the closed polygon forms.
     */
    private void closePolygon( List<Node> base, List<Node> append ) {
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
    private boolean segmentInsidePolygon( Node n1, Node n2, List<Node> polygon ) {
	EastNorth en1 = n1.getEastNorth();
	EastNorth en2 = n2.getEastNorth();
	Node testNode = new Node(new EastNorth((en1.east() + en2.east()) / 2.0, (en1.north() + en2.north()) / 2.0));
	return Geometry.nodeInsidePolygon(testNode, polygon);
    }

    /**
     * Removes any intersections between multipolygons.
     * @param r1 First multipolygon.
     * @param r2 Second multipolygon.
     * @param commands List of commands. Only add way commands go there, also see wayDiff.
     * @param wayDiff The mapping old way to new Way: if there is no entry in this map, it is created, and
     * a ChangeCommand is issued.
     */
    private static void collideMultipolygons( Relation r1, Relation r2, List<Command> commands, Map<Way, Way> wayDiff ) {
    }

    /**
     * Splits a way with regard to containing relations. This modifies the way and the relation, be prepared.
     * @param w The way.
     * @param n The node to split at.
     * @param commands List of commands to add way/relation changing to. If null, never mind.
     * @return Newly created ways. <b>Warning:</b> if commands is no not, newWays contains {@code w}.
     */
    public static List<Way> splitWay( Way w, Node n1, Node n2, List<Command> commands ) {
	List<Node> nodes = new ArrayList<Node>(w.getNodes());
	if( w.isClosed())
	    nodes.remove(nodes.size()-1);
	int index1 = nodes.indexOf(n1);
	int index2 = n2 == null ? -1 : nodes.indexOf(n2);
	if( index1 > index2 ) {
	    int tmp = index1;
	    index1 = index2;
	    index2 = tmp;
	}
	// right now index2 >= index1
	if( index2 < 1 || index1 >= w.getNodesCount() - 1 || index2 >= w.getNodesCount() )
	    return Collections.emptyList();
	if( w.isClosed() && (index1 < 0 || index1 == index2 || index1 + w.getNodesCount() == index2) )
	    return Collections.emptyList();

	// make a list of segments
	List<List<Node>> chunks = new ArrayList<List<Node>>(2);
	List<Node> chunk = new ArrayList<Node>();
	for( int i = 0; i < nodes.size(); i++ ) {
	    chunk.add(nodes.get(i));
	    if( (w.isClosed() || chunk.size() > 1) && (i == index1 || i == index2) ) {
		chunks.add(chunk);
		chunk = new ArrayList<Node>();
		chunk.add(nodes.get(i));
	    }
	}
	chunks.add(chunk);

	// for closed way ignore the way boundary
	if( w.isClosed() ) {
	    chunks.get(chunks.size() - 1).addAll(chunks.get(0));
	    chunks.remove(0);
	} else if( chunks.get(chunks.size()-1).size() < 2 )
	    chunks.remove(chunks.size()-1);
	
	// todo remove debug: show chunks array contents
	/*for( List<Node> c1 : chunks ) {
	    for( Node cn1 : c1 )
		System.out.print(cn1.getId() + ",");
	    System.out.println();
	}*/

	// build a map of referencing relations
	Map<Relation, Integer> references = new HashMap<Relation, Integer>();
	List<Command> relationCommands = new ArrayList<Command>();
	for( OsmPrimitive p : w.getReferrers() ) {
	    if( p instanceof Relation ) {
		Relation rel = commands == null ? (Relation)p : new Relation((Relation)p);
		if( commands != null )
		    relationCommands.add(new ChangeCommand((Relation)p, rel));
		for( int i = 0; i < rel.getMembersCount(); i++ )
		    if( rel.getMember(i).getMember().equals(w) )
			references.put(rel, Integer.valueOf(i));
	    }
	}

	// build ways
	List<Way> result = new ArrayList<Way>();
	Way updatedWay = commands == null ? w : new Way(w);
	updatedWay.setNodes(chunks.get(0));
	if( commands != null ) {
	    commands.add(new ChangeCommand(w, updatedWay));
	    result.add(updatedWay);
	}

	for( int i = 1; i < chunks.size(); i++ ) {
	    List<Node> achunk = chunks.get(i);
	    Way newWay = new Way();
	    newWay.setKeys(w.getKeys());
	    result.add(newWay);
	    for( Relation rel : references.keySet() ) {
		int relIndex = references.get(rel);
		rel.addMember(relIndex + 1, new RelationMember(rel.getMember(relIndex).getRole(), newWay));
	    }
	    newWay.setNodes(achunk);
	    if( commands != null )
		commands.add(new AddCommand(newWay));
	}
	if( commands != null )
	    commands.addAll(relationCommands);
	return result;
    }

    public static List<Way> splitWay( Way w, Node n1, Node n2 ) {
	return splitWay(w, n1, n2, null);
    }

    /**
     * Find a way the tips of a segment, ensure it's in a multipolygon and try to close the relation.
     */
    private Relation tryToCloseOneWay( Way segment ) {
	if( segment.isClosed() || segment.isIncomplete() )
	    return null;

	List<Way> ways = intersection(
		OsmPrimitive.getFilteredList(segment.firstNode().getReferrers(), Way.class),
		OsmPrimitive.getFilteredList(segment.lastNode().getReferrers(), Way.class));
	ways.remove(segment);
	for( Iterator<Way> iter = ways.iterator(); iter.hasNext(); ) {
	    boolean save = false;
	    for( OsmPrimitive ref : iter.next().getReferrers() )
		if( ref instanceof Relation && ((Relation)ref).isMultipolygon() && !ref.isDeleted() )
		    save = true;
	    if( !save )
		iter.remove();
	}
	if( ways.isEmpty() )
	    return null; // well...
	Way target = ways.get(0);

	// time to create a new multipolygon relation and a command stack
	List<Command> commands = new ArrayList<Command>();
	Relation newRelation = new Relation();
	newRelation.put("type", "multipolygon");
	newRelation.addMember(new RelationMember("outer", segment));
	Collection<String> linearTags = Main.pref.getCollection(PREF_MULTIPOLY + "lineartags", DEFAULT_LINEAR_TAGS);
	Way segmentCopy = new Way(segment);
	boolean changed = false;
	for( String key : segmentCopy.keySet() ) {
	    if( !linearTags.contains(key) ) {
		newRelation.put(key, segmentCopy.get(key));
		segmentCopy.remove(key);
		changed = true;
	    }
	}
	if( changed )
	    commands.add(new ChangeCommand(segment, segmentCopy));

	// now split the way, at last
	List<Way> newWays = new ArrayList<Way>(splitWay(target, segment.firstNode(), segment.lastNode(), commands));

	Way addingWay = null;
	if( target.isClosed() ) {
	    Way utarget = newWays.get(1);
	    Way alternate = newWays.get(0);
	    List<Node> testRing = new ArrayList<Node>(segment.getNodes());
	    closePolygon(testRing, utarget.getNodes());
	    addingWay = segmentInsidePolygon(alternate.getNode(0), alternate.getNode(1), testRing) ? alternate : utarget;
	} else {
	    for( Way w : newWays ) {
		if( (w.firstNode().equals(segment.firstNode()) && w.lastNode().equals(segment.lastNode()))
			|| (w.firstNode().equals(segment.lastNode()) && w.lastNode().equals(segment.firstNode())) ) {
		    addingWay = w;
		    break;
		}
	    }
	}
	newRelation.addMember(new RelationMember("outer", addingWay.getUniqueId() == target.getUniqueId() ? target : addingWay));
	commands.add(new AddCommand(newRelation));
	Main.main.undoRedo.add(new SequenceCommand(tr("Complete multipolygon for way {0}",
		DefaultNameFormatter.getInstance().format(segment)), commands));
	return newRelation;
    }

    /**
     * Find a multipolygon at the tips of a segment, try to close the way.
     * 
     * Note: this method is abandoned because of it's skyrocketing complexity. The only thing is
     * left to write is splitting and ordering ways (see below). But I doubt there is a point to it.
     */
    private Relation tryToCloseOneWayOld( Way segment ) {
	if( segment.isClosed() || segment.isIncomplete() )
	    return null;

	// find relations that have ways from both arrays
	Set<Relation> relations1 = new HashSet<Relation>();
	for( Way way : OsmPrimitive.getFilteredList(segment.firstNode().getReferrers(), Way.class) )
	    relations1.addAll(OsmPrimitive.getFilteredList(way.getReferrers(), Relation.class));
	Set<Relation> relations2 = new HashSet<Relation>();
	for( Way way : OsmPrimitive.getFilteredList(segment.lastNode().getReferrers(), Way.class) )
	    relations2.addAll(OsmPrimitive.getFilteredList(way.getReferrers(), Relation.class));
	List<Relation> relations = intersection(relations1, relations2);
	for( Iterator<Relation> iter = relations.iterator(); iter.hasNext(); ) {
	    Relation candidate = iter.next();
	    if( !candidate.isMultipolygon() || candidate.isDeleted() || candidate.isIncomplete() )
		iter.remove();
	}
	if( relations.isEmpty() )
	    return null;
	int i = 0;
	String error = "";
	MultipolygonCreate mpc = new MultipolygonCreate();
	JoinedPolygon poly = null;
	while( error != null && i < relations.size() ) {
	    error = mpc.makeFromWays(OsmPrimitive.getFilteredSet(relations.get(i).getMemberPrimitives(), Way.class));
	    if( error != null ) {
		for( JoinedPolygon p : mpc.outerWays ) {
		    if( p.nodes.contains(segment.firstNode()) && p.nodes.contains(segment.lastNode()) ) {
			poly = p;
			break;
		    }
		}
	    }
	    i++;
	}
	if( poly == null )
	    return null; // no correct multipolygons with outer contour that segment touches
	Relation multipolygon = relations.get(i - 1);

	// time to create a new multipolygon relation and a command stack
	List<Command> commands = new ArrayList<Command>();
	Relation newRelation = new Relation();
	newRelation.put("type", "multipolygon");
	newRelation.addMember(new RelationMember("outer", segment));
	Collection<String> linearTags = Main.pref.getCollection(PREF_MULTIPOLY + "lineartags", DEFAULT_LINEAR_TAGS);
	Way segmentCopy = new Way(segment);
	boolean changed = false;
	for( String key : segmentCopy.keySet() ) {
	    if( !linearTags.contains(key) ) {
		newRelation.put(key, segmentCopy.get(key));
		segmentCopy.remove(key);
		changed = true;
	    }
	}
	if( changed )
	    commands.add(new ChangeCommand(segment, segmentCopy));

	// find a path from one point to another via found outer contour
	// but first, determine in which order to traverse nodes
	int index1 = poly.nodes.indexOf(segment.firstNode());
	int index2 = poly.nodes.indexOf(segment.lastNode());

	List<List<Node>> chunks = new ArrayList<List<Node>>(2);
	chunks.add(new ArrayList<Node>());
	chunks.add(new ArrayList<Node>());
	int chunk = 0;
	i = index1;
	boolean madeCircle = false;
	while( i != index1 || !madeCircle ) {
	    chunks.get(chunk).add(poly.nodes.get(i));
	    if( i == index2 ) {
		chunk = 1 - chunk;
		chunks.get(chunk).add(poly.nodes.get(i));
	    }
	    if( ++i >= poly.nodes.size() )
		i = 0;
	    madeCircle = true;
	}
	chunks.get(chunk).add(poly.nodes.get(i));

	// check which segment to add
	List<Node> testRing = new ArrayList<Node>(segment.getNodes());
	closePolygon(testRing, chunks.get(0));
//	Node startNode = segmentInsidePolygon(chunks.get(1).get(0), chunks.get(1).get(1), testRing) ? segment.lastNode() : segment.firstNode();
//	Node endNode = startNode.equals(segment.firstNode()) ? segment.lastNode() : segment.firstNode();
	int startIndex = segmentInsidePolygon(chunks.get(1).get(0), chunks.get(1).get(1), testRing) ? index2 : index1;
	int endIndex = startIndex == index2 ? index1 : index2;
	Node startNode = poly.nodes.get(startIndex);
	Node endNode = poly.nodes.get(endIndex);

	// add ways containing nodes from startNode to endNode
	// note: they are in order!
	i = 0;
	while( i < poly.ways.size() && !poly.ways.get(i).containsNode(startNode) )
	    i++;
	int startNodeIndex = poly.ways.get(i).getNodes().indexOf(startNode);
	if( startNodeIndex == 0 || startNodeIndex == poly.ways.get(i).getNodesCount() - 1 )
	    i++; // if it's the last node, take next way

	if( poly.ways.get(i).containsNode(endNode) ) {
	    // ok, both nodes are in the same way
	    // split it, return the new part
	    List<Way> newWays = splitWay(poly.ways.get(i), startNode, endNode, commands);
	    // find which of the parts we need (in case of closed way)
	    Node testNode = poly.nodes.get((index1 + 1) % poly.nodes.size());
	} else {
	    // so, let's take ways one by one
	    // todo: split way 1 and add relevant part
	    List<Way> newWays = splitWay(poly.ways.get(i), startNode, endNode, commands);
	    i++;
	    while( !poly.ways.get(i).containsNode(endNode) ) {
		newRelation.addMember(new RelationMember("outer", poly.ways.get(i)));
		i++;
	    }
	    // todo: split way 2 and add relevant part
	    newWays = splitWay(poly.ways.get(i), startNode, endNode, commands);
	}

	commands.add(new AddCommand(newRelation));
	Main.main.undoRedo.add(new SequenceCommand(tr("Complete multipolygon for way {0}",
		DefaultNameFormatter.getInstance().format(segment)), commands));
	return newRelation;
    }

    /**
     * Returns all elements from {@code list1} that are in {@code list2}.
     */
    private static <T> List<T> intersection( Collection<T> list1, Collection<T> list2 ) {
	List<T> result = new ArrayList<T>();
	for( T item : list1 )
	    if( list2.contains(item) )
		result.add(item);
	return result;
    }
}
