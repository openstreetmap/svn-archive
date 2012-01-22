// License: GPL.
package converttomultipoly;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Convert an area into an advance multipolygon.
 *
 * New relation with type=multipolygon is created for each ways.
 *
 * All the tags (except the source tag) will be moved into the relation.
 */
@SuppressWarnings("serial")
public class MultipolyAction extends JosmAction {

    public MultipolyAction() {
        super(tr("Convert to multipolygon"), "multipoly_convert",
                tr("Convert to multipolygon."), 
			Shortcut.registerShortcut("tools:multipolyconv", tr("Tool: {0}",tr("Convert to multipolygon")), KeyEvent.VK_M,
                        Shortcut.GROUP_EDIT, Shortcut.SHIFT_DEFAULT), true);
    }

    /**
     * The action button has been clicked
     *
     * @param e
     *            Action Event
     */
    public void actionPerformed(ActionEvent e) {

        // Get all ways in some type=multipolygon relation
        HashSet<OsmPrimitive> relationsInMulti = new HashSet<OsmPrimitive>();
        for (Relation r : Main.main.getCurrentDataSet().getRelations()) {
            if (!r.isUsable())
                continue;
            if (r.get("type") != "multipolygon")
                continue;
            for (RelationMember rm : r.getMembers()) {
                OsmPrimitive m = rm.getMember();
                if (m instanceof Way && rm.getRole().compareTo("inner") != 0) {
                    relationsInMulti.add(m);
                }
            }
        }

        // List of selected ways
        ArrayList<Way> selectedWays = new ArrayList<Way>();

        // For every selected way
        for (OsmPrimitive osm : Main.main.getCurrentDataSet().getSelected()) {
            if (osm instanceof Way) {
                Way way = (Way) osm;
                // Check if way is already in another multipolygon
                if (relationsInMulti.contains(osm)) {
                    JOptionPane
                            .showMessageDialog(
                                    Main.parent,
                                    tr("One of the selected ways is already part of another multipolygon."));
                    return;
                }

                selectedWays.add(way);
            }
        }

        if (Main.map == null) {
            JOptionPane.showMessageDialog(Main.parent, tr("No data loaded."));
            return;
        }

        Collection<Command> cmds = new LinkedList<Command>();
        // Add ways to it
        for (int i = 0; i < selectedWays.size(); i++) {
            Way way = selectedWays.get(i);

            // Create new relation
            Relation rel = new Relation();
            rel.put("type", "multipolygon");

            RelationMember rm = new RelationMember("outer", way);
            rel.addMember(rm);

            for (String key : way.getKeys().keySet()) {
                if (!key.equals("area") || !way.get(key).equals("yes")) {
                    rel.put(key, way.get(key));
                }
                if (!key.equals("source")) {
                    cmds.add(new ChangePropertyCommand(way, key, null));
                }
            }
            // Add relation
            cmds.add(new AddCommand(rel));
        }
        // Commit
        Main.main.undoRedo.add(new SequenceCommand(tr("Create multipolygon"), cmds));
        Main.map.repaint();
    }

    /** Enable this action only if something is selected */
    @Override
    protected void updateEnabledState() {
        if (getCurrentDataSet() == null) {
            setEnabled(false);
        } else {
            updateEnabledState(getCurrentDataSet().getSelected());
        }
    }

    /** Enable this action only if something is selected */
    @Override
    protected void updateEnabledState(Collection<? extends OsmPrimitive> selection) {
        if (selection == null) {
            setEnabled(false);
            return;
        }
        for (OsmPrimitive primitive: selection) {
            if (!(primitive instanceof Way)) {
                setEnabled(false);
                return;
            }
            if (!((Way)primitive).isClosed()) {
                setEnabled(false);
                return;
            }
            for (Relation r: OsmPrimitive.getFilteredList(primitive.getReferrers(), Relation.class)) {
                for (RelationMember rm: r.getMembers()) {
                    if (rm.getMember() == primitive && !"inner".equals(rm.getRole())) {
                        setEnabled(false);
                        return;
                    }
                }
            }
        }
        setEnabled(selection.size() >= 1);
    }
}
