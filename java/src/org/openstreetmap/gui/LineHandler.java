package org.openstreetmap.gui;

import org.openstreetmap.processing.OsmApplet;
import org.openstreetmap.util.Line;
import org.openstreetmap.util.Node;
import org.openstreetmap.util.OsmPrimitive;

/**
 * Handles line segment editing
 *  
 * @author Imi
 */
public class LineHandler extends GuiHandler {

    public LineHandler(OsmPrimitive line, OsmApplet applet) {
        super(line, applet);
        updateBasic();
        updateNodeTabFromOsm();
    }

    public void turnAround() {
      // sync not essential to prevent concurrent mod, but keeps everything consistent
      synchronized (map) { 
        Line ls = ((Line)osm);
        Node n = ls.from;
        ls.from = ls.to;
        ls.to = n;
      }
      updateNodeTabFromOsm();
      applet.redraw();
    }

    public void updateLineNode(String what) {
        Line l = (Line)osm;
        long id;
        try {
            id = Long.parseLong(getString(find(what), "text"));
        } catch (NumberFormatException e) {
            MsgBox.msg("Please enter the id of the target node.");
            return;
        }
        Node newNode = (Node) map.getNode(Node.key(id));
        if (newNode == null) {
            MsgBox.msg("Node with id "+id+" not found.");
            return;
        }
        if (what.equals("from_node"))
            l.from = newNode;
        else
            l.to = newNode;
        updateNodeTabFromOsm();
        applet.redraw();
    }

    private void updateNodeTabFromOsm() {
        setString(find("from_node"), "text", ""+((Line)osm).from.id);
        setString(find("to_node"), "text", ""+((Line)osm).to.id);
    }
}
