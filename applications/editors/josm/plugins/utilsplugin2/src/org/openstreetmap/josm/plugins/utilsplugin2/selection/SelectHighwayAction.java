// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.utilsplugin2.selection;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Select all connected ways for a street if one way is selected (determine by name/ref),
 * select highway ways between two selected ways.
 *
 * @author zverik
 */
public class SelectHighwayAction extends JosmAction {

    public SelectHighwayAction() {
        super(tr("Select Highway"), "selecthighway", tr("Select highway for the name/ref given"),
                Shortcut.registerShortcut("tools:selecthighway", tr("Tool: {0}", "Select Highway"),
                        KeyEvent.VK_W, Shortcut.ALT_CTRL), true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DataSet ds = getLayerManager().getActiveDataSet();
        List<Way> selectedWays = new ArrayList<>(ds.getSelectedWays());

        if (selectedWays.size() == 1) {
            ds.setSelected(selectNamedRoad(selectedWays.get(0)));
        } else if (selectedWays.size() == 2) {
            ds.setSelected(selectHighwayBetween(selectedWays.get(0), selectedWays.get(1)));
        } else {
            new Notification(
                    tr("Please select one or two ways for this action")
                    ).setIcon(JOptionPane.WARNING_MESSAGE).show();
        }
    }

    private static Set<Way> selectNamedRoad(Way firstWay) {
        Set<Way> newWays = new HashSet<>();
        String key = firstWay.hasKey("name") ? "name" : "ref";
        if (firstWay.hasKey(key)) {
            String value = firstWay.get(key);
            Queue<Node> nodeQueue = new LinkedList<>();
            nodeQueue.add(firstWay.firstNode());
            while (!nodeQueue.isEmpty()) {
                Node node = nodeQueue.remove();
                for (Way p : node.getParentWays()) {
                    if (!p.isDisabled() && !newWays.contains(p) && p.hasKey(key) && p.get(key).equals(value)) {
                        newWays.add(p);
                        nodeQueue.add(p.firstNode().equals(node) ? p.lastNode() : p.firstNode());
                    }
                }
            }
        }
        return newWays;
    }

    private static Set<Way> selectHighwayBetween(Way firstWay, Way lastWay) {
        int minRank = Math.min(getHighwayRank(firstWay), getHighwayRank(lastWay));
        HighwayTree firstTree = new HighwayTree(firstWay, minRank);
        HighwayTree secondTree = new HighwayTree(lastWay, minRank);
        Way intersection = firstTree.getIntersection(secondTree);
        while (intersection == null && (firstTree.canMoveOn() || secondTree.canMoveOn())) {
            firstTree.processNextLevel();
            secondTree.processNextLevel();
            intersection = firstTree.getIntersection(secondTree);
        }
        Set<Way> newWays = new HashSet<>();
        newWays.addAll(firstTree.getPath(intersection));
        newWays.addAll(secondTree.getPath(intersection));
        return newWays;
    }

    private static int getHighwayRank(OsmPrimitive way) {
        if (!way.hasKey("highway"))
            return 0;
        String highway = way.get("highway");
        if ("path".equals(highway) || "footway".equals(highway) || "cycleway".equals(highway))
            return 1;
        else if ("track".equals(highway) || "service".equals(highway))
            return 2;
        else if ("unclassified".equals(highway) || "residential".equals(highway))
            return 3;
        else if ("tertiary".equals(highway) || "tertiary_link".equals(highway))
            return 4;
        else if ("secondary".equals(highway) || "secondary_link".equals(highway))
            return 5;
        else if ("primary".equals(highway) || "primary_link".equals(highway))
            return 6;
        else if ("trunk".equals(highway) || "trunk_link".equals(highway) || "motorway".equals(highway) || "motorway_link".equals(highway))
            return 7;
        return 0;
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
        int count = 0, rank = 100;
        for (OsmPrimitive p : selection) {
            if (p instanceof Way) {
                count++;
                rank = Math.min(rank, getHighwayRank(p));
            }
        }
        setEnabled(count == 1 || (count == 2 && rank > 0));
    }

    private static class HighwayTree {
        private List<Way> tree;
        private List<Integer> refs;
        private List<Node> nodesToCheck;
        private List<Integer> nodeRefs;
        private int minHighwayRank;

        HighwayTree(Way from, int minHighwayRank) {
            tree = new ArrayList<>(1);
            refs = new ArrayList<>(1);
            tree.add(from);
            refs.add(Integer.valueOf(-1));
            this.minHighwayRank = minHighwayRank;
            nodesToCheck = new ArrayList<>(2);
            nodeRefs = new ArrayList<>(2);
            nodesToCheck.add(from.firstNode());
            nodesToCheck.add(from.lastNode());
            nodeRefs.add(Integer.valueOf(0));
            nodeRefs.add(Integer.valueOf(0));
        }

        public void processNextLevel() {
            List<Node> newNodes = new ArrayList<>();
            List<Integer> newIdx = new ArrayList<>();
            for (int i = 0; i < nodesToCheck.size(); i++) {
                Node node = nodesToCheck.get(i);
                Integer nodeRef = nodeRefs.get(i);
                for (Way way : node.getParentWays()) {
                    if ((way.firstNode().equals(node) || way.lastNode().equals(node)) &&
                            !tree.contains(way) && suits(way)) {
                        tree.add(way);
                        refs.add(nodeRef);
                        Node newNode = way.firstNode().equals(node) ? way.lastNode() : way.firstNode();
                        newNodes.add(newNode);
                        newIdx.add(Integer.valueOf(tree.size() - 1));
                    }
                }
            }
            nodesToCheck = newNodes;
            nodeRefs = newIdx;
        }

        private boolean suits(Way w) {
            return getHighwayRank(w) >= minHighwayRank;
        }

        public boolean canMoveOn() {
            return !nodesToCheck.isEmpty() && tree.size() < 10000;
        }

        public Way getIntersection(HighwayTree other) {
            for (Way w : other.tree) {
                if (tree.contains(w))
                    return w;
            }
            return null;
        }

        public List<Way> getPath(Way to) {
            if (to == null)
                return Collections.singletonList(tree.get(0));
            int pos = tree.indexOf(to);
            if (pos < 0)
                throw new ArrayIndexOutOfBoundsException("Way " + to + " is not in the tree.");
            List<Way> result = new ArrayList<>(1);
            while (pos >= 0) {
                result.add(tree.get(pos));
                pos = refs.get(pos);
            }
            return result;
        }
    }
}
