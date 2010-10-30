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

import java.util.ConcurrentModificationException;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Changeset;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.visitor.Visitor;

public class AddressFinderThread implements Runnable, Visitor {
	private AddressNode addressNode;
	private double minDist;
	private Node nearestNode;
	private Node osmAddressNode;
	private boolean isRunning = false;
	private String nearestName = null;
	private String currentName = null;
	
	/**
	 * @param addressNode
	 */
	public AddressFinderThread(AddressNode addressNode) {
		super();
		setAddressNode(addressNode);		
	}

	public AddressFinderThread() {
		this(null);
	}

	public void setAddressNode(AddressNode addressNode) {
		if (isRunning) {
			throw new ConcurrentModificationException();
		}
		this.addressNode = addressNode;
		if (addressNode != null && addressNode.getOsmObject() instanceof Node) {
			osmAddressNode = (Node) addressNode.getOsmObject();
		}
	}

	public AddressNode getAddressNode() {
		return addressNode;
	}
	
	public double getMinDist() {
		return minDist;
	}

	public Node getNearestNode() {
		return nearestNode;
	}

	/**
	 * @return the nearestName
	 */
	public String getGuessedName() {
		return nearestName;
	}

	/**
	 * @return the isRunning
	 */
	public boolean isRunning() {
		return isRunning;
	}

	@Override
	public void run() {
		if (Main.main.getCurrentDataSet() == null || osmAddressNode == null) return;

		isRunning = true;
		synchronized(this) {			
			try {
				minDist = Double.MAX_VALUE;
				for (OsmPrimitive osmPrimitive : Main.main.getCurrentDataSet().getWays()) {
					osmPrimitive.visit(this);
				}
				
				if (nearestName != null) {
					System.out.println("Picked " + nearestName + " with distance " + minDist + "m");
					addressNode.setGuessedStreetName(nearestName);
				}
			} finally {
				isRunning = false;
			}
		}
	}


	@Override
	public void visit(Node n) {
		double dist = osmAddressNode.getCoor().greatCircleDistance(n.getCoor());
		
		if (dist < minDist) {
			minDist = dist;
			nearestNode = n;
			nearestName = currentName;
		}
	}


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


	@Override
	public void visit(Relation e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(Changeset cs) {
		// TODO Auto-generated method stub
		
	}

}
