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
