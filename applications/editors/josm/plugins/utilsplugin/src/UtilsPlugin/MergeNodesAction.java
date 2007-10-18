package UtilsPlugin;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collection;
import java.util.Collections;

import java.awt.event.ActionEvent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.visitor.CollectBackReferencesVisitor;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.SequenceCommand;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

class MergeNodesAction extends JosmAction {
	public MergeNodesAction() {
		super(tr("Merge nodes"), "mergenodes",
			tr("Merge nodes"), 0, 0, true);
	}

	public void actionPerformed(ActionEvent e) {
		Collection<OsmPrimitive> sel = Main.ds.getSelected();
		Collection<Node> nodes = new ArrayList<Node>();

		for (OsmPrimitive osm : sel)
			if (osm instanceof Node)
				nodes.add((Node)osm);
		if (nodes.size() < 2) {
			JOptionPane.showMessageDialog(Main.parent,
				tr("Must select at least two nodes."));
			return;
		}

		// Find the node with the lowest ID.
		// We're gonna keep our 3-digit node ids.
		Node target = null;
		for (Node n : nodes) {
			if (target == null || target.id == 0 || n.id < target.id) {
				target = n;
			}
		}

		Collection<Command> cmds = new LinkedList<Command>();

		Node newTarget = new Node(target);
		cmds.add(new ChangeCommand(target, newTarget));

		// Don't place the merged node on one of the former nodes.
		// Place it right there in the middle.
		double x = 0, y = 0;
		for (Node n : nodes) {
			x += n.eastNorth.east();
			y += n.eastNorth.north();
		}
		newTarget.eastNorth = new EastNorth(
			x / nodes.size(), y / nodes.size());

		nodes.remove(target);

		cmds.add(new DeleteCommand(nodes));

		for (Way w : Main.ds.ways) {
			if (w.deleted || w.incomplete) continue;

			boolean affected = false;
			for (Node n : nodes) {
				if (w.nodes.contains(n)) {
					affected = true;
					break;
				}
			}
			if (!affected) continue;

			// Replace the old nodes with the merged ones
			Way wnew = new Way(w);
			for (int i = 0; i < wnew.nodes.size(); i++) {
				if (nodes.contains(wnew.nodes.get(i))) {
					wnew.nodes.set(i, newTarget);
				}
			}

			// Remove duplicates
			Node lastN = null;
			for (int i = wnew.nodes.size() - 1; i >= 0; i--) {
				if (lastN == wnew.nodes.get(i)) {
					wnew.nodes.remove(i);
					if (i < wnew.nodes.size()) i++;
				}
			}

			if (wnew.nodes.size() < 2) {
				CollectBackReferencesVisitor backRefV =
					new CollectBackReferencesVisitor(Main.ds, false);
				backRefV.visit(w);
				if (!backRefV.data.isEmpty()) {
					JOptionPane.showMessageDialog(Main.parent,
						tr("Cannot merge nodes: " +
							"Would have to delete way that is still used."));
					return;
				}

				cmds.add(new DeleteCommand(Collections.singleton(w)));
			} else {
				cmds.add(new ChangeCommand(w, wnew));
			}
		}

		Main.main.undoRedo.add(new SequenceCommand(tr("Merge Nodes"), cmds));
		Main.map.repaint();
	}
}
