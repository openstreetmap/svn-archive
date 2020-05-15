// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.alignways;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.alignways.AlignWaysDialog.AligningModeOption;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Makes a pair of selected way segments parallel by rotating one of them around a chosen pivot.
 * @author tilusnet &lt;tilusnet@gmail.com&gt;
 */
public class AlignWaysAction extends JosmAction {

    private static final long serialVersionUID = -1540319652562985458L;

    public AlignWaysAction() {
        super(
                tr("Align Way Segments"),
                "alignways",
                tr("Makes a pair of selected way segments parallel by rotating one of them "
                        + "around a chosen pivot."),
                Shortcut.registerShortcut("tools:alignways", tr("Tool: {0}", tr("Align Ways")),
                                KeyEvent.VK_SPACE, Shortcut.SHIFT),
                true);
        setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isEnabled())
            return;
        DataSet ds = getLayerManager().getEditDataSet();
        if (ds == null)
            return;

        Collection<Node> affectableNodes = AlignWaysSegmentMgr.getInstance(MainApplication.getMap().mapView)
        		.getSelectedNodes();

        // c is the last command launched, if any
        Command c = UndoRedoHandler.getInstance().getLastCommand();

        // Potentially add my type of command only if last command wasn't my type
        // or, if it was, the rotated nodes were not the same as now
        if (!(c instanceof AlignWaysCmdKeepLength
                && affectableNodes.equals(((AlignWaysCmdKeepLength) c).getPrevAffectedNodes()))) {

            AlignWaysCmdKeepLength cmdAW;
            if (AlignWaysPlugin.getAwDialog().getAwOpt() == AligningModeOption.ALGN_OPT_KEEP_ANGLE) {
                cmdAW = new AlignWaysCmdKeepAngles(ds);
            } else {
                cmdAW = new AlignWaysCmdKeepLength(ds);
            }

            if (cmdAW.executable()) {
                // This will also trigger AlignWaysCmdKeepLength.executeCommand()
                UndoRedoHandler.getInstance().add(cmdAW);
            }
        }

        MainApplication.getMap().mapView.repaint();
    }
}
