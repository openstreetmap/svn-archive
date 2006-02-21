/**
 * 
 */
package org.openstreetmap.processing;

/**
 * Base class for all edit modes in the applet.
 */
public class EditMode {
	boolean over = false;

	public void mouseReleased() {}
	public void mousePressed() {}
	public void mouseMoved() {}
	public void mouseDragged() {}
	public void keyPressed() {}
	public void keyReleased() {}
	public void draw() {}
	public void set() {}
	public void unset() {}
}