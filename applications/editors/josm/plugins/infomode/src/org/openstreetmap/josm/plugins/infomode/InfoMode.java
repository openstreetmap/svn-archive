/*
 * This file is part of InfoMode plugin for JOSM.
 * http://wiki.openstreetmap.org/wiki/JOSM/Plugins/InfoMode
 *
 * Licence: GPL v2 or later
 * Author:  Alexei Kasatkin, 2011
 * Thanks to authors of BuildingTools, ImproveWayAccuracy
 * for good sample code
 */
package org.openstreetmap.josm.plugins.infomode;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.gpx.WayPoint;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.text.DateFormat;
import java.util.Locale;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.gpx.GpxTrack;
import org.openstreetmap.josm.data.gpx.GpxTrackSegment;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.MapViewPaintable;
import org.openstreetmap.josm.tools.Shortcut;

class InfoMode extends MapMode implements MapViewPaintable, AWTEventListener {
    private MapView mv;
    private String statusText;
    private boolean drawing;
    private boolean ctrl;
    private boolean shift;
    private boolean oldCtrl;
    private boolean oldShift;
    private EastNorth pos;
    DateFormat df;

    InfoMode(MapFrame mapFrame) {
        super(tr("InfoMode"), "infomode.png", tr("GPX info mode"), Shortcut.registerShortcut(
                "mapmode/infomode",
                tr("Mode: {0}", tr("GPX info mode")),
                KeyEvent.VK_BACK_SLASH, Shortcut.GROUP_EDIT), mapFrame, Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT,DateFormat.DEFAULT, Locale.getDefault()); 
        
    }

// <editor-fold defaultstate="collapsed" desc="Event listeners">

    @Override
    public void enterMode() {
        System.out.println("entering mode");
        if (!isEnabled()) return;
        super.enterMode();
        System.out.println("enter mode");
        
        
        mv = Main.map.mapView;
        
        Main.map.mapView.addMouseListener(this);
        Main.map.mapView.addMouseMotionListener(this);
        Main.map.mapView.addTemporaryLayer(this);


        try {
            Toolkit.getDefaultToolkit().addAWTEventListener(this,
                    AWTEvent.KEY_EVENT_MASK);
        } catch (SecurityException ex) {
        }
    }

    @Override
    public void exitMode() {
        super.exitMode();
        System.out.println("exit mode");
        Main.map.mapView.removeMouseListener(this);
        Main.map.mapView.removeMouseMotionListener(this);

        Main.map.mapView.removeTemporaryLayer(this);

        try {
            Toolkit.getDefaultToolkit().removeAWTEventListener(this);
        } catch (SecurityException ex) {
        }
        
        repaint();
    }

    @Override
    public boolean layerIsSupported(Layer l) {
        return true;
    }



    //////////    Event listener methods

    @Override
    public void paint(Graphics2D g, MapView mv, Bounds bbox) {
        
        Layer l = Main.main.getActiveLayer();
        
        if (l instanceof GpxLayer && pos!=null) {
            GpxLayer gpxL = (GpxLayer )l;
            
            double minDist=1e9,d,len=0;
            WayPoint wp=null;
            GpxTrack trk=null;
            for (GpxTrack track : gpxL.data.tracks) {
                for (GpxTrackSegment seg : track.getSegments()) {
                    for (WayPoint S : seg.getWayPoints()) {
                        d = S.getEastNorth().distance(pos);
                        if (d<minDist && d<10) {
                            minDist = d;
                            wp=S;
                            trk=track;
                            }
                        if (track==trk) len+=seg.length();
                        }
                    }
            }
            if (wp!=null) {
                Point p = mv.getPoint(wp.getCoor());
                g.setColor(Color.RED);
                g.fillOval(p.x-10, p.y-10, 20, 20); // mark selected point
                p.translate(20, 5);
                g.setColor(Color.WHITE);
                g.fillRoundRect(p.x, p.y-25, 200, 50,10,10);
                g.setColor(Color.BLACK);
                g.drawString(df.format(wp.getTime()), p.x+5, p.y-10);
                String s = (String) trk.getAttributes().get("name");
                if (s!=null) g.drawString(s, p.x+5, p.y+5);
                System.out.println(trk.getAttributes().toString());                
                System.out.println(wp.attr.toString());                
                s = (String) wp.attr.get("ele");
                String s1=null;
                try {s1 = String.format("%3.1f", Double.parseDouble(s));} catch (Exception e) {  }
                if (s1!=null) g.drawString("H="+s1, p.x+5, p.y+20);
            }
            
        }
    }

    @Override
    public void eventDispatched(AWTEvent event) {
        updateKeyModifiers((InputEvent) event);
        if (event.getID() == KeyEvent.KEY_PRESSED) {
            doKeyEvent((KeyEvent) event);
        }
//        updateStatusLine();
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!isEnabled()) return;
        if (e.getButton() != MouseEvent.BUTTON1) return;
        
        //setStatusLine(tr("Please move the mouse to draw new way"));
        repaint();

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (!isEnabled()) return;
        if (e.getButton() != MouseEvent.BUTTON1) return;
        
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (!isEnabled()) return;
        pos = mv.getEastNorth(e.getX(), e.getY());
        repaint();
    }

    private void doKeyEvent(KeyEvent e) {
        ///  System.out.println(e);
        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
        }
    }

    /**
     * Updates shift and ctrl key states
     */
    private void updateKeyModifiers(InputEvent e) {
        oldCtrl = ctrl;
        oldShift = shift;
        ctrl = (e.getModifiers() & ActionEvent.CTRL_MASK) != 0;
        shift = (e.getModifiers() & ActionEvent.SHIFT_MASK) != 0;
    }

    @Override
    protected void updateStatusLine() {
        Main.map.statusLine.setHelpText(statusText);
        Main.map.statusLine.repaint();
    }
// </editor-fold>



    private void repaint() {
        Main.map.mapView.repaint();
    }
    private void setStatusLine(String tr) {
        statusText=tr;
        updateStatusLine();
    }

    

    
}
