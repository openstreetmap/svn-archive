// License: GPL. Copyright 2007 by Immanuel Scholz and others
package sk.zdila.josm.plugin.simplify;

import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trn;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.command.MoveCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSource;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.HelpAwareOptionPane;
import org.openstreetmap.josm.gui.HelpAwareOptionPane.ButtonSpec;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

public class SimplifyAreaAction extends JosmAction {

	private static final long serialVersionUID = 6854238214548011750L;

	public SimplifyAreaAction() {
		super(tr("Simplify Area"), "simplify", tr("Delete unnecessary nodes from an area."),
				Shortcut.registerShortcut("tools:simplifyArea", tr("Tool: {0}", tr("Simplify Area")), KeyEvent.VK_A, Shortcut.GROUP_EDIT, Shortcut.SHIFT_DEFAULT), true);
	}


	private List<Bounds> getCurrentEditBounds() {
		final LinkedList<Bounds> bounds = new LinkedList<Bounds>();
		final OsmDataLayer dataLayer = Main.map.mapView.getEditLayer();
		for (final DataSource ds : dataLayer.data.dataSources) {
			if (ds.bounds != null) {
				bounds.add(ds.bounds);
			}
		}
		return bounds;
	}


	private boolean isInBounds(final Node node, final List<Bounds> bounds) {
		for (final Bounds b : bounds) {
			if (b.contains(node.getCoor())) {
				return true;
			}
		}
		return false;
	}


	private boolean confirmWayWithNodesOutsideBoundingBox() {
		final ButtonSpec[] options = new ButtonSpec[] { new ButtonSpec(tr("Yes, delete nodes"), ImageProvider.get("ok"), tr("Delete nodes outside of downloaded data regions"), null),
				new ButtonSpec(tr("No, abort"), ImageProvider.get("cancel"), tr("Cancel operation"), null) };
		final int ret = HelpAwareOptionPane.showOptionDialog(
				Main.parent,
				"<html>" + trn("The selected way has nodes outside of the downloaded data region.", "The selected ways have nodes outside of the downloaded data region.", getCurrentDataSet().getSelectedWays().size())
						+ "<br>" + tr("This can lead to nodes being deleted accidentally.") + "<br>" + tr("Do you want to delete them anyway?") + "</html>",
				tr("Delete nodes outside of data regions?"), JOptionPane.WARNING_MESSAGE, null, // no special icon
				options, options[0], null);
		return ret == 0;
	}


	private void alertSelectAtLeastOneWay() {
		HelpAwareOptionPane.showOptionDialog(Main.parent, tr("Please select at least one way to simplify."), tr("Warning"), JOptionPane.WARNING_MESSAGE, null);
	}


	private boolean confirmSimplifyManyWays(final int numWays) {
		final ButtonSpec[] options = new ButtonSpec[] { new ButtonSpec(tr("Yes"), ImageProvider.get("ok"), tr("Simplify all selected ways"), null),
				new ButtonSpec(tr("Cancel"), ImageProvider.get("cancel"), tr("Cancel operation"), null) };
		final int ret = HelpAwareOptionPane.showOptionDialog(Main.parent, tr("The selection contains {0} ways. Are you sure you want to simplify them all?", numWays), tr("Simplify ways?"),
				JOptionPane.WARNING_MESSAGE, null, // no special icon
				options, options[0], null);
		return ret == 0;
	}


	@Override
	public void actionPerformed(final ActionEvent e) {
		final Collection<OsmPrimitive> selection = getCurrentDataSet().getSelected();

		final List<Bounds> bounds = getCurrentEditBounds();
		for (final OsmPrimitive prim : selection) {
			if (prim instanceof Way && bounds.size() > 0) {
				final Way way = (Way) prim;
				// We check if each node of each way is at least in one download
				// bounding box. Otherwise nodes may get deleted that are necessary by
				// unloaded ways (see Ticket #1594)
				for (final Node node : way.getNodes()) {
					if (!isInBounds(node, bounds)) {
						if (!confirmWayWithNodesOutsideBoundingBox()) {
							return;
						}
						break;
					}
				}
			}
		}
		final List<Way> ways = OsmPrimitive.getFilteredList(selection, Way.class);
		if (ways.isEmpty()) {
			alertSelectAtLeastOneWay();
			return;
		} else if (ways.size() > 10) {
			if (!confirmSimplifyManyWays(ways.size())) {
				return;
			}
		}

		final Collection<Command> allCommands = new LinkedList<Command>();
		for (final Way way : ways) {
			final SequenceCommand simplifyCommand = simplifyWay(way);
			if (simplifyCommand == null) {
				continue;
			}
			allCommands.add(simplifyCommand);
		}

		if (!allCommands.isEmpty()) {
			final SequenceCommand rootCommand = new SequenceCommand(trn("Simplify {0} way", "Simplify {0} ways", allCommands.size(), allCommands.size()), allCommands);
			Main.main.undoRedo.add(rootCommand);
			Main.map.repaint();
		}
	}


	/**
	 * Replies true if <code>node</code> is a required node which can't be removed in order to simplify the way.
	 *
	 * @param way
	 *            the way to be simplified
	 * @param node
	 *            the node to check
	 * @return true if <code>node</code> is a required node which can't be removed in order to simplify the way.
	 */
	private boolean isRequiredNode(final Way way, final Node node) {
		final List<OsmPrimitive> parents = new LinkedList<OsmPrimitive>(node.getReferrers());
		parents.remove(way);
		return !parents.isEmpty() || node.isTagged();
	}


