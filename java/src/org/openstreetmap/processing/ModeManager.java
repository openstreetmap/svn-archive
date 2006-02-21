/**
 * 
 */
package org.openstreetmap.processing;

import java.util.Vector;


/**
 * Manages the different edit modes in the applet.
 */
public class ModeManager {

	/**
	 * The applet whose modes should be managed
	 */
	private final OsmApplet applet;
	
	Vector modes;
	boolean overButton;
	EditMode currentMode;
	int x, y;

	ModeManager(OsmApplet applet) {
		this.applet = applet;
		modes = new Vector();
		overButton = false;
		x = 5;
		y = 5;
	}

	public void addMode(EditMode mode) {
		modes.addElement(mode);
	}

	public EditMode getMode(int i) {
		return (EditMode)modes.elementAt(i);
	}

	public int getNumModes() {
		return modes.size();
	}

	public void draw() {
		// System.out.println("draw() START in ModeManager:
		// overButton="+overButton);

		overButton = false;

		this.applet.pushMatrix();
		this.applet.translate(x, y);
		for (int i = 0; i < getNumModes(); i++) {
			EditMode mode = getMode(i);
			this.applet.strokeWeight(1);
			this.applet.fill(200);
			mode.over = this.applet.mouseX > x + (i * this.applet.buttonWidth)
					&& this.applet.mouseX < this.applet.buttonWidth + x + (i * this.applet.buttonWidth)
					&& this.applet.mouseY < y + this.applet.buttonHeight && this.applet.mouseY > y;
			this.applet.stroke(0);
			this.applet.fill(mode.over || currentMode == mode ? 255 : 200);
			this.applet.rect(0, 0, this.applet.buttonWidth, this.applet.buttonHeight);
			mode.draw();
			overButton = overButton || mode.over;
			this.applet.translate(this.applet.buttonWidth, 0);
		}
		this.applet.popMatrix();

		// System.out.println("draw() END in ModeManager:
		// overButton="+overButton);
	}

	public void mouseReleased() {
		System.out.println("mouse relesed in mode manager");
		for (int i = 0; i < getNumModes(); i++) {
			EditMode mode = getMode(i);
			if (mode.over) {
				if (currentMode != null) {
					currentMode.unset();
				}
				currentMode = mode;
				currentMode.set();
				break;
			}
		}
		if (currentMode != null && !overButton) {
			currentMode.mouseReleased();
		}
		OsmApplet.print(currentMode);
		OsmApplet.print("ready:" + this.applet.ready);
		this.applet.redraw();
	}

	public void mousePressed() {
		System.out.println("mousePressed in ModeManager with currentMode="
				+ currentMode + " and overButton=" + overButton);
		if (currentMode != null && !overButton) {
			currentMode.mousePressed();
			this.applet.redraw();
		}
	}

	public void mouseMoved() {
		if (currentMode != null) {
			currentMode.mouseMoved();
			this.applet.redraw();
		} else {
			if (this.applet.mouseY < this.applet.buttonHeight
					&& this.applet.mouseX < (x + getNumModes() * this.applet.buttonWidth)) {
				this.applet.redraw();
			}
		}
	}

	public void mouseDragged() {
		if (currentMode != null) {
			currentMode.mouseDragged();
			this.applet.redraw();
		}
	}

	public void keyPressed() {
		if (currentMode != null) {
			currentMode.keyPressed();
			this.applet.redraw();
		}
	}

	public void keyReleased() {
		if (currentMode != null) {
			currentMode.keyReleased();
			this.applet.redraw();
		}
	}

}