// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Cursor;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JoinNodeWayAction;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
//import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SplitWayCommand;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.TagMap;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.WaySegment;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * The DoubleSplitAction is a mapmode that allows users to add a bus_bay,a
 * bridge or a tunnel .
 *
 * @author Biswesh
 */
public class DoubleSplitAction extends MapMode {

	private static final String MAP_MODE_NAME = "Double Split";

	private transient Set<OsmPrimitive> newHighlights = new HashSet<>();
	private transient Set<OsmPrimitive> oldHighlights = new HashSet<>();
	private int nodeCount = 0;
	private List<Node> atNodes = new ArrayList<>();
	private Way previousAffectedWay;

	private final Cursor cursorJoinNode;
	private final Cursor cursorJoinWay;

	/**
	 * Creates a new DoubleSplitAction
	 */
	public DoubleSplitAction() {
		super(tr(MAP_MODE_NAME), "logo_double_split", tr(MAP_MODE_NAME), null, getCursor());

		cursorJoinNode = ImageProvider.getCursor("crosshair", "joinnode");
		cursorJoinWay = ImageProvider.getCursor("crosshair", "joinway");
	}

	private static Cursor getCursor() {
		Cursor cursor = ImageProvider.getCursor("crosshair", "bus");
		if (cursor == null)
			cursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
		return cursor;
	}

	@Override
	public void enterMode() {
		super.enterMode();
		MainApplication.getMap().mapView.addMouseListener(this);
		MainApplication.getMap().mapView.addMouseMotionListener(this);
	}

	@Override
	public void exitMode() {
		// if we have one node selected and we exit the mode then undo the node
		if (nodeCount == 1) {
			for (int i = 0; i < 2; i++) {
				MainApplication.undoRedo.undo();
			}
			nodeCount = 0;
			atNodes.clear();
			previousAffectedWay = null;
			updateHighlights();
		}
		super.exitMode();
		MainApplication.getMap().mapView.removeMouseListener(this);
		MainApplication.getMap().mapView.removeMouseMotionListener(this);
	}

	private void reset() {
		nodeCount = 0;
		atNodes.clear();
		previousAffectedWay = null;
		updateHighlights();
	}

	private boolean startEndPoints(){
		if (atNodes.get(0).isConnectionNode() && atNodes.get(1).isConnectionNode()) {
			for (Way way : atNodes.get(0).getParentWays()) {
				if (atNodes.get(1).getParentWays().contains(way)) {
					List<TagMap> affectedKeysList = new ArrayList<>();
					affectedKeysList.add(way.getKeys());
					newHighlights.add(way);
					dialogBox(Arrays.asList(way), affectedKeysList);
					return true;
				}
			}

			reset();
			return true;
		}
		return false;
	}

	private boolean firstNodeIsConnectionNode(Node node, Way affected) {
		if (node.isConnectionNode()) {
			if (node.getParentWays().contains(affected))
				previousAffectedWay = affected;
			else {
				reset();
				return true;
			}
		}
		return false;
	}

	private boolean secondNodeIsConnectionNode(Node node) {
		if (atNodes.get(1).isConnectionNode()) {
			if (atNodes.get(1).getParentWays().contains(previousAffectedWay))
				return false;
			else {
				reset();
				return true;
			}
		}
		return false;
	}

	private Node checkCommonNode(Way affected) {

        // check if they have any common node
		List<Node> presentNodeList = affected.getNodes();
		for (Node previousNode : previousAffectedWay.getNodes()) {
			if (presentNodeList.contains(previousNode)) {
				return previousNode;
			}
		}

		return null;
	}

