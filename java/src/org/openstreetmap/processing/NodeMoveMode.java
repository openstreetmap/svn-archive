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

	public NodeMoveMode(OsmApplet applet) {
		this.applet = applet;
	}

	float lastOffsetX = 0.0f, lastOffsetY = 0.0f;

	public void mousePressed() {
		for (Iterator it = applet.nodes.values().iterator(); it.hasNext();) {
			Node p = (Node)it.next();
			if (applet.mouseOverPoint(p)) {
				applet.selectedNode = p;
				OsmApplet.println("selected: " + applet.selectedNode);
				lastOffsetX = applet.selectedNode.x - applet.mouseX;
				lastOffsetY = applet.selectedNode.y - applet.mouseY;
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
		if (applet.selectedNode != null) {
			applet.selectedNode.unproject(applet.tiles);
			applet.osm.moveNode(applet.selectedNode);
			applet.selectedNode = null;
		} else {
			OsmApplet.println("no selectedNode on mouse release");
		}
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
			applet.selectedNode.unproject(applet.tiles);
			applet.osm.moveNode(applet.selectedNode);
			applet.selectedNode = null;
		}
	}

	public String getDescription() {
		return "Drag to move nodes";
	}
}