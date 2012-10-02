package relcontext.relationfix;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.MultipolygonCreate;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;

/**
 * @see http://wiki.openstreetmap.org/wiki/Relation:multipolygon
 */
public class MultipolygonFixer extends RelationFixer {

	public MultipolygonFixer() {
		super("multipolygon");
	}

	protected MultipolygonFixer(String...types) {
		super(types);
	}


	@Override
	public boolean isRelationGood(Relation rel) {
		for (RelationMember m : rel.getMembers())
			if (m.getType().equals(OsmPrimitiveType.WAY) && !("outer".equals(m.getRole()) || "inner".equals(m.getRole()))) {
			    setWarningMessage(tr("Way without ''inner'' or ''outer'' role found"));
			    return false;
			}
		clearWarningMessage();
		return true;
	}

	@Override
	public Command fixRelation(Relation rel) {
		Relation rr = fixMultipolygonRoles(rel);
		return rr != null? new ChangeCommand(rel, rr) : null;
	}

	/**
     * Basically, created multipolygon from scratch, and if successful, replace roles with new ones.
     */
    protected Relation fixMultipolygonRoles( Relation source ) {
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
}
