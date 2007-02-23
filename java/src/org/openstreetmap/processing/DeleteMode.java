/**
 * 
 */
package org.openstreetmap.processing;

import java.util.Iterator;

import org.openstreetmap.client.MapData;
import org.openstreetmap.gui.MsgBox;
import org.openstreetmap.util.Line;
import org.openstreetmap.util.Node;
import org.openstreetmap.util.OsmPrimitive;

/**
 * Edit mode to delete some objects.
 */
public class DeleteMode extends EditMode {
  /**
   * Back reference to the applet.
   */
  private final OsmApplet applet;
  
  /**
   * Used to track successive clicks, editing different features
   */
  private int clickCount = 0;

  public DeleteMode(OsmApplet applet) {
    this.applet = applet;
  }

  public void mouseReleased() {
    MapData map = applet.getMapData();
    boolean doDelete = false;
    OsmPrimitive del = null;
    String message = null; // failure message
    synchronized (map) { // see MapData comments
      del = applet.getNearest();
      if (del instanceof Node) {
        Node p = (Node) del;
        if (applet.mouseOverPoint(p.coor)) {
          if (p.id == 0) {
            message = "Creating node, still awaiting server response.";
          }
          else if (!p.lines.isEmpty()) {
            // special case: if click on node where segment goes from 
            // that node to the same node, delete that segment
            for (Iterator it = p.lines.iterator(); it.hasNext();) {
              Line l = (Line) it.next();
              if (l.from.equals(l.to)) {
                del = l;
                doDelete = true;
                break;
              }
            }
            if (!doDelete) {
              message = "Remove segment(s) first.";
            }
          }
          else {
            doDelete = true;
            del = p;
          }
        }
      }
      else if (del instanceof Line) {
        Line l = (Line) del;
        if (l.mouseOver(applet.mouseX, applet.mouseY, applet.strokeWeight)) {
          if (l.id == 0) {
            message = "Creating line, still awaiting server response.";
          }
          else {
            doDelete = true;
            if (!l.ways.isEmpty()) {
              int wayIndex = clickCount % l.ways.size();
              del = (OsmPrimitive) l.ways.get(wayIndex);
              clickCount ++;
            }
          }
        }
      }
    } // sync
    
    // moved to after sync block (don't want to call high-level routines
    // with low-level map lock) 
    if (doDelete) {
      int answer = MsgBox.show("Really delete the " + del.getTypeName() + " "
          + del.getName() + "?", new String[] { "OK", "Cancel" });
      if (answer == 1)
        return;
      OsmApplet.println("deleting " + del);
      if (del instanceof Node) {
        applet.osm.deleteNode((Node) del);
      }
      else {
        applet.osm.removePrimitive(del);
      }
      clickCount = 0;
    }
    else if (del != null && message != null) {
      MsgBox.msg("Unable to delete " + del.getTypeName() + ": " + message);
    }
  }

  public void draw() {
    applet.stroke(0);
    applet.noFill();
    applet.line(applet.buttonWidth * 0.2f, applet.buttonHeight * 0.2f,
        applet.buttonWidth * 0.8f, applet.buttonHeight * 0.8f);
    applet.line(applet.buttonWidth * 0.8f, applet.buttonHeight * 0.2f,
        applet.buttonWidth * 0.2f, applet.buttonHeight * 0.8f);
  }

  public String getDescription() {
    return "Click to delete an object";
  }

  public void mouseDragged() {
    applet.redraw();
  }
}