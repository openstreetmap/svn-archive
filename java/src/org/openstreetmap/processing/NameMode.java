/**
 * 
 */
package org.openstreetmap.processing;

import java.util.Iterator;

import org.openstreetmap.util.Line;

/**
 * The mode to change the name of an line segment.
 */
public class NameMode extends EditMode {
	/**
	 * Back reference to the applet.
	 */
	private final OsmApplet applet;
	
	/**
	 * The name of the segment as the mode started.
	 */
	private String oldName;

	public NameMode(OsmApplet applet) {
		this.applet = applet;
	}

	public void keyPressed() {
		System.out.println("got key " + applet.key + " with keyCode " + applet.keyCode
				+ " and numeric val "
				+ java.lang.Character.getNumericValue(applet.key));
		if (applet.selectedLine != null) {
			if (oldName == null)
				oldName = applet.selectedLine.getName();
			
			// should check for key == CODED but there's a Processing bug
			if (java.lang.Character.getNumericValue(applet.key) == -1 && applet.keyCode != 32 && applet.keyCode != 222) { 
				if (applet.keyCode == OsmApplet.BACKSPACE && applet.selectedLine.getName().length() > 0) {
					applet.selectedLine.setName(
							applet.selectedLine.getName().substring(0, applet.selectedLine.getName().length() - 1));
					applet.selectedLine.nameChanged = true;
				} else if (applet.keyCode == OsmApplet.ENTER) {
					if (applet.selectedLine.nameChanged) {
						if (applet.osm != null) {
							String newName = applet.selectedLine.getName();
							applet.selectedLine.setName(oldName);
							applet.osm.updateLineName(applet.selectedLine, newName);
						}
					}
					applet.selectedLine = null;
				}
			} else {
				applet.selectedLine.setName(applet.selectedLine.getName() + applet.key);
				applet.selectedLine.nameChanged = true;
			}
		}
	}

	public void mouseReleased() {
		Line previousSelection = applet.selectedLine;
		applet.selectedLine = null;
		for (Iterator it = applet.lines.values().iterator(); it.hasNext();) {
			Line l = (Line)it.next();
			if (l.mouseOver(applet.mouseX, applet.mouseY, applet.strokeWeight)) {
				applet.selectedLine = l;
				break;
			}
		}
		if (previousSelection != null && previousSelection != applet.selectedLine) {
			if (previousSelection.nameChanged) {
				if (applet.osm != null && oldName != null) {
					String newName = previousSelection.getName();
					previousSelection.setName(oldName);
					applet.osm.updateLineName(previousSelection, newName);
				}
			}
			applet.selectedLine = null;
		}
	}

	public void draw() {
		applet.fill(0);
		applet.textFont(applet.font);
		applet.textSize(11);
		applet.textAlign(OsmApplet.CENTER);
		applet.text("A", 1 + applet.buttonWidth * 0.5f, 5 + (applet.buttonHeight * 0.5f));
	}

	public String getDescription() {
		return "Change the name of objects";
	}
}