// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.dialogs.relation.RelationEditor;
import org.openstreetmap.josm.plugins.pt_assistant.PTAssistantPlugin;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Gives the user the possibility to edit the current highlighted relation
 * without selecting it again
 *
 * @author giacomo
 *
 */
public class EditHighlightedRelationsAction extends JosmAction {

    private static final String actionName = "Edit Highlighted Relation";
    private static final long serialVersionUID = 2681464946469047054L;

    /**
     * Creates a new EditHighlightedRelationsAction
     */
    public EditHighlightedRelationsAction() {
        super(tr(actionName), new ImageProvider("dialogs", "edit"), tr(actionName),
                Shortcut.registerShortcut("Edit Highlighted Relation", tr(actionName), KeyEvent.VK_K, Shortcut.ALT),
                false, "editHighlightedRelations", false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (Relation relation : PTAssistantPlugin.getHighlightedRelations()) {
            RelationEditor editor = RelationEditor.getEditor(
                    MainApplication.getLayerManager().getEditLayer(), relation, null);
            editor.setVisible(true);
        }
    }

}
