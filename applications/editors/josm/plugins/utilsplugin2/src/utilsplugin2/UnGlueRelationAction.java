// License: GPL v2 or later. Copyright 2010 by Kalle Lampila and others
// See LICENSE file for details.
package utilsplugin2;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trn;

import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.tools.Shortcut;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;

/**
 * Duplicate nodes, ways and relations that are used by multiple relations.
 *
 * Resulting nodes, ways and relations are identical as the orginals.
 *
 * @author Kalle Lampila
 *
 */
public class UnGlueRelationAction extends JosmAction {

    /**
     * Create a new UnGlueRelationAction.
     */
    public UnGlueRelationAction() {
        super(tr("UnGlue Relation"), "ungluerelations", tr("Duplicate nodes, ways and relations that are used by multiple relations."),
              Shortcut.registerShortcut("tools:ungluerelation", tr("Tool: {0}", tr("UnGlue Relations")), KeyEvent.VK_G, Shortcut.GROUPS_ALT1+Shortcut.GROUP_DIRECT2 ), true);
        putValue("help", ht("/Action/UnGlueRelation"));
    }

    /**
     * Called when the action is executed.
     */
    public void actionPerformed(ActionEvent e) {

        LinkedList<Command> cmds = new LinkedList<Command>();
        List<OsmPrimitive> newPrimitives = new LinkedList<OsmPrimitive>();
        Collection<OsmPrimitive> selection = getCurrentDataSet().getSelected();

        for (OsmPrimitive p : selection) {
            boolean first = true;
            for (Relation relation : OsmPrimitive.getFilteredList(p.getReferrers(), Relation.class)) {
                if (relation.isDeleted()) {
                    continue;
                }
                if (!first) {
                    OsmPrimitive newp;
                    switch(p.getType()) {
                    case NODE: newp = new Node((Node)p, true); break;
                    case WAY: newp = new Way((Way)p, true); break;
                    case RELATION: newp = new Relation((Relation)p, true); break;
                    default: throw new AssertionError();
                    }                    
                    newPrimitives.add(newp);
                    cmds.add(new AddCommand(newp));
                    cmds.add(new ChangeCommand(relation, changeRelationMember(relation, p, newp)));
                } else {
                    first = false;
                }
            }
        }

        if (newPrimitives.isEmpty() ) {
            // error message nothing to do
        }
        else {
            Main.main.undoRedo.add(new SequenceCommand(tr("Unglued Relations"), cmds));
            //Set selection all primiteves (new and old)
            newPrimitives.addAll(selection);
            getCurrentDataSet().setSelected(newPrimitives);
            Main.map.mapView.repaint();
        }
    }

    /**
     * Change member in relation to another one
     * @param relation
     * @param orginalMember member to change
     * @param newMember
     * @return new relation were change is made
     */
    private Relation changeRelationMember(Relation relation, OsmPrimitive orginalMember, OsmPrimitive newMember) {
        LinkedList<RelationMember> newrms = new LinkedList<RelationMember>();
        for (RelationMember rm : relation.getMembers()) {
            if (rm.getMember() == orginalMember) {
                newrms.add(new RelationMember(rm.getRole(),newMember));
            } else {
                newrms.add(rm);
            }
        }
        Relation newRelation  = new Relation(relation);
        newRelation.setMembers(newrms);
        return newRelation;
    }

    @Override
    protected void updateEnabledState() {
        if (getCurrentDataSet() == null) {
            setEnabled(false);
        } else {
            updateEnabledState(getCurrentDataSet().getSelected());
        }
    }

    @Override
    protected void updateEnabledState(Collection<? extends OsmPrimitive> selection) {
        setEnabled(selection != null && !selection.isEmpty());
    }

}
