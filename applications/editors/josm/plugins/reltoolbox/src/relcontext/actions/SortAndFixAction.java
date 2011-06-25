package relcontext.actions;

import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static org.openstreetmap.josm.tools.I18n.tr;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.tools.ImageProvider;
import relcontext.ChosenRelation;
import relcontext.ChosenRelationListener;

public class SortAndFixAction extends AbstractAction implements ChosenRelationListener {
    private ChosenRelation rel;

    public SortAndFixAction( ChosenRelation rel ) {
        super();
//        putValue(Action.NAME, "AZ");
        putValue(Action.SMALL_ICON, ImageProvider.get("data", "warning"));
        putValue(Action.SHORT_DESCRIPTION, tr("Fix roles of the chosen relation members"));
        this.rel = rel;
        rel.addChosenRelationListener(this);
        setEnabled(false);
    }

    public void actionPerformed( ActionEvent e ) {
        Command c = fixRelation(rel.get());
        if( c != null )
            Main.main.undoRedo.add(c);
    }

    public void chosenRelationChanged( Relation oldRelation, Relation newRelation ) {
        setEnabled(newRelation != null && needsFixing( newRelation));
    }

    public static boolean needsFixing( Relation rel ) {
        return !isIncomplete(rel) && (areMultipolygonTagsEmpty(rel) || areBoundaryTagsNotRight(rel));
    }

    public static Command fixRelation( Relation rel ) {
        Relation r = rel;
        boolean fixed = false;
        // todo: sort members
        // todo: set roles for multipolygon members
        Relation rr = fixMultipolygonRoles(r);
        if( rr != null ) {
            r = rr;
            fixed = true;
        }
        // todo: set roles for boundary members
        rr= fixBoundaryRoles(r);
        if( rr != null ) {
            r = rr;
            fixed = true;
        }

        return fixed ? new ChangeCommand(rel, r) : null;
    }

    protected static boolean isIncomplete( Relation r ) {
        if( r == null || r.isIncomplete() || r.isDeleted() )
            return true;
        for( RelationMember m : r.getMembers())
            if( m.getMember().isIncomplete() )
                return true;
        return false;
    }

    /**
     * Check for ways that have roles different from "outer" and "inner".
     */
    private static boolean areMultipolygonTagsEmpty( Relation r ) {
        if( r == null || r.getMembersCount() == 0 || !ChosenRelation.isMultipolygon(r) )
            return false;
        for( RelationMember m : r.getMembers() ) {
            if( m.getType().equals(OsmPrimitiveType.WAY) && (m.getRole() == null || (!m.getRole().equals("outer") && !m.getRole().equals("inner"))) )
                return true;
        }
        return false;
    }

    /**
     * Check for nodes and relations without needed roles.
     */
    private static boolean areBoundaryTagsNotRight( Relation r ) {
        if( r == null || r.getMembersCount() == 0 || !r.hasKey("type") || !r.get("type").equals("boundary") )
            return false;
        for( RelationMember m : r.getMembers() ) {
            if( m.getType().equals(OsmPrimitiveType.RELATION) && (m.getRole() == null || !m.getRole().equals("subarea")) )
                return true;
            else if(m.getType().equals(OsmPrimitiveType.NODE) && (m.getRole() == null || (!m.getRole().equals("label") && !m.getRole().equals("admin_centre"))) )
                return true;
        }
        return false;
    }

    /**
     * Basically, created multipolygon from scratch, and if successful, replace roles with new ones.
     */
    private static Relation fixMultipolygonRoles( Relation source ) {
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
            if( m.isWay() ) {
                String role = null;
                if( outerWays.contains((Way)m.getMember()) )
                    role = "outer";
                else if( innerWays.contains((Way)m.getMember()) )
                    role = "inner";
                if( role != null && !role.equals(m.getRole()) ) {
                    r.setMember(i, new RelationMember(role, m.getMember()));
                    fixed = true;
                }
            }
        }
        return fixed ? r : null;
    }

    private static Relation fixBoundaryRoles( Relation source ) {
        Relation r = new Relation(source);
        boolean fixed = false;
        for( int i = 0; i < r.getMembersCount(); i++ ) {
            RelationMember m = r.getMember(i);
            String role = null;
            if( m.isRelation() )
                role = "subarea";
            else if( m.isNode() ) {
                Node n = (Node)m.getMember();
                if( !n.isIncomplete() ) {
                    if( n.hasKey("place") )
                        role = "admin_centre";
                    else
                        role = "label";
                }
            }
            if( role != null && !role.equals(m.getRole()) ) {
                r.setMember(i, new RelationMember(role, m.getMember()));
                fixed = true;
            }
        }
        return fixed ? r : null;
    }
}
