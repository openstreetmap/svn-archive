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
import org.openstreetmap.josm.command.*;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.*;
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
		    getCurrentDataSet().setSelected(newRelation);
		    return;
		}
	    }
	    if( areAllOfThoseRings(getCurrentDataSet().getSelectedWays()) ) {
		List<Relation> rels = makeManySimpleMultipolygons(getCurrentDataSet().getSelectedWays());
		if( chRel != null )
		    chRel.set(rels.size() == 1 ? rels.get(0) : null);
		if( rels.size() == 1 )
		    getCurrentDataSet().setSelected(rels);
		else
		    getCurrentDataSet().clearSelection();
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
	for( Way way : ways ) {
	    if( way.isClosed() )
		rings.add(way);
	    else
		return false;
	}
	if( rings.isEmpty() || ways.size() == 1 )
	    return false; // todo: for one ring, attach it to neares multipolygons

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
	System.out.println("---------------------------------------");
	List<TheRing> rings = new ArrayList<TheRing>(selection.size());
	for( Way w : selection )
	    rings.add(new TheRing(w));
	for( int i = 0; i < rings.size()-1; i++ )
	    for( int j = i+1; j < rings.size(); j++ )
		rings.get(i).collide(rings.get(j));
	TheRing.redistributeSegments(rings);
	List<Command> commands = new ArrayList<Command>();
	List<Relation> relations = new ArrayList<Relation>();
	for( TheRing r : rings ) {
	    commands.addAll(r.getCommands());
	    relations.add(r.getRelation());
	}
	Main.main.undoRedo.add(new SequenceCommand(tr("Create multipolygons from rings"), commands));
	return relations;
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
