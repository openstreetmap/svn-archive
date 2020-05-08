// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.utilsplugin2.actions;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.MoveCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Mirror the selected ways nodes or ways along line given by two first selected points
 *
 * Note: If a ways are selected, their nodes are mirrored
 *
 * @author Alexei Kasatkin, based on much copy+paste from other MirrorAction :)
 */
public final class SymmetryAction extends JosmAction {

    /**
     * Constructs a new {@code SymmetryAction}.
     */
    public SymmetryAction() {
        super(tr("Symmetry"), "symmetry", tr("Mirror selected nodes and ways."),
                Shortcut.registerShortcut("tools:symmetry", tr("Tool: {0}", tr("Symmetry")),
                        KeyEvent.VK_S, Shortcut.ALT_SHIFT), true);
        putValue("help", ht("/Action/Symmetry"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Collection<OsmPrimitive> sel = getLayerManager().getEditDataSet().getSelected();
        HashSet<Node> nodes = new HashSet<>();
        EastNorth p1 = null, p2 = null;

        for (OsmPrimitive osm : sel) {
            if (osm instanceof Node) {
                if (p1 == null) p1 = ((Node) osm).getEastNorth(); else
                    if (p2 == null) p2 = ((Node) osm).getEastNorth(); else
                        nodes.add((Node) osm);
            }
        }
        for (OsmPrimitive osm : sel) {
            if (osm instanceof Way) {
                nodes.addAll(((Way) osm).getNodes());
            }
        }

        if (p1 == null || p2 == null || nodes.size() < 1) {
            new Notification(
                    tr("Please select at least two nodes for symmetry axis and something else to mirror.")
                    ).setIcon(JOptionPane.WARNING_MESSAGE).show();
            return;
        }

        double ne, nn, l, e0, n0;
        e0 = p1.east();
        n0 = p1.north();
        ne = -(p2.north() - p1.north());
        nn = (p2.east() - p1.east());
        l = Math.hypot(ne, nn);
        ne /= l; nn /= l; // normal unit vector

        Collection<Command> cmds = new LinkedList<>();

        for (Node n : nodes) {
            EastNorth c = n.getEastNorth();
            double pr = (c.east()-e0)*ne + (c.north()-n0)*nn;
            //pr=10;
            cmds.add(new MoveCommand(n, -2*ne*pr, -2*nn*pr));
        }

        UndoRedoHandler.getInstance().add(new SequenceCommand(tr("Symmetry"), cmds));
        MainApplication.getMap().repaint();
    }

    @Override
    protected void updateEnabledState() {
        updateEnabledStateOnCurrentSelection();
    }

    @Override
    protected void updateEnabledState(Collection<? extends OsmPrimitive> selection) {
        setEnabled(selection != null && !selection.isEmpty());
    }
}
