/**
 * 
 */
package org.openstreetmap.processing;


import processing.core.PImage;

/**
 * Edit mode to move the applet view.
 */
public class MoveMode extends EditMode {

	/**
	 * Back reference to the applet
	 */
	private final OsmApplet applet;

	public MoveMode(OsmApplet applet) {
		this.applet = applet;
		hand = applet.loadImage("/data/hand.png");
	}

	int lastmX, lastmY;
	PImage hand;

	public void mouseReleased() {
    applet.cursor(OsmApplet.HAND);
    applet.redraw();
	}

	public void mousePressed() {
    applet.cursor(OsmApplet.MOVE);
    applet.tiles.startDrag();
		lastmX = applet.mouseX;
		lastmY = applet.mouseY;
	}

	public void mouseDragged() {
    applet.cursor(OsmApplet.MOVE);
		applet.tiles.drag(lastmX - applet.mouseX, applet.mouseY - lastmY);
		lastmX = applet.mouseX;
		lastmY = applet.mouseY;
		if (applet.online) {
			applet.updatelinks();
		}
	}

	public void draw() {
		// imagehere
		applet.noFill();
		applet.stroke(0);
		applet.strokeWeight(1);
		applet.image(hand, 1, 2);
	}

	public String getDescription() {
		return "Move the displayed area";
	}

  public int getMinAppletStatus() {
    return OsmApplet.BROWSEABLE;
  }
}