package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import java.io.Serializable;

import org.openstreetmap.josm.data.osm.PrimitiveId;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.SimplePrimitiveId;
import org.openstreetmap.josm.tools.CheckParameterUtil;

/**
 * RelationMemberModel is a mutable relation member. In contrast to
 * {@link RelationMember} it doesn't keep references to the referred
 * primitive. Internally, it only keeps their the unique id.
 *
 *
 */
public class RelationMemberModel implements Serializable{
    private String role;
    private SimplePrimitiveId target;
    
    /**
     * Creates a new relation member model
     * 
     * @param role the member role. Reset to "" if null.
     * @param target the id of the target object. Must not be null.
     * @throws IllegalArgumentException thrown if {@code target} is null
     */
    public RelationMemberModel(String role, PrimitiveId target) throws IllegalArgumentException {
        CheckParameterUtil.ensureParameterNotNull(target, "target");
        this.role = role == null? "" : role;
        this.target = new SimplePrimitiveId(target.getUniqueId(), target.getType());
    }
    
    /**
     * Creates a new relation member model from a relation member 
     * 
     * @param member the relation member. Must not be null.
     * @throws IllegalArgumentException thrown if {@code member} is null
     */
    public RelationMemberModel(RelationMember member) throws IllegalArgumentException{
        CheckParameterUtil.ensureParameterNotNull(member, "member");
        this.role = member.getRole();
        setTarget(member.getMember().getPrimitiveId());
    }

    /**
     * Replies the current role in this model. Never null.
     * 
     * @return the current role in this model
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the current role in this model. 
     * 
     * @param role the role. Reset to "" if null.
     */
    public void setRole(String role) {
        this.role = role == null? "" : role;
    }

    /**
     * Replies the id of the target object of this relation member.
     * 
     * @return the id of the target object of this relation member.
     */
    public PrimitiveId getTarget() {
        return target;
    }

    /**
     * Sets the id of the target object.  
     * 
     * @param target the id of the target object. Must not be null.
     * @throws IllegalArgumentException thrown if {@code target} is null
     */
    public void setTarget(PrimitiveId target) throws IllegalArgumentException{
        CheckParameterUtil.ensureParameterNotNull(target, "target");
        this.target = new SimplePrimitiveId(target.getUniqueId(), target.getType());
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((role == null) ? 0 : role.hashCode());
        result = prime * result + ((target == null) ? 0 : target.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RelationMemberModel other = (RelationMemberModel) obj;
        if (role == null) {
            if (other.role != null)
                return false;
        } else if (!role.equals(other.role))
            return false;
        if (target == null) {
            if (other.target != null)
                return false;
        } else if (!target.equals(other.target))
            return false;
        return true;
    }
}
