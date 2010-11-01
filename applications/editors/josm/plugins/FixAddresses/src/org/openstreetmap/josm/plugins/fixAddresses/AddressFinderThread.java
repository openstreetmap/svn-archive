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

public class AddressFinderThread extends PleaseWaitRunnable implements Visitor {
	private List<AddressNode> addressesToGuess;
	private double minDist;
	private AddressNode curAddressNode;
	private boolean isRunning = false;
	private String nearestName = null;
	private String currentName = null;
	private boolean cancelled;
	
	/**
	 * @param nodes
	 */
	public AddressFinderThread(List<AddressNode> nodes, String title) {
		super(title != null ? title : tr("Searching"));
		setAddressEditContainer(nodes);		
	}

	public void setAddressEditContainer(List<AddressNode> nodes) {
		if (isRunning) {
			throw new ConcurrentModificationException();
		}
		this.addressesToGuess = nodes;		
	}

	public List<AddressNode> getAddressEditContainer() {
		return addressesToGuess;
	}
	/**
	 * @return the isRunning
	 */
	public boolean isRunning() {
		return isRunning;
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
			nearestName = currentName;
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
		
		currentName = TagUtils.getNameValue(w);
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
		cancelled = true;		
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
		cancelled = false;
		
		progressMonitor.subTask(tr("Searching") + "...");
		
		try {
			progressMonitor.setTicksCount(addressesToGuess.size());
			
			List<AddressNode> shadowCopy = new ArrayList<AddressNode>(addressesToGuess);
			for (AddressNode aNode : shadowCopy) {					
				minDist = Double.MAX_VALUE;
				curAddressNode = aNode;
				
				if (aNode.hasStreetName()) {
					progressMonitor.worked(1);
					continue;
				}
				
				// check for cancel
				if (cancelled) {
					break;
				}

				// visit osm data
				for (OsmPrimitive osmPrimitive : Main.main.getCurrentDataSet().getWays()) {
					if (cancelled) {
						break;
					}
					osmPrimitive.visit(this);

				}
				
				// we found something
				if (nearestName != null) {
					progressMonitor.subTask(String.format("%s: %s (%4.1f m)", tr("Guess"), nearestName, minDist));
					aNode.setGuessedStreetName(nearestName);
					nearestName = null;
				} else {
					System.out.println("Did not find a street for " + aNode);
				}
				// report progress
				progressMonitor.worked(1);				
			}
		} finally {
			isRunning = false;
		}
	}
}
