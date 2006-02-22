/**
 * 
 */
package org.openstreetmap.processing;

import java.util.Iterator;

import org.openstreetmap.util.Line;

/**
 * The mode to change the name of an object.
 */
public class NameMode extends EditMode {
	/**
	 * Back reference to the applet.
	 */
	private final OsmApplet applet;

	/**
	 * @param applet
	 */
	public NameMode(OsmApplet applet) {
		this.applet = applet;
	}

	public void keyPressed() {
		System.out.println("got key " + this.applet.key + " with keyCode " + this.applet.keyCode
				+ " and numeric val "
				+ java.lang.Character.getNumericValue(this.applet.key));
		if (this.applet.selectedLine != null) {
			if (java.lang.Character.getNumericValue(this.applet.key) == -1
					&& this.applet.keyCode != 32 && this.applet.keyCode != 222) { // should check
															// for key ==
															// CODED but
															// there's a
															// Processing
															// bug
				if (this.applet.keyCode == OsmApplet.BACKSPACE
						&& this.applet.selectedLine.getName().length() > 0) {
					this.applet.selectedLine.setName(this.applet.selectedLine.getName().substring(
							0, this.applet.selectedLine.getName().length() - 1));
					applet.selectedLine.nameChanged = true;
				} else if (this.applet.keyCode == OsmApplet.ENTER) {
					if (applet.selectedLine.nameChanged) {
						if (this.applet.osm != null) {
							this.applet.osm.updateLineName(this.applet.selectedLine);
						}
					}
					this.applet.selectedLine = null;
				}
			} else {
				applet.selectedLine.setName(this.applet.selectedLine.getName() + this.applet.key);
				applet.selectedLine.nameChanged = true;
			}
		}
	}

	public void mouseReleased() {
		Line previousSelection = this.applet.selectedLine;
		this.applet.selectedLine = null;
		for (Iterator it = this.applet.lines.values().iterator(); it.hasNext();) {
			Line l = (Line)it.next();
			if (l.mouseOver(this.applet.mouseX, this.applet.mouseY, this.applet.strokeWeight)) {
				this.applet.selectedLine = l;
				break;
			}
		}
		if (previousSelection != null && previousSelection != this.applet.selectedLine) {
			if (previousSelection.nameChanged) {
				if (this.applet.osm != null) {
					this.applet.osm.updateLineName(previousSelection);
				}
			}
			this.applet.selectedLine = null;
		}
	}

	public void draw() {
		this.applet.fill(0);
		this.applet.textFont(this.applet.font);
		this.applet.textSize(11);
		this.applet.textAlign(OsmApplet.CENTER);
		this.applet.text("A", 1 + this.applet.buttonWidth * 0.5f, 5 + (this.applet.buttonHeight * 0.5f));
	}

	public String getDescription() {
		return "Change the name of objects";
	}
}