/**
 * 
 */
package org.openstreetmap.processing;

import java.util.Iterator;

import org.openstreetmap.util.Node;

/**
 * The edit mode to add a new node within the applet.
 */
public class NodeMode extends EditMode {

	/**
	 * Back reference to the applet.
	 */
	private final OsmApplet applet;

	public NodeMode(OsmApplet applet) {
		this.applet = applet;
	}

	public void mouseReleased() {
		boolean overOne = false; // points can't overlap
		for (Iterator it = applet.nodes.values().iterator(); it.hasNext();) {
			Node p = (Node)it.next();
			if (applet.mouseOverPoint(p)) {
				overOne = true;
				applet.redraw();
				break;
			}
		}
		if (!overOne) {
			Node node = new Node(applet.mouseX, applet.mouseY, applet.tiles);
			String tempKey = "temp_" + Math.random();
			if (applet.osm != null) {
				applet.osm.createNode(node, tempKey);
			}
			applet.nodes.put(tempKey, node);

			OsmApplet.println(node);
		}
	}

	public void draw() {
		applet.fill(0);
		applet.stroke(0);
		applet.ellipseMode(OsmApplet.CENTER);
		applet.ellipse(applet.buttonWidth / 2.0f, applet.buttonHeight / 2.0f, 5, 5);
	}
}