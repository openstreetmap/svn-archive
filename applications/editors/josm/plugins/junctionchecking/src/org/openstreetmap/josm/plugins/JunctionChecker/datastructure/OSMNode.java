package org.openstreetmap.josm.plugins.JunctionChecker.datastructure;

import java.util.ArrayList;


/**
 * @author  joerg
 */
public class OSMNode extends OSMEntity {
	
	private double latitude;
	private double longitude;
	private ArrayList<Channel> outgoingChannels = new ArrayList<Channel>();
	private ArrayList<OSMNode> succNodeList = new ArrayList<OSMNode>();
	private ArrayList<OSMNode> predNodeList = new ArrayList<OSMNode>();
	
	public void addOutgoingChannel(Channel channel) {
		outgoingChannels.add(channel);
	}
	
	public ArrayList<Channel> getOutgoingChannels() {
		return outgoingChannels;
	}
	
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	public String toString(){
		return valuestoString() + "Lat: " + latitude + "\n" + "Lon: " + longitude;
	}

	public ArrayList<OSMNode> getSuccNodeList() {
		return succNodeList;
	}

	public ArrayList<OSMNode> getPredNodeList() {
		return predNodeList;
	}
	
	public void addSuccNode(OSMNode node) {
		succNodeList.add(node);
	}
	
	public void addPredNode(OSMNode node) {
		predNodeList.add(node);
	}
	
	
	
}
