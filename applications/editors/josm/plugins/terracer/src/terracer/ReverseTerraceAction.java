package terracer;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.visitor.CollectBackReferencesVisitor;
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
						KeyEvent.VK_R, Shortcut.GROUP_EDIT,
						Shortcut.SHIFT_DEFAULT),
						true);
	}

	/**
	 * Breadth-first searches based on the selection while the selection is a way
	 * with a building=* tag and then applies the addr:housenumber tag in reverse
	 * order.
	 */
	public void actionPerformed(ActionEvent e) {
		Collection<OsmPrimitive> sel = Main.main.getCurrentDataSet().getSelected();

		// set to keep track of all the nodes that have been visited - that is: if
		// we encounter them again we will not follow onto the connected ways.
		HashSet<Node> visitedNodes = new HashSet<Node>();

		// set to keep track of the ways the algorithm has seen, but not yet visited.
		// since when a way is visited all of its nodes are marked as visited, there
		// is no need to keep a visitedWays set.
		HashSet<Way> front = new HashSet<Way>();

		// initialise the set with all the buildings in the selection. this means
		// there is undefined behaviour when there is a multiple selection, as the
		// ordering will be based on the hash.
		for (OsmPrimitive prim : sel) {
			if (prim.keySet().contains("building") && prim instanceof Way) {
				front.add((Way)prim);
			}
		}

		// this is like a visitedWays set, but in a linear order.
		LinkedList<Way> orderedWays = new LinkedList<Way>();

		// and the tags to reverse on the orderedWays.
		LinkedList<String> houseNumbers = new LinkedList<String>();

		while (front.size() > 0) {
			// Java apparently doesn't have useful methods to get single items from sets...
			Way w = front.iterator().next();

			// visit all the nodes in the way, adding the building's they're members of
			// to the front.
			for (Node n : w.getNodes()) {
				if (!visitedNodes.contains(n)) {
					CollectBackReferencesVisitor v = new CollectBackReferencesVisitor(Main.main.getCurrentDataSet());
					v.visit(n);
					for (OsmPrimitive prim : v.getData()) {
						if (prim.keySet().contains("building") && prim instanceof Way) {
							front.add((Way)prim);
						}
					}
					visitedNodes.add(n);
				}
			}

			// we've finished visiting this way, so record the attributes we're interested
			// in for re-writing.
			front.remove(w);
			orderedWays.addLast(w);
			houseNumbers.addFirst(w.get("addr:housenumber"));
		}

		Collection<Command> commands = new LinkedList<Command>();
		// what, no zipWith?
		for (int i = 0; i < orderedWays.size(); ++i) {
			commands.add(new ChangePropertyCommand(
					orderedWays.get(i),
					"addr:housenumber",
					houseNumbers.get(i)));
		}

		Main.main.undoRedo.add(new SequenceCommand(tr("Reverse Terrace"), commands));
		Main.main.getCurrentDataSet().setSelected(orderedWays);
	}

}