	private void removeFirstNode(Way affected) {

		// select the first node
		Node nodeToBeDeleted = atNodes.get(0);

		if (nodeToBeDeleted != null) {
			// remove first node from list
			atNodes.remove(0);

			// remove last 2 commands from command list
			Command lastCommand = MainApplication.undoRedo.commands.removeLast();
			Command secondLastCommand = MainApplication.undoRedo.commands.removeLast();

			// now we can undo the previous node as the command for present node has been
			// removed from list
			for (int i = 0; i < 2; i++) {
				MainApplication.undoRedo.undo();
			}

			// now again add back the last 2 commands, so overall we undo third last and
		    // fourth last command
			MainApplication.undoRedo.commands.add(secondLastCommand);
			MainApplication.undoRedo.commands.add(lastCommand);

			MainApplication.undoRedo.redo();
		}

		previousAffectedWay = affected;

	}

	private void addKeysOnBothWays(Node commonNode, Way affected) {
		List<TagMap> affectedKeysList = new ArrayList<>();

		List<Node> nodelist1 = Arrays.asList(atNodes.get(0), commonNode);
		List<Node> nodelist2 = Arrays.asList(atNodes.get(1), commonNode);

		affectedKeysList.add(previousAffectedWay.getKeys());
		affectedKeysList.add(affected.getKeys());

		// split both the ways separately

		SplitWayCommand result1 = SplitWayCommand.split(previousAffectedWay, nodelist1, Collections.emptyList());

		SplitWayCommand result2 = SplitWayCommand.split(affected, nodelist2, Collections.emptyList());

		MainApplication.undoRedo.add(result1);
		MainApplication.undoRedo.add(result2);

		Way way1 = null, way2 = null;

		// Find middle way which is a part of both the ways, so find the 2 ways which
		// together would form middle way
		boolean isOriginalWay = true; // we check both the original way and new ways
		for (Way way : result1.getNewWays()) {
			if (way.containsNode(commonNode) && way.containsNode(atNodes.get(0))) {
				way1 = way;
				isOriginalWay = false;
				break;
			}
		}
		if (isOriginalWay) {
			Way way = result1.getOriginalWay();
			if (way.containsNode(commonNode) && way.containsNode(atNodes.get(0))) {
				way1 = way;
			}
		}

		// now do for 2nd way
		isOriginalWay = true;

		for (Way way : result2.getNewWays()) {
			if (way.containsNode(commonNode) && way.containsNode(atNodes.get(1))) {
				way2 = way;
				isOriginalWay = false;
				break;
			}
		}

		if (isOriginalWay) {
			Way way = result2.getOriginalWay();
			if (way.containsNode(commonNode) && way.containsNode(atNodes.get(1))) {
				way2 = way;
			}
		}

		if (way1 != null && way2 != null) {
			List<Way> selectedWays = Arrays.asList(way1, way2);
			newHighlights.add(way1);
			newHighlights.add(way2);
			dialogBox(selectedWays, affectedKeysList);
		}
	}

