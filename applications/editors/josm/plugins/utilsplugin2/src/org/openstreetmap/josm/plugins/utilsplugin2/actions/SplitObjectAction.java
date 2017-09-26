// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.utilsplugin2.actions;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trn;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.command.SplitWayCommand;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Splits a closed way (polygon) into two closed ways.
 *
 * The closed ways are just split at the selected nodes (which must be exactly two).
 * The nodes remain in their original order.
 *
 * This is similar to SplitWayAction with the addition that the split ways are closed
 * immediately.
 */
public class SplitObjectAction extends JosmAction {
    /**
     * Create a new SplitObjectAction.
     */
    public SplitObjectAction() {
        super(tr("Split Object"), "splitobject", tr("Split an object at the selected nodes."),
                Shortcut.registerShortcut("tools:splitobject", tr("Tool: {0}", tr("Split Object")), KeyEvent.VK_X, Shortcut.ALT),
                true);
        putValue("help", ht("/Action/SplitObject"));
    }

    /**
     * Called when the action is executed.
     *
     * This method performs an expensive check whether the selection clearly defines one
     * of the split actions outlined above, and if yes, calls the splitObject method.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Collection<OsmPrimitive> selection = getLayerManager().getEditDataSet().getSelected();

        List<Node> selectedNodes = OsmPrimitive.getFilteredList(selection, Node.class);
        List<Way> selectedWays = OsmPrimitive.getFilteredList(selection, Way.class);

        if (!checkSelection(selection)) {
            showWarningNotification(tr("The current selection cannot be used for splitting."));
            return;
        }

        Way selectedWay = null;
        Way splitWay = null;

        if (selectedNodes.size() != 2) {            // if not exactly 2 nodes are selected - try to find split way
            selectedNodes.clear();                  // empty selected nodes (see #8237)
            for (Way selWay : selectedWays) {       // we assume not more 2 ways in the list
                if (selWay != null &&               // If one of selected ways is not closed we have it to get split points
                        selWay.isUsable() &&
                        !selWay.isClosed() &&
                        selWay.getKeys().isEmpty()) {
                    selectedNodes.add(selWay.firstNode());
                    selectedNodes.add(selWay.lastNode());
                    splitWay = selWay;
                } else {
                    selectedWay = selWay;           // use another way as selected way
                }
            }
        }

        // If only nodes are selected, try to guess which way to split. This works if there
        // is exactly one way that all nodes are part of.
        if (selectedWay == null && !selectedNodes.isEmpty()) {
            Map<Way, Integer> wayOccurenceCounter = new HashMap<>();
            for (Node n : selectedNodes) {
                for (Way w : OsmPrimitive.getFilteredList(n.getReferrers(), Way.class)) {
                    if (!w.isUsable()) {
                        continue;
                    }
                    int last = w.getNodesCount() - 1;
                    // Only closed ways with at least four nodes
                    // (i.e. five members since the first/last is listed twice)
                    // can be split into two objects
                    if (last <= 4 || !w.isClosed()) {
                        continue;
                    }
                    for (Node wn : w.getNodes()) {
                        if (n.equals(wn)) {
                            Integer old = wayOccurenceCounter.get(w);
                            wayOccurenceCounter.put(w, (old == null) ? 1 : old + 1);
                            break;
                        }
                    }
                }
            }
            if (wayOccurenceCounter.isEmpty()) {
                showWarningNotification(
                        trn("The selected node is not in the middle of any way.",
                                "The selected nodes are not in the middle of any way.",
                                selectedNodes.size()));
                return;
            }

            for (Entry<Way, Integer> entry : wayOccurenceCounter.entrySet()) {
                if (entry.getValue().equals(selectedNodes.size())) {
                    if (selectedWay != null) {
                        showWarningNotification(
                                trn("There is more than one way using the node you selected. Please select the way also.",
                                        "There is more than one way using the nodes you selected. Please select the way also.",
                                        selectedNodes.size())
                                );
                        return;
                    }
                    selectedWay = entry.getKey();
                }
            }

            if (selectedWay == null) {
                showWarningNotification(tr("The selected nodes do not share the same way."));
                return;
            }

            // If a way and nodes are selected, verify that the nodes
            // are part of the way and that the way is closed.
        } else if (selectedWay != null && !selectedNodes.isEmpty()) {
            if (!selectedWay.isClosed()) {
                showWarningNotification(tr("The selected way is not closed."));
                return;
            }
            HashSet<Node> nds = new HashSet<>(selectedNodes);
            nds.removeAll(selectedWay.getNodes());
            if (!nds.isEmpty()) {
                showWarningNotification(
                        trn("The selected way does not contain the selected node.",
                                "The selected way does not contain all the selected nodes.",
                                selectedNodes.size()));
                return;
            }
        } else if (selectedWay != null && selectedNodes.isEmpty()) {
            showWarningNotification(
                    tr("The selected way is not a split way, please select split points or split way too."));
            return;
        }

        // we're guaranteed to have two nodes
        Node node1 = selectedNodes.get(0);
        int nodeIndex1 = -1;
        Node node2 = selectedNodes.get(1);
        int nodeIndex2 = -1;
        int i = 0;
        for (Node wn : selectedWay.getNodes()) {
            if (nodeIndex1 == -1 && wn.equals(node1)) {
                nodeIndex1 = i;
            } else if (nodeIndex2 == -1 && wn.equals(node2)) {
                nodeIndex2 = i;
            }
            i++;
        }
        // both nodes aren't allowed to be consecutive
        if (nodeIndex1 == nodeIndex2 + 1 ||
                nodeIndex2 == nodeIndex1 + 1 ||
                // minus 2 because we've a circular way where
                // the penultimate node is the last unique one
                (nodeIndex1 == 0 && nodeIndex2 == selectedWay.getNodesCount() - 2) ||
                (nodeIndex2 == 0 && nodeIndex1 == selectedWay.getNodesCount() - 2)) {
            showWarningNotification(
                    tr("The selected nodes can not be consecutive nodes in the object."));
            return;
        }

        List<List<Node>> wayChunks = SplitWayCommand.buildSplitChunks(selectedWay, selectedNodes);
        if (wayChunks != null) {
            // close the chunks
            // update the logic - if we have splitWay not null, we have to add points from it to both chunks (in the correct direction)
            if (splitWay == null) {
                for (List<Node> wayChunk : wayChunks) {
                    wayChunk.add(wayChunk.get(0));
                }
            } else {
                for (List<Node> wayChunk : wayChunks) {
                    // check direction of the chunk and add splitWay nodes in the correct order
                    List<Node> way = splitWay.getNodes();
                    if (wayChunk.get(0).equals(splitWay.firstNode())) {
                        // add way to the end in the opposite direction.
                        way.remove(way.size()-1); // remove the last node
                        Collections.reverse(way);
                    } else {
                        // add way to the end in the given direction, remove the first node
                        way.remove(0);
                    }
                    wayChunk.addAll(way);
                }
            }
            SplitWayCommand result = SplitWayCommand.splitWay(
                    selectedWay, wayChunks, Collections.<OsmPrimitive>emptyList());
            MainApplication.undoRedo.add(result);
            if (splitWay != null)
                MainApplication.undoRedo.add(new DeleteCommand(splitWay));
            getLayerManager().getEditDataSet().setSelected(result.getNewSelection());
        }
    }

    /**
     * Checks if the selection consists of something we can work with.
     * Checks only if the number and type of items selected looks good;
     * does not check whether the selected items are really a valid
     * input for splitting (this would be too expensive to be carried
     * out from the selectionChanged listener).
     */
    private boolean checkSelection(Collection<? extends OsmPrimitive> selection) {
        int node = 0;
        int ways = 0;
        for (OsmPrimitive p : selection) {
            if (p instanceof Way) {
                ways++;
            } else if (p instanceof Node) {
                node++;
            } else
                return false;
        }
        return node == 2 || ways == 1 || ways == 2; //only 2 nodes selected. one split-way selected. split-way + way to split.
    }

    @Override
    protected void updateEnabledState() {
        updateEnabledStateOnCurrentSelection();
    }

    @Override
    protected void updateEnabledState(Collection<? extends OsmPrimitive> selection) {
        if (selection == null) {
            setEnabled(false);
            return;
        }
        setEnabled(checkSelection(selection));
    }

    void showWarningNotification(String msg) {
        new Notification(msg)
        .setIcon(JOptionPane.WARNING_MESSAGE).show();
    }
}
