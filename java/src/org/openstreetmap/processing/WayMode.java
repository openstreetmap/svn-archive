package org.openstreetmap.processing;

import java.util.Iterator;

import org.openstreetmap.util.Line;
import org.openstreetmap.util.Node;

/**
 * Edit mode to select objects then draw a new way between them.
 */
public class WayMode extends EditMode {
	/**
	 * Back reference to the applet. 
	 */
	private final OsmApplet applet;

	public WayMode(OsmApplet applet) {
		this.applet = applet;
	}

	public void mouseReleased() {
		Line nearLine = null;
		float nearDist = Float.MAX_VALUE;
		for (Iterator it = applet.lines.values().iterator(); it.hasNext();) {
			Line line = (Line)it.next();
			Node n = new Node(applet.mouseX, applet.mouseY, applet.tiles);
			float dist = line.distance(n);
			if (dist < nearDist) {
				nearDist = dist;
				nearLine = line;
			}
		}
		if (nearDist < 20) {
			nearLine.selected = !nearLine.selected;
			applet.repaint();
		}
	}

	public void draw() {
		applet.noFill();
		applet.stroke(0);
		applet.strokeWeight(5);
		applet.line(2, 2, applet.buttonWidth - 2, applet.buttonHeight/2);
		applet.line(2, applet.buttonHeight - 2, applet.buttonWidth - 2, applet.buttonHeight/2);
		applet.stroke(255);
		applet.strokeWeight(4);
		applet.line(2, 2, applet.buttonWidth - 2, applet.buttonHeight/2);
		applet.line(2, applet.buttonHeight - 2, applet.buttonWidth - 2, applet.buttonHeight/2);
	}

	public String getDescription() {
		return "Select line segments by clicking and click mode button again to create way.";
	}
}