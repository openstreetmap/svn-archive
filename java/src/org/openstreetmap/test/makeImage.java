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

  } // main

  public BufferedImage getImageFromCoord(float latitude, float longitude, float scale)
  {

    MapBean mb = new MapBean();
//    mb.setScale(10404.917f);
//    mb.setCenter(51.526447f, -0.14746371f);

    mb.setScale(scale);
    mb.setCenter(latitude, longitude);

    OMGraphicList gl = getGraphics(mb.getProjection());

    BufferedImage bi = new BufferedImage(600,600, BufferedImage.TYPE_INT_RGB);

    Graphics g = bi.getGraphics();

    g.setColor(new Color(231,231,214));
    g.fillRect(0,0,600,600);

    g.setColor(Color.BLACK);
    gl.render(g);

    /*
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
    */

    return bi;

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

      float lon1 = ((Float)e.nextElement()).floatValue();
      float lat1 = ((Float)e.nextElement()).floatValue();
      float lon2 = ((Float)e.nextElement()).floatValue();
      float lat2 = ((Float)e.nextElement()).floatValue();

      System.out.println("adding street " + lon1 + "," + lat1 + " " + lon2 + "," + lat2);

      osmStreetSegment oml = new osmStreetSegment(lat1, lon1, lat2, lon2,
          com.bbn.openmap.omGraphics.geom.BasicGeometry.LINETYPE_STRAIGHT,
          id
          );

      omgl.add(oml);
    }

    v = osmSH.getPoints("applet",a.getLatitude(),a.getLongitude(),b.getLatitude(),b.getLongitude());

    e = v.elements();

    while( e.hasMoreElements() )
    {
      float lat = ((Float)e.nextElement()).floatValue();
      float lon = ((Float)e.nextElement()).floatValue();
      

      OMCircle omc = new OMCircle( lat,
          lon,
          5f,
          com.bbn.openmap.proj.Length.METER
          );

      omc.setLinePaint(Color.gray);
      omc.setSelectPaint(Color.red);
      omc.setFillPaint(OMGraphic.clear);

      
      omgl.add(omc);
    }


    omgl.generate(proj);


    osmSH.closeDatabase();

    return omgl;


  } // getGraphics



} // makeImage
