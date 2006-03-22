/**
 * 
 */
package org.openstreetmap.processing;

import java.util.Iterator;

import org.openstreetmap.util.Line;
import org.openstreetmap.util.Node;

/**
 * Edit mode to delete some objects.
 */
public class DeleteMode extends EditMode {
	/**
	 * Back reference to the applet.
	 */
	private final OsmApplet applet;

	public DeleteMode(OsmApplet applet) {
		this.applet = applet;
	}

	public void mouseReleased() {
		boolean gotOne = false;
		for (Iterator it = applet.nodes.values().iterator(); it.hasNext();) {
			Node p = (Node)it.next();
			if (applet.mouseOverPoint(p.coor) && p.id != 0) {
				boolean del = true;
				// TODO prompt for delete
				if (del) {
					OsmApplet.println("deleting " + p);
					applet.osm.deleteNode(p);
				} else {
					OsmApplet.println("not deleting " + p);
				}
				gotOne = true;
				break;
			}
		}
		if (!gotOne) {
			for (Iterator ll = applet.lines.values().iterator(); ll.hasNext();) {
				Line l = (Line)ll.next();
				if (l.mouseOver(applet.mouseX, applet.mouseY, applet.strokeWeight) && l.id != 0) {
					boolean del = true;
					// TODO prompt for delete
					if (del) {
						OsmApplet.println("deleting " + l);
						applet.osm.removePrimitive(l);
					} else {
						OsmApplet.println("not deleting " + l);
					}
					break;
				}
			}
		}
	}

	public void draw() {
		applet.stroke(0);
		applet.noFill();
		applet.line(applet.buttonWidth * 0.2f, applet.buttonHeight * 0.2f, applet.buttonWidth * 0.8f,
				applet.buttonHeight * 0.8f);
		applet.line(applet.buttonWidth * 0.8f, applet.buttonHeight * 0.2f, applet.buttonWidth * 0.2f,
				applet.buttonHeight * 0.8f);
	}

	public String getDescription() {
		return "Click to delete an object";
	}
}