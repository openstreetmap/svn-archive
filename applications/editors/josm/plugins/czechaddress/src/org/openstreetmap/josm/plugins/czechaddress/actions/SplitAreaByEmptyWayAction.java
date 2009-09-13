package org.openstreetmap.josm.plugins.czechaddress.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Collections;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
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

        Collection<OsmPrimitive> selectedWays = Main.main.getCurrentDataSet().getSelectedWays();
        Collection<OsmPrimitive> newSelection = Main.main.getCurrentDataSet().getSelected();

        for (OsmPrimitive prim : selectedWays) {
            if (!((Way) prim).isClosed()) continue;
                Way area = (Way) prim;

            for (OsmPrimitive prim2 : Main.main.getCurrentDataSet().allNonDeletedPrimitives()) {
                if (!(prim2 instanceof Way)) continue;
                if (prim2.equals(prim))      continue;
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

        Way tempBorder = new Way(border);

        int index1 = area.getNodes().indexOf(tempBorder.firstNode());
        int index2 = area.getNodes().indexOf(tempBorder.lastNode());
        if (index1 == index2)
            return 1;

        if (index1 > index2) {
            Collections.reverse(tempBorder.getNodes());
            index1 = area.getNodes().indexOf(tempBorder.firstNode());
            index2 = area.getNodes().indexOf(tempBorder.lastNode());
        }

        for (Relation relation : Main.main.getCurrentDataSet().relations)
            for (RelationMember areaMember : relation.getMembers())
                if (area.equals(areaMember.getMember()))
                    return 2;

        for (String key : area.keySet()) {
            newArea1.put(key, area.get(key));
            newArea2.put(key, area.get(key));
        }

        newArea1.getNodes().addAll(area.getNodes().subList(0, index1));
        newArea1.getNodes().addAll(tempBorder.getNodes());
        newArea1.getNodes().addAll(area.getNodes().subList(index2, area.getNodes().size()-1));
        newArea1.getNodes().add(area.getNodes().get(0));

        Collections.reverse(tempBorder.getNodes());
        newArea2.getNodes().addAll(area.getNodes().subList(index1, index2));
        newArea2.getNodes().addAll(tempBorder.getNodes());
        newArea2.getNodes().add(area.getNodes().get(index1));

        removeDuplicateNodesFromWay(newArea1);
        removeDuplicateNodesFromWay(newArea2);

        return 0;
    }

    /**
     * Removes all consequent nodes, which are on the same way.
     */
    void removeDuplicateNodesFromWay(Way w) {
        int i=0;
        while (i<w.getNodes().size()-1) {
            if (w.getNodes().get(i).equals(w.getNodes().get(i+1)))
                w.getNodes().remove(i);
            else
                i++;
        }
    }
}
