// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses.gui.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.List;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.fixAddresses.AddressEditContainer;
import org.openstreetmap.josm.plugins.fixAddresses.GuessAddressRunnable;
import org.openstreetmap.josm.plugins.fixAddresses.IProgressMonitorFinishedListener;
import org.openstreetmap.josm.plugins.fixAddresses.OSMAddress;
import org.openstreetmap.josm.plugins.fixAddresses.gui.AddressEditSelectionEvent;

/**
 * Guesses address tags by picking the closest street node with a name. The same is done
 * with city, post code, state,... However, I strongly encourage you to check the result.
 *
 * @author Oliver Wieland &lt;oliver.wieland@online.de>
 */
@SuppressWarnings("serial")
public class GuessAddressDataAction extends AbstractAddressEditAction implements IProgressMonitorFinishedListener {

    /**
     * Instantiates a new "guess address data" action.
     */
    public GuessAddressDataAction() {
        super(tr("Guess"), "guessstreets_24", tr("Tries to guess address data by picking the name of the closest object with according tag."),
            "fixaddresses/guessaddressdata");
    }

    @Override
    public void updateEnabledState(AddressEditSelectionEvent ev) {
        setEnabled(ev != null && ev.hasAddresses());
    }

    @Override
    protected void updateEnabledState(AddressEditContainer container) {
        setEnabled(container != null && container.getNumberOfInvalidAddresses() > 0);
    }

    @Override
    public void addressEditActionPerformed(AddressEditContainer container) {
        if (container == null || container.getNumberOfInvalidAddresses() == 0) return;

        internalGuessAddresses(container.getAllAddressesToFix());
    }

    @Override
    public void addressEditActionPerformed(AddressEditSelectionEvent ev) {
        if (ev == null || !ev.hasAddresses()) return;

        // guess tags for selected addresses only
        internalGuessAddresses(ev.getSelectedIncompleteAddresses());
        internalGuessAddresses(ev.getSelectedUnresolvedAddresses());
    }

    /**
     * Internal method to start several threads guessing tag values for the given list of addresses.
     * @param nodes list of OSM addresses
     */
    private void internalGuessAddresses(List<OSMAddress> nodes) {
        if (nodes == null)
            return;

        // Launch address guessing thread
        GuessAddressRunnable aft = new GuessAddressRunnable(nodes, tr("Guessing address values"));
        aft.addFinishListener(this);
        MainApplication.worker.submit(aft);
    }

    @Override
    public void finished() {
        if (container != null) {
            container.invalidate();
        }
    }
}