	private void addKeys(Way affected) {
		List<TagMap> affectedKeysList = new ArrayList<>();
		Way selectedWay = null;

		SplitWayCommand result = SplitWayCommand.split(affected, atNodes, Collections.emptyList());
		if (result == null)
			return;

		MainApplication.undoRedo.add(result);

		// Find the middle way after split
		List<Way> affectedWayList = result.getNewWays();
		for (Way way : affectedWayList) {
			if (atNodes.contains(way.firstNode()) && atNodes.contains(way.lastNode())) {
				selectedWay = way;
				break;
			}
		}

		if (selectedWay != null) {
			affectedKeysList.add(affected.getKeys());
			newHighlights.add(selectedWay);
			dialogBox(Arrays.asList(selectedWay), affectedKeysList);
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {

		// while the mouse is moving, surroundings are checked
		// if anything is found, it will be highlighted.
		// priority is given to nodes
		Cursor newCurs = getCursor();

		Node n = MainApplication.getMap().mapView.getNearestNode(e.getPoint(), OsmPrimitive::isUsable);
		if (n != null) {
			newHighlights.add(n);
			newCurs = cursorJoinNode;
		} else {
			List<WaySegment> wss = MainApplication.getMap().mapView.getNearestWaySegments(e.getPoint(),
					OsmPrimitive::isSelectable);

			if (!wss.isEmpty()) {
				for (WaySegment ws : wss) {
					newHighlights.add(ws.way);
				}
				newCurs = cursorJoinWay;
			}
		}

		MainApplication.getMap().mapView.setCursor(newCurs);
		updateHighlights();
	}

	@Override
	public void mouseClicked(MouseEvent e) {

		Boolean newNode = false;
		Node newStopPos;

		// check if the user has selected an existing node, or a new one
		Node n = MainApplication.getMap().mapView.getNearestNode(e.getPoint(), OsmPrimitive::isUsable);
		if (n == null) {
			newNode = true;
			newStopPos = new Node(MainApplication.getMap().mapView.getLatLon(e.getX(), e.getY()));
		} else {
			newStopPos = new Node(n);
		}

		if (newNode) {
			MainApplication.undoRedo.add(new AddCommand(getLayerManager().getEditDataSet(), newStopPos));
		} else {
			MainApplication.undoRedo.add(new ChangeCommand(n, newStopPos));
			newStopPos = n;
		}

		MainApplication.getLayerManager().getEditLayer().data.setSelected(newStopPos);

		// join the node to the way only if the node is new
		if (newNode) {
			JoinNodeWayAction joinNodeWayAction = JoinNodeWayAction.createMoveNodeOntoWayAction();
			joinNodeWayAction.actionPerformed(null);
		}

		if (newStopPos.getParentWays().isEmpty())
			return;

		Way affected = newStopPos.getParentWays().get(0);

		if (affected == null)
			return;

		atNodes.add(newStopPos);

		// to check the number of nodes that have been selected
		// do not split if this is the first selected node
		if (nodeCount == 0) {
			previousAffectedWay = affected;
			nodeCount++;
			return;
		}

		// if both the nodes are starting and ending points of the same way
		// we don't split the way, just add new key-value to the way
		boolean areStartEndPoints = startEndPoints();
		if (areStartEndPoints)
			return;

		// if first node is a connection node
		boolean isConnectionNode = firstNodeIsConnectionNode(atNodes.get(0), affected);
		if (isConnectionNode)
			return;

		// if second node is a connection node
		isConnectionNode = secondNodeIsConnectionNode(atNodes.get(1));
		if (isConnectionNode)
			return;
		else {
			if (atNodes.get(1).isConnectionNode()) {
				affected = previousAffectedWay;
			}
		}


		// if both the nodes are not on same way and don't have any common node then
		// make second node as first node
		Node commonNode = null;
		boolean twoWaysWithCommonNode = false;
		if (previousAffectedWay != affected) {
			commonNode = checkCommonNode(affected);
			if (commonNode == null) {
				removeFirstNode(affected);
				return;
			} else {
				twoWaysWithCommonNode = true;
			}
		}


		// ****need to add undoredo for previousAffectedWay, atNode, nodeCount

		if (twoWaysWithCommonNode) {
			addKeysOnBothWays(commonNode, affected);

		} else {
            addKeys(affected);
		}

		// reset values of all the variables after two nodes are selected and split
		reset();

	}

	private void dialogBox(List<Way> selectedWay, List<TagMap> affectedKeysList) {

		final ExtendedDialog dialog = new SelectFromOptionDialog(selectedWay, affectedKeysList);
		dialog.toggleEnable("way.split.segment-selection-dialog");
		if (!dialog.toggleCheckState()) {
			dialog.setModal(false);
			dialog.showDialog();
			return; // splitting is performed in SegmentToKeepSelectionDialog.buttonAction()
		}

	}

	// turn off what has been highlighted on last mouse move and highlight what has
	// to be highlighted now

    private void updateHighlights() {
		if (oldHighlights.isEmpty() && newHighlights.isEmpty()) {
			return;
		}

		for (OsmPrimitive osm : oldHighlights) {
			osm.setHighlighted(false);
		}

		for (OsmPrimitive osm : newHighlights) {
			osm.setHighlighted(true);
		}

		MainApplication.getLayerManager().getEditLayer().invalidate();

		oldHighlights.clear();
		oldHighlights.addAll(newHighlights);
		newHighlights.clear();
	}

	// A dialogBox to query whether to select bus_bay, tunnel or bridge.

	static class SelectFromOptionDialog extends ExtendedDialog {
		static final AtomicInteger DISPLAY_COUNT = new AtomicInteger();
		final transient List<Way> selectedWay;
		private JComboBox<String> keys;
		private JComboBox<String> values;
		private List<TagMap> affectedKeysList;

		SelectFromOptionDialog(List<Way> selectedWay, List<TagMap> affectedKeysList) {
			super(Main.parent, tr("What do you want the segment to be?"), new String[] { tr("Ok"), tr("Cancel") },
					true);
			this.selectedWay = selectedWay;
			this.affectedKeysList = affectedKeysList;

			setButtonIcons("ok", "cancel");
			setCancelButton(2);
			configureContextsensitiveHelp("/Dialog/AddValue", true /* show help button */);

			final JPanel pane = new JPanel(new GridBagLayout());
			pane.add(new JLabel("Select the appropriate option"), GBC.eol().fill(GBC.HORIZONTAL));

			keys = new JComboBox<>();
			values = new JComboBox<>();
			keys.setEditable(true);
			keys.setModel(new DefaultComboBoxModel<>(new String[] { "bus_bay", "bridge", "tunnel" }));
			values.setModel(new DefaultComboBoxModel<>(new String[] { "both", "right", "left" }));

			// below code changes the list in values on the basis of key
			keys.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if ("bus_bay".equals(keys.getSelectedItem())) {
						values.setModel(new DefaultComboBoxModel<>(new String[] { "both", "right", "left" }));
					} else if ("bridge".equals(keys.getSelectedItem())) {
						values.setModel(new DefaultComboBoxModel<>(new String[] { "yes" }));
					} else if ("tunnel".equals(keys.getSelectedItem())) {
						values.setModel(new DefaultComboBoxModel<>(new String[] { "yes", "culvert" }));
					}
				}
			});

			pane.add(keys, GBC.eop().fill(GBC.HORIZONTAL));
			pane.add(values, GBC.eop().fill(GBC.HORIZONTAL));

			setContent(pane, false);
			setDefaultCloseOperation(HIDE_ON_CLOSE);
		}

