/*
 * Copyright (C) 2005 Tom Carden (tom@somethingmodern.com), Steve Coast (steve@asklater.com)
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


package org.openstreetmap.client;

import org.openstreetmap.util.Point;
import org.openstreetmap.processing.OSMApplet;
import java.lang.*;
import java.util.*;
import processing.core.PImage;

public class Tile extends Thread
{
  private static final int tileWidth = 256;
  private static final int tileHeight = 128;

  private static final double PI = 3.14159265358979323846;

	private static final double lat_range = PI;
	private static final double lon_range = PI;

  private long zoom;

  private long windowWidth;
  private long windowHeight;

  private long widthOfWorld;
  private long heightOfWorld;
    
  private double lat;
  private double lon;

  private double centerX;
  private double centerY;

  private long leftX;
  private long rightX;
  private long topY;
  private long botY;

  // we're ignoring the wms URL provided by the <applet> tag at the moment
  // FIXME make it take a set of wms URLs to plot
  private String wmsURL = "http://www.openstreetmap.org/tile/0.1/wms?map=/usr/lib/cgi-bin/steve/wms.map&service=WMS&WMTVER=1.0.0&REQUEST=map&STYLES=&TRANSPARENT=TRUE";

  OSMApplet applet;

  Hashtable images = new Hashtable();

  Vector imv = new Vector();

  ImFetch imf;
  VFetch vf;

  public boolean viewChanged = false;
  long timeChanged;
  
  public Tile(OSMApplet p, String url, double la, double lo, int wW, int wH, int z)
  {
    
    applet = p;
    wmsURL = url;
    
  	// NOTE:
	  // lat is actually the Mercator "y" value
  	// the input la ranges from -77 to +77 degrees (or something), so the
	  // output of this function is between plus and minus 2.1721218
  	// this is in lat_range for reference.
    lat = Math.log(Math.tan( (PI / 4.0) + (PI * la / 360.0) ) );
    
  	// the range of this variable is -PI to PI?
    lon = PI * lo / 180.0;

    windowWidth = (long)wW;
    windowHeight = (long)wH;
    zoom = (long)z;

    widthOfWorld = 512 * (long)Math.pow(2, (double)zoom);
    heightOfWorld = 512 * (long)Math.pow(2, (double)zoom);

  	// this is the center of the tile in "world" units - a zero origin
	  // coordinate system with range -widthOfWorld/2 to +widthOfWorld/2
    centerX = (double)(lon / lon_range * (double)(widthOfWorld / 2));
    centerY = (double)(lat / lat_range * (double)(heightOfWorld / 2));

    imf = new ImFetch(this);
    imf.start();

    vf = new VFetch(this);
    vf.start();

    recalc();
  } // tile

  
  private void recalc()
  {
    
    leftX = (long)Math.floor( (centerX - (windowWidth / 2)) / tileWidth);
    rightX = (long)Math.ceil( (centerX + (windowWidth / 2)) / tileWidth);

    topY = (long)Math.floor( (centerY - (windowHeight / 2)) / tileHeight);
    botY = (long)Math.ceil( (centerY + (windowHeight / 2)) / tileHeight);

    /*
    System.out.println(" lon(0) = " + lon(0) );
    System.out.println(" x(lon(0)) = " + x(lon(0)) );

    System.out.println(" lat(0) = " + lat(0) );
    System.out.println(" y(lat(0)) = " + y(lat(0)) );
    */
  } // recalc

  public void drag(int dx, int dy)
  {
    updateChange();
    centerX += dx;
    centerY += dy;

    recalc();
    removeUnusedTiles();
    applet.reProject();
    grabTiles();

    applet.redraw();

  } // drag

  private void grabTiles()
  {
    for(long x = leftX; x < rightX + 1; x++)
    {
      for(long y = topY; y < botY + 1; y++)
      {
        //System.out.println("would grab tile " + x + ", " + y + " (" + pXtoLon(x*tileWidth) + ", " + pYtoLat(y*tileHeight) + ") -> (" + pXtoLon((1+x)*tileWidth) + ", " + pYtoLat((1+y)*tileHeight) + ")"  + " would put tile at (" + ((x*tileWidth)-centerX+(windowWidth/2)) + ", " + ((y*tileHeight)-centerY+(windowHeight/2)) + ")");

        String u = wmsURL + "&LAYERS=landsat" + "&bbox="+ pXtoLon(x*tileWidth) +","+ pYtoLat((y-1)*tileHeight) +","+ pXtoLon((1+x)*tileWidth) +","+pYtoLat(y*tileHeight)+"&width="+tileWidth+"&height="+tileHeight;
        ImBundle ib = new ImBundle(x,y,u,"landsat");
        if( !contains( ib ) )
        {
          imv.add( ib );
        }

        u = wmsURL + "&format=png&LAYERS=gpx" + "&bbox="+ pXtoLon(x*tileWidth) +","+ pYtoLat((y-1)*tileHeight) +","+ pXtoLon((1+x)*tileWidth) +","+pYtoLat(y*tileHeight)+"&width="+tileWidth+"&height="+tileHeight;

        ib = new ImBundle(x,y,u,"gpx");
        if( !contains( ib ) )
        {
          imv.add( ib );
        }

      }
    }


  } // grabTiles

  public void downloadImage(ImBundle ib)
  {

    System.out.println("Trying to download image " + ib.s);

    PImage i = applet.loadImage(ib.s);

    if( i == null || i.width == 0 || i.height == 0)
    {
      System.out.println("BAD IMAGE: " + ib.s);
    }
    else
    {
      addImage(ib.key, i);
      applet.redraw();
    }

  } // getImage

  private synchronized void addImage(String key, PImage img)
  {
    System.out.println("adding image " + key);
    images.put(key, img);
  } // addImage

  private synchronized boolean contains(ImBundle ib)
  {
    // is the image already downloaded or in the queue?
    if( images.containsKey(ib.key))
    {
      return true;
    }

    // the following should really be replaced with imv.contains(ib)
    // but it wouldn't work for some reason

    Enumeration e = imv.elements();

    while(e.hasMoreElements())
    {
      ImBundle iother = (ImBundle)e.nextElement();
      if( iother.equals(ib))
      {
        return true;
      }
    }

    return false;
  } // contains

  private synchronized PImage getImage(String key)
  {
    Object a = images.get(key);
    if( a == null)
    {
      return null;
    }
    return (PImage)a;
  } // getImage

  
  private synchronized void removeUnusedTiles()
  {
    // build a new hashtable with the images we want
    Hashtable ht = new Hashtable();
    Vector v = new Vector();
    for(long x = leftX; x < rightX + 1; x++)
    {
      for(long y = topY; y < botY + 1; y++)
      {
        String mykey = "landsat_" + x + "," + y;
        
        if( images.containsKey(mykey))
        {
          PImage pi = (PImage)images.get(mykey);
          ht.put(mykey, pi);
        }
        else
        {
           imf.remove(mykey);
        }

        mykey = "gpx_" + x + "," + y;

        if( images.containsKey(mykey))
        {
          PImage pi = (PImage)images.get(mykey);
          ht.put(mykey, pi);
        }
        else
        {
           imf.remove(mykey);
        }

      }
    }

    images = null;
    images = ht;
    
  } // removeUnusedTiles

  
  public String toString()
  {
    return "[tile.java lat,long = (" + lat + "," + lon + ") world width,height = (" + widthOfWorld + "," + heightOfWorld + ") center = (" + centerX + "," + centerY + ") tile bounds: (" + leftX + " -> " + rightX + ", " + topY + " -> " + botY + ")]";

  } // toString


  public void run()
  {
    System.out.println("would run tile here");

    grabTiles();

  } // run


  // turns "world units" into degrees?
  private double pXtoLon(double pX)
  { //     (degrees   )  (unit to lon) (pixel to unit)
    return (180.0 / PI) * lon_range * (2.0 * pX / (double)widthOfWorld);
  } // pXtoLon

  // x from lon
  public double x(double l)
  {
    return ((l * PI * widthOfWorld) / (360.0 * lon_range))- centerX+(windowWidth/2);
  } // lonToX

  public double lon(double x)
  {
    return -(360.0 * lon_range * (- centerX + (windowWidth / 2) -x)) / (PI * widthOfWorld);
  } // lon


  private double pYtoLat(double pY)
  {
    // the mercator y value found from inverse of line 78
    double merc_y = lat_range * (2.0 * pY / (double)heightOfWorld);
    // transform merc_y back to latitude in degrees
    return (180.0 / PI) * (2.0 * Math.atan(Math.exp(merc_y)) - PI / 2.0);
  } // pYtoLat

  // y from lat
  public double y(double l)
  {
    return centerY + (windowHeight / 2.0) - ( (heightOfWorld * Math.log(Math.tan( (90.0 + l) * PI / 360.0 ))) / (2.0 * lat_range) );
  } // y

  // lat from y
  public double lat(double y)
  {
    return (180.0 * 

        ((2.0 * Math.atan(Math.exp(  ( lat_range * ( 2.0 * centerY + windowHeight - 2.0 * y)) / heightOfWorld ) )) - PI/2)


        ) / PI;
  } // lat


  public Point getTopLeft()
  {
    return new Point(
        lat(0),
        lon(0)
        );
  } // getTopLeft

  public Point getBotRight()
  {
    return new Point(
        lat(windowHeight),
        lon(windowWidth) 
        );
  } // getBotRight


  public synchronized void draw()
  {
    //System.out.println("Drawing tiles...");
    applet.background(100);
    for(long x = leftX; x < rightX + 1; x++)
    {
      for(long y = topY; y < botY + 1; y++)
      {
        

        PImage p_gpx = getImage("gpx_" + x + "," + y);
        PImage p_landsat = getImage("landsat_" + x + "," + y);
        if( p_gpx == null && p_landsat == null)
        {
          applet.stroke(255);
          applet.fill(255);
          applet.text("Loading tile...", (int)(((x+.5)*tileWidth)-(long)centerX+(windowWidth/2)), (int)(windowHeight - (  ((y+.5)*tileHeight) -(long)centerY+(windowHeight/2)) ));
        }
        else
        {   
          if(p_landsat != null)
          {
            applet.image(p_landsat, (x*tileWidth)-(long)centerX+(windowWidth/2), windowHeight - ((y*tileHeight)-(long)centerY+(windowHeight/2)) );
          }

          if(p_gpx != null)
          {
            applet.image(p_gpx, (x*tileWidth)-(long)centerX+(windowWidth/2), windowHeight - ((y*tileHeight)-(long)centerY+(windowHeight/2)) );
          }
        }
         
      }
    }

  } // draw


  public float kilometersPerPixel()
  {
    return (float)((40008.0 / 360.0) *  45.0 * (float)Math.pow(2.0, -6 -(double)zoom));
  } // kilometersPerPixel

  public synchronized ImBundle getEle()
  {

    Object[] t = imv.toArray();

    Arrays.sort( t, new IMBComparator( ((double)rightX + (double)leftX)/2.0, ((double)botY + (double)topY)/2.0 ));
 
    for(int n = 0; n < t.length; n++)
    {
      ImBundle i = (ImBundle)t[n];
      if(i.key.startsWith("gpx"))
      {
        imv.remove(i);
        return i;
      }
    }

    ImBundle ib = (ImBundle)t[0];
    imv.remove(ib);
    
    //System.out.println("getEle " + ib.key);
    return ib;
  } // getEle

  private void zoom()
  {
    // call this after modifying the zoom level
    //
    // the zoom functions should be synchronized? it causes the applet to hang :-/
    // 
   
    updateChange();

    applet.recalcStrokeWeight();

    widthOfWorld = 512 * (long)Math.pow(2, (double)zoom);
    heightOfWorld = 512 * (long)Math.pow(2, (double)zoom);


    centerX = (double)(lon / lon_range * (double)(widthOfWorld / 2));
    centerY = (double)(lat / lat_range * (double)(heightOfWorld / 2));
   
    recalc();
    
    images.clear();
    applet.reProject();
    grabTiles();

    applet.redraw();
  } // zoom
  
  public void zoomin()
  {
    zoom++;
    zoom();
  }
  public void zoomout()
  {
    zoom--;
    if(zoom < 14)
    {
      zoom = 14;
    }
    zoom();
  }
  public long getzoom() 
  {
    return zoom;
  }

  private void updateChange()
  {
    timeChanged = System.currentTimeMillis();
    viewChanged = true;
  } // updateChange

} // Tile


