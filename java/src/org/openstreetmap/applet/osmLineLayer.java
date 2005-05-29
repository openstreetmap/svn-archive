/*
 * Copyright (C) 2004 Stephen Coast (steve@fractalus.com)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 *  
 */
package org.openstreetmap.applet;

import java.awt.Graphics;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.openstreetmap.client.osmServerClient;
import org.openstreetmap.util.Logger;
import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.Layer;
import com.bbn.openmap.event.LayerStatusEvent;
import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;

public class osmLineLayer extends Layer {

    private int nodeSelected = -1;
    private osmServerClient serverClient;
    private OMGraphicList nodeGraphics;
    private OMGraphicList lineGraphics;
    private osmAppletLineDrawListener oLDL;
    private osmDisplay display;
    private boolean bStartingUp = false;
    private Hashtable nodeHashTable = new Hashtable();

    public osmLineLayer(osmDisplay oDisplay) {
        super();
        display = oDisplay;
        serverClient = display.getServerClient();
        oLDL = new osmAppletLineDrawListener(display, this);
        nodeGraphics = new OMGraphicList(50);
        lineGraphics = new OMGraphicList(50);
    }

    public osmAppletLineDrawListener getMouseListener() {
        return oLDL;
    }

    public void projectionChanged(ProjectionEvent pe) {
        Projection proj = setProjection(pe);
        Logger.log("proj change on line layer to" + proj);
        if (proj != null) {
            Logger.log("projection changed...");
            createGraphicsAndRepaint();
        }
        fireStatusUpdate(LayerStatusEvent.FINISH_WORKING);
    }

    private void createGraphicsAndRepaint() {
        if (display.startingUp()) {
            return;
        }
        nodeGraphics.clear();
        lineGraphics.clear();
        Projection proj = getProjection();
        LatLonPoint upperLeft = proj.getUpperLeft();
        LatLonPoint lowerRight = proj.getLowerRight();
        nodeHashTable = serverClient.getNodes(upperLeft, lowerRight);
        {
	        Enumeration en = nodeHashTable.elements();
	        while (en.hasMoreElements()) {
	            Node n = (Node) en.nextElement();
	            nodeGraphics.add(n);
	        }
        }
        {
            Vector lines = serverClient.getLines(nodeHashTable);
	        Enumeration en = lines.elements();
	        while (en.hasMoreElements()) {
	            OMGraphic g = (OMGraphic) en.nextElement();
	            lineGraphics.add(g);
	        }
        }
        nodeGraphics.generate(proj);
        lineGraphics.generate(proj);
        repaint();
    }

    public void paint(Graphics g) {
        lineGraphics.render(g);
        nodeGraphics.render(g);
    }

    public void setMouseListen(boolean bYesNo) {
        oLDL.setMouseListen(bYesNo);
    }

    public MapMouseListener getMapMouseListener() {
        Logger.log("asked for maplistener");
        return oLDL;
    }

    public void addNode(LatLonPoint p) {
        if (!display.checkLogin()) {
            // not logged in
            return;
        }
        Logger.log("trying to add node  " + p.getLatitude() + "," + p.getLongitude());
        int uid = serverClient.addNode((double) p.getLatitude(), (double) p.getLongitude());
        if (uid != -1) {
            Node n = new Node(uid, (double) p.getLatitude(), (double) p.getLongitude());
            nodeHashTable.put("" + n.getUID(), n);
            nodeGraphics.add(n);
            nodeGraphics.generate(getProjection(), true);
            repaint();
            display.paintBean();
        }
    }

    public void moveNode(LatLonPoint p) {
        int x = (int) getProjection().forward(p).getX();
        int y = (int) getProjection().forward(p).getY();
        if (nodeSelected != -1) {
            Logger.log("moving node..." + nodeSelected);
            // move it
            if (serverClient.moveNode(nodeSelected, (double) p.getLatitude(), (double) p.getLongitude())) {
                createGraphicsAndRepaint();
            }
            nodeSelected = -1;
        }
        else {
            // find a node to move!
            OMCircle g = (OMCircle) nodeGraphics.findClosest(x, y, 10);
            if (g != null) {
                nodeSelected = ((Node) g).getUID();
                Logger.log("selected a node! " + nodeSelected);
            }
        }
    }

    public void deleteNode(LatLonPoint p) {
        //FIXME : put up a 'r u sure?' dialog
        int x = (int) getProjection().forward(p).getX();
        int y = (int) getProjection().forward(p).getY();
        OMCircle g = (OMCircle) nodeGraphics.findClosest(x, y, 10);
        if (g != null) {
            serverClient.deleteNode(((Node) g).getUID());
        }
        createGraphicsAndRepaint();
    }

    public boolean newLine(LatLonPoint p) {
        int x = (int) getProjection().forward(p).getX();
        int y = (int) getProjection().forward(p).getY();
        if (nodeSelected != -1) {
            Logger.log("linking node..." + nodeSelected);
            // move it
            OMCircle g = (OMCircle) nodeGraphics.findClosest(x, y, 10);
            if (g != null) {
                int n = ((Node) g).getUID();
                if (n != -1 && n != nodeSelected) {
                    int nLineUID = serverClient.newLine(nodeSelected, n);
                    if (nLineUID != -1) {
                        createGraphicsAndRepaint();
                    }
                }
            }
            nodeSelected = -1;
            return false;
        }
        else {
            // find a node to move!
            OMCircle g = (OMCircle) nodeGraphics.findClosest(x, y, 10);
            if (g != null) {
                nodeSelected = ((Node) g).getUID();
                Logger.log("selected a node! " + nodeSelected);
                return true;
            }
        }
        return false;
    }
    
}
