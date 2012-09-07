package relcontext.relationfix;

import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;


/**
 * @see http://wiki.openstreetmap.org/wiki/Relation:boundary
 */
public class BoundaryFixer extends MultipolygonFixer {

	public BoundaryFixer() {
		super(new String[]{"boundary", "multipolygon"});
	}
	
	/**
	 * For boundary relations both "boundary" and "multipolygon" types are applicable, but 
	 * it should also have key boundary=administrative to be fully boundary.
	 * @see http://wiki.openstreetmap.org/wiki/Relation:boundary
	 */
	@Override
	public boolean isFixerApplicable(Relation rel) {
		return super.isFixerApplicable(rel) && "administrative".equals(rel.get("boundary"));
	}
	
	@Override
	public boolean isRelationGood(Relation rel) {
		for( RelationMember m : rel.getMembers() ) {
            if (m.getType().equals(OsmPrimitiveType.RELATION) && !"subarea".equals(m.getRole()))
                return false;
            if (m.getType().equals(OsmPrimitiveType.NODE) && !("label".equals(m.getRole()) || "admin_centre".equals(m.getRole())))
                return false;
            if (m.getType().equals(OsmPrimitiveType.WAY) && !("outer".equals(m.getRole()) || "inner".equals(m.getRole())))
				return false;
        }
		return true;
	}

	@Override
	public Command fixRelation(Relation rel) {
		Relation r = rel;
		Relation rr = fixMultipolygonRoles(r);
		boolean fixed = false;
		if (rr != null) {
			fixed = true;
			r = rr;
		}
		rr = fixBoundaryRoles(r);
		if (rr != null) {
			fixed = true;
			r = rr;
		}
		return fixed ? new ChangeCommand(rel, r) : null;
	}

	private Relation fixBoundaryRoles( Relation source ) {
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
