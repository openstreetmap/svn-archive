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
 * Remembers last source value and put it on selected object(s).
 *
 * @author Zverik
 */
public class TagSourceAction extends JosmAction {
    private static final String TITLE = tr("Add Source Tag");
    private String source;
    private Set<OsmPrimitive> selectionBuf = new HashSet<OsmPrimitive>();
    private boolean clickedTwice = false;

    public TagSourceAction() {
        super(TITLE, "dumbutils/sourcetag", tr("Add remembered source tag"),
                Shortcut.registerShortcut("tools:sourcetag", tr("Tool: {0}", tr("Add Source Tag")), KeyEvent.VK_S, Shortcut.ALT_CTRL)
                , true);
        source = Main.pref.get("sourcetag.value");
    }

    public void actionPerformed( ActionEvent e ) {
        Collection<OsmPrimitive> selection = getCurrentDataSet().getSelected();
        if( selection.isEmpty() || source == null || source.length() == 0 )
            return;

        Main.main.undoRedo.add(new ChangePropertyCommand(selection, "source", source));
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
        if( selection == null || selection.isEmpty() ) {
            selectionBuf.clear();
            clickedTwice = false;
            setEnabled(false);
            return;
        }

        if( selectionBuf.size() == selection.size() && selectionBuf.containsAll(selection) ) {
            if( !clickedTwice )
                clickedTwice = true;
            else {
                // tags may have been changed, get the source
                String newSource = null;
                for( OsmPrimitive p : selection ) {
                    String value = p.get("source");
                    if( value != null && newSource == null )
                        newSource = value;
                    else if( value != null ? !value.equals(newSource) : newSource != null ) {
                        newSource = "";
                        break;
                    }
                }
                if( newSource != null && newSource.length() > 0 && !newSource.equals(source) ) {
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
