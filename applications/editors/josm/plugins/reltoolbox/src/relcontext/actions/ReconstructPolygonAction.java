package relcontext.actions;

import static org.openstreetmap.josm.tools.I18n.tr;
import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.*;
import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.data.osm.MultipolygonCreate.JoinedPolygon;
import org.openstreetmap.josm.gui.DefaultNameFormatter;
import org.openstreetmap.josm.tools.ImageProvider;
import relcontext.ChosenRelation;
import relcontext.ChosenRelationListener;

/**
 * Make a single polygon out of the multipolygon relation. The relation must have only outer members.
 * @author Zverik
 */
public class ReconstructPolygonAction extends AbstractAction implements ChosenRelationListener {
    private ChosenRelation rel;
    
    private static final List<String> IRRELEVANT_KEYS = Arrays.asList(new String[] {
	"source", "created_by", "note"});

    public ReconstructPolygonAction( ChosenRelation rel ) {
        super(tr("Reconstruct polygon"));
        putValue(SMALL_ICON, ImageProvider.get("dialogs", "filter"));
	putValue(LONG_DESCRIPTION, "Reconstruct polygon from multipolygon relation");
        this.rel = rel;
        rel.addChosenRelationListener(this);
        setEnabled(isSuitableRelation(rel.get()));
    }

    public void actionPerformed( ActionEvent e ) {
        Relation r = rel.get();
	List<Way> ways = new ArrayList<Way>();
	boolean wont = false;
	for( RelationMember m : r.getMembers() ) {
	    if( m.isWay() )
		ways.add(m.getWay());
	    else
		wont = true;
	}
	if( wont ) {
	    JOptionPane.showMessageDialog(Main.parent, tr("Multipolygon must consist only of ways"), tr("Reconstruct polygon"), JOptionPane.ERROR_MESSAGE);
	    return;
	}
	
	MultipolygonCreate mpc = new MultipolygonCreate();
	String error = mpc.makeFromWays(ways);
	if( error != null ) {
	    JOptionPane.showMessageDialog(Main.parent, error);
	    return;
	}
	
	if( !mpc.innerWays.isEmpty() ) {
	    JOptionPane.showMessageDialog(Main.parent, tr("Reconstruction of polygons can be done only from outer ways"), tr("Reconstruct polygon"), JOptionPane.ERROR_MESSAGE);
	    return;
	}
	
	rel.clear();
	List<Way> newSelection = new ArrayList<Way>();
	List<Command> commands = new ArrayList<Command>();
	commands.add(new DeleteCommand(r));
	
	for( JoinedPolygon p : mpc.outerWays ) {
	    // move all tags from relation and common tags from ways
	    Map<String, String> tags = p.ways.get(0).getKeys();
	    List<OsmPrimitive> relations = p.ways.get(0).getReferrers();
	    Set<String> noTags = new HashSet<String>(r.keySet());
	    for( int i = 1; i < p.ways.size(); i++ ) {
		Way w = p.ways.get(i);
		for( String key : w.keySet() ) {
		    String value = w.get(key);
		    if( !noTags.contains(key) && tags.containsKey(key) && !tags.get(key).equals(value) ) {
			tags.remove(key);
			noTags.add(key);
		    }
		}
		List<OsmPrimitive> referrers = w.getReferrers();
		for( Iterator<OsmPrimitive> ref1 = relations.iterator(); ref1.hasNext(); )
		    if( !referrers.contains(ref1.next()) )
			ref1.remove();
	    }
	    tags.putAll(r.getKeys());
	    tags.remove("type");
	    
	    // then delete ways that are not relevant (do not take part in other relations of have strange tags)
	    Way candidateWay = null;
	    for( Way w : p.ways ) {
		if( w.getReferrers().equals(relations) ) {
		    // check tags that remain
		    Set<String> keys = new HashSet<String>(w.keySet());
		    keys.removeAll(tags.keySet());
		    keys.removeAll(IRRELEVANT_KEYS);
		    if( keys.isEmpty() ) {
			if( candidateWay == null )
			    candidateWay = w;
			else {
			    if( candidateWay.isNew() && !w.isNew() ) {
				// prefer ways that are already in the database
				Way tmp = w;
				w = candidateWay;
				candidateWay = w;
			    }
			    commands.add(new DeleteCommand(w));
			}
		    }
		}
	    }
	    
	    // take the first way, put all nodes into it, making it a closed polygon
	    Way result = candidateWay == null ? new Way() : new Way(candidateWay);
	    result.setNodes(p.nodes);
	    result.addNode(result.firstNode());
	    result.setKeys(tags);
	    newSelection.add(candidateWay == null ? result : candidateWay);
	    commands.add(candidateWay == null ? new AddCommand(result) : new ChangeCommand(candidateWay, result));
	}
	
        Main.main.undoRedo.add(new SequenceCommand(tr("Reconstruct polygons from relation {0}",
		r.getDisplayName(DefaultNameFormatter.getInstance())), commands));
	Main.main.getCurrentDataSet().setSelected(newSelection);
    }

    public void chosenRelationChanged( Relation oldRelation, Relation newRelation ) {
	setEnabled(isSuitableRelation(newRelation));
    }
    
    private boolean isSuitableRelation( Relation newRelation ) {
	if( newRelation == null || !"multipolygon".equals(newRelation.get("type")) || newRelation.getMembersCount() == 0 )
	    return false;
	else {
	    for( RelationMember m : newRelation.getMembers() )
		if( "inner".equals(m.getRole()) )
		    return false;
	    return true;
	}
    }
}
