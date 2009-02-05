/**
 * Terracer: A JOSM Plugin for terraced houses.
 * 
 * Copyright 2009 CloudMade Ltd.
 * 
 * Released under the GPLv2, see LICENSE file for details.
 */
package terracer;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Terraces a quadrilateral, closed way into a series of quadrilateral,
 * closed ways.
 * 
 * At present it only works on quadrilaterals, but there is no reason
 * why it couldn't be extended to work with other shapes too. The 
 * algorithm employed is naive, but it works in the simple case. 
 * 
 * @author zere
 */
public final class TerracerAction extends JosmAction {

	public TerracerAction() {
		super(tr("Terrace a building"), 
				"terrace", 
				tr("Creates individual buildings from a long building."),
				Shortcut.registerShortcut("tools:Terracer", 
						tr("Tool: {0}", tr("Terrace a building")),
						KeyEvent.VK_T, Shortcut.GROUP_EDIT, 
						Shortcut.SHIFT_DEFAULT),
						true);
	}

	/**
	 * Checks that the selection is OK. If not, displays error message. If so
	 * calls to terraceBuilding(), which does all the real work.
	 */
	public void actionPerformed(ActionEvent e) {
		Collection<OsmPrimitive> sel = Main.ds.getSelected();
		boolean badSelect = false;

		if (sel.size() == 1) {
			OsmPrimitive prim = sel.iterator().next();

			if (prim instanceof Way) {
				Way way = (Way)prim;

				if ((way.nodes.size() == 5) &&
						way.isClosed()) {
					terraceBuilding(way);

				} else {
					badSelect = true;
				}
			} else {
				badSelect = true;
			}
		} else {
			badSelect = true;
		}

		if (badSelect) {
			JOptionPane.showMessageDialog(Main.parent, 
					tr("Select a single, closed way of four nodes."));
		}
	}

	/**
	 * Terraces a single, closed, quadrilateral way.
	 * 
	 * Any node must be adjacent to both a short and long edge, we naively
	 * choose the longest edge and its opposite and interpolate along them
	 * linearly to produce new nodes. Those nodes are then assembled into
	 * closed, quadrilateral ways and left in the selection.
	 * 
	 * @param w The closed, quadrilateral way to terrace.
	 */
	private void terraceBuilding(Way w) {
		// first ask the user how many buildings to terrace into
		int nb = Integer.parseInt(
				JOptionPane.showInputDialog(
						tr("How many buildings are in the terrace?")));

		// now find which is the longest side connecting the first node
		Node[] nodes = w.nodes.toArray(new Node[] {});
		double side1 = nodes[0].coor.greatCircleDistance(nodes[1].coor);
		double side2 = nodes[0].coor.greatCircleDistance(nodes[3].coor);
		
		// new nodes array to hold all intermediate nodes
		Node[][] new_nodes = new Node[2][nb + 1];
		
		if (side1 > side2) {
			new_nodes[0][0] = nodes[0];
			new_nodes[0][nb] = nodes[1];
			new_nodes[1][0] = nodes[3];
			new_nodes[1][nb] = nodes[2];
		} else {
			new_nodes[0][0] = nodes[0];
			new_nodes[0][nb] = nodes[3];
			new_nodes[1][0] = nodes[1];
			new_nodes[1][nb] = nodes[2];
		}
			
        Collection<Command> commands = new LinkedList<Command>();
        Collection<Way> ways = new LinkedList<Way>();

        // create intermediate nodes by interpolating. 
        for (int i = 1; i < nb; ++i) {
			new_nodes[0][i] = interpolateNode(new_nodes[0][0], new_nodes[0][nb],
					(double)(i)/(double)(nb));
			new_nodes[1][i] = interpolateNode(new_nodes[1][0], new_nodes[1][nb],
					(double)(i)/(double)(nb));
			commands.add(new AddCommand(new_nodes[0][i]));
			commands.add(new AddCommand(new_nodes[1][i]));
		}

        // assemble new quadrilateral, closed ways
        for (int i = 0; i < nb; ++i) {
        	Way terr = new Way();
        	terr.addNode(new_nodes[0][i]);
        	terr.addNode(new_nodes[0][i+1]);
        	terr.addNode(new_nodes[1][i+1]);
        	terr.addNode(new_nodes[1][i]);
        	terr.addNode(new_nodes[0][i]);
        	ways.add(terr);
        	commands.add(new AddCommand(terr));
        }
        
        Main.main.undoRedo.add(new SequenceCommand(tr("Terrace"), commands));
        Main.ds.setSelected(ways);
	}

	/**
	 * Creates a new node at the interpolated position between the argument
	 * nodes. Interpolates linearly in Lat/Lon coordinates.
	 * 
	 * @param a First node, at which f=0.
	 * @param b Last node, at which f=1.
	 * @param f Fractional position between first and last nodes.
	 * @return A new node at the interpolated position.
	 */
	private Node interpolateNode(Node a, Node b, double f) {
        // this isn't quite right - we should probably be interpolating
		// screen coordinates rather than lat/lon, but it doesn't seem to
		// make a great deal of difference at the scale of most terraces.
		Node n = new Node(new LatLon(
				a.coor.lat() * (1.0 - f) + b.coor.lat() * f,
				a.coor.lon() * (1.0 - f) + b.coor.lon() * f
				));
		return n;
	}
}