		@Override
		protected void buttonAction(int buttonIndex, ActionEvent evt) {
			super.buttonAction(buttonIndex, evt);
			toggleSaveState(); // necessary since #showDialog() does not handle it due to the non-modal dialog

			if (getValue() == 1) {
				TagMap newKeys1 = this.affectedKeysList.get(0);
				newKeys1.put(keys.getSelectedItem().toString(), values.getSelectedItem().toString());

				if (keys.getSelectedItem() == "bridge") {
					newKeys1.put("layer", "1");
					this.selectedWay.get(0).setKeys(newKeys1);
				} else if (keys.getSelectedItem() == "tunnel") {
					newKeys1.put("layer", "-1");
					this.selectedWay.get(0).setKeys(newKeys1);
				} else {
					this.selectedWay.get(0).setKeys(newKeys1);
				}

				if (this.affectedKeysList.size() == 2) {
					TagMap newKeys2 = this.affectedKeysList.get(1);
					newKeys2.put(keys.getSelectedItem().toString(), values.getSelectedItem().toString());

					if (keys.getSelectedItem() == "bridge") {
						newKeys2.put("layer", "1");
						this.selectedWay.get(1).setKeys(newKeys2);
					} else if (keys.getSelectedItem() == "tunnel") {
						newKeys2.put("layer", "-1");
						this.selectedWay.get(1).setKeys(newKeys2);
					} else {
						this.selectedWay.get(1).setKeys(newKeys2);
					}
				}
			}
		}
	}
}
