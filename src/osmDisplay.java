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


import com.bbn.openmap.layer.shape.ShapeLayer;

import java.util.Properties;



public class osmDisplay
{
    JLabel label = new JLabel("OpenStreetMap pre-pre-pre alpha");
    BufferedMapBean mapBean;
    
    public osmDisplay(Container cp)
    {

        mapBean = new BufferedMapBean();
        
        osmPointsLayer shapeLayer = new osmPointsLayer();
        Properties shapeLayerProps = new Properties();
        
        shapeLayerProps.put("prettyName", "temporary points");
        shapeLayerProps.put("lineColor", "000000");
        shapeLayerProps.put("fillColor", "BDDE83");
        shapeLayer.setProperties(shapeLayerProps);

        // Add the political layer to the map
        mapBean.add(shapeLayer);

        // Add the map to the frame

        osmAppletButtons buttons = new osmAppletButtons(this);

        cp.add( buttons, BorderLayout.NORTH);    
        cp.add( mapBean, BorderLayout.CENTER);
        cp.add(label,    BorderLayout.SOUTH);
        
        mapBean.setScale(400000);
        mapBean.setCenter(51.4f, 0.0f);

    }

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
     

} // osmDisplay
