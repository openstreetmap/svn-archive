// License: GPL. Copyright 2007 by Immanuel Scholz and others
package org.openstreetmap.josm.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.HashSet;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.NodePair;
import org.openstreetmap.josm.tools.GBC;

/**
 * Combines multiple ways into one.
 * 
 * @author Imi
 */
public class CombineWayAction extends JosmAction implements SelectionChangedListener {

	public CombineWayAction() {
		super(tr("Combine Way"), "combineway", tr("Combine several ways into one."), KeyEvent.VK_C, KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK, true);
		DataSet.selListeners.add(this);
	}

	public void actionPerformed(ActionEvent event) {
		Collection<OsmPrimitive> selection = Main.ds.getSelected();
		LinkedList<Way> selectedWays = new LinkedList<Way>();
		
		for (OsmPrimitive osm : selection)
			if (osm instanceof Way)
				selectedWays.add((Way)osm);

		if (selectedWays.size() < 2) {
			JOptionPane.showMessageDialog(Main.parent, tr("Please select at least two ways to combine."));
			return;
		}

		// collect properties for later conflict resolving
		Map<String, Set<String>> props = new TreeMap<String, Set<String>>();
		for (Way w : selectedWays) {
			for (Entry<String,String> e : w.entrySet()) {
				if (!props.containsKey(e.getKey()))
					props.put(e.getKey(), new TreeSet<String>());
				props.get(e.getKey()).add(e.getValue());
			}
		}
		
		// Battle plan:
		//  1. Split the ways into small chunks of 2 nodes and weed out
		//	   duplicates.
		//  2. Take a chunk and see if others could be appended or prepended,
		//	   if so, do it and remove it from the list of remaining chunks.
		//	   Rather, rinse, repeat.
		//  3. If this algorithm does not produce a single way,
		//     complain to the user.
		//  4. Profit!
		
		HashSet<NodePair> chunkSet = new HashSet<NodePair>();
		for (Way w : selectedWays) {
			if (w.nodes.size() == 0) continue;
			Node lastN = null;
			for (Node n : w.nodes) {
				if (lastN == null) {
					lastN = n;
					continue;
				}
				chunkSet.add(new NodePair(lastN, n));
				lastN = n;
			}
		}
		LinkedList<NodePair> chunks = new LinkedList<NodePair>(chunkSet);

		if (chunks.isEmpty()) {
			JOptionPane.showMessageDialog(Main.parent, tr("All the ways were empty"));
			return;
		}

		List<Node> nodeList = chunks.poll().toArrayList();
		while (!chunks.isEmpty()) {
			ListIterator<NodePair> it = chunks.listIterator();
			boolean foundChunk = false;
			while (it.hasNext()) {
				NodePair curChunk = it.next();
				if (curChunk.a == nodeList.get(nodeList.size() - 1)) { // append
					nodeList.add(curChunk.b);
					foundChunk = true;
				} else if (curChunk.b == nodeList.get(0)) { // prepend
					nodeList.add(0, curChunk.a);
					foundChunk = true;
				}
				if (foundChunk) {
					it.remove();
					break;
				}
			}
			if (!foundChunk) break;
		}

		if (!chunks.isEmpty()) {
			JOptionPane.showMessageDialog(Main.parent,
				tr("Could not combine ways (Hint: ways have to point into the same direction)"));
			return;
		}

		Way newWay = new Way(selectedWays.get(0));
		newWay.nodes.clear();
		newWay.nodes.addAll(nodeList);
		
		// display conflict dialog
		Map<String, JComboBox> components = new HashMap<String, JComboBox>();
		JPanel p = new JPanel(new GridBagLayout());
		for (Entry<String, Set<String>> e : props.entrySet()) {
			if (e.getValue().size() > 1) {
				JComboBox c = new JComboBox(e.getValue().toArray());
				c.setEditable(true);
				p.add(new JLabel(e.getKey()), GBC.std());
				p.add(Box.createHorizontalStrut(10), GBC.std());
				p.add(c, GBC.eol());
				components.put(e.getKey(), c);
			} else
				newWay.put(e.getKey(), e.getValue().iterator().next());
		}
		if (!components.isEmpty()) {
			int answer = JOptionPane.showConfirmDialog(Main.parent, p, tr("Enter values for all conflicts."), JOptionPane.OK_CANCEL_OPTION);
			if (answer != JOptionPane.OK_OPTION)
				return;
			for (Entry<String, JComboBox> e : components.entrySet())
				newWay.put(e.getKey(), e.getValue().getEditor().getItem().toString());
		}

		LinkedList<Command> cmds = new LinkedList<Command>();
		cmds.add(new DeleteCommand(selectedWays.subList(1, selectedWays.size())));
		cmds.add(new ChangeCommand(selectedWays.peek(), newWay));
		Main.main.undoRedo.add(new SequenceCommand(tr("Combine {0} ways", selectedWays.size()), cmds));
		Main.ds.setSelected(selectedWays.peek());
	}

	/**
	 * Enable the "Combine way" menu option if more then one way is selected
	 */
	public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {
		boolean first = false;
		for (OsmPrimitive osm : newSelection) {
			if (osm instanceof Way) {
				if (first) {
					setEnabled(true);
					return;
				}
				first = true;
			}
		}
		setEnabled(false);
	}
}
