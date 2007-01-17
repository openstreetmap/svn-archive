/**
 * 
 */
package org.openstreetmap.processing;

import java.util.Iterator;

import org.openstreetmap.util.Line;
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
			if (applet.mouseOverPoint(p.coor)) {
				overOne = true;
				applet.redraw();
				break;
			}
		}
		if (!overOne) {
            // Are they actually over a segment?
            // (If so, we'll insert the node into that)
            Line lineInto = null;
            for (Iterator it = applet.lines.values().iterator(); it.hasNext();) {
                Line l = (Line)it.next();
                if (l.id != 0 && l.mouseOver(applet.mouseX, applet.mouseY, applet.strokeWeight)) {
                    lineInto = l;
                }
            }
            
            // Add the node
			Node node = new Node(applet.mouseX, applet.mouseY, applet.tiles);
			String tempKey = "temp_" + Math.random();
			if (applet.osm != null) {
				applet.osm.createNode(node, tempKey);
			}

			OsmApplet.println(node);
            
            // If we're inserting into a segment, change
            //  that segment to end of the new node, and 
            //  add a new segment from that node to the end
            OsmApplet.println("Line was " + lineInto);
            Line newLine = new Line(lineInto.to, node);
            tempKey = "temp_" + Math.random();
            if (applet.osm != null) {
                applet.osm.createLine(newLine, tempKey);
            }
            lineInto.to = node;
            applet.osm.updateLine(lineInto);
            OsmApplet.println("Line now " + lineInto + " and " + newLine);
            
            // If this was part of a way, update that too
		}
	}

	public void draw() {
		applet.fill(0);
		applet.stroke(0);
		applet.ellipseMode(OsmApplet.CENTER);
		applet.ellipse(applet.buttonWidth / 2.0f, applet.buttonHeight / 2.0f, 5, 5);
	}

	public String getDescription() {
		return "Click to add new nodes";
	}
}