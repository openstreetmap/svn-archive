// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.housenumbertool;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Set;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.DataSelectionListener;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.event.SelectionEventManager;
import org.openstreetmap.josm.tools.Shortcut;

public class LaunchAction extends JosmAction implements DataSelectionListener {

    private OsmPrimitive selection = null;

    private File pluginDir;

    /**
     * Constructs a new {@code LaunchAction}.
     * @param pluginDir plugin directory
     */
    public LaunchAction(File pluginDir) {
        super("HouseNumberTaggingTool", 
              "home-icon32", 
              "Launches the HouseNumberTaggingTool dialog", 
              Shortcut.registerShortcut("edit:housenumbertaggingtool", "HouseNumberTaggingTool", KeyEvent.VK_K, Shortcut.DIRECT),
              true);

        this.pluginDir = pluginDir;
        SelectionEventManager.getInstance().addSelectionListener(this);
        setEnabled(false);
    }

    /**
     * launch the editor
     */
    protected void launchEditor() {
        if (!isEnabled()) {
            return;
        }
      
        TagDialog dialog = new TagDialog(pluginDir, selection);
        dialog.showDialog();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        launchEditor();
    }

    @Override
    public void selectionChanged(SelectionChangeEvent event) {
        Set<OsmPrimitive> newSelection = event.getSelection();
        if (newSelection != null && newSelection.size() == 1) {
            setEnabled(true);
            selection  =  newSelection.iterator().next();
        } else {
            setEnabled(false);
            selection = null;
        }
    }
}
