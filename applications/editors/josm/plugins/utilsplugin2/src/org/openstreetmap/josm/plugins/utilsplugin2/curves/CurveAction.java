// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.utilsplugin2.curves;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.tools.Shortcut;

// TODO: investigate splines

/**
 * Create a circle arc
 */
public class CurveAction extends JosmAction {

    private static final long serialVersionUID = 1L;

    public CurveAction() {
        super(tr("Circle arc"), "circlearc", tr("Create a circle arc"),
                Shortcut.registerShortcut("tools:createcurve", tr("Tool: {0}", tr("Create a circle arc")), KeyEvent.VK_C,
                        Shortcut.SHIFT), true);
        putValue("help", ht("/Action/CreateCircleArc"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isEnabled())
            return;

        List<Node> selectedNodes = new ArrayList<>(getLayerManager().getEditDataSet().getSelectedNodes());
        List<Way> selectedWays = new ArrayList<>(getLayerManager().getEditDataSet().getSelectedWays());

        Collection<Command> cmds = CircleArcMaker.doCircleArc(selectedNodes, selectedWays);
        if (cmds == null || cmds.isEmpty()) {
            new Notification(tr("Could not use selection to create a curve")).setIcon(JOptionPane.WARNING_MESSAGE).show();
        } else {
            UndoRedoHandler.getInstance().add(new SequenceCommand("Create a curve", cmds));
        }
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
