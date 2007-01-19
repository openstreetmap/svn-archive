/**
 * 
 */
package org.openstreetmap.processing;

import java.util.Iterator;

import org.openstreetmap.gui.MsgBox;
import org.openstreetmap.util.Line;
import org.openstreetmap.util.Node;
import org.openstreetmap.util.OsmPrimitive;

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
		for (Iterator it = applet.nodes.values().iterator(); it.hasNext();) {
			Node p = (Node)it.next();
			if (applet.mouseOverPoint(p.coor) && p.id != 0) {
                if (p.lines.isEmpty()){
                    int answer = MsgBox.show("Really delete the node "+p.getName()+"?", new String[]{"OK", "Cancel"});
                        if (answer == 1)
					return;
                    OsmApplet.println("deleting " + p);
                    applet.osm.deleteNode(p);
                }
                return;
			}
		}
		for (Iterator it = applet.lines.values().iterator(); it.hasNext();) {
			Line l = (Line)it.next();
			if (l.id != 0 && l.mouseOver(applet.mouseX, applet.mouseY, applet.strokeWeight)) {
				OsmPrimitive del = l.ways.isEmpty() ? l : (OsmPrimitive)l.ways.get(0);
				int answer = MsgBox.show("Really delete the "+del.getTypeName()+" "+del.getName()+"?", new String[]{"OK", "Cancel"});
				if (answer == 1)
					return;
				OsmApplet.println("deleting " + l);
				applet.osm.removePrimitive(l.ways.isEmpty() ? l : (OsmPrimitive)l.ways.get(0));
				return;
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