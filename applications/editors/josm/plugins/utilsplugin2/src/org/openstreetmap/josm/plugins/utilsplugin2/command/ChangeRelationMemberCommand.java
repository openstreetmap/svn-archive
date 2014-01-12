// License: GPL v2 or later. See LICENSE file for details.
package org.openstreetmap.josm.plugins.utilsplugin2.command;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Collection;
import java.util.LinkedList;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.gui.DefaultNameFormatter;

/**
 * Change member in relation to another one
 */
public class ChangeRelationMemberCommand extends Command {

    private final Relation relation;
    private final OsmPrimitive oldMember;
    private final OsmPrimitive newMember;
    private boolean oldModified;

    public ChangeRelationMemberCommand(Relation relation, OsmPrimitive oldMember, OsmPrimitive newMember) {
        this.relation = relation;
        this.oldMember = oldMember;
        this.newMember = newMember;
    }

    private void replaceMembers(OsmPrimitive oldP, OsmPrimitive newP) {
        if (relation == null || oldMember == null || newMember == null) {
            return;
        }
        LinkedList<RelationMember> newrms = new LinkedList<RelationMember>();
        for (RelationMember rm : relation.getMembers()) {
            if (rm.getMember() == oldP) {
                newrms.add(new RelationMember(rm.getRole(), newP));
            } else {
                newrms.add(rm);
            }
        }
        relation.setMembers(newrms);
    }
    
    @Override
    public boolean executeCommand() {
        oldModified = relation.isModified();
        replaceMembers(oldMember, newMember);
        relation.setModified(true);
        return true;
    }

    @Override
    public void fillModifiedData(Collection<OsmPrimitive> modified,
            Collection<OsmPrimitive> deleted, Collection<OsmPrimitive> added) {
        modified.add(relation);
    }

    @Override
    public void undoCommand() {
        replaceMembers(newMember, oldMember);
        relation.setModified(oldModified);
    }

    @Override
    public String getDescriptionText() {
        return tr("Change relation member for {0} {1}",
                OsmPrimitiveType.from(relation),
                relation.getDisplayName(DefaultNameFormatter.getInstance()));
    }
}