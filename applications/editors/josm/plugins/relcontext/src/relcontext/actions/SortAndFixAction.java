package relcontext.actions;

import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.AbstractAction;
import static org.openstreetmap.josm.tools.I18n.tr;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.*;
import relcontext.ChosenRelation;
import relcontext.ChosenRelationListener;

public class SortAndFixAction extends AbstractAction implements ChosenRelationListener {
    private ChosenRelation rel;

    public SortAndFixAction( ChosenRelation rel ) {
        super("AZ");
        this.rel = rel;
        rel.addChosenRelationListener(this);
        setEnabled(false);
    }

    public void actionPerformed( ActionEvent e ) {
        if( rel.get() == null ) return;
        Relation r = rel.get();
        List<Command> commands = new ArrayList<Command>();
        // todo: sort members
        // todo: set roles for multipolygon members
        Relation fixed = fixMultipolygonRoles(rel.get());
        if( fixed != null ) {
            commands.add(new ChangeCommand(r, fixed));
            r = fixed;
        }
        // todo: set roles for boundary members

        if( !commands.isEmpty() )
            Main.main.undoRedo.add(new SequenceCommand(tr("Sort and fix relation"), commands));
    }

    public void chosenRelationChanged( Relation oldRelation, Relation newRelation ) {
        setEnabled(newRelation != null && areMultipolygonTagsEmpty());
        // todo: enable when needs fixing (empty roles or not ordered members)
    }

    private boolean areMultipolygonTagsEmpty() {
        Relation r = rel == null ? null : rel.get();
        if( r == null || r.getMembersCount() == 0 || !rel.isMultipolygon() )
            return false;
        for( RelationMember m : r.getMembers() ) {
            if( m.getType().equals(OsmPrimitiveType.WAY) && (m.getRole() == null || (!m.getRole().equals("outer") && !m.getRole().equals("inner"))) )
                return true;
        }
        return false;
    }

    /**
     * Basically, created multipolygon from scratch, and if successful, replace roles with new ones.
     */
    private Relation fixMultipolygonRoles( Relation source ) {
        Collection<Way> ways = new ArrayList<Way>();
        for( OsmPrimitive p : source.getMemberPrimitives() )
            if( p instanceof Way )
                ways.add((Way)p);
        MultipolygonCreate mpc = new MultipolygonCreate();
        String error = mpc.makeFromWays(ways);
        if( error != null )
            return null;

        Relation r = new Relation(source);
        boolean fixed = false;
        Set<Way> outerWays = new HashSet<Way>();
        for( MultipolygonCreate.JoinedPolygon poly : mpc.outerWays )
            for( Way w : poly.ways )
                outerWays.add(w);
        Set<Way> innerWays = new HashSet<Way>();
        for( MultipolygonCreate.JoinedPolygon poly : mpc.innerWays )
            for( Way w : poly.ways )
                innerWays.add(w);
        for( int i = 0; i < r.getMembersCount(); i++ ) {
            RelationMember m = r.getMember(i);
            if( m.getType().equals(OsmPrimitiveType.WAY) ) {
                String role = null;
                if( outerWays.contains(m.getMember()) )
                    role = "outer";
                else if( innerWays.contains(m.getMember()) )
                    role = "inner";
                if( role != null && !role.equals(m.getRole()) ) {
                    r.setMember(i, new RelationMember(role, m.getMember()));
                    fixed = true;
                }
            }
        }
        return fixed ? r : null;
    }
}