	/**
	 * Simplifies a way
	 *
	 * @param w
	 *            the way to simplify
	 */
	private SequenceCommand simplifyWay(final Way w) {
		final double angleThreshold = Double.parseDouble(Main.pref.get("simplify-area.angle", "10.0"));
		final double distanceTreshold = Double.parseDouble(Main.pref.get("simplify-area.distance", "0.2"));
		final double areaTreshold = Double.parseDouble(Main.pref.get("simplify-area.area", "5.0"));

		final List<Node> nodes = w.getNodes();
		final int size = nodes.size();

		if (size == 0) {
			return null;
		}

		final List<MoveCommand> moveCommandList = new ArrayList<MoveCommand>();

		final boolean closed = nodes.get(0).equals(nodes.get(size - 1));

		final List<Node> newNodes = new ArrayList<Node>(size);

		if (closed) {
			nodes.remove(size - 1); // remove end node ( = start node)
		}

		{
			// remove near nodes
			for (int i = 0; i < size; i++) {
				final boolean closing = closed && i == size - 1;
				final Node n1 = closing ? nodes.get(0) : nodes.get(i);

				if (newNodes.isEmpty()) {
					newNodes.add(n1);
					continue;
				}

				final Node n2 = newNodes.get(newNodes.size() - 1);

				final LatLon coord1 = n1.getCoor();
				final LatLon coord2 = n2.getCoor();

				if (isRequiredNode(w, n1) || isRequiredNode(w, n2) || computeDistance(coord1, coord2) > distanceTreshold) {
					if (!closing) {
						newNodes.add(n1);
					}
				} else {
					moveCommandList.add(new MoveCommand(n2, new LatLon((coord1.lat() + coord2.lat()) / 2.0, (coord1.lon() + coord2.lon()) / 2.0)));
					if (closing) {
						newNodes.remove(0);
					}
				}
			}
		}

		final int size2 = newNodes.size();

		final List<Node> newNodes2 = new ArrayList<Node>(size2);

		Node prevNode = null;
		LatLon coord1 = null;
		LatLon coord2 = null;

		for (int i = 0, len = size2 + 1 + (closed ? 1 : 0); i < len; i++) {
			final Node n = newNodes.get(i % size2);
			final LatLon coord3 = n.getCoor();

			if (coord1 != null) {
				if (isRequiredNode(w, prevNode) ||
						Math.abs(computeBearing(coord2, coord3) - computeBearing(coord1, coord2)) > angleThreshold ||
						computeArea(coord1, coord2, coord3) > areaTreshold) {
					newNodes2.add(prevNode);
				} else {
					coord2 = coord1; // at the end of the iteration preserve coord1
				}
			} else if (!closed && prevNode != null) {
				newNodes2.add(prevNode);
			}

			coord1 = coord2;
			coord2 = coord3;
			prevNode = n;
		}

		if (closed) {
			newNodes2.add(newNodes2.get(0)); // set end node ( = start node)
		}

		final HashSet<Node> delNodes = new HashSet<Node>();
		delNodes.addAll(nodes);
		delNodes.removeAll(newNodes2);

		if (delNodes.isEmpty()) {
			return null;
		}

		final Collection<Command> cmds = new LinkedList<Command>();
		final Way newWay = new Way(w);
		newWay.setNodes(newNodes2);

		cmds.addAll(moveCommandList);
		cmds.add(new ChangeCommand(w, newWay));
		cmds.add(new DeleteCommand(delNodes));
		return new SequenceCommand(trn("Simplify Way (remove {0} node)", "Simplify Way (remove {0} nodes)", delNodes.size(), delNodes.size()), cmds);
	}


	private double computeBearing(final LatLon coord1, final LatLon coord2) {
		final double lon1 = Math.toRadians(coord1.getX());
		final double lat1 = Math.toRadians(coord1.getY());

		final double lon2 = Math.toRadians(coord2.getX());
		final double lat2 = Math.toRadians(coord2.getY());

		final double dLon = lon2 - lon1;
		final double y = Math.sin(dLon) * Math.cos(lat2);
		final double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
		return Math.toDegrees(Math.atan2(y, x));
	}


	private double computeDistance(final LatLon coord1, final LatLon coord2) {
		final double lon1 = Math.toRadians(coord1.getX());
		final double lon2 = Math.toRadians(coord2.getX());
		final double lat1 = Math.toRadians(coord1.getY());
		final double lat2 = Math.toRadians(coord2.getY());

		final double R = 6378137d; // m
		final double dLon = lon2 - lon1;
		final double dLat = lat2 - lat1;
		final double a = Math.sin(dLat / 2d) * Math.sin(dLat / 2d) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2d) * Math.sin(dLon / 2d);
		final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return R * c;
	}


	private double computeArea(final LatLon coord1, final LatLon coord2, final LatLon coord3) {
		final double a = computeDistance(coord1, coord2);
		final double b = computeDistance(coord2, coord3);
		final double c = computeDistance(coord3, coord1);

		final double p = (a + b + c) / 2.0;

		return Math.sqrt(p * (p - a) * (p - b) * (p - c));
	}


	@Override
	protected void updateEnabledState() {
		if (getCurrentDataSet() == null) {
			setEnabled(false);
		} else {
			updateEnabledState(getCurrentDataSet().getSelected());
		}
	}


	@Override
	protected void updateEnabledState(final Collection<? extends OsmPrimitive> selection) {
		setEnabled(selection != null && !selection.isEmpty());
	}

}
