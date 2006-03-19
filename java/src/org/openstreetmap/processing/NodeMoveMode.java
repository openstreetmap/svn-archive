/**
 * 
 */
package org.openstreetmap.processing;

import java.util.Iterator;

import org.openstreetmap.util.Node;

/**
 * Edit mode to move a node.
 */
public class NodeMoveMode extends EditMode {
	/**
	 * Back reference to the applet
	 */
	private final OsmApplet applet;

	/**
	 * Offset between mouse position and the nodes x/y at the time, the node started
	 * to move.
	 */
	private float lastOffsetX = 0.0f, lastOffsetY = 0.0f;
	
	/**
	 * Original position of the node
	 */
	private float origX, origY;

	public NodeMoveMode(OsmApplet applet) {
		this.applet = applet;
	}

	public void mousePressed() {
		for (Iterator it = applet.nodes.values().iterator(); it.hasNext();) {
			Node p = (Node)it.next();
			if (applet.mouseOverPoint(p.coor)) {
				applet.selectedNode = p;
				OsmApplet.println("selected: " + p);
				lastOffsetX = p.coor.x - applet.mouseX;
				lastOffsetY = p.coor.y - applet.mouseY;
				origX = p.coor.x;
				origY = p.coor.y;
				break;
			}
		}
		OsmApplet.println("selected: " + applet.selectedNode);
	}

	public void mouseDragged() {
		if (applet.selectedNode != null) {
			applet.selectedNode.coor.x = applet.mouseX + lastOffsetX;
			applet.selectedNode.coor.y = applet.mouseY + lastOffsetY;
			// println("node moved:" + selectedNode.x + " " +
			// selectedNode.y);
		} else {
			OsmApplet.println("no selectedNode");
		}
	}

	public void mouseReleased() {
		unset();
	}

	public void draw() {
		applet.stroke(0);
		applet.noFill();
		applet.line(applet.buttonWidth / 2.0f, applet.buttonHeight * 0.2f, applet.buttonWidth / 2.0f,
				applet.buttonHeight * 0.8f);
		applet.line(applet.buttonWidth * 0.2f, applet.buttonHeight / 2.0f, applet.buttonWidth * 0.8f,
				applet.buttonHeight / 2.0f);
	}

	public void unset() {
		if (applet.selectedNode != null) {
			double origLat = applet.selectedNode.coor.lat;
			double origLon = applet.selectedNode.coor.lon;
			applet.selectedNode.coor.unproject(applet.tiles);
			double newLat = applet.selectedNode.coor.lat;
			double newLon = applet.selectedNode.coor.lon;
			float newX = applet.selectedNode.coor.x;
			float newY = applet.selectedNode.coor.y;
			applet.selectedNode.coor.lat = origLat;
			applet.selectedNode.coor.lon = origLon;
			applet.selectedNode.coor.x = origX;
			applet.selectedNode.coor.y = origY;
			applet.osm.moveNode(applet.selectedNode, newLat, newLon, newX, newY);
			applet.selectedNode = null;
		} else {
			OsmApplet.println("no selectedNode on mouse release");
		}
	}

	public String getDescription() {
		return "Drag to move nodes";
	}
}