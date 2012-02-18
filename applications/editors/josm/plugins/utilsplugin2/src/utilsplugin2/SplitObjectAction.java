// License: GPL. Copyright 2007 by Immanuel Scholz and others
package utilsplugin2;

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

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.actions.SplitWayAction;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
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
                Shortcut.registerShortcut("tools:splitobject", tr("Tool: {0}", tr("Split Object")),
                KeyEvent.VK_X,  Shortcut.ALT)
                , true);
        putValue("help", ht("/Action/SplitObject"));
    }

    /**
     * Called when the action is executed.
     *
     * This method performs an expensive check whether the selection clearly defines one
     * of the split actions outlined above, and if yes, calls the splitObject method.
     */
    public void actionPerformed(ActionEvent e) {
        Collection<OsmPrimitive> selection = getCurrentDataSet().getSelected();

        List<Node> selectedNodes = OsmPrimitive.getFilteredList(selection, Node.class);
        List<Way> selectedWays = OsmPrimitive.getFilteredList(selection, Way.class);

        if (!checkSelection(selection)) {
            JOptionPane.showMessageDialog(
                    Main.parent,
                    tr("The current selection cannot be used for splitting."),
                    tr("Warning"),
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }


        Way selectedWay = null;
        if (!selectedWays.isEmpty()){
            selectedWay = selectedWays.get(0);
        }

        // If only nodes are selected, try to guess which way to split. This works if there
        // is exactly one way that all nodes are part of.
        if (selectedWay == null && !selectedNodes.isEmpty()) {
            Map<Way, Integer> wayOccurenceCounter = new HashMap<Way, Integer>();
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
                JOptionPane.showMessageDialog(Main.parent,
                        trn("The selected node is not in the middle of any way.",
                                "The selected nodes are not in the middle of any way.",
                                selectedNodes.size()),
                                tr("Warning"),
                                JOptionPane.WARNING_MESSAGE);
                return;
            }

            for (Entry<Way, Integer> entry : wayOccurenceCounter.entrySet()) {
                if (entry.getValue().equals(selectedNodes.size())) {
                    if (selectedWay != null) {
                        JOptionPane.showMessageDialog(Main.parent,
                                trn("There is more than one way using the node you selected. Please select the way also.",
                                        "There is more than one way using the nodes you selected. Please select the way also.",
                                        selectedNodes.size()),
                                        tr("Warning"),
                                        JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    selectedWay = entry.getKey();
                }
            }

            if (selectedWay == null) {
                JOptionPane.showMessageDialog(Main.parent,
                        tr("The selected nodes do not share the same way."),
                        tr("Warning"),
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // If a way and nodes are selected, verify that the nodes
            // are part of the way and that the way is closed.
        } else if (selectedWay != null && !selectedNodes.isEmpty()) {
            if (!selectedWay.isClosed()) {
                JOptionPane.showMessageDialog(Main.parent,
                        tr("The selected way is not closed."),
                        tr("Warning"),
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            HashSet<Node> nds = new HashSet<Node>(selectedNodes);
            nds.removeAll(selectedWay.getNodes());
            if (!nds.isEmpty()) {
                JOptionPane.showMessageDialog(Main.parent,
                        trn("The selected way does not contain the selected node.",
                                "The selected way does not contain all the selected nodes.",
                                selectedNodes.size()),
                                tr("Warning"),
                                JOptionPane.WARNING_MESSAGE);
                return;
            }
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
            JOptionPane.showMessageDialog(Main.parent,
                    tr("The selected nodes can not be consecutive nodes in the object."),
                    tr("Warning"),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<List<Node>> wayChunks = SplitWayAction.buildSplitChunks(selectedWay, selectedNodes);
        //        List<List<Node>> wayChunks = buildSplitChunks(selectedWay, selectedNodes);
        if (wayChunks != null) {
            // close the chunks
            for (List<Node> wayChunk : wayChunks) {
                wayChunk.add(wayChunk.get(0));
            }
            SplitWayAction.SplitWayResult result = SplitWayAction.splitWay(getEditLayer(), selectedWay, wayChunks, Collections.<OsmPrimitive>emptyList());
            //            SplitObjectResult result = splitObject(getEditLayer(),selectedWay, wayChunks);
            Main.main.undoRedo.add(result.getCommand());
            getCurrentDataSet().setSelected(result.getNewSelection());
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
        boolean way = false;
        int node = 0;
        for (OsmPrimitive p : selection) {
            if (p instanceof Way && !way) {
                way = true;
            } else if (p instanceof Node) {
                node++;
            } else
                return false;
        }
        return node == 2;
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
    protected void updateEnabledState(Collection<? extends OsmPrimitive> selection) {
        if (selection == null) {
            setEnabled(false);
            return;
        }
        setEnabled(checkSelection(selection));
    }
}
