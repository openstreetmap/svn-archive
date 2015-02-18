package org.openstreetmap.josm.plugins.utilsplugin2.actions;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.*;
import java.util.*;
import java.awt.event.KeyEvent;
import org.openstreetmap.josm.tools.Shortcut;
import java.awt.event.ActionEvent;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Remembers tags of selected object(s) and when clicked, pastes them onto selection.
 *
 * @author Zverik
 */
public class TagBufferAction extends JosmAction {
    private static final String TITLE = tr("Copy tags from previous selection");
    private Map<String, String> tags = new HashMap<>();
    private Map<String, String> currentTags = new HashMap<>();
    private Set<OsmPrimitive> selectionBuf = new HashSet<>();

    public TagBufferAction() {
        super(TITLE, "dumbutils/tagbuffer", tr("Pastes tags of previously selected object(s)"),
                Shortcut.registerShortcut("tools:tagbuffer", tr("Tool: {0}", tr("Copy tags from previous selection")), KeyEvent.VK_R, Shortcut.SHIFT)
        , true, false);
        // The fields are not initialized while the super constructor is running, so we have to call this afterwards:
        installAdapters();
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        Collection<OsmPrimitive> selection = getCurrentDataSet().getSelected();
        if( selection.isEmpty() )
            return;

        List<Command> commands = new ArrayList<>();
        for( String key : tags.keySet() ) {
            String value = tags.get(key);
            boolean foundNew = false;
            for( OsmPrimitive p : selection ) {
                if( !p.hasKey(key) || !p.get(key).equals(value) ) {
                    foundNew = true;
                    break;
                }
            }
            if( foundNew )
                commands.add(new ChangePropertyCommand(selection, key, value));
        }
        
        if( !commands.isEmpty() )
            Main.main.undoRedo.add(new SequenceCommand(TITLE, commands));
    }

    @Override
    protected void updateEnabledState() {
        if( getCurrentDataSet() == null ) {
            setEnabled(false);
            if( selectionBuf != null )
                selectionBuf.clear();
        }  else
            updateEnabledState(getCurrentDataSet().getSelected());
    }

    @Override
    protected void updateEnabledState( Collection<? extends OsmPrimitive> selection ) {
        // selection changed => check if selection is completely different from before
        boolean foundOld = false;
        if( selection != null ) {
            for( OsmPrimitive p : selectionBuf ) {
                if( selection.contains(p) ) {
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
        if( !foundOld ) {
            // selection has completely changed, remember tags
            tags.clear();
            tags.putAll(currentTags);
        }
        if( getCurrentDataSet() != null)
            rememberSelectionTags();

        setEnabled(selection != null && !selection.isEmpty() && !tags.isEmpty());
    }

    private void rememberSelectionTags() {
        // Fix #8350 - only care about tagged objects
        Collection<OsmPrimitive> selectedTaggedObjects = new ArrayList<>(getCurrentDataSet().getSelected());
        for (Iterator<OsmPrimitive> it = selectedTaggedObjects.iterator(); it.hasNext(); ) {
            if (!it.next().isTagged()) {
                it.remove();
            }
        }
        if( !selectedTaggedObjects.isEmpty() ) {
            currentTags.clear();
            Set<String> bad = new HashSet<>();
            for( OsmPrimitive p : selectedTaggedObjects ) {
                if( currentTags.isEmpty() ) {
                    for( String key : p.keySet() )
                        currentTags.put(key, p.get(key));
                } else {
                    for( String key : p.keySet() )
                        if( !currentTags.containsKey(key) || !currentTags.get(key).equals(p.get(key)) )
                            bad.add(key);
                    for( String key : currentTags.keySet() )
                        if( !p.hasKey(key) )
                            bad.add(key);
                }
            }
            for( String key : bad )
                currentTags.remove(key);
        }
    }
}