class ImFetch extends Thread
{

  Tile tiles;

  public ImFetch(Tile t)
  {
    tiles = t;
  } // QueueThread

  public void run()
  {
    while (true)
    {
      wait(1000);
      while(!tiles.imv.isEmpty())
      {
        if( !tiles.imv.isEmpty() ){
          ImBundle s = tiles.getEle();
          tiles.downloadImage(s);
        }

      }
    }
  }

  public void wait(int milliseconds)
  {
    try {sleep(milliseconds);} catch(Exception e){}
  }

  public synchronized void remove(String s)
  {
    Enumeration e = tiles.imv.elements();
    while(e.hasMoreElements())
    {
      ImBundle ib = (ImBundle)e.nextElement();
      if(ib.key.equals(s))
      {
        tiles.imv.remove(ib);
      }

    }
  } // remove


} // ImFetch

class ImBundle
{
  long x;
  long y;
  String s;
  String key;
  String type;

  public ImBundle(long xx, long yy)
  {
    x = xx;
    y = yy;
    key = x + "," + y;
  } // ImBundle

  public ImBundle(long xx, long yy, String ss, String t)
  {
    x = xx;
    y = yy;
    s = ss;
    type = t; 
    key = t + "_" + x + "," + y;
  } // ImBundle

  public boolean equals(ImBundle other)
  {
    return x == other.x && y == other.y && type.equals(other.type);
  }

