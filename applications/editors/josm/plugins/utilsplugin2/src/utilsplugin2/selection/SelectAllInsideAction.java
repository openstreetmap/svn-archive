// License: GPL. Copyright 2011 by Alexei Kasatkin
package utilsplugin2.selection;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.tools.Shortcut;

/**
 *    Extends current selection by selecting nodes on all touched ways
 */
public class SelectAllInsideAction extends JosmAction {

    public SelectAllInsideAction() {
        super(tr("All inside [testing]"), "selinside", tr("Select all inside selected polygons"),
                Shortcut.registerShortcut("tools:selinside", tr("Tool: {0}","All inside"),
                KeyEvent.VK_I, Shortcut.GROUP_EDIT ,KeyEvent.ALT_DOWN_MASK|KeyEvent.SHIFT_DOWN_MASK), true);
        putValue("help", ht("/Action/SelectAllInside"));
    }

    public void actionPerformed(ActionEvent e) {
        long t=System.currentTimeMillis();
        Collection<OsmPrimitive> selection = getCurrentDataSet().getSelected();
        Set<Way> selectedWays = OsmPrimitive.getFilteredSet(getCurrentDataSet().getSelected(), Way.class);
        Set<Relation> selectedRels = OsmPrimitive.getFilteredSet(getCurrentDataSet().getSelected(), Relation.class);

        for (Relation r: selectedRels) {
            if (!r.isMultipolygon()) selectedRels.remove(r);
        }

        Set<Way> newWays = new HashSet<Way>();
        Set<Node> newNodes = new HashSet<Node>();
        // select ways attached to already selected ways
        if (!selectedWays.isEmpty()) {
            for (Way w: selectedWays) {
                NodeWayUtils.addAllInsideWay(getCurrentDataSet(),w,newWays,newNodes);
            }
        }
        if (!selectedRels.isEmpty()) {
            for (Relation r: selectedRels) {
                NodeWayUtils.addAllInsideMultipolygon(getCurrentDataSet(),r,newWays,newNodes);
            }
        }
        if (!newWays.isEmpty() || !newNodes.isEmpty()) {
            getCurrentDataSet().addSelected(newWays);
            getCurrentDataSet().addSelected(newNodes);
        } else{
        JOptionPane.showMessageDialog(Main.parent,
               tr("Nothing found. Please select some closed ways or multipolygons to find all primitives inside them!"),
               tr("Warning"), JOptionPane.WARNING_MESSAGE);
        }

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
        setEnabled(!selection.isEmpty());
    }


}
