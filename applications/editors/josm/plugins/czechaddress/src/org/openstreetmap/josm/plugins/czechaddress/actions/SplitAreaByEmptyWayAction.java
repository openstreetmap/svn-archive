package org.openstreetmap.josm.plugins.czechaddress.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Splits all selected areas (= closed {@link Way}s) into separate areas.
 * Their border line is any Way, which has no key-value pairs and which
 * shares first and last node with the splitted area.
 *
 * WARNING: The current implementation does not handle relations. If the
 * original area is a member of some relation, this action rejects to
 * preform the split.
 *
 * @author Radomír Černoch, radomir.cernoch@gmail.com
 */
public class SplitAreaByEmptyWayAction extends JosmAction {

    /**
     * Defaults constructor, which registers itself into the JOSM menu.
     */
    public SplitAreaByEmptyWayAction() {
        super(tr("Split area"),
              "splitarea.png",
              tr("Splits an area by an untagged way."),
              Shortcut.registerShortcut("tools:splitarea",
                    tr("Tool: {0}", tr("Split area")),
                    KeyEvent.VK_S, Shortcut.GROUP_MENU),
              true);
    }

    /**
     * Goes over all selected areas (=closed Ways) and splits them into 2
     * by any untagged way, which starts and ends at the border line of that
     * area.
     */
    public void actionPerformed(ActionEvent e) {

        Collection<Way> selectedWays = Main.main.getCurrentDataSet().getSelectedWays();
        Collection<Way> newSelection = new LinkedList<Way>(Main.main.getCurrentDataSet().getSelectedWays());

        for (Way area : selectedWays) {
            if (! area.isClosed()) continue;

            for (OsmPrimitive prim2 : Main.main.getCurrentDataSet().allNonDeletedPrimitives()) {
                if (!(prim2 instanceof Way)) continue;
                if (prim2.equals(area))      continue;
                Way border = (Way) prim2;
                if (border.getNodes().size() == 0)   continue;
                if (border.keySet().size() > 0) continue;
                if (!area.getNodes().contains(border.firstNode())) continue;
                if (!area.getNodes().contains(border.lastNode()))  continue;

                Way newArea1 = new Way();
                Way newArea2 = new Way();

                int errorCode = splitArea(area, border, newArea1, newArea2);

                if (errorCode == 2) {
                    JOptionPane.showMessageDialog(Main.parent,
                        tr("The selected area cannot be splitted, because it is a member of some relation.\n"+
                            "Remove the area from the relation before splitting it."));
                    break;
                }

                if (errorCode == 0) {
                    Main.main.getCurrentDataSet().addPrimitive(newArea1);
                    Main.main.getCurrentDataSet().addPrimitive(newArea2);

                    area.setDeleted(true);
                    border.setDeleted(true);
                    newSelection.remove(area);
                    newSelection.remove(border);

                    newSelection.add(newArea1);
                    newSelection.add(newArea2);

                    break;
                }
            }
        }

        Main.main.getCurrentDataSet().setSelected(newSelection);
    }

    /**
     * Splits a given area into 2 areas. newArea1 and newArea2 must be
     * referneces to already existing areas.
     *
     * @param area the original area
     * @param border border line, which goes across the area
     * @param newArea1 reference to the first new area
     * @param newArea2 reference to the second new area
     * @return
     */
    private int splitArea(Way area, Way border, Way newArea1, Way newArea2) {

        for (Relation r : Main.main.getCurrentDataSet().getRelations())
            for (RelationMember rm : r.getMembers())
                if (rm.refersTo(area) || rm.refersTo(border))
                    return 2;

        List<Node> bordNodes = border.getNodes();
        List<Node> areaNodes = area.getNodes();

        int index1 = areaNodes.indexOf(bordNodes.get(0));
        int index2 = areaNodes.indexOf(bordNodes.get(bordNodes.size()-1));
        if (index1 == index2)
            return 1;

        if (index1 > index2) {
            Collections.reverse(areaNodes);
            index1 = areaNodes.indexOf(bordNodes.get(0));
            index2 = areaNodes.indexOf(bordNodes.get(bordNodes.size()-1));
        }

        for (String key : area.keySet()) {
            newArea1.put(key, area.get(key));
            newArea2.put(key, area.get(key));
        }

        List<Node> newNodeList1 = newArea1.getNodes();
        List<Node> newNodeList2 = newArea1.getNodes();

        newNodeList1.addAll(areaNodes.subList(0, index1));
        newNodeList1.addAll(bordNodes);
        newNodeList1.addAll(areaNodes.subList(index2 + 1, areaNodes.size()));

        Collections.reverse(bordNodes);
        newNodeList2.addAll(areaNodes.subList(index1, index2));
        newNodeList2.addAll(bordNodes);

        newArea1.setNodes(newNodeList1);
        newArea2.setNodes(newNodeList2);

        return 0;
    }
}
