/**
 * 
 */
package org.openstreetmap.josm.plugins.alignways;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.visitor.AllNodesVisitor;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * @author tilusnet <tilusnet@gmail.com>
 * 
 */
public class AlignWaysAction extends JosmAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1540319652562985458L;

	public AlignWaysAction() {
		super(tr("Align Way Segments"), "alignways",
				tr("Makes a pair of selected way segments parallel by rotating one of them " +
				"around a chosen pivot."),
				Shortcut.registerShortcut("tools:alignways", tr("Tool: {0}",
						tr("Align Ways")), KeyEvent.VK_A, Shortcut.GROUP_EDIT,
						Shortcut.SHIFT_DEFAULT), true);
		setEnabled(false);
	}

	public void actionPerformed(ActionEvent e) {
		if (!isEnabled())
			return;
		if (getCurrentDataSet() == null)
			return;

		Collection<OsmPrimitive> selection = getCurrentDataSet().getSelected();
		Collection<Node> affectedNodes = AllNodesVisitor.getAllNodes(selection);

		Command c = !Main.main.undoRedo.commands.isEmpty() ? Main.main.undoRedo.commands
				.getLast()
				: null;

				if (!(c instanceof AlignWaysRotateCommand &&
						affectedNodes.equals(((AlignWaysRotateCommand) c).getRotatedNodes()))) {
					Main.main.undoRedo.add(c = new AlignWaysRotateCommand());
				}

				// Warn user if reference and alignee segment nodes are common:
				// We cannot align two connected segment
				if (((AlignWaysRotateCommand) c).areSegsConnected()) {
					// Revert move
					((AlignWaysRotateCommand) c).undoCommand();
					JOptionPane.showMessageDialog(Main.parent,
							tr("You cannot align connected segments.\n"
									+ "Please select two segments that don''t share any nodes."),
									tr("AlignWayS message"), JOptionPane.WARNING_MESSAGE);
					return;
				}

				for (Node n : affectedNodes) {
					if (n.getCoor().isOutSideWorld()) {
						// Revert move
						((AlignWaysRotateCommand) c).undoCommand();
						JOptionPane.showMessageDialog(Main.parent,
								tr("Aligning would result nodes outside the world.\n" +
								"Your action is being reverted."),
								tr("AlignWayS message"), JOptionPane.WARNING_MESSAGE);
						return;
					}

				}

				Main.map.mapView.repaint();

				return;
	}

}
