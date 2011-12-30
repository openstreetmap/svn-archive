/**
 * 
 */
package com.tilusnet.josm.plugins.alignways;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.tools.Shortcut;

import com.tilusnet.josm.plugins.alignways.AlignWaysDialog.AligningModeOption;

/**
 * @author tilusnet <tilusnet@gmail.com>
 * 
 */
public class AlignWaysAction extends JosmAction {

    private static final long serialVersionUID = -1540319652562985458L;

    public AlignWaysAction() {
        super(
                tr("Align Way Segments"),
                "alignways",
                tr("Makes a pair of selected way segments parallel by rotating one of them "
                        + "around a chosen pivot."), Shortcut.registerShortcut(
                                "tools:alignways", tr("Tool: {0}", tr("Align Ways")),
                                KeyEvent.VK_A, Shortcut.GROUP_EDIT,
                                KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK), true);
        setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isEnabled())
            return;
        if (getCurrentDataSet() == null)
            return;

        Collection<Node> affectableNodes = AlignWaysSegmentMgr.getInstance(
                Main.map.mapView).getSelectedNodes();

        // c is the last command launched, if any
        Command c = !Main.main.undoRedo.commands.isEmpty() ? Main.main.undoRedo.commands
                .getLast() : null;

                // Potentially add my type of command only if last command wasn't my type
                // or, if it was, the rotated nodes were not the same as now
                if (!(c instanceof AlignWaysCmdKeepLength && affectableNodes
                        .equals(((AlignWaysCmdKeepLength) c).getPrevAffectedNodes()))) {

                    AlignWaysCmdKeepLength cmdAW;
                    if (AlignWaysPlugin.awDialog.getAwOpt() == AligningModeOption.ALGN_OPT_KEEP_ANGLE) {
                        cmdAW = new AlignWaysCmdKeepAngles();
                    } else {
                        cmdAW = new AlignWaysCmdKeepLength();
                    }

                    if (cmdAW.executable()) {
                        // This will also trigger AlignWaysCmdKeepLength.executeCommand()
                        Main.main.undoRedo.add(cmdAW);
                    }
                }

                Main.map.mapView.repaint();

                return;
    }

}
