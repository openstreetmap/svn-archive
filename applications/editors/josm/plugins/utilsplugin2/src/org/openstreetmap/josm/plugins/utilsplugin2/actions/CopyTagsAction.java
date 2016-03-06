// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.utilsplugin2.actions;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.tools.Shortcut;
import org.openstreetmap.josm.tools.Utils;

/**
 * Copy OSM primitives to clipboard in order to paste them, or their tags, somewhere else.
 * @since 404
 */
public final class CopyTagsAction extends JosmAction {

    /**
     * Constructs a new {@code CopyTagsAction}.
     */
    public CopyTagsAction() {
        super(tr("Copy Tags"), "copy",
                tr("Copy all tags of selected objects to paste buffer."),
                createShortcut(), true, CopyTagsAction.class.getName(), true);
        putValue("help", ht("/Action/CopyTags"));
    }

    public static Shortcut createShortcut() {
        return Shortcut.registerShortcut("system:copytags", tr("Edit: {0}", tr("Copy Tags")), KeyEvent.CHAR_UNDEFINED, Shortcut.NONE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isEmptySelection()) return;
        Collection<OsmPrimitive> selection = getCurrentDataSet().getSelected();
        copy(getEditLayer(), selection);
    }

    /**
     * Copies the tags of object to the clipboard. The output by this function
     * looks similar to: key=value\nkey=value
     * @param source The OSM data layer source
     * @param primitives The OSM primitives to copy
     */
    public static void copy(OsmDataLayer source, Collection<OsmPrimitive> primitives) {
        Set<String> values = new TreeSet<>();
        for (OsmPrimitive p : primitives) {
            for (Entry<String, String> kv : p.getKeys().entrySet()) {
                values.add(new Tag(kv.getKey(), kv.getValue()).toString());
            }
        }
        if (!values.isEmpty()) Utils.copyToClipboard(Utils.join("\n", values));
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

    private static boolean isEmptySelection() {
        Collection<OsmPrimitive> sel = getCurrentDataSet().getSelected();
        if (sel.isEmpty()) {
            JOptionPane.showMessageDialog(
                    Main.parent,
                    tr("Please select something to copy."),
                    tr("Information"),
                    JOptionPane.INFORMATION_MESSAGE
            );
            return true;
        }
        return false;
    }
}
