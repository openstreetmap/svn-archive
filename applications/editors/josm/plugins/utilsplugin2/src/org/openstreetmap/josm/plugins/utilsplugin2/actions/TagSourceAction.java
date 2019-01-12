// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.utilsplugin2.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Remembers last source value and put it on selected object(s).
 *
 * @author Zverik
 */
public class TagSourceAction extends JosmAction {
    private static final String TITLE = tr("Add Source Tag");
    private String source;
    private List<OsmPrimitive> selectionBuf = new ArrayList<>();

    public TagSourceAction() {
        super(TITLE, "dumbutils/sourcetag", tr("Add remembered source tag"),
                Shortcut.registerShortcut("tools:sourcetag", tr("Tool: {0}", tr("Add Source Tag")), KeyEvent.VK_S, Shortcut.ALT_CTRL),
                true, false);
        source = Config.getPref().get("sourcetag.value");
        // The fields are not initialized while the super constructor is running, so we have to call this afterwards:
        installAdapters();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Collection<OsmPrimitive> selection = getLayerManager().getEditDataSet().getSelected();
        if (selection.isEmpty() || source == null || source.length() == 0)
            return;

        UndoRedoHandler.getInstance().add(new ChangePropertyCommand(selection, "source", source));
    }

    @Override
    protected void updateEnabledState() {
        if (getLayerManager().getEditDataSet() == null) {
            setEnabled(false);
            selectionBuf = new ArrayList<>();
        } else
            updateEnabledState(getLayerManager().getEditDataSet().getSelected());
    }

    @Override
    protected void updateEnabledState(Collection<? extends OsmPrimitive> selection) {
        if (!selectionBuf.isEmpty()) {
            String newSource = null;
            for (OsmPrimitive p : selectionBuf) {
                String s = p.get("source");
                if (s != null) {
                    if (newSource == null)
                        newSource = s;
                    else {
                        if (!newSource.equals(s)) {
                            newSource = null;
                            break;
                        }
                    }
                }
            }
            if (newSource != null && !newSource.isEmpty()) {
                source = newSource;
                Config.getPref().put("sourcetag.value", source);
            }
        }
        selectionBuf = new ArrayList<>(selection);
        setEnabled(!selection.isEmpty() && source != null && !source.isEmpty());
    }
}
