// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses.gui.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;

import org.openstreetmap.josm.plugins.fixAddresses.AddressEditContainer;
import org.openstreetmap.josm.plugins.fixAddresses.IOSMEntity;
import org.openstreetmap.josm.plugins.fixAddresses.OSMAddress;
import org.openstreetmap.josm.plugins.fixAddresses.StringUtils;
import org.openstreetmap.josm.plugins.fixAddresses.gui.AddressEditSelectionEvent;
import org.openstreetmap.josm.plugins.fixAddresses.gui.AddressEditTableModel;

/**
 * Applies the guessed values for a set of addresses.
 * @author Oliver Wieland &lt;oliver.wieland@online.de>
 *
 */
@SuppressWarnings("serial")
public class ApplyAllGuessesAction extends AbstractAddressEditAction implements MouseListener {
    private String tag;
    /**
     * Instantiates a new "apply all guesses" action.
     * @param tag tag to analyze
     */
    public ApplyAllGuessesAction(String tag) {
        super(tr("Apply"), "applyguesses_24", tr("Turns all guesses into the corresponding tag values."),
            "fixaddresses/applyallguesses");
        this.tag = tag;
    }

    /**
     * Instantiates a new "apply all guesses" action.
     */
    public ApplyAllGuessesAction() {
        this(null);
    }

    @Override
    public void addressEditActionPerformed(AddressEditSelectionEvent ev) {
        if (ev == null) return;

        if (ev.getSelectedUnresolvedAddresses() != null) {
            List<OSMAddress> addrToFix = ev.getSelectedUnresolvedAddresses();
            applyGuesses(addrToFix);
        }

        if (ev.getSelectedIncompleteAddresses() != null) {
            List<OSMAddress> addrToFix = ev.getSelectedIncompleteAddresses();
            applyGuesses(addrToFix);
        }
    }

    @Override
    protected void updateEnabledState(AddressEditContainer container) {
        setEnabled(container != null && container.getNumberOfGuesses() > 0);
    }

    /**
     * Apply guesses.
     *
     * @param addrToFix the addr to fix
     */
    private void applyGuesses(List<OSMAddress> addrToFix) {
        beginTransaction(tr("Applied guessed values"));
        List<OSMAddress> addrToFixShadow = new ArrayList<>(addrToFix);
        for (OSMAddress aNode : addrToFixShadow) {
            beginObjectTransaction(aNode);

            if (StringUtils.isNullOrEmpty(tag)) { // tag given?
                aNode.applyAllGuesses(); // no -> apply all guesses
            } else { // apply guessed values for single tag only
                aNode.applyGuessForTag(tag);
            }
            finishObjectTransaction(aNode);
        }
        finishTransaction();
    }

    @Override
    protected void updateEnabledState(AddressEditSelectionEvent event) {
        setEnabled(event.hasAddressesWithGuesses());
    }

    @Override
    public void addressEditActionPerformed(AddressEditContainer container) {
        if (container == null || container.getNumberOfIncompleteAddresses() == 0) return;

        List<OSMAddress> addrToFix = container.getUnresolvedAddresses();
        applyGuesses(addrToFix);

        addrToFix = container.getIncompleteAddresses();
        applyGuesses(addrToFix);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        JTable table = (JTable) e.getSource();
        Point p = e.getPoint();
        if (e.getClickCount() == 2) {
            AddressEditTableModel model = (AddressEditTableModel) table.getModel();
            if (model != null) {
                int row = table.rowAtPoint(p);
                IOSMEntity node = model.getEntityOfRow(row);
                if (node instanceof OSMAddress) {
                    beginTransaction(tr("Applied guessed values for ") + node.getOsmObject());
                    beginObjectTransaction(node);
                    OSMAddress aNode = (OSMAddress) node;

                    aNode.applyAllGuesses();

                    finishObjectTransaction(node);
                    finishTransaction();
                }
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }
}
