package utilsplugin2.dumbutils;

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
    private Map<String, String> tags = new HashMap<String, String>();
    private Map<String, String> currentTags = new HashMap<String, String>();
    private Set<OsmPrimitive> selectionBuf = new HashSet<OsmPrimitive>();

    public TagBufferAction() {
        super(TITLE, "dumbutils/tagbuffer", tr("Pastes tags of previously selected object(s)"),
                Shortcut.registerShortcut("tools:tagbuffer", tr("Tool: {0}", TITLE), KeyEvent.VK_R, Shortcut.GROUP_EDIT, Shortcut.SHIFT_DEFAULT), true);
    }

    public void actionPerformed( ActionEvent e ) {
        Collection<OsmPrimitive> selection = getCurrentDataSet().getSelected();
        if( selection.isEmpty() )
            return;

        List<Command> commands = new ArrayList<Command>();
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
        rememberSelectionTags();

        setEnabled(selection != null && !selection.isEmpty() && !tags.isEmpty());
    }

    private void rememberSelectionTags() {
        if( getCurrentDataSet() != null && !getCurrentDataSet().getSelected().isEmpty() ) {
            currentTags.clear();
            Set<String> bad = new HashSet<String>();
            for( OsmPrimitive p : getCurrentDataSet().getSelected() ) {
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
