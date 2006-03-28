package org.openstreetmap.processing;

import java.awt.Point;
import java.util.Iterator;

import org.openstreetmap.gui.GuiHandler;
import org.openstreetmap.gui.GuiLauncher;
import org.openstreetmap.gui.WayHandler;
import org.openstreetmap.util.Line;
import org.openstreetmap.util.LineOnlyId;
import org.openstreetmap.util.Node;
import org.openstreetmap.util.Way;

/**
 * Edit mode to select objects then draw a new way between them.
 */
public class WayMode extends EditMode {

	/**
	 * Back reference to the applet. 
	 */
	private final OsmApplet applet;
	private GuiLauncher dlg;
	private Point location;

	public WayMode(OsmApplet applet) {
		this.applet = applet;
	}

	//TODO: There should be already a near* function in OsmApplet. Use this.
	public void mouseReleased() {
		Line nearLine = null;
		float nearDist = Float.MAX_VALUE;
		for (Iterator it = applet.lines.values().iterator(); it.hasNext();) {
			Line line = (Line)it.next();
			if (line instanceof LineOnlyId)
				continue;
			Node n = new Node(applet.mouseX, applet.mouseY, applet.tiles);
			float dist = line.distance(n);
			if (dist < nearDist) {
				nearDist = dist;
				nearLine = line;
			}
		}
		if (nearDist < 20) {
			if (applet.selectedLine.contains(nearLine.key())) {
				applet.selectedLine.remove(nearLine.key());
				if (applet.selectedLine.isEmpty() && dlg != null)
					dlg.setVisible(false);
			} else {
				applet.selectedLine.add(nearLine.key());
				if (applet.selectedLine.size() == 1)
					openProperties();
			}
			if (dlg != null) {
				Way way = (Way)((GuiHandler)dlg.handler).osm;
				way.lines.clear();
				for (Iterator it = applet.selectedLine.iterator(); it.hasNext();) {
					String lineKey = (String)it.next();
					Line line = (Line)applet.lines.get(lineKey);
					if (line != null)
						way.lines.add(line);
					else
						way.lines.add(new LineOnlyId(Line.getIdFromKey(lineKey)));
				}
				((WayHandler)dlg.handler).updateSegmentsFromList();
			}
			applet.redraw();
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
	
	public void openProperties() {
		if (applet.selectedLine.isEmpty())
			return;
		final Way way = new Way(0);
		final WayHandler wayHandler = new WayHandler(way, applet, null);

		if (dlg != null) {
			location = dlg.getLocation();
			dlg.setVisible(false);
		}
		dlg = new GuiLauncher("New way properties", wayHandler){
			public void setVisible(final boolean visible) {
				if (!visible) {
					if (dlg != null)
						location = dlg.getLocation();
					if (handler != null && !((GuiHandler)handler).cancelled) {
						applet.osm.createWay(way);
						handler = null;
					}
					applet.selectedLine.clear();
					applet.redraw();
					dlg = null;
				}
				super.setVisible(visible);
			}
		};
		if (location != null)
			dlg.setLocation(location);
		dlg.setVisible(true);
	}
}
