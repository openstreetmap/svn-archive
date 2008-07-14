/*
 *  JOSMng - a Java Open Street Map editor, the next generation.
 * 
 *  Copyright (C) 2008 Petr Nejedly <P.Nejedly@sh.cvut.cz>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */

package org.openstreetmap.josmng.osm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A representation of an OSM relation.
 * 
 * @author nenik
 */
public class Relation extends OsmPrimitive {
    private final Map<OsmPrimitive,String> members = new HashMap<OsmPrimitive, String>();

    Relation(DataSet source, long id, int stamp, String user, boolean vis, Map<OsmPrimitive,String> members) {
        super(source, id, stamp, user, vis);
        if (members != null) {
            this.members.putAll(members);
            for (OsmPrimitive member : members.keySet()) member.addReferrer(this);
        }
    }

    @Override void visit(Visitor v) {
        v.visit(this);
    }
    
    public Map<OsmPrimitive, String> getMembers() {
        return Collections.unmodifiableMap(members);
    }
    
    public void addMember(OsmPrimitive member, String role) {
        ChangeMembersEdit ch = new ChangeMembersEdit(member);
        setMemberRoleImpl(member, role);
        source.postEdit(ch);
    }

    public void removeMember(OsmPrimitive member) {
        ChangeMembersEdit ch = new ChangeMembersEdit(member);
        setMemberRoleImpl(member, null);
        source.postEdit(ch);
    }
    
    void setMemberRoleImpl(OsmPrimitive prim, String role) {
        if (prim == null) {
            System.err.println("null member in rel#" + getId());
        }
        if (role == null) {
            members.remove(prim);
            prim.removeReferrer(this);
        } else {
            String oldRole = members.put(prim, role);
            if (oldRole == null) prim.addReferrer(this);
        }
        source.fireRelationMembersChanged(this);
    }

    @Override void setDeletedImpl(boolean deleted) {
        if (deleted) {
            for(OsmPrimitive member : members.keySet()) member.removeReferrer(this);
        } else {
            for(OsmPrimitive member : members.keySet()) member.addReferrer(this);                    
        }
        super.setDeletedImpl(deleted);
    }

    private class ChangeMembersEdit extends PrimitiveToggleEdit {
        OsmPrimitive member;
        String role; // or null if deleted

        public ChangeMembersEdit(OsmPrimitive member) {
            super ("change members");
            this.member = member;
            this.role = members.get(member); // or null if not present
        }
        
        protected void toggle() {
            String origRole = members.get(member);
            setMemberRoleImpl(member, role);
            role = origRole;
        }
    }
}
