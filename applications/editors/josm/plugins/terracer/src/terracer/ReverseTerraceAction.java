package terracer;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Tool to reverse the house numbers in a terrace.
 *
 * Useful for when you're using the Terracer tool and the house numbers come out
 * in the wrong direction, or when someone has added house numbers in the wrong
 * direction anyway.
 *
 * Finds all connected ways which have a building=* tag on them in order (breadth
 * first search) and then changes the tags to be the reverse of the order in which
 * they were found.
 */
public class ReverseTerraceAction extends JosmAction {

    public ReverseTerraceAction() {
        super(tr("Reverse a terrace"),
            "reverse_terrace",
            tr("Reverses house numbers on a terrace."),
            Shortcut.registerShortcut("tools:ReverseTerrace",
                    tr("Tool: {0}", tr("Reverse a Terrace")),
                    KeyEvent.VK_V, Shortcut.ALT_CTRL_SHIFT),
                        true);
    }

    /**
     * Breadth-first searches based on the selection while the selection is a way
     * with a building=* tag and then applies the addr:housenumber tag in reverse
     * order.
     */
    public void actionPerformed(ActionEvent e) {
        Collection<Way> selectedWays = Main.main.getCurrentDataSet().getSelectedWays();

        // Set to keep track of all the nodes that have been visited - that is: if
        // we encounter them again we will not follow onto the connected ways.
        HashSet<Node> visitedNodes = new HashSet<Node>();

        // Set to keep track of the ways the algorithm has seen, but not yet visited.
        // Since when a way is visited all of its nodes are marked as visited, there
        // is no need to keep a visitedWays set.
        HashSet<Way> front = new HashSet<Way>();

        // Find the first or last way from the teracced houses.
        // It should be connected to exactly one other way.
        for (Way w : selectedWays) {
            int conn = 0;
            for (Way v : selectedWays) {
                if (w.equals(v)) continue;
                if (!Collections.disjoint(w.getNodes(), v.getNodes())) {
                    ++conn;
                }
            }
            if (conn == 1) {
                front.add(w);
                break;
            }
        }

        if (front.isEmpty()) {
            JOptionPane.showMessageDialog(Main.parent,
                    tr("Cannot reverse!"));
            return;
        }

        // This is like a visitedWays set, but in a linear order.
        LinkedList<Way> orderedWays = new LinkedList<Way>();

        // And the tags to reverse on the orderedWays.
        LinkedList<String> houseNumbers = new LinkedList<String>();

        while (front.size() > 0) {
            // Java apparently doesn't have useful methods to get single items from sets...
            Way w = front.iterator().next();

            // Visit all the nodes in the way, adding the building's they're members of
            // to the front.
            for (Node n : w.getNodes()) {
                if (!visitedNodes.contains(n)) {
                    for (OsmPrimitive prim : n.getReferrers()) {
                        if (prim.keySet().contains("building") && prim instanceof Way) {
                            front.add((Way)prim);
                        }
                    }
                    visitedNodes.add(n);
                }
            }

            // We've finished visiting this way, so record the attributes we're interested
            // in for re-writing.
            front.remove(w);
            orderedWays.addLast(w);
            houseNumbers.addFirst(w.get("addr:housenumber"));
        }

        Collection<Command> commands = new LinkedList<Command>();
        for (int i = 0; i < orderedWays.size(); ++i) {
            commands.add(new ChangePropertyCommand(
                    orderedWays.get(i),
                    "addr:housenumber",
                    houseNumbers.get(i)));
        }

        Main.main.undoRedo.add(new SequenceCommand(tr("Reverse Terrace"), commands));
        Main.main.getCurrentDataSet().setSelected(orderedWays);
    }

    @Override
    protected void updateEnabledState() {
        setEnabled(getCurrentDataSet() != null);
    }
}
