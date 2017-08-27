// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.utilsplugin2.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Remembers last source value and put it on selected object(s).
 *
 * @author Zverik
 */
public class TagSourceAction extends JosmAction {
    private static final String TITLE = tr("Add Source Tag");
    private String source;
    private Set<OsmPrimitive> selectionBuf = new HashSet<>();
    private boolean clickedTwice = false;

    public TagSourceAction() {
        super(TITLE, "dumbutils/sourcetag", tr("Add remembered source tag"),
                Shortcut.registerShortcut("tools:sourcetag", tr("Tool: {0}", tr("Add Source Tag")), KeyEvent.VK_S, Shortcut.ALT_CTRL),
                true, false);
        source = Main.pref.get("sourcetag.value");
        // The fields are not initialized while the super constructor is running, so we have to call this afterwards:
        installAdapters();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Collection<OsmPrimitive> selection = getLayerManager().getEditDataSet().getSelected();
        if (selection.isEmpty() || source == null || source.length() == 0)
            return;

        MainApplication.undoRedo.add(new ChangePropertyCommand(selection, "source", source));
    }

    @Override
    protected void updateEnabledState() {
        if (getLayerManager().getEditDataSet() == null) {
            setEnabled(false);
            if (selectionBuf != null)
                selectionBuf.clear();
        } else
            updateEnabledState(getLayerManager().getEditDataSet().getSelected());
    }

    @Override
    protected void updateEnabledState(Collection<? extends OsmPrimitive> selection) {
        if (selection == null || selection.isEmpty()) {
            selectionBuf.clear();
            clickedTwice = false;
            setEnabled(false);
            return;
        }

        if (selectionBuf.size() == selection.size() && selectionBuf.containsAll(selection)) {
            if (!clickedTwice)
                clickedTwice = true;
            else {
                // tags may have been changed, get the source
                String newSource = null;
                for (OsmPrimitive p : selection) {
                    String value = p.get("source");
                    if (value != null && newSource == null)
                        newSource = value;
                    else if (value != null ? !value.equals(newSource) : newSource != null) {
                        newSource = "";
                        break;
                    }
                }
                if (newSource != null && newSource.length() > 0 && !newSource.equals(source)) {
                    source = newSource;
                    Main.pref.put("sourcetag.value", source);
                }
            }
        } else
            clickedTwice = false;
        selectionBuf.clear();
        selectionBuf.addAll(selection);
        setEnabled(source != null && source.length() > 0);
    }
}
