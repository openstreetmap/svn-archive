/*
   Copyright (C) 2004 Stephen Coast (steve@fractalus.com)

   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License
   as published by the Free Software Foundation; either version 2
   of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

 */


import java.lang.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;

import com.bbn.openmap.BufferedMapBean;
import com.bbn.openmap.proj.*;
import com.bbn.openmap.gui.*;
import com.bbn.openmap.event.*;
import com.bbn.openmap.*;


import com.bbn.openmap.layer.shape.ShapeLayer;

import java.util.Properties;



public class osmDisplay
{
  JLabel label = new JLabel("OpenStreetMap pre-pre-pre alpha");
  BufferedMapBean mapBean;

  osmServerClient osc = new osmServerClient();
  
  osmPointsLayer pointsLayer;
  
  public osmDisplay(Container cp)
  {

    MapHandler mh = new MapHandler();
    
    mapBean = new BufferedMapBean();

    mh.add(mapBean);

    MouseDelegator mouseDelegator = new MouseDelegator();
    
    mh.add(mouseDelegator);
  
    SelectMouseMode selectMouseMode = new SelectMouseMode();
    
    mh.add(selectMouseMode);
    
    mouseDelegator.setActive(selectMouseMode);
    

    pointsLayer = new osmPointsLayer(this);
    
    Properties shapeLayerProps = new Properties();

    shapeLayerProps.put("prettyName", "Recorded points");
    shapeLayerProps.put("lineColor", "000000");
    shapeLayerProps.put("fillColor", "BDDE83");
    pointsLayer.setProperties(shapeLayerProps);

    //mapBean.add(shapeLayer);
    mapBean.add(pointsLayer);

    // Add the map to the frame

    osmAppletButtons buttons = new osmAppletButtons(this);

    cp.add( buttons, BorderLayout.NORTH);    
    cp.add( mapBean, BorderLayout.CENTER);
    cp.add(label,    BorderLayout.SOUTH);

    mapBean.setScale(10404.917f);

    mapBean.setCenter(51.526447f, -0.14746371f);

    pointsLayer.setStartingUp(false);

  }


  
  public osmServerClient getServerClient()
  {
    return osc;

  } // getServerClient



  public void left()
  {

    Projection p = mapBean.getProjection();

    float left = p.getUpperLeft().getLongitude();
    float right = p.getLowerRight().getLongitude();

    mapBean.setCenter( mapBean.getCenter().getLatitude(),
        mapBean.getCenter().getLongitude() - (right-left)/4);

  } // left



  public void right()
  {

    Projection p = mapBean.getProjection();

    float left = p.getUpperLeft().getLongitude();
    float right = p.getLowerRight().getLongitude();

    mapBean.setCenter( mapBean.getCenter().getLatitude(),
        mapBean.getCenter().getLongitude() + (right-left)/4);

  } // right



  public void up()
  {

    Projection p = mapBean.getProjection();

    float up = p.getUpperLeft().getLatitude();
    float down = p.getLowerRight().getLatitude();

    mapBean.setCenter( mapBean.getCenter().getLatitude() + (up-down)/4,
        mapBean.getCenter().getLongitude());

  } // up



  public void down()
  {

    Projection p = mapBean.getProjection();

    float up = p.getUpperLeft().getLatitude();
    float down = p.getLowerRight().getLatitude();

    mapBean.setCenter( mapBean.getCenter().getLatitude() - (up-down)/4,
        mapBean.getCenter().getLongitude());

  } // down


  public void zoomin()
  {
    mapBean.setScale( mapBean.getScale() / 1.5f);

  } // zoomin


  public void zoomout()
  {
    mapBean.setScale( mapBean.getScale() * 1.5f);

  } // zoomout


  public void deletePoints()
  {
    if( checkLogin() )
    {
      pointsLayer.deleteSelectedPoints();

    }
    

  } // deletePoints


  public boolean checkLogin()
  {
    if( osc.loggedIn() )
    {
      return true;

    }

    JOptionPane.showMessageDialog((JFrame)null,
        "Not logged in, or login timeout",
        "Login",
        JOptionPane.WARNING_MESSAGE);

    return false;
    
  } // checkLogin


} // osmDisplay
