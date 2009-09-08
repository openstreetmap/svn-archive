/**
 * License: GPL. Copyright 2008. Martin Garbe (leo at running-sheep dot com)
 */
package org.openstreetmap.josm.plugins.editgpx;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.gui.MapFrame;


public class EditGpxMode extends MapMode {

    private static final long serialVersionUID = 7940589057093872411L;
    Point pointPressed;
    DataSet dataSet;
    MapFrame mapFrame;
    Rectangle oldRect;
    MapFrame frame;

    public EditGpxMode(MapFrame mapFrame, String name, String desc, DataSet ds) {
        super(name, "editgpx_mode.png", desc, mapFrame, Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        dataSet = ds;
    }

    @Override public void enterMode() {
        super.enterMode();
        Main.map.mapView.addMouseListener(this);
        Main.map.mapView.addMouseMotionListener(this);
    }

    @Override public void exitMode() {
        super.exitMode();
        Main.map.mapView.removeMouseListener(this);
        Main.map.mapView.removeMouseMotionListener(this);
    }


    @Override public void mousePressed(MouseEvent e) {
        pointPressed = new Point(e.getPoint());
    }


    @Override public void mouseDragged(MouseEvent e) {
        if ( (e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) ==  MouseEvent.BUTTON1_DOWN_MASK) {
            //if button1 is hold, draw the rectangle.
            paintRect(pointPressed, e.getPoint());
        }
    }

    @Override public void mouseReleased(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1) {
            return;
        } else {
            Point pointReleased = e.getPoint();

            Rectangle r = createRect(pointReleased, pointPressed);

            //go through nodes and mark the ones in the selection rect as deleted
            for (Node n: dataSet.nodes) {
                Point p = Main.map.mapView.getPoint(n);
                if (r.contains(p)) {
                	n.setDeleted(true);
                }
            }
            oldRect = null;
            Main.map.mapView.repaint();
        }
    }

    /**
     * create rectangle out of two given corners
     */
    public Rectangle createRect(Point p1, Point p2) {
        int x,y,w,h;
        if (p1.x == p2.x && p1.y == p2.y) {
            //if p1 and p2 same points draw a small rectangle around them
            x = p1.x -1;
            y = p1.y -1;
            w = 3;
            h = 3;
        } else {
            if (p1.x < p2.x){
                x = p1.x;
                w = p2.x-p1.x;
            } else {
                x = p2.x;
                w = p1.x-p2.x;
            }
            if (p1.y < p2.y) {
                y = p1.y;
                h = p2.y-p1.y;
            } else {
                y = p2.y;
                h = p1.y-p2.y;
            }
        }
        return new Rectangle(x,y,w,h);
    }

    /**
     * Draw a selection rectangle on screen.
     */
    private void paintRect(Point p1, Point p2) {
        Graphics g = frame.getGraphics();//Main.map.mapView.getGraphics();

        Rectangle r = oldRect;
        if (r != null) {
            //overwrite old rct
            g.setXORMode(Color.BLACK);
            g.setColor(Color.WHITE);
            g.drawRect(r.x,r.y,r.width,r.height);
        }

        g.setXORMode(Color.BLACK);
        g.setColor(Color.WHITE);
        r = createRect(p1,p2);
        g.drawRect(r.x,r.y,r.width,r.height);
        oldRect = r;
    }


    public void setFrame(MapFrame mapFrame) {
        frame = mapFrame;
    }
}
