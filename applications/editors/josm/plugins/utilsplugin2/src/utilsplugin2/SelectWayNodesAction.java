// License: GPL. Copyright 2010 by Hanno Hecker
package utilsplugin2;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trn;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Select all nodes of a selected way.
 *
 */

public class SelectWayNodesAction extends JosmAction {

    private Node selectedNode;
    private ArrayList<Node> selectedNodes;

    /**
     * Create a new SelectWayNodesAction
     */
    public SelectWayNodesAction() {
        super(tr("Select Way Nodes"), null, tr("Select all nodes of a selected way."),
                Shortcut.registerShortcut("tools:selectwaynodes", tr("Tool: {0}", tr("Select Way Nodes")), KeyEvent.VK_N, Shortcut.GROUP_EDIT), true);
        putValue("help", ht("/Action/SelectWayNodes"));
    }

    /**
     * Called when the action is executed.
     *
     * This method does some checking on the selection and calls the matching selectWayNodes method.
     */
    public void actionPerformed(ActionEvent e) {
        Collection<OsmPrimitive> selection = getCurrentDataSet().getSelected();

        String errMsg = null;
        for (OsmPrimitive p : selection) {
            if (p instanceof Way) {
                Way w = (Way) p;
                if (!w.isUsable() || w.getNodesCount() < 1) {
                    continue;
                }
                selectWayNodes(w);
            }
            else if (p instanceof Node) {
                Node n = (Node) p;
                if (selectedNodes == null) {
                    selectedNodes = new ArrayList<Node>();
                }
                selectedNodes.add(n);
            }
        }
            
        getCurrentDataSet().setSelected(selectedNodes);
        selectedNodes = null;
    }

    private void selectWayNodes(Way w) {
        
        for (Node n : w.getNodes()) {
            if (selectedNodes == null) {
                selectedNodes = new ArrayList<Node>();
            }
            selectedNodes.add(n);
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
        setEnabled(selection != null && !selection.isEmpty());
    }
}

