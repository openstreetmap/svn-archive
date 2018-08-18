// License: GPL. For details, see LICENSE file.
package MichiganLeft;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Plugin for easily creating turn restrictions at "Michigan left" intersections.
 */
public class MichiganLeft extends Plugin {
    JMenuItem MichiganLeft;

    /**
     * Constructs a new {@code MichiganLeft} plugin.
     *
     * @param info plugin info
     */
    public MichiganLeft(PluginInformation info) {
        super(info);
        MichiganLeft = MainMenu.add(MainApplication.getMenu().dataMenu, new MichiganLeftAction());
    }

    private static class MichiganLeftAction extends JosmAction {
        private LinkedList<Command> cmds = new LinkedList<>();

        MichiganLeftAction() {
            super(tr("Michigan Left"), "michigan_left",
                tr("Adds no left turn for sets of 4 or 5 ways."),
                Shortcut.registerShortcut("tools:michigan_left",
                    tr("Tool: {0}", tr("Michigan Left")), KeyEvent.VK_N, Shortcut.ALT_SHIFT), true);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DataSet ds = MainApplication.getLayerManager().getEditDataSet();
            Collection<OsmPrimitive> mainSelection = ds.getSelected();

            ArrayList<OsmPrimitive> selection = new ArrayList<>();

            for (OsmPrimitive prim : mainSelection) {
                selection.add(prim);
            }

            int ways = 0;
            for (OsmPrimitive prim : selection) {
                if (prim instanceof Way)
                    ways++;
            }

            if ((ways != 4) && (ways != 5)) {
                JOptionPane.showMessageDialog(MainApplication.getMainFrame(),
                    tr("Please select 4 or 5 ways to assign no left turns."));
                return;
            }

            if (ways == 4) {
                // Find extremities of ways
                Hashtable<Node, Integer> extremNodes = new Hashtable<>();
                for (OsmPrimitive prim : selection) {
                    if (prim instanceof Way) {
                        Way way = (Way) prim;
                        incrementHashtable(extremNodes, way.firstNode());
                        incrementHashtable(extremNodes, way.lastNode());
                    }
                }
                if (extremNodes.size() != 4) {
                    JOptionPane.showMessageDialog(MainApplication.getMainFrame(),
                        tr("Please select 4 ways that form a closed relation."));
                    return;
                }

                // order the ways
                ArrayList<Way> orderedWays = new ArrayList<>();
                Way currentWay = (Way) selection.iterator().next();
                orderedWays.add(currentWay);
                selection.remove(currentWay);
                while (selection.size() > 0) {
                    boolean found = false;
                    Node nextNode = currentWay.lastNode();
                    for (OsmPrimitive prim : selection) {
                        Way tmpWay = (Way) prim;
                        if (tmpWay.firstNode() == nextNode) {
                            orderedWays.add(tmpWay);
                            selection.remove(prim);
                            currentWay = tmpWay;
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        JOptionPane.showMessageDialog(MainApplication.getMainFrame(),
                            tr("Unable to order the ways. Please verify their directions"));
                        return;
                    }
                }

                // Build relations
                for (int index = 0; index < 4; index++) {
                    Way firstWay = orderedWays.get(index);
                    Way lastWay = orderedWays.get((index + 1) % 4);
                    Node lastNode = firstWay.lastNode();

                    cmds.add(new AddCommand(ds, buildRelation(firstWay, lastWay, lastNode)));
                }
                Command c = new SequenceCommand(tr("Create Michigan left turn restriction"), cmds);
                UndoRedoHandler.getInstance().add(c);
                cmds.clear();
            }

            if (ways == 5) {
                // Find extremities of ways
                Hashtable<Node, Integer> extremNodes = new Hashtable<>();
                for (OsmPrimitive prim : selection) {
                    if (prim instanceof Way) {
                        Way way = (Way) prim;
                        incrementHashtable(extremNodes, way.firstNode());
                        incrementHashtable(extremNodes, way.lastNode());
                    }
                }

                ArrayList<Node> viaNodes = new ArrayList<>();
                // find via nodes (they have 3 occurences in the list)
                for (Enumeration<Node> enumKey = extremNodes.keys(); enumKey.hasMoreElements();) {
                    Node extrem = enumKey.nextElement();
                    Integer nb = extremNodes.get(extrem);
                    if (nb.intValue() == 3) {
                        viaNodes.add(extrem);
                    }
                }

                if (viaNodes.size() != 2) {
                    JOptionPane.showMessageDialog(MainApplication.getMainFrame(),
                        tr("Unable to find via nodes. Please check your selection"));
                    return;
                }

                Node viaFirst = viaNodes.get(0);
                Node viaLast = viaNodes.get(1); // Find middle segment

                Way middle = null;
                for (OsmPrimitive prim : selection) {
                    if (prim instanceof Way) {
                        Way way = (Way) prim;
                        Node first = way.firstNode();
                        Node last = way.lastNode();

                        if ((first.equals(viaFirst) && last.equals(viaLast))
                         || (first.equals(viaLast) && last.equals(viaFirst)))
                            middle = way;
                    }
                }

                // Build relations
                for (OsmPrimitive prim : selection) {
                    if (prim instanceof Way) {
                        Way way = (Way) prim;
                        if (way != middle) {
                            Node first = way.firstNode();
                            Node last = way.lastNode();

                            if (first == viaFirst)
                                buildRelation(middle, way, viaNodes.get(0));
                            else if (first == viaLast)
                                buildRelation(middle, way, viaNodes.get(1));
                            else if (last == viaFirst)
                                buildRelation(way, middle, viaNodes.get(0));
                            else if (last == viaLast)
                                buildRelation(way, middle, viaNodes.get(1));
                        }
                    }
                }
                Command c = new SequenceCommand(tr("Create Michigan left turn restriction"), cmds);
                UndoRedoHandler.getInstance().add(c);
                cmds.clear();
            }
        }

        public void incrementHashtable(Hashtable<Node, Integer> hash, Node node) {
            if (hash.containsKey(node)) {
                Integer nb = hash.get(node);
                hash.put(node, Integer.valueOf(nb.intValue() + 1));
            } else {
                hash.put(node, Integer.valueOf(1));
            }
        }

        public Relation buildRelation(Way fromWay, Way toWay, Node viaNode) {
            Relation relation = new Relation();

            RelationMember from = new RelationMember("from", fromWay);
            relation.addMember(from);

            RelationMember to = new RelationMember("to", toWay);
            relation.addMember(to);

            RelationMember via = new RelationMember("via", viaNode);
            relation.addMember(via);

            relation.put("type", "restriction");
            relation.put("restriction", "no_left_turn");

            return relation;
        }

        @Override
        protected void updateEnabledState() {
            setEnabled(getLayerManager().getEditLayer() != null);
        }

        @Override
        protected void updateEnabledState(Collection<? extends OsmPrimitive> selection) {
            // do nothing
        }
    }
}
