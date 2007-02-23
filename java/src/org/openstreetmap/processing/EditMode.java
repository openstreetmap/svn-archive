/**
 * 
 */
package org.openstreetmap.processing;

/**
 * Base class for all edit modes in the applet.
 */
abstract public class EditMode {
	private boolean over = false;

	public void mouseReleased() {}
	public void mousePressed() {}
	public void mouseMoved() {}
	public void mouseDragged() {}
	public void keyPressed() {}
	public void keyReleased() {}
	public void draw() {}
	public void set() {}
	public void unset() {}
  
  /**
   * The minimum applet status that allows this mode to be selected.
   * 
   * @return OsmApplet.NOT_READY, OsmApplet.BROWSEABLE or OsmApplet.EDITABLE
   */
  public int getMinAppletStatus() {
    return OsmApplet.EDITABLE;
  }
	
	abstract public String getDescription();
  
  synchronized public void setOver(boolean over) {
    //OsmApplet.debug("setOver()");
    this.over = over;
  }
  
  synchronized public boolean isOver() {
    //OsmApplet.debug("isOver()");
    return over;
  }
}