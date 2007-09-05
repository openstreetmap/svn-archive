package org.openstreetmap.josm.data.osm;

/**
 * This is the reverse of RelationMember: a helper linkage class
 * used in OsmPrimitive to link back to Relations. We could have
 * re-used RelationMember for that but it would have been a hack.
 * 
 * The back references are a kidn of "cache", they are not 
 * authoritative - true relationship information is stored in 
 * the relations themselves, not in the objects they point to.
 * 
 * @author Frederik Ramm <frederik@remote.org>
 */
public class RelationMemberBackReference {

	public String role;
	public Relation relation;
	public RelationMemberBackReference(Relation e, String r) { relation = e; role = r; }

	/**
	 * The equals method is important for the removal of back references.
	 */
	public boolean equals(Object other) {
		if (!(other instanceof RelationMemberBackReference)) return false;
		
		return role.equals(((RelationMemberBackReference)other).role) && 
			relation.equals(((RelationMemberBackReference)other).relation);
	}
}
