import java.lang.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.proj.*;


import com.bbn.openmap.layer.shape.ShapeLayer;

import java.util.Properties;



public class osmDisplay
{
    JLabel label = new JLabel("OpenStreetMap pre-pre-pre alpha. (control-)Mouse drag to do things");

    public osmDisplay(Container cp)
    {

        MapBean mapBean = new MapBean();
        cp.add(label, BorderLayout.SOUTH);
        
        osmPointsLayer shapeLayer = new osmPointsLayer();
        Properties shapeLayerProps = new Properties();
        
        shapeLayerProps.put("prettyName", "Political Solid");
        shapeLayerProps.put("lineColor", "000000");
        shapeLayerProps.put("fillColor", "BDDE83");
        shapeLayer.setProperties(shapeLayerProps);

        // Add the political layer to the map
        mapBean.add(shapeLayer);

        // Add the map to the frame
        cp.add(mapBean);

        Projection mp = mapBean.getProjection();
    
        System.out.println(" " + mp.getUpperLeft());
        
        mapBean.setScale(400000);
        mapBean.setCenter(51.4f, 0.0f);


        System.out.println(" " + mp.getUpperLeft());
    }
 
} // osmDisplay
