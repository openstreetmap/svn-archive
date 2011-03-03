package org.openstreetmap.josm.plugins.JunctionChecker.datastructure;

/**
 * @author  joerg
 */
public class Graph {
	
	//Boundingbox
	private double bbleft;
	private double bbright;
	private double bbbottom;
	private double bbtop;
	
	
	public double getBbleft() {
		return bbleft;
	}
	public void setBbleft(double bbleft) {
		this.bbleft = bbleft;
	}
	public double getBbright() {
		return bbright;
	}
	public void setBbright(double bbright) {
		this.bbright = bbright;
	}
	public double getBbbottom() {
		return bbbottom;
	}
	public void setBbbottom(double bbbottom) {
		this.bbbottom = bbbottom;
	}
	public double getBbtop() {
		return bbtop;
	}
	public void setBbtop(double bbtop) {
		this.bbtop = bbtop;
	}
	public boolean isInBBox(OSMNode node) {
		if ( (node.getLatitude() <= bbtop) && (node.getLatitude() >= bbbottom)
				&& (node.getLongitude() >= bbleft) && (node.getLongitude() <=bbright)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	
}
