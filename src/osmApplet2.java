import javax.swing.*;
import java.awt.*;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.layer.shape.ShapeLayer;

import java.util.Properties;




public class osmApplet2 extends JApplet {
    JLabel label = new JLabel("OpenStreetMap pre-pre-pre alpha. (control-)Mouse drag to do things");
    
    public osmApplet2() {
        getRootPane().putClientProperty("defeatSystemEventQueueCheck",
                                        Boolean.TRUE);
    } // osmApplet

    
    public void init() {

        //Add border.  Should use createLineBorder, but then the bottom
        //and left lines don't appear -- seems to be an off-by-one error.

        MapBean mapBean = new MapBean();
        getContentPane().add(label, BorderLayout.SOUTH);
        ShapeLayer shapeLayer = new ShapeLayer();
        Properties shapeLayerProps = new Properties();
        shapeLayerProps.put("prettyName", "Political Solid");
        shapeLayerProps.put("lineColor", "000000");
        shapeLayerProps.put("fillColor", "BDDE83");
        shapeLayer.setProperties(shapeLayerProps);

        // Add the political layer to the map
        mapBean.add(shapeLayer);

        // Add the map to the frame
        getContentPane().add(mapBean);

       

        
        
       // getContentPane().add(somepane, BorderLayout.CENTER);

        
    } // init

    
    public void setStatusLabel(String s)
    {

      label.setText(s);

      label.repaint();
    } 
}
