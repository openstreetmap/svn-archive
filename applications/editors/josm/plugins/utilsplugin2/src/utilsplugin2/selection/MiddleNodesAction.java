// License: GPL. Copyright 2011 by Alexei Kasatkin and others
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
import org.openstreetmap.josm.data.osm.*;

import org.openstreetmap.josm.tools.Shortcut;

/**
 *    Selects nodes between two selected
 */
public class MiddleNodesAction extends JosmAction {

    public static final boolean treeMode = false;

    public MiddleNodesAction() {
        super(tr("Middle nodes"), "midnodes", tr("Select middle nodes"),
                Shortcut.registerShortcut("tools:midnodes", tr("Tool: {0}","Middle nodes"),
                KeyEvent.VK_E,  Shortcut.ALT_SHIFT), true);
        putValue("help", ht("/Action/MiddleNodes"));
    }

    private  Set<Way> activeWays = new HashSet<Way>();

    public void actionPerformed(ActionEvent e) {
        Collection<OsmPrimitive> selection = getCurrentDataSet().getSelected();
        Set<Node> selectedNodes = OsmPrimitive.getFilteredSet(selection, Node.class);

        Set<Way> selectedWays = OsmPrimitive.getFilteredSet(getCurrentDataSet().getSelected(), Way.class);
        
        // if no 2 nodes and no ways are selected, do nothing
        if (selectedNodes.size() != 2) {
            JOptionPane.showMessageDialog(Main.parent,
                    tr("Please select two nodes connected by way!"),
                    tr("Warning"),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Set<Node> newNodes = new HashSet <Node>();
        NodeWayUtils.addMiddle(selectedNodes, newNodes);
        
        // select only newly found nodes
        newNodes.removeAll(selectedNodes);
        getCurrentDataSet().addSelected(newNodes);
        newNodes = null;

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
