package org.openstreetmap.josm.plugins.tageditor;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.tools.Shortcut;

public class LaunchAction extends JosmAction implements SelectionChangedListener {

    public LaunchAction()  {
        super(
                tr("Edit tags"),
                (String)null, //TODO: set "tag-editor" and add /images/tag-editor.png to distrib
                tr("Launches the tag editor dialog"),
                Shortcut.registerShortcut("edit:launchtageditor", tr("Launches the tag editor dialog"),
                        KeyEvent.VK_1,Shortcut.ALT_SHIFT)
                , true, "tageditor/launch", true);

        DataSet.addSelectionListener(this);
        setEnabled(false);
    }

    /**
     * launch the editor
     */
    protected void launchEditor() {
        if (!isEnabled())
            return;
        TagEditorDialog dialog = TagEditorDialog.getInstance();
        dialog.startEditSession();
        dialog.setVisible(true);
    }

    @Override
	public void actionPerformed(ActionEvent e) {
        launchEditor();
    }

    @Override
	public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {
        setEnabled(newSelection != null && newSelection.size() >0);
    }
}
