/**
 * 
 */
package org.openstreetmap.processing;

import java.util.ArrayList;
import java.util.Iterator;

import org.openstreetmap.client.MapData;
import org.openstreetmap.util.Line;
import org.openstreetmap.util.Node;
import org.openstreetmap.util.OsmPrimitive;
import org.openstreetmap.util.Way;

/**
 * The edit mode to add a new node within the applet.
 */
public class NodeMode extends EditMode {

  /**
   * Back reference to the applet.
   */
  private final OsmApplet applet;

  public NodeMode(OsmApplet applet) {
    this.applet = applet;
  }

  public void mouseReleased() {
    MapData map = applet.getMapData();
    boolean overOne = false; // points can't overlap
    Line lineInto = null;
    
    // first off, analyse map to decide what going to do (sync
    // on map for consistency / prevent concurrent mod errors)
    synchronized (map) {
      for (Iterator it = map.nodesIterator(); it.hasNext();) {
        Node p = (Node) it.next();
        if (applet.mouseOverPoint(p.coor)) {
          overOne = true;
          break;
        }
      }
      if (!overOne) {
        // Are they actually over a segment?
        // (If so, we'll insert the node into that)
        for (Iterator it = map.linesIterator(); it.hasNext();) {
          Line l = (Line) it.next();
          if (l.id != 0
              && l.mouseOver(applet.mouseX, applet.mouseY, applet.strokeWeight)) {
            lineInto = l;
          }
        }
      }
    } // sync
    
    // NB: don't acquire other locks from within map sync (use as low level lock)
    // i.e. avoid (i) redraw() attempts to lock applet, (ii) server requests
    
    if (overOne) {
      applet.redraw();
      return;
    }
    
    // Add the node
    Node node = new Node(applet.mouseX, applet.mouseY, applet.tiles);
    String tempKey = "temp_" + Math.random();
    Line newLine = null;
    ArrayList waysToUpdate = new ArrayList();;

    // NB: breaking some unwritten conventions here:  editing of map
    // directly in event thread code, and not in a ServerCommand.
    // if fails to contact server, won't know what edits lost unless
    // map refreshes and user checks...
      
    if (lineInto != null) {
      // If we're inserting into a segment, change
      // that segment to end of the new node, and
      // add a new segment from that node to the end
      applet.debug("Line was " + lineInto);
      newLine = new Line(node, lineInto.to);
      lineInto.to = node;
      tempKey = "temp_" + Math.random();
      applet.debug("Line now " + lineInto + " and " + newLine);

      // If this was part of a way, update that too
      if (lineInto.ways != null) {
        synchronized (map) {
          for (Iterator it = lineInto.ways.iterator(); it.hasNext();) {
            Way way = (Way) it.next();
    
            // Check that the way doesn't already have the
            // new segment. (This method is sometimes
            // called twice, FNAR)
            boolean addSegment = true;
            for (Iterator lit = way.lines.iterator(); lit.hasNext();) {
              OsmPrimitive thisLine = (OsmPrimitive) lit.next();
              if (thisLine.key() == newLine.key()) {
                addSegment = false;
              }
            }
    
            // Add the new segment after the current one
            // TODO: Add either before or after based on direction
            if (addSegment) {
              ArrayList newLines = new ArrayList();
              for (Iterator lit = way.lines.iterator(); lit.hasNext();) {
                OsmPrimitive thisLine = (OsmPrimitive) lit.next();
                newLines.add(thisLine);
                if (thisLine.id == lineInto.id) {
                  newLines.add(newLine);
                }
              }
              way.lines = newLines;
              waysToUpdate.add(way);
              applet.debug("Way now " + way);
            }
          }
        } // sync
      }
    }
    
    // do server updates
    // left to end so that outside map sync (avoiding deadlocks) 
    applet.osm.createNode(node, tempKey);
    String msg = "creating " + node;
    if (lineInto != null) {
      applet.osm.createLine(newLine, tempKey);
      applet.osm.updateLine(lineInto);
      msg += " in line " + lineInto + " (new line " + newLine + "), updating " + waysToUpdate.size() + " ways.";
      for (Iterator wit = waysToUpdate.iterator(); wit.hasNext();) {
        applet.osm.updateWay((Way) wit.next());
      }
    }
  }

  public void draw() {
    applet.fill(0);
    applet.stroke(0);
    applet.ellipseMode(OsmApplet.CENTER);
    applet.ellipse(applet.buttonWidth / 2.0f, applet.buttonHeight / 2.0f, 5, 5);
  }

  public String getDescription() {
    return "Click to add new nodes";
  }
}
