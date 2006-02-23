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
			if (applet.mouseOverPoint(p)) {
				applet.selectedNode = p;
				OsmApplet.println("selected: " + p);
				lastOffsetX = p.x - applet.mouseX;
				lastOffsetY = p.y - applet.mouseY;
				origX = p.x;
				origY = p.y;
				break;
			}
		}
		OsmApplet.println("selected: " + applet.selectedNode);
	}

	public void mouseDragged() {
		if (applet.selectedNode != null) {
			applet.selectedNode.x = applet.mouseX + lastOffsetX;
			applet.selectedNode.y = applet.mouseY + lastOffsetY;
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
			double origLat = applet.selectedNode.lat;
			double origLon = applet.selectedNode.lon;
			applet.selectedNode.unproject(applet.tiles);
			double newLat = applet.selectedNode.lat;
			double newLon = applet.selectedNode.lon;
			float newX = applet.selectedNode.x;
			float newY = applet.selectedNode.y;
			applet.selectedNode.lat = origLat;
			applet.selectedNode.lon = origLon;
			applet.selectedNode.x = origX;
			applet.selectedNode.y = origY;
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