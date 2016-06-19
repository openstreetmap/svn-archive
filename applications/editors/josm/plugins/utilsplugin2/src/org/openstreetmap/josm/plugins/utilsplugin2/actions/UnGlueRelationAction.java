// License: GPL v2 or later. Copyright 2010 by Kalle Lampila and others
// See LICENSE file for details.
package org.openstreetmap.josm.plugins.utilsplugin2.actions;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.utilsplugin2.command.ChangeRelationMemberCommand;
import org.openstreetmap.josm.tools.Shortcut;

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
              Shortcut.registerShortcut("tools:ungluerelation", tr("Tool: {0}", tr("UnGlue Relations")), KeyEvent.VK_G, Shortcut.ALT_SHIFT ), true);
        putValue("help", ht("/Action/UnGlueRelation"));
    }

    /**
     * Called when the action is executed.
     */
    @Override
    public void actionPerformed(ActionEvent e) {

        LinkedList<Command> cmds = new LinkedList<>();
        List<OsmPrimitive> newPrimitives = new LinkedList<>();
        Collection<OsmPrimitive> selection = getLayerManager().getEditDataSet().getSelected();

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
                    cmds.add(new ChangeRelationMemberCommand(relation, p, newp));
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
            getLayerManager().getEditDataSet().setSelected(newPrimitives);
            Main.map.mapView.repaint();
        }
    }

    @Override
    protected void updateEnabledState() {
        updateEnabledStateOnCurrentSelection();
    }

    @Override
    protected void updateEnabledState(Collection<? extends OsmPrimitive> selection) {
        setEnabled(selection != null && !selection.isEmpty());
    }
}
