// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.utilsplugin2.replacegeometry;

import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trn;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.DefaultNameFormatter;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.RelationToChildReference;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.tools.MultiMap;

public class ReplaceMembershipAction extends JosmAction {

    public ReplaceMembershipAction() {
        super(
                tr("Replace Membership"), (String) null,
                tr("In relations where the selected object is member of, replace it with a new one"), null, false,
                ReplaceMembershipAction.class.getName(), true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final Iterator<OsmPrimitive> selection = getLayerManager().getEditDataSet().getSelected().iterator();
        final OsmPrimitive firstObject = selection.next();
        final OsmPrimitive secondObject = selection.next();

        final ReplaceGeometryCommand command = getReplaceGeometryCommand(firstObject, secondObject);
        final int affectedRelations = command.getChildren().size();
        if (affectedRelations > 0) {
            Main.main.undoRedo.add(command);
            new Notification(trn("Replaced ''{0}'' by ''{1}'' in {2} relation", "Replaced ''{0}'' by ''{1}'' in {2} relations",
                    affectedRelations,
                    firstObject.getDisplayName(DefaultNameFormatter.getInstance()),
                    secondObject.getDisplayName(DefaultNameFormatter.getInstance()),
                    affectedRelations
                    )).setIcon(JOptionPane.INFORMATION_MESSAGE).show();
        } else {
            new Notification(tr("The first selected object ''{0}'' is not part of any relation",
                    firstObject.getDisplayName(DefaultNameFormatter.getInstance())
                    )).setIcon(JOptionPane.WARNING_MESSAGE).show();
        }
    }

    static ReplaceGeometryCommand getReplaceGeometryCommand(OsmPrimitive firstObject, OsmPrimitive secondObject) {
        final MultiMap<Relation, RelationToChildReference> byRelation = new MultiMap<>();
        for (final RelationToChildReference i : RelationToChildReference.getRelationToChildReferences(firstObject)) {
            byRelation.put(i.getParent(), i);
        }

        final List<Command> commands = new ArrayList<>();
        for (final Map.Entry<Relation, Set<RelationToChildReference>> i : byRelation.entrySet()) {
            final Relation oldRelation = i.getKey();
            final Relation newRelation = new Relation(oldRelation);
            for (final RelationToChildReference reference : i.getValue()) {
                newRelation.setMember(reference.getPosition(), new RelationMember(reference.getRole(), secondObject));
            }
            commands.add(new ChangeCommand(oldRelation, newRelation));
        }

        return new ReplaceGeometryCommand(tr("Replace Membership"), commands);
    }

    @Override
    protected void updateEnabledState() {
        updateEnabledStateOnCurrentSelection();
    }

    @Override
    protected void updateEnabledState(Collection<? extends OsmPrimitive> selection) {
        setEnabled(selection != null && selection.size() == 2);
    }
}
