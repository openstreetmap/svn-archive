package org.openstreetmap.processing;

import java.awt.Point;
import java.util.Iterator;

import org.openstreetmap.client.MapData;
import org.openstreetmap.gui.GuiHandler;
import org.openstreetmap.gui.GuiLauncher;
import org.openstreetmap.gui.MsgBox;
import org.openstreetmap.gui.WayHandler;
import org.openstreetmap.util.Line;
import org.openstreetmap.util.LineOnlyId;
import org.openstreetmap.util.OsmPrimitive;
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

	public void mouseReleased() {
    Line nearLine = null;
    OsmPrimitive nearPrimitive = null;
    
    nearPrimitive = applet.getNearest();
    if (nearPrimitive instanceof Line) {
      if (nearPrimitive.id == 0) {
        MsgBox.msg("Cannot use new segment, still awaiting server response.");
      }
      else {
        nearLine = (Line) nearPrimitive;
      }
    }
    if (nearLine == null) {
      // MsgBox.msg("No line selected."); // TODO error message display that doesn't require OK
    }
    else {
      boolean closeDialog = false;
      boolean openDialog = false;
      synchronized (applet) { // guard against concurrent mods in selected line
        if (applet.selectedLine.contains(nearLine.key())) {
          applet.selectedLine.remove(nearLine.key());
          if (applet.selectedLine.isEmpty() && dlg != null)
            closeDialog = true;
        } else {
          applet.selectedLine.add(nearLine.key());
          if (applet.selectedLine.size() == 1)
            openDialog = true;
        }
      }
      // NB: call from outside of applet sync
      if (closeDialog) {
        dlg.setVisible(false);
      }
      else if (openDialog) {
        openProperties();
      }
			if (dlg != null) {
				Way way = (Way)((GuiHandler)dlg.handler).osm;
        // sync'ed iteration to prevent concurrent mod - .osm still points to object in map
        MapData map = applet.getMapData();
        synchronized (map) {
          way.lines.clear();
          // no need to sync on selectedline - all updates done on this event thread
          for (Iterator it = applet.selectedLine.iterator(); it.hasNext();) {
            String lineKey = (String)it.next();
            Line line = (Line) map.getLine(lineKey);
            if (line != null)
              way.lines.add(line);
            else
              way.lines.add(new LineOnlyId(Line.getIdFromKey(lineKey)));
          }
          ((WayHandler)dlg.handler).updateSegmentsFromList();
        }
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
					applet.clearSelectedLine();
					applet.redraw();
					dlg = null;
				}
				super.setVisible(visible);
			}
		};
		if (location != null) {
			dlg.setLocation(location);
    }
		dlg.setVisible(true);
	}

  public void mouseDragged() {
    applet.redraw();
  }
}
