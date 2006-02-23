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
		// System.out.println("draw() START in ModeManager: overButton="+overButton);

		overButton = false;

		applet.pushMatrix();
		applet.translate(x, y);
		for (int i = 0; i < getNumModes(); i++) {
			EditMode mode = getMode(i);
			applet.strokeWeight(1);
			applet.fill(200);
			mode.over = applet.mouseX > x + (i * applet.buttonWidth)
					&& applet.mouseX < applet.buttonWidth + x + (i * applet.buttonWidth)
					&& applet.mouseY < y + applet.buttonHeight && applet.mouseY > y;
			applet.stroke(0);
			applet.fill(mode.over || currentMode == mode ? 255 : 200);
			applet.rect(0, 0, applet.buttonWidth, applet.buttonHeight);
			mode.draw();
			overButton = overButton || mode.over;
			applet.translate(applet.buttonWidth, 0);
		}
		applet.popMatrix();

		// System.out.println("draw() END in ModeManager: overButton="+overButton);
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
		OsmApplet.print("ready:" + applet.ready);
		applet.redraw();
	}

	public void mousePressed() {
		System.out.println("mousePressed in ModeManager with currentMode="
				+ currentMode + " and overButton=" + overButton);
		if (currentMode != null && !overButton) {
			currentMode.mousePressed();
			applet.redraw();
		}
	}

	public void mouseMoved() {
		if (currentMode != null) {
			currentMode.mouseMoved();
			applet.redraw();
		} else {
			if (applet.mouseY < applet.buttonHeight && applet.mouseX < (x + getNumModes() * applet.buttonWidth)) {
				applet.redraw();
			}
		}
	}

	public void mouseDragged() {
		if (currentMode != null) {
			currentMode.mouseDragged();
			applet.redraw();
		}
	}

	public void keyPressed() {
		if (currentMode != null) {
			currentMode.keyPressed();
			applet.redraw();
		}
	}

	public void keyReleased() {
		if (currentMode != null) {
			currentMode.keyReleased();
			applet.redraw();
		}
	}

}