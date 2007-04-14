/**
 * 
 */
package org.openstreetmap.processing;

import java.util.Iterator;

import org.openstreetmap.client.MapData;
import org.openstreetmap.util.Line;
import org.openstreetmap.util.Node;
import org.openstreetmap.util.OsmPrimitive;

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
    OsmPrimitive p = applet.getNearest();
    if (p instanceof Node) {
      if (applet.mouseOverPoint(((Node) p).coor)) {
        applet.start = (Node) p;
      }
    }
	}

	public void mouseReleased() {
    OsmPrimitive p = applet.getNearest(true);
    if (p instanceof Node) {
      if (applet.mouseOverPoint(((Node) p).coor)) {
        if (applet.start != null && !applet.start.equals(p)) {
          Line line = new Line(applet.start, (Node) p);
          String tempKey = "temp_" + Math.random();
          if (applet.osm != null) {
            applet.osm.createLine(line, tempKey);
          }
        }
      }
    }
		applet.start = null;
		applet.resetTempLine();
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

	public String getDescription() {
		return "Drag to draw a line segment";
	}

  public void mouseDragged() {
    applet.redraw();
  }
}