  public double distance(double xx, double yy)
  {
     return Math.pow( ((double)x-xx),2) + Math.pow( ((double)y-yy),2);
  }

} // ImBundle

class IMBComparator implements java.util.Comparator
{
  double cx;
  double cy;
  
  public IMBComparator(double x, double y)
  {
    cx = x;
    cy = y;
  }
  public int compare(Object a, Object b)
  {
    ImBundle aa = (ImBundle)a;
    ImBundle bb = (ImBundle)b;

    double ad = aa.distance(cx,cy);
    double bd = bb.distance(cx,cy);

    if(ad == bd)
    {
      return 0;
    }

    if(ad < bd)
    {
      return -1;
    }
    else
    {
      return 1;
    }

  } // compare

} // IMBComparator



class VFetch extends Thread
{

  Tile tiles;

  public VFetch(Tile t)
  {
    tiles = t;
  } // QueueThread

  public void run()
  {
    while (true)
    {
      wait(1000);
      if(tiles.viewChanged && tiles.timeChanged < System.currentTimeMillis() - 10000)
      {
        tiles.viewChanged = false;
        tiles.applet.lines.clear();
        tiles.applet.nodes.clear();

        tiles.applet.redraw();
        tiles.applet.osm.getNodesAndLines(tiles.getTopLeft(),tiles.getBotRight(), tiles);
        tiles.applet.redraw();
      }
    }
  }

  public void wait(int milliseconds)
  {
    try {sleep(milliseconds);} catch(Exception e){}
  }

} // VFetch


