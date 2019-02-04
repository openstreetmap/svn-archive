// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.io.OsmTransferException;
import org.xml.sax.SAXException;

/**
 * The class GuessAddressRunnable scans the data set to find the best guess for a missing address field.
 *
 * The guessing procedure itself is implemented by defining "guessers" using the {@link GuessedValueHandler}
 * class. A guessed field does not modify the corresponding property of {@link OSMAddress} itself, but
 * adds the guessed value to a shadowed field by calling {@link OSMAddress#setGuessedValue}.
 */
public class GuessAddressRunnable extends PleaseWaitRunnable {
    private List<OSMAddress> addressesToGuess;
    private List<IProgressMonitorFinishedListener> finishListeners = new ArrayList<>();
    private boolean isRunning = false;
    private boolean canceled;

    private GuessedValueHandler[] wayGuessers = new GuessedValueHandler[]{new GuessStreetValueHandler(TagConstants.ADDR_STREET_TAG)};
    private GuessedValueHandler[] nodeGuessers = new GuessedValueHandler[]{
            new GuessedValueHandler(TagConstants.ADDR_POSTCODE_TAG, 500.0),
            new GuessedValueHandler(TagConstants.ADDR_CITY_TAG, 5000.0),
            new GuessedValueHandler(TagConstants.ADDR_STATE_TAG, 5000.0),
            new GuessedValueHandler(TagConstants.ADDR_COUNTRY_TAG, 5000.0),
            new GuessedValueHandler(TagConstants.ADDR_CITY_TAG, 2000.0)
    };

    /**
     * Instantiates a new guess address runnable.
     *
     * @param addresses the addresses to guess the values for
     * @param title the title of progress monitor
     */
    public GuessAddressRunnable(List<OSMAddress> addresses, String title) {
        super(title != null ? title : tr("Searching"));
        setAddressEditContainer(addresses);
    }

    /**
     * Sets the address edit container.
     *
     * @param nodes the new address edit container
     */
    public void setAddressEditContainer(List<OSMAddress> nodes) {
        if (isRunning) {
            throw new ConcurrentModificationException();
        }
        this.addressesToGuess = nodes;
    }

    /**
     * Gets the addresses to guess.
     *
     * @return the addresses to guess
     */
    public List<OSMAddress> getAddressesToGuess() {
        return addressesToGuess;
    }

    /**
     * @return the isRunning
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Adds a finish listener.
     *
     * @param l the listener to add
     */
    public void addFinishListener(IProgressMonitorFinishedListener l) {
        finishListeners.add(l);
    }

    /**
     * Removes a finish listener.
     *
     * @param l the listener to remove
     */
    public void removeFinishListener(IProgressMonitorFinishedListener l) {
        finishListeners.remove(l);
    }

    /**
     * Fires the 'finished' event after the thread has done his work.
     */
    protected void fireFinished() {
        for (IProgressMonitorFinishedListener l : finishListeners) {
            l.finished();
        }
        // this event is fired only once, then we disconnect all listeners
        finishListeners.clear();
    }

    @Override
    protected void cancel() {
        canceled = true;
    }

    @Override
    protected void finish() {
        // nothing to do yet
    }

    @Override
    protected void realRun() throws SAXException, IOException,
    OsmTransferException {

        DataSet ds = MainApplication.getLayerManager().getEditDataSet();
        if (ds == null || addressesToGuess == null) return;

        isRunning = true;
        canceled = false;

        // Start progress monitor to guess address values
        progressMonitor.subTask(tr("Searching") + "...");

        try {
            progressMonitor.setTicksCount(addressesToGuess.size());

            List<OSMAddress> shadowCopy = new ArrayList<>(addressesToGuess);
            for (OSMAddress aNode : shadowCopy) {
                if (!aNode.needsGuess()) { // nothing to do
                    progressMonitor.worked(1);
                    continue;
                }

                // check for cancel
                if (canceled) {
                    break;
                }

                // Update progress monitor
                progressMonitor.subTask(tr("Guess values for ") + aNode);

                // Run way-related guessers
                for (int i = 0; i < wayGuessers.length; i++) {
                    GuessedValueHandler guesser = wayGuessers[i];

                    guesser.setAddressNode(aNode);

                    // visit osm data
                    for (Way way : ds.getWays()) {
                        if (canceled) {
                            break;
                        }
                        way.accept(guesser);
                    }

                    String guessedVal = guesser.getCurrentValue();
                    if (guessedVal != null) {
                        aNode.setGuessedValue(guesser.getTag(), guessedVal, guesser.getSourceNode());
                    }
                }

                // Run node-related guessers
                for (int i = 0; i < nodeGuessers.length; i++) {
                    GuessedValueHandler guesser = nodeGuessers[i];

                    guesser.setAddressNode(aNode);

                    // visit osm data
                    for (Node node : ds.getNodes()) {
                        if (canceled) {
                            break;
                        }
                        node.accept(guesser);
                    }

                    String guessedVal = guesser.getCurrentValue();
                    if (guessedVal != null) {
                        aNode.setGuessedValue(guesser.getTag(), guessedVal, guesser.getSourceNode());
                    }
                }

                // report progress
                progressMonitor.worked(1);
            }
        } finally {
            isRunning = false;
            fireFinished();
        }
    }

    private static class GuessStreetValueHandler extends GuessedValueHandler {
        GuessStreetValueHandler(String tag) {
            this(tag, null);
        }

        GuessStreetValueHandler(String tag, OSMAddress aNode) {
            super(tag, aNode, 200.0);
        }

        @Override
        public void visit(Node n) {
            // do nothing
        }

        @Override
        public void visit(Way w) {
            if (TagUtils.isStreetSupportingHousenumbers(w)) {
                OSMAddress aNode = getAddressNode();
                String newVal = TagUtils.getNameValue(w);

                if (newVal != null) {
                    double dist = OsmUtils.getMinimumDistanceToWay(aNode.getCoor(), w);

                    if (dist < minDist && dist < getMaxDistance()) {
                        minDist = dist;
                        currentValue = newVal;
                        srcNode = w;
                    }
                }
            }
        }
    }
}
