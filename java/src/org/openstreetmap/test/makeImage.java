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

// TODO rename this to something sensible, like ImageMaker?
// TODO sanity check for width and height?
public class makeImage
{

  /* added these so tiles can be smaller - TomC */
  public static final int DEFAULT_WIDTH = 600;
  public static final int DEFAULT_HEIGHT = 600;
  
  /* not sure these are needed, but they were magic numbers before - TomC */
  public static final float DEFAULT_LATITUDE = 51.526447f;
  public static final float DEFAULT_LONGITUDE = -0.14746371f;
  public static final float DEFAULT_SCALE = 10404.917f;
  
  private boolean logging = true;

  /** convenience method uses DEFAULT_WIDTH and DEFAULT_HEIGHT */
  public BufferedImage getImageFromCoord(float latitude, float longitude, float scale) 
  {
	return getImageFromCoord(DEFAULT_WIDTH,DEFAULT_HEIGHT,latitude,longitude,scale);
  }
  
  public BufferedImage getImageFromCoord(int width, int height, float latitude, float longitude, float scale)
  {
    Proj projection = new Mercator(new LatLonPoint(latitude, longitude), scale, width, height);
    
    OMGraphicList gl = getGraphics(projection);

    BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

    Graphics g = bi.getGraphics();

    g.setColor(Color.white);
    g.fillRect(0,0,width,height);

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

  private void log(String s) {
    System.out.println("makeImage: " + s);
  }
  
  private OMGraphicList getGraphics(Projection proj)
  {
    OMGraphicList omgl = new OMGraphicList(1000);

    osmServerHandler osmSH = new osmServerHandler();
    
    LatLonPoint a = proj.getUpperLeft();
    LatLonPoint b = proj.getLowerRight();

    Vector v;
    Enumeration e;
    /*
    v = osmSH.getStreets("applet",a.getLatitude(),a.getLongitude(),b.getLatitude(),b.getLongitude());

    e = v.elements();

    if (logging) log("reading streets...");

    while( e.hasMoreElements() )
    {

      int id = ((Integer)e.nextElement()).intValue();

      float lon1 = ((Float)e.nextElement()).floatValue();
      float lat1 = ((Float)e.nextElement()).floatValue();
      float lon2 = ((Float)e.nextElement()).floatValue();
      float lat2 = ((Float)e.nextElement()).floatValue();

//      if (logging) log("adding street " + lon1 + "," + lat1 + " " + lon2 + "," + lat2);

      osmStreetSegment oml = new osmStreetSegment(lat1, lon1, lat2, lon2,
          com.bbn.openmap.omGraphics.geom.BasicGeometry.LINETYPE_STRAIGHT,
          id
          );

      omgl.add(oml);
    }
    */
    /*
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
    */
    // start nodes

    v = osmSH.getNodes("applet",a.getLatitude(),a.getLongitude(),b.getLatitude(),b.getLongitude());
    e = v.elements();
    
    if (logging) log("got " + v.size() + " nodes");

    Vector nodes = new Vector();
    Hashtable nodesToPoints = new Hashtable();
	    
    while( e.hasMoreElements() )
    {
      try {	   
	     Vector v2 = (Vector)e.nextElement();
	     Enumeration e2 = v2.elements(); 
	      Integer id = (Integer)e2.nextElement();
	      nodes.add(id);
	      double lat = ((Double)e2.nextElement()).doubleValue();
	      double lon = ((Double)e2.nextElement()).doubleValue();
	      if (logging) log(id + " " + lat + " " + lon);
              nodesToPoints.put(id,new LatLonPoint(lat,lon));
      }
      catch (Exception ex) {
	if (logging) ex.printStackTrace();
      }
 /*     
      OMCircle omc = new OMCircle( lat,
          lon,
          5f,
          com.bbn.openmap.proj.Length.METER
          );

      omc.setLinePaint(Color.black);
      omc.setSelectPaint(Color.red);
      omc.setFillPaint(OMGraphic.clear);

      omgl.add(omc); */
    }

    if (logging) log("done adding nodes, got " + nodes.size() + " node ids");
    // finish nodes

    // start lines
    
    v = osmSH.getLines("applet", nodes);
    e = v.elements();    

    if (logging) log("got " + v.size() + " lines");
    
    while( e.hasMoreElements() )
    {
      try {
 	      Vector v2 = (Vector)e.nextElement();
 	      Enumeration e2 = v2.elements();
 
	      int id = ((Integer)e2.nextElement()).intValue();
	      
	      Integer nid1 = (Integer)e2.nextElement();
	      Integer nid2 = (Integer)e2.nextElement();
	      
 	      LatLonPoint n1 = (LatLonPoint)nodesToPoints.get(nid1);
 	      LatLonPoint n2 = (LatLonPoint)nodesToPoints.get(nid2);
 
	      if (n1 == null) { // n1 is outside the current projection, so wasn't included by getNodes
		     Vector v3 = osmSH.getNode("applet",nid1.toString());
		     Enumeration e3 = v3.elements();
	       	     float lat = ((Float)e3.nextElement()).floatValue();
		     float lon = ((Float)e3.nextElement()).floatValue();
		     n1 = new LatLonPoint(lat,lon);
		     nodesToPoints.put(nid1,n1); // not strictly necessary, but might be used later so I'll be consistent
		     nodes.add(nid1); // ditto
	      }
	      if (n2 == null) { // n2 is outside the current projection, so wasn't included by getNodes
		     Vector v3 = osmSH.getNode("applet",nid2.toString());
		     Enumeration e3 = v3.elements();
	       	     float lat = ((Float)e3.nextElement()).floatValue();
		     float lon = ((Float)e3.nextElement()).floatValue();
		     n2 = new LatLonPoint(lat,lon);
		     nodesToPoints.put(nid2,n2);
		     nodes.add(nid2);
	      }
	      osmStreetSegment oml = new osmStreetSegment( n1.getLatitude(), n1.getLongitude(), 
							   n2.getLatitude(), n2.getLongitude(),
			     				   com.bbn.openmap.omGraphics.geom.BasicGeometry.LINETYPE_STRAIGHT,
							   id );
	      omgl.add(oml);
      }
      catch (Exception ex) {
	      if (logging) ex.printStackTrace();
      }
    }

    // end lines
    
    omgl.generate(proj);

    osmSH.closeDatabase();

    return omgl;

  } // getGraphics

} // makeImage


