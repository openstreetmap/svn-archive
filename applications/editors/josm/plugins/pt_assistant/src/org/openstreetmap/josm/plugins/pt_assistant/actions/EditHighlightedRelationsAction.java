// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.gui.dialogs.relation.RelationEditor;
import org.openstreetmap.josm.plugins.pt_assistant.PTAssistantPlugin;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Gives the user the possibility to edit the current highlighted relation without selecting it again
 *
 * @author giacomo
 *
 */
public class EditHighlightedRelationsAction extends JosmAction {

    private static final long serialVersionUID = 2681464946469047054L;

    /**
     * Default constructor
     */
    public EditHighlightedRelationsAction() {
        super(tr("Edit Highlighted Relation"), new ImageProvider("dialogs", "edit"), tr("Edit Highlighted Relation"),
                Shortcut.registerShortcut("Edit Highlighted Relation", tr("Edit Highlighted Relation"),
                		KeyEvent.VK_K, Shortcut.ALT),
                false, "editHighlightedRelations", false);
    }

    /**
     * Applies the fixes, resets the last fix attribute
     */
    @Override
    public void actionPerformed(ActionEvent e) {
    	for(Relation relation : PTAssistantPlugin.getHighlightedRelations()) {
    		RelationEditor editor = RelationEditor.getEditor(Main.getLayerManager().getEditLayer(), relation, null);
            editor.setVisible(true);
    	}
    }

}
