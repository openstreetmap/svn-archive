package dumbutils;

import java.awt.geom.Area;
import org.openstreetmap.josm.data.osm.Node;
import java.util.*;
import org.openstreetmap.josm.command.*;
import org.openstreetmap.josm.Main;
import javax.swing.JOptionPane;
import org.openstreetmap.josm.data.osm.Way;
import java.awt.event.KeyEvent;
import org.openstreetmap.josm.tools.Shortcut;
import java.awt.event.ActionEvent;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.DefaultNameFormatter;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Replaces already existing way with the other, fresh created. Select both ways and push the button.
 *
 * @author Zverik
 */
class ReplaceGeometryAction extends JosmAction {
    private static final String TITLE = "Replace geometry";

    public ReplaceGeometryAction() {
        super(tr(TITLE), "replacegeometry", tr("Replace geometry of selected way with a new one"),
                Shortcut.registerShortcut("tools:replacegeometry", tr(TITLE), KeyEvent.VK_G, Shortcut.GROUP_HOTKEY), true);
    }

    public void actionPerformed( ActionEvent e ) {
        // There must be two ways selected: one with id > 0 and one new.
        List<Way> selection = OsmPrimitive.getFilteredList(getCurrentDataSet().getSelected(), Way.class);
        if( selection.size() != 2 ) {
            JOptionPane.showMessageDialog(Main.parent,
                    tr("This tool replaces geometry of one way with another, and requires two ways to be selected."),
                    tr(TITLE), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int idxNew = selection.get(0).isNew() ? 0 : 1;
        Way geometry = selection.get(idxNew);
        Way way = selection.get(1 - idxNew);
        if( way.isNew() || !geometry.isNew() ) {
            JOptionPane.showMessageDialog(Main.parent,
                    tr("Please select one way that exists in the database and one new way with correct geometry."),
                    tr(TITLE), JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Now do the replacement
        List<Command> commands = new ArrayList<Command>();
        Way result = new Way(way);
        result.setNodes(geometry.getNodes());
        // Copy tags from temporary way (source etc.)
        for( String key : geometry.keySet() )
            result.put(key, geometry.get(key));
        commands.add(new ChangeCommand(way, result));
        commands.add(new DeleteCommand(geometry));

        // Check if there are unconnected nodes, delete them
        Set<Node> nodesToDelete = new HashSet<Node>();
        Area a = getCurrentDataSet().getDataSourceArea();
        for( Node node : way.getNodes() ) {
            if( !node.isDeleted() && node.isReferredByWays(1) && (node.isNewOrUndeleted() || a.contains(node.getCoor())) )
                nodesToDelete.add(node);
        }
        if( !nodesToDelete.isEmpty() )
            commands.add(new DeleteCommand(nodesToDelete));

        // Two items in undo stack: change original way and delete geometry way
        Main.main.undoRedo.add(new SequenceCommand(
                tr("Replace geometry of way {0}", way.getDisplayName(DefaultNameFormatter.getInstance())),
                commands));
    }
}

