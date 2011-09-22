/**
 * 
 */
package org.openstreetmap.josm.plugins.alignways;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * @author tilusnet <tilusnet@gmail.com>
 * 
 */
public class AlignWaysAction extends JosmAction {

    /**
     * 
     */
    private static final long serialVersionUID = -1540319652562985458L;

    public AlignWaysAction() {
        super(tr("Align Way Segments"), "alignways",
                tr("Makes a pair of selected way segments parallel by rotating one of them " +
                "around a chosen pivot."),
                Shortcut.registerShortcut("tools:alignways", tr("Tool: {0}",
                        tr("Align Ways")), KeyEvent.VK_A, Shortcut.GROUP_EDIT, KeyEvent.CTRL_DOWN_MASK|KeyEvent.ALT_DOWN_MASK), true);
        setEnabled(false);
    }

    public void actionPerformed(ActionEvent e) {
        if (!isEnabled())
            return;
        if (getCurrentDataSet() == null)
            return;

        Collection<Node> affectedNodes = AlignWaysSegmentMgr.getInstance(Main.map.mapView).getSelectedNodes();

        Command c = !Main.main.undoRedo.commands.isEmpty() ? Main.main.undoRedo.commands
                .getLast()
                : null;

                if (!(c instanceof AlignWaysRotateCommand &&
                        affectedNodes.equals(((AlignWaysRotateCommand) c).getRotatedNodes()))) {
                    c = new AlignWaysRotateCommand();
                    if (actionValid((AlignWaysRotateCommand)c, affectedNodes)) {
                        Main.main.undoRedo.add(c);
                    }
                }

                Main.map.mapView.repaint();

                return;
    }


    /**
     * Validates the circumstances of the alignment (rotation) command to be executed.
     * @param c Command to be verified.
     * @param affectedNodes Nodes to be affected by the action.
     * @return true if the aligning action can be done, false otherwise.
     */
    private boolean actionValid(AlignWaysRotateCommand c, Collection<Node> affectedNodes) {
        // Deny action if reference and alignee segment cannot be aligned
        if (!c.areSegsAlignable()) {
            JOptionPane.showMessageDialog(Main.parent,
                    tr("Please select two segments that don''t share any nodes\n"
                            + " or put the pivot on their common node.\n"),
                            tr("AlignWayS: Alignment not possible"), JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // Deny action if the nodes would end up outside world
        for (Node n : affectedNodes) {
            if (n.getCoor().isOutSideWorld()) {
                // Revert move
                (c).undoCommand();
                JOptionPane.showMessageDialog(Main.parent,
                        tr("Aligning would result nodes outside the world.\n"),
                        tr("AlignWayS: Alignment not possible"), JOptionPane.WARNING_MESSAGE);
                return false;
            }

        }

        // Action valid
        return true;
    }

}
