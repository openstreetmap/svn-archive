package dumbutils;

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
class TagBufferAction extends JosmAction {
    private static final String TITLE = "Paste remembered tags";
    private Map<String, String> tags = new HashMap<String, String>();

    public TagBufferAction() {
        super(tr(TITLE), "tagbuffer", tr("Pastes tags of previously selected object(s)"),
                Shortcut.registerShortcut("tools:tagbuffer", tr(TITLE), KeyEvent.VK_R, Shortcut.GROUP_EDIT), true);
    }

    public void actionPerformed( ActionEvent e ) {
        for( OsmPrimitive p : getCurrentDataSet().getSelected() ) {
            for( String key : tags.keySet() )
                p.put(key, tags.get(key));
            // todo: undoRedo
        }
    }
}

