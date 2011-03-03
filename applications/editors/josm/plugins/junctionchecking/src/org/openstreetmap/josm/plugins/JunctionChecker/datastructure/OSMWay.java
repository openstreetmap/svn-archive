package org.openstreetmap.josm.plugins.JunctionChecker.datastructure;

import java.util.Vector;

/**
 * @author joerg
 */
public class OSMWay extends OSMEntity {

	private Vector<OSMNode> nodes = new Vector<OSMNode>();

	public OSMNode[] getNodes() {
		OSMNode[] nodearray = new OSMNode[nodes.size()];
		return (OSMNode[]) nodes.toArray(nodearray);
	}

	public OSMNode getToNode() {
		return nodes.lastElement();
	}

	public OSMNode getFromNode() {
		return nodes.firstElement();
	}

	public void addNode(OSMNode node) {
		nodes.add(node);
	}

	public String tosString() {
		System.out.println(this.getId());
		return this.valuestoString() + "fromNodeID: "
				+ this.getFromNode().getId() + "\ntoNodeID: "
				+ this.getToNode().getId();
	}

	/**
	 * ist der OSMWay eine Einbahnstraße?
	 * 
	 * @return true wenn ja, sonst nein
	 */
	public boolean isOneWay() {
		// TODO Tippfehler berücksichtigen
		// evtl. doch über ein XML-File konfigurieren?
		if (this.hasKey("oneway")) {
			String t = this.getValue("oneway");
			if (t.equals("1") || t.equals("true") || t.equals("yes")) {
				return true;
			}
		}
		String t = this.getValue("highway");
		if (t != null) {
			if (t.equals("motorway") || t.equals("motorway_link")) {
				return true;
			}

			else {
				return false;
			}
		} else
			return false;
	}
}
