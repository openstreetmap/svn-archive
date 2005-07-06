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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.util.Properties;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.openstreetmap.client.osmServerClient;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.MouseDelegator;
import com.bbn.openmap.event.SelectMouseMode;
import com.bbn.openmap.proj.Projection;

public class osmDisplay {

    public static final int MODE_POINTS = 1;
    public static final int MODE_LINES = 2;
    public static final int MODE_AREAS = 3;
    
    boolean bStartingUp = true;
    int nCurrentMode = MODE_LINES;
    JLabel label = new JLabel("OpenStreetMap pre-pre alpha");
    MapBean mapBean;
    osmServerClient osc = new osmServerClient();
    osmPointsLayer pointsLayer;
    osmSelectLayer selectLayer;
    osmLineLayer lineLayer;

    public osmDisplay(float fScale, float fLat, float fLon, Container cp) {
        MapHandler mh = new MapHandler();
        mapBean = new MapBean();
        mh.add(mapBean);
        MouseDelegator mouseDelegator = new MouseDelegator();
        mh.add(mouseDelegator);
        SelectMouseMode selectMouseMode = new SelectMouseMode();
        mh.add(selectMouseMode);
        mouseDelegator.setActive(selectMouseMode);
        selectLayer = new osmSelectLayer(this);
        pointsLayer = new osmPointsLayer(this);
        lineLayer = new osmLineLayer(this);
        Properties shapeLayerProps = new Properties();
        shapeLayerProps.put("prettyName", "Recorded points");
        shapeLayerProps.put("lineColor", "000000");
        shapeLayerProps.put("fillColor", "efefde");
        pointsLayer.setProperties(shapeLayerProps);
        selectLayer.setVisible(true);
        osmAppletButtons buttons = new osmAppletButtons(this, lineLayer.getMouseListener());
        cp.add(buttons, BorderLayout.NORTH);
        cp.add(mapBean, BorderLayout.CENTER);
        cp.add(label, BorderLayout.SOUTH);
        mapBean.setScale(fScale);
        mapBean.setCenter(fLat, fLon);
        mapBean.add(selectLayer);
        mapBean.add(lineLayer);
        mapBean.add(pointsLayer);
        mapBean.setBackgroundColor(Color.white);
        bStartingUp = false;
        setMode(MODE_LINES);
    }

    public boolean startingUp() {
        return bStartingUp;
    }

    public void paintBean() {
        System.out.println("repainting bean");
        mapBean.setBufferDirty(true);
        mapBean.repaint();
    }

    public osmServerClient getServerClient() {
        return osc;
    }

    public osmSelectLayer getSelectLayer() {
        return selectLayer;
    }

    public void left() {
        Projection p = mapBean.getProjection();
        float left = p.getUpperLeft().getLongitude();
        float right = p.getLowerRight().getLongitude();
        mapBean.setCenter(mapBean.getCenter().getLatitude(), mapBean.getCenter().getLongitude() - (right - left) / 4);
    }

    public void right() {
        Projection p = mapBean.getProjection();
        float left = p.getUpperLeft().getLongitude();
        float right = p.getLowerRight().getLongitude();
        mapBean.setCenter(mapBean.getCenter().getLatitude(), mapBean.getCenter().getLongitude() + (right - left) / 4);
    }

    public void up() {
        Projection p = mapBean.getProjection();
        float up = p.getUpperLeft().getLatitude();
        float down = p.getLowerRight().getLatitude();
        mapBean.setCenter(mapBean.getCenter().getLatitude() + (up - down) / 4, mapBean.getCenter().getLongitude());
    }

    public void down() {
        Projection p = mapBean.getProjection();
        float up = p.getUpperLeft().getLatitude();
        float down = p.getLowerRight().getLatitude();
        mapBean.setCenter(mapBean.getCenter().getLatitude() - (up - down) / 4, mapBean.getCenter().getLongitude());
    }

    public void zoomin() {
        mapBean.setScale(mapBean.getScale() / 1.5f);
    }
    
    public void zoomout() {
        mapBean.setScale(mapBean.getScale() * 1.5f);
    }

    public void deletePoints() {
        if (checkLogin()) {
            pointsLayer.deleteSelectedPoints();
        }
    }

    public boolean checkLogin() {
        if (osc.loggedIn()) {
            return true;
        }
        osmAppletLoginWindow loginWindow = new osmAppletLoginWindow((JFrame) null, true, this);
        return osc.loggedIn();
    }

    public void setMode(int n) {
        nCurrentMode = n;
        System.out.println("switching to mode " + n);
        switch (nCurrentMode) {
        case MODE_LINES:
            pointsLayer.setMouseListen(false);
            lineLayer.setMouseListen(true);
            break;
        }
    }
}
