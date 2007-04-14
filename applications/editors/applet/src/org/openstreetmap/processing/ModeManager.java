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
	private EditMode currentMode;
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
  			mode.setOver(applet.mouseX > x + (i * applet.buttonWidth)
  					&& applet.mouseX < applet.buttonWidth + x + (i * applet.buttonWidth)
  					&& applet.mouseY < y + applet.buttonHeight && applet.mouseY > y);
  			applet.stroke(0);
  			applet.fill(mode.isOver() || getCurrentMode() == mode ? 255 : 200);
  			applet.rect(0, 0, applet.buttonWidth, applet.buttonHeight);
  			mode.draw();
  			overButton = overButton || mode.isOver();
        if (mode.isOver()) {
          showTooltip(getModeDescription(mode));
        }
  			applet.translate(applet.buttonWidth, 0);
  		}
		applet.popMatrix();

		// System.out.println("draw() END in ModeManager: overButton="+overButton);
	}
  
  private String getModeDescription(EditMode mode) {
    String tip = mode.getDescription();
    int minStatus = mode.getMinAppletStatus();
    if (applet.getStatus() < minStatus) {
      if (minStatus == OsmApplet.EDITABLE) {
        tip += " [Disabled until data downloaded]";
      }
      else if (minStatus == OsmApplet.BROWSEABLE) {
        tip += " [Disabled until applet started]";
      }
    }
    return tip;
  }

  /**
   * Draw 'tooltip'-like message - just underneath where currently rendering.
   *  
   * @param description Tip for the tool.
   */
  private void showTooltip(String description) {
    applet.pushMatrix();
      applet.strokeWeight(1);
      applet.fill(255, 255f, 187f);
      applet.stroke(0, 0, 0);
      applet.translate(0, y + applet.buttonHeight + 2);
      applet.textSize(10);
      applet.rect(0, 0, applet.textWidth(description) + 5, 16);
      applet.textAlign(applet.LEFT);
      applet.fill(0, 0, 0);
      applet.text(description, 3, 12);
    applet.popMatrix();
  }

	public void mouseReleased() {
    // TODO should move zoom in/out buttons to same place as on view pane
    // and make them respond immediately (as keys, as on view pane) - no
    // point having a mode for it, because doesn't zoom to location of mouse
    // click anyway
		for (int i = 0; i < getNumModes(); i++) {
			EditMode mode = getMode(i);
			if (mode.isOver() && applet.getStatus() >= mode.getMinAppletStatus()) {
        applet.debug("released:isOver " + mode.getClass());
				if (getCurrentMode() != null) {
					getCurrentMode().unset();
				}
				setCurrentMode(mode);
				getCurrentMode().set();
				break;
			}
		}
		if (getCurrentMode() != null && !overButton && isModeReady()) {
			getCurrentMode().mouseReleased();
		}
		//OsmApplet.print(getCurrentMode());
		//OsmApplet.print("ready:" + applet.getStatus());
		applet.redraw();
	}

	public void mousePressed() {
		if (getCurrentMode() != null && !overButton && isModeReady()) {
			getCurrentMode().mousePressed();
			applet.redraw();
		}
	}

	public void mouseMoved() {
		if (getCurrentMode() != null && isModeReady()) {
			getCurrentMode().mouseMoved();
			applet.redraw();  // playing on safe side - any mouse movement causes multiple redraws
		} else {
			if (applet.mouseY < applet.buttonHeight && applet.mouseX < (x + getNumModes() * applet.buttonWidth)) {
				applet.redraw();
			}
		}
	}

	public void mouseDragged() {
		if (getCurrentMode() != null && isModeReady()) {
			getCurrentMode().mouseDragged();
			//applet.redraw();  // too many events generated here for move mode - call redraw from
                          // specific mode mouseDragged handlers
		}
	}

	public void keyPressed() {
		if (getCurrentMode() != null && isModeReady()) {
			getCurrentMode().keyPressed();
			applet.redraw();
		}
	}

	public void keyReleased() {
		if (getCurrentMode() != null && isModeReady()) {
			getCurrentMode().keyReleased();
			applet.redraw();
		}
	}

  synchronized void setCurrentMode(EditMode currentMode) {
    //OsmApplet.debug("setCurrentMode()");
    this.currentMode = currentMode;
  }

  synchronized EditMode getCurrentMode() {
    //OsmApplet.debug("getCurrentMode()");
    return currentMode;
  }

  /**
   * Whether mode can be selected / used based on applet status.
   * 
   * @return <code>true</code> if mode can be used / forwarded to.
   */
  private boolean isModeReady() {
    return applet.getStatus() >= getCurrentMode().getMinAppletStatus();
  }
}