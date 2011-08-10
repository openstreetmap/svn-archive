/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.openstreetmap.josm.plugins.fixAddresses;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Changeset;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.visitor.Visitor;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.io.OsmTransferException;
import org.xml.sax.SAXException;

/**
 * The class GuessAddressRunnable scans the data set to find the best guess for a missing address field.
 *
 * The guessing procedure itself is implemented by defining "guessers" using the {@link GuessedValueHandler}
 * class. A guessed field does not modify the corresponding property of {@link OSMAddress} itself, but
 * adds the guessed value to a shadowed field by calling {@link OSMAddress#setGuessedValue(String, String)}.
 */
public class GuessAddressRunnable extends PleaseWaitRunnable implements Visitor {
	private List<OSMAddress> addressesToGuess;
	private List<IProgressMonitorFinishedListener> finishListeners = new ArrayList<IProgressMonitorFinishedListener>();
	private double minDist;
	private OSMAddress curAddressNode;
	private boolean isRunning = false;
	private boolean canceled;


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

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.data.osm.visitor.Visitor#visit(org.openstreetmap.josm.data.osm.Node)
	 */
	@Override
	public void visit(Node n) {
		if (n == null) return;
		if (curAddressNode == null) return;

		// If the coordinates are null, we are screwed anyway
		LatLon ll = curAddressNode.getCoor();
		if (ll == null) return;

		double dist = ll.greatCircleDistance(n.getCoor());

		if (dist < minDist) {
			minDist = dist;
		}
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.data.osm.visitor.Visitor#visit(org.openstreetmap.josm.data.osm.Way)
	 */
	@Override
	public void visit(Way w) {
		// skip non-streets and streets without name
		if (!TagUtils.hasHighwayTag(w)) return;
		if (!TagUtils.hasNameTag(w)) return;

		for (Node node : w.getNodes()) {
			visit(node);
		}
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.data.osm.visitor.Visitor#visit(org.openstreetmap.josm.data.osm.Relation)
	 */
	@Override
	public void visit(Relation e) {
		// nothing to do yet
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.data.osm.visitor.Visitor#visit(org.openstreetmap.josm.data.osm.Changeset)
	 */
	@Override
	public void visit(Changeset cs) {
		// nothing to do yet
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.gui.PleaseWaitRunnable#cancel()
	 */
	@Override
	protected void cancel() {
		canceled = true;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.gui.PleaseWaitRunnable#finish()
	 */
	@Override
	protected void finish() {
		// nothing to do yet
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.gui.PleaseWaitRunnable#realRun()
	 */
	@Override
	protected void realRun() throws SAXException, IOException,
			OsmTransferException {
		if (Main.main.getCurrentDataSet() == null || addressesToGuess == null) return;

		isRunning = true;
		canceled = false;

		// Start progress monitor to guess address values
		progressMonitor.subTask(tr("Searching") + "...");

		try {
			progressMonitor.setTicksCount(addressesToGuess.size());



			List<OSMAddress> shadowCopy = new ArrayList<OSMAddress>(addressesToGuess);
			for (OSMAddress aNode : shadowCopy) {
				minDist = Double.MAX_VALUE;
				curAddressNode = aNode;

				// setup guessing handlers for address tags
				GuessedValueHandler[] guessers = new GuessedValueHandler[]{
						new GuessStreetValueHandler(TagUtils.ADDR_STREET_TAG, aNode),
						new GuessedValueHandler(TagUtils.ADDR_POSTCODE_TAG, aNode, 500.0),
						new GuessedValueHandler(TagUtils.ADDR_CITY_TAG, aNode, 5000.0),
						new GuessedValueHandler(TagUtils.ADDR_STATE_TAG, aNode, 5000.0),
						new GuessedValueHandler(TagUtils.ADDR_COUNTRY_TAG, aNode, 5000.0),
						new GuessedValueHandler(TagUtils.ADDR_CITY_TAG, aNode, 2000.0)
				};

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

				// visit osm data
				for (OsmPrimitive osmPrimitive : Main.main.getCurrentDataSet().allPrimitives()) {
					if (canceled) {
						break;
					}

					// guess values
					for (int i = 0; i < guessers.length; i++) {
						osmPrimitive.visit(guessers[i]);

						if (guessers[i].currentValue == null && i == 0) {
							//System.err.println("Guess #" + i + " failed for " + aNode);
						}
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

	private class GuessStreetValueHandler extends GuessedValueHandler {

		public GuessStreetValueHandler(String tag, OSMAddress aNode) {
			super(tag, aNode, 200.0);
		}

		/* (non-Javadoc)
		 * @see org.openstreetmap.josm.plugins.fixAddresses.GuessedValueHandler#visit(org.openstreetmap.josm.data.osm.Node)
		 */
		@Override
		public void visit(Node n) {
			// do nothing
		}

		/* (non-Javadoc)
		 * @see org.openstreetmap.josm.plugins.fixAddresses.GuessedValueHandler#visit(org.openstreetmap.josm.data.osm.Way)
		 */
		@Override
		public void visit(Way w) {
			if (TagUtils.isStreetSupportingHousenumbers(w)) {
				OSMAddress aNode = getAddressNode();
				double dist = OsmUtils.getMinimumDistanceToWay(aNode.getCoor(), w);

				if (dist < minDist && dist < getMaxDistance()) {
					//System.out.println(String.format("New guess %s: %4.2f m", TagUtils.getNameValue(w), dist));
					minDist = dist;
					currentValue = TagUtils.getNameValue(w);
					aNode.setGuessedValue(getTag(), currentValue, w);
				} else {
					//System.out.println(String.format("Skipped %s: %4.2f m", TagUtils.getNameValue(w), dist));
				}
			}
		}
	}
}
