package org.openstreetmap.test;

import com.bbn.openmap.proj.*;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.*;
import java.lang.*;
import java.util.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import org.openstreetmap.server.osmServerHandler;
import org.openstreetmap.applet.osmStreetSegment;


public class makeImage
{

  public static void main(String[] args)
  {
    new makeImage().go();


  } // main

  public void go()
  {

    MapBean mb = new MapBean();
    mb.setScale(10404.917f);
    mb.setCenter(51.526447f, -0.14746371f);

    OMGraphicList gl = getGraphics(mb.getProjection());

    gl.add( new OMCircle(51.526447f, -0.14746371f,10f,com.bbn.openmap.proj.Length.METER) );

    BufferedImage bi = new BufferedImage(600,600, BufferedImage.TYPE_INT_RGB);

    Graphics g = bi.getGraphics();

    g.setColor(new Color(231,231,214));
    g.fillRect(0,0,600,600);

    g.setColor(Color.BLACK);
    gl.render(g);


    try
    {
      // Save as PNG
      File file = new File("newimage.png");
      ImageIO.write(bi, "png", file);
    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();

    }

  }

  private OMGraphicList getGraphics(Projection proj)
  {
    OMGraphicList omgl = new OMGraphicList(1000);

    osmServerHandler osmSH = new osmServerHandler();
    
    LatLonPoint a = proj.getUpperLeft();
    LatLonPoint b = proj.getLowerRight();

    Vector v = osmSH.getStreets("applet",a.getLatitude(),a.getLongitude(),b.getLatitude(),b.getLongitude());

    Enumeration e = v.elements();

    System.out.println("reading streets...");

    while( e.hasMoreElements() )
    {

      int id = ((Integer)e.nextElement()).intValue();

      float lon1 = (float)((Double)e.nextElement()).doubleValue();
      float lat1 = (float)((Double)e.nextElement()).doubleValue();
      float lon2 = (float)((Double)e.nextElement()).doubleValue();
      float lat2 = (float)((Double)e.nextElement()).doubleValue();

      System.out.println("adding street " + lon1 + "," + lat1 + " " + lon2 + "," + lat2);

      osmStreetSegment oml = new osmStreetSegment(lat1, lon1, lat2, lon2,
          com.bbn.openmap.omGraphics.geom.BasicGeometry.LINETYPE_STRAIGHT,
          id
          );

      omgl.add(oml);
    }

    omgl.generate(proj);

    return omgl;



  } // getGraphics


} // makeImage
