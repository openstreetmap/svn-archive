// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.utilsplugin2.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.data.osm.TagCollection;
import org.openstreetmap.josm.tools.Shortcut;
import org.openstreetmap.josm.tools.SubclassFilteredCollection;

/**
 * Remembers tags of selected object(s) and when clicked, pastes them onto selection.
 *
 * @author Zverik
 */
public class TagBufferAction extends JosmAction {
    private static final String TITLE = tr("Copy tags from previous selection");
    private static final TagCollection EmptyTags = new TagCollection();
    private final Set<OsmPrimitive> selectionBuf = new HashSet<>();
    private TagCollection tagsToPaste = EmptyTags;
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
        if (selection.isEmpty() || tagsToPaste.isEmpty())
            return;

        List<Command> commands = new ArrayList<>();
        for (Tag tag : tagsToPaste) {
            boolean foundNew = false;
            for (OsmPrimitive p : selection) {
                if (!p.hasTag(tag.getKey(), tag.getValue())) {
                    foundNew = true;
                    break;
                }
            }
            if (foundNew)
                commands.add(new ChangePropertyCommand(selection, tag.getKey(), tag.getValue()));
        }

        if (!commands.isEmpty())
            UndoRedoHandler.getInstance().add(new SequenceCommand(TITLE, commands));
    }

    @Override
    protected void updateEnabledState() {
        if (getLayerManager().getEditDataSet() == null) {
            setEnabled(false);
            selectionBuf.clear();
            tagsToPaste = EmptyTags;
        } else
            updateEnabledState(getLayerManager().getEditDataSet().getSelected());
    }

    @Override
    protected void updateEnabledState(Collection<? extends OsmPrimitive> selection) {
        TagCollection oldTags = getCommonTags(selectionBuf);
        if (!oldTags.isEmpty()) {
                tagsToPaste = new TagCollection(oldTags);
        }
        selectionBuf.clear();
        selectionBuf.addAll(selection);

        setEnabled(!selection.isEmpty() && !tagsToPaste.isEmpty());
    }

    /**
     * Find those tags which appear in all primitives of the selection
     * @param selection the selection
     */
    private static TagCollection getCommonTags(Set<OsmPrimitive> selection) {
        if (selection.isEmpty())
            return EmptyTags;
        // Fix #8350 - only care about tagged objects
        return TagCollection.commonToAllPrimitives(SubclassFilteredCollection.filter(selection, p -> p.isTagged()));
    }
}
