// License: GPL. Copyright 2011 by Alexei Kasatkin and Martin Ždila
package utilsplugin2;

import java.awt.MouseInfo;
import java.awt.Point;
import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JOptionPane;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangeNodesCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.MoveCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.*;

import org.openstreetmap.josm.tools.Shortcut;

/**
 *    Unselects all nodes
 */
public class ExtractPointAction extends JosmAction {

    
    public ExtractPointAction() {
        super(tr("Extract node"), "extpoint",
                tr("Extracts node from a way"),
                Shortcut.registerShortcut("tools:extnode", tr("Tool: {0}","Extract node"),
                KeyEvent.VK_J, Shortcut.GROUP_MNEMONIC,KeyEvent.ALT_MASK  ), true);
        putValue("help", ht("/Action/ExtractNode"));
    }

    public void actionPerformed(ActionEvent e) {
        Collection<OsmPrimitive> selection = getCurrentDataSet().getSelected();
        List<Node> selectedNodes = OsmPrimitive.getFilteredList(selection, Node.class);
        if (selectedNodes.size()!=1) {
             JOptionPane.showMessageDialog(Main.parent,
                    tr("This tool extracts node from its ways and requires single node to be selected."),
                    tr("Extract node"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Node nd = selectedNodes.get(0);
        Node ndCopy = new Node(nd.getCoor());
        List<Command> cmds = new LinkedList<Command>();
        
        Point p = Main.map.mapView.getMousePosition();
        if (p!=null) cmds.add(new MoveCommand(nd,Main.map.mapView.getLatLon(p.x, p.y)));
        List<OsmPrimitive> refs = nd.getReferrers();
        cmds.add(new AddCommand(ndCopy));
        
        for (OsmPrimitive pr: refs) {
            if (pr instanceof Way) {
                Way w=(Way)pr;
                List<Node> nodes = w.getNodes();
                int idx=nodes.indexOf(nd);
                nodes.set(idx, ndCopy); // replace node with its copy
                cmds.add(new ChangeNodesCommand(w, nodes));
            }
        }
        if (cmds.size()>1) Main.main.undoRedo.add(new SequenceCommand(tr("Extract node from line"),cmds));
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
        setEnabled(selection.size()==1);
    }
}
