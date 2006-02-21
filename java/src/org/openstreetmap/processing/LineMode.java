/**
 * 
 */
package org.openstreetmap.processing;

import java.util.Iterator;

import org.openstreetmap.util.Line;
import org.openstreetmap.util.Node;

/**
 * Edit mode to draw a line segment between two points.
 */
public class LineMode extends EditMode {
	/**
	 * Back reference to the applet. 
	 */
	private final OsmApplet applet;

	/**
	 * @param applet
	 */
	public LineMode(OsmApplet applet) {
		this.applet = applet;
	}

	public void mousePressed() {
		for (Iterator it = applet.nodes.values().iterator(); it.hasNext();) {
			Node p = (Node)it.next();
			if (applet.mouseOverPoint(p)) {
				applet.start = p;
				break;
			}
		}
	}

	public void mouseReleased() {
		for (Iterator e = applet.nodes.values().iterator(); e.hasNext();) {
			Node p = (Node)e.next();
			if (applet.mouseOverPoint(p)) {
				if (applet.start != null) {
					Line line = new Line(applet.start, p);
					String tempKey = "temp_" + Math.random();
					if (applet.osm != null) {
						applet.osm.createLine(line, tempKey);
					}
					applet.lines.put(tempKey, line);
				}
				break;
			}
		}
		applet.start = null;
		applet.tempLine.from = null;
		applet.tempLine.to = null;
	}

	public void draw() {
		applet.noFill();
		applet.stroke(0);
		applet.strokeWeight(5);
		applet.line(2, 2, applet.buttonWidth - 2, applet.buttonHeight - 2);
		applet.stroke(255);
		applet.strokeWeight(4);
		applet.line(2, 2, applet.buttonWidth - 2, applet.buttonHeight - 2);
	}
}