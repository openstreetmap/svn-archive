// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.utilsplugin2.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.tools.Predicate;
import org.openstreetmap.josm.tools.Shortcut;
import org.openstreetmap.josm.tools.Utils;

/**
 * Remembers tags of selected object(s) and when clicked, pastes them onto selection.
 *
 * @author Zverik
 */
public class TagBufferAction extends JosmAction {
    private static final String TITLE = tr("Copy tags from previous selection");
    private static final Predicate<OsmPrimitive> IS_TAGGED_PREDICATE = new Predicate<OsmPrimitive>() {
        @Override
        public boolean evaluate(OsmPrimitive object) {
            return object.isTagged();
        }
    };
    private Map<String, String> tags = new HashMap<>();
    private Map<String, String> currentTags = new HashMap<>();
    private Set<OsmPrimitive> selectionBuf = new HashSet<>();

    /**
     * Constructs a new {@code TagBufferAction}.
     */
    public TagBufferAction() {
        super(TITLE, "dumbutils/tagbuffer", tr("Pastes tags of previously selected object(s)"),
                Shortcut.registerShortcut("tools:tagbuffer", tr("Tool: {0}", tr("Copy tags from previous selection")),
                        KeyEvent.VK_R, Shortcut.SHIFT),
                true, false);
        // The fields are not initialized while the super constructor is running, so we have to call this afterwards:
        installAdapters();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Collection<OsmPrimitive> selection = getLayerManager().getEditDataSet().getSelected();
        if (selection.isEmpty())
            return;

        List<Command> commands = new ArrayList<>();
        for (String key : tags.keySet()) {
            String value = tags.get(key);
            boolean foundNew = false;
            for (OsmPrimitive p : selection) {
                if (!p.hasKey(key) || !p.get(key).equals(value)) {
                    foundNew = true;
                    break;
                }
            }
            if (foundNew)
                commands.add(new ChangePropertyCommand(selection, key, value));
        }

        if (!commands.isEmpty())
            Main.main.undoRedo.add(new SequenceCommand(TITLE, commands));
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
        // selection changed => check if selection is completely different from before
        boolean foundOld = false;
        if (selection != null) {
            for (OsmPrimitive p : selectionBuf) {
                if (selection.contains(p)) {
                    foundOld = true;
                    break;
                }
            }
            selectionBuf.clear();
            selectionBuf.addAll(selection);
        } else {
            foundOld = selectionBuf.isEmpty();
            selectionBuf.clear();
        }
        if (!foundOld) {
            // selection has completely changed, remember tags
            tags.clear();
            tags.putAll(currentTags);
        }
        if (getLayerManager().getEditDataSet() != null)
            rememberSelectionTags();

        setEnabled(selection != null && !selection.isEmpty() && !tags.isEmpty());
    }

    private void rememberSelectionTags() {
        // Fix #8350 - only care about tagged objects
        final Collection<OsmPrimitive> selectedTaggedObjects = Utils.filter(
                getLayerManager().getEditDataSet().getSelected(), IS_TAGGED_PREDICATE);
        if (!selectedTaggedObjects.isEmpty()) {
            currentTags.clear();
            Set<String> bad = new HashSet<>();
            for (OsmPrimitive p : selectedTaggedObjects) {
                if (currentTags.isEmpty()) {
                    for (String key : p.keySet()) {
                        currentTags.put(key, p.get(key));
                    }
                } else {
                    for (String key : p.keySet()) {
                        if (!currentTags.containsKey(key) || !currentTags.get(key).equals(p.get(key)))
                            bad.add(key);
                    }
                    for (String key : currentTags.keySet()) {
                        if (!p.hasKey(key))
                            bad.add(key);
                    }
                }
            }
            for (String key : bad) {
                currentTags.remove(key);
            }
        }
    }
}
