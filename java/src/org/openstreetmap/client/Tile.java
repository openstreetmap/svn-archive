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

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import java.lang.Math.*;

import org.openstreetmap.gui.MsgBox;
import org.openstreetmap.processing.OsmApplet;
import org.openstreetmap.util.Point;

import processing.core.PImage;

public class Tile extends Thread {
	private static final int tileWidth = 256;
	private static final int tileHeight = 128;
	private static final double PI = 3.14159265358979323846;
	private static final double lat_range = PI;
	private static final double lon_range = PI;

	public long zoom;
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
  // private String wmsURL = "http://www.openstreetmap.org/tile/0.1/wms?map=/usr/lib/cgi-bin/steve/wms.map&service=WMS&WMTVER=1.0.0&REQUEST=map&STYLES=&TRANSPARENT=TRUE";
  private String[] wmsURL;

  public int y_x = 65480;
  public int y_y = 21963;
  public int y_x_max = 65480;
  public int y_y_max = 21963;

  OsmApplet applet;
  Map images = new HashMap();

  // FIXME: Multiple threads are accessing this vector. 
  // If you replace the enumerations with iterations, you can see the concurrent 
  // modifications as exceptions. Multiple access on enumerations yields undefined behaviour.
  Vector imv = new Vector();
  ImFetch imf;
  VFetch vf;

  public boolean viewChanged = false;

  long timeChanged;

  public Tile(OsmApplet p, String url, double la, double lo, int wW, int wH,
      int z) {

    applet = p;
    wmsURL = url.split(";");

    // NOTE:
    // lat is actually the Mercator "y" value
    // the input la ranges from -77 to +77 degrees (or something), so the
    // output of this function is between plus and minus 2.1721218
    // this is in lat_range for reference.
    lat = Math.log(Math.tan((PI / 4.0) + (PI * la / 360.0)));

    // the range of this variable is -PI to PI?
    lon = PI * lo / 180.0;

    windowWidth = wW;
    windowHeight = wH;
    zoom = z;

    widthOfWorld = 512 * (1 << zoom);
    heightOfWorld = 512 * (1 << zoom);

    // this is the center of the tile in "world" units - a zero origin
    // coordinate system with range -widthOfWorld/2 to +widthOfWorld/2
    centerX = (lon / lon_range * (widthOfWorld / 2));
    centerY = (lat / lat_range * (heightOfWorld / 2));

    imf = new ImFetch(this);
    imf.start();

    vf = new VFetch(this);
    vf.start();

    recalc();
  } // tile

  private void recalc() {
    leftX = (long)Math.floor((centerX - (windowWidth / 2)) / tileWidth);
    rightX = (long)Math.ceil((centerX + (windowWidth / 2)) / tileWidth);

    topY = (long)Math.floor((centerY - (windowHeight / 2)) / tileHeight);
    botY = (long)Math.ceil((centerY + (windowHeight / 2)) / tileHeight);

    /*
     * System.out.println(" lon(0) = " + lon(0) ); System.out.println("
     * x(lon(0)) = " + x(lon(0)) );
     * 
     * System.out.println(" lat(0) = " + lat(0) ); System.out.println("
     * y(lat(0)) = " + y(lat(0)) );
     */
  } // recalc

  public void drag(int dx, int dy) {
    updateChange();
    centerX += dx;
    centerY += dy;

    // lat = Math.log(Math.tan( (PI / 4.0) + (PI * la / 360.0) ) );
    // lon = PI * lo / 180.0;

    lat = Math.log(Math.tan((PI / 4.0) + (PI * lat(windowHeight / 2) / 360.0)));
    lon = PI * lon(windowWidth / 2) / 180.0;

    recalc();
    removeUnusedTiles();
    applet.reProject();
    grabTiles();

    System.out.println(lat + "," + lon);

    applet.redraw();
  } // drag

  private void grabTiles() {
    /*for (long x = leftX; x < rightX + 1; x++) {
      for (long y = topY; y < botY + 1; y++) {
        for (int i = 0; i <= wmsURL.length - 1; i++) {
          // System.out.println("would grab tile " + x + ", " + y + "
          // (" + pXtoLon(x*tileWidth) + ", " + pYtoLat(y*tileHeight)
          // + ") -> (" + pXtoLon((1+x)*tileWidth) + ", " +
          // pYtoLat((1+y)*tileHeight) + ")" + " would put tile at ("
          // + ((x*tileWidth)-centerX+(windowWidth/2)) + ", " +
          // ((y*tileHeight)-centerY+(windowHeight/2)) + ")");
          String u = wmsURL[i] + "&BBOX=" + pXtoLon(x * tileWidth)
            + "," + pYtoLat((y - 1) * tileHeight) + ","
            + pXtoLon((1 + x) * tileWidth) + ","
            + pYtoLat(y * tileHeight) + "&WIDTH=" + tileWidth
            + "&HEIGHT=" + tileHeight;
          ImBundle ib = new ImBundle(x, y, u, "" + i);
          if (!contains(ib)) {
            //						imv.add(ib);
          }
        }
*/
        y_y = lat_to_yahoo(lat(0));
        y_x = lon_to_yahoo(lon(0));

        y_y_max = lat_to_yahoo(lat(500)) -1;
        y_x_max = lon_to_yahoo(lon(700)) + 2;

        System.out.println("OOOOOOOO=======>    " + y_x + "," + y_y);

        for(int a = y_x; a < y_x_max; a++) {
          for(int b = y_y; b > y_y_max; b--) {
            String u = "http://us.maps3.yimg.com/aerial.maps.yimg.com/tile?v=1.4&t=a&x=" + a  + "&y=" + b + "&z=1";
            ImBundle ib = new ImBundle(a, b, u, "yahoo");
            if (!contains(ib)) {
              imv.add(ib);
            }
          }
        }
/*

      }
    }
    */
  } // grabTiles

  private int lon_to_yahoo(double lon) {
    return (int)( ((lon + 180) /360) * 131072);
  } // lat_to_yahoo


  private double yahoo_to_lon(int yahoo) {
    return -45.0 * ( 65536.0 - (double)yahoo ) / 16384.0;
  }

  private int lat_to_yahoo(double lat) {
    return  (int)(  (  Math.log(Math.tan( (3.141592 / 4) + ( lat * 3.141592 / 180.0 / 2)   )) )  / 3.141592 * 131072 / 2);
  } // lon_to_yahoo 

  private double yahoo_to_lat(int yahoo) {
    return -90.0 * (Math.PI - (4.0 * java.lang.Math.atan( java.lang.Math.exp(Math.PI * (double)yahoo / 65536)))) / Math.PI;
  }

  public void downloadImage(ImBundle ib) {
    System.out.println("Trying to download image " + ib.s);
    PImage i = applet.loadImage(ib.s);

    if (i == null || i.width == 0 || i.height == 0) {
      System.out.println("BAD IMAGE: " + ib.s);
    } else {
      addImage(ib.key, i);
      applet.redraw();
    }
  } // getImage

  private synchronized void addImage(String key, PImage img) {
    System.out.println("adding image " + key);
    images.put(key, img);
  } // addImage

  private synchronized boolean contains(ImBundle ib) {
    // is the image already downloaded or in the queue?
    if (images.containsKey(ib.key)) {
      return true;
    }

    // the following should really be replaced with imv.contains(ib)
    // but it wouldn't work for some reason
    // (imi): I don't believe it does not work with imv.contains.
    // Please proove the above statement with a test case!
    for (Enumeration it = imv.elements(); it.hasMoreElements();) {
      ImBundle iother = (ImBundle)it.nextElement();
      if (iother.equals(ib))
        return true;
    }
    return false;
  } // contains


  private synchronized PImage getImage(String key) {
    return (PImage)images.get(key);
  }

  private synchronized void removeUnusedTiles() {
    // build a new hashtable with the images we want
    Map ht = new HashMap();


    y_y = lat_to_yahoo(lat(0));
    y_x = lon_to_yahoo(lon(0));

    y_y_max = lat_to_yahoo(lat(500)) -1;
    y_x_max = lon_to_yahoo(lon(700)) + 2;

    System.out.println("OOOOOOOO=======>    " + y_x + "," + y_y);

    for(int a = y_x; a < y_x_max; a++) {
      for(int b = y_y; b > y_y_max; b--) {
        String u = "http://us.maps3.yimg.com/aerial.maps.yimg.com/tile?v=1.4&t=a&x=" + a  + "&y=" + b + "&z=1";
        ImBundle ib = new ImBundle(a, b, u, "yahoo");
        String mykey = ib.key;
        if(images.containsKey(mykey)) {
          PImage pi = (PImage)images.get(mykey);
          ht.put(mykey, pi);
        } else {
          imf.remove(mykey);
        }

      }
    }


    images = ht;
  }


  public String toString() {
    return "[tile.java lat,long = (" + lat + "," + lon
      + ") world width,height = (" + widthOfWorld + ","
      + heightOfWorld + ") center = (" + centerX + "," + centerY
      + ") tile bounds: (" + leftX + " -> " + rightX + ", " + topY
      + " -> " + botY + ")]";
  }

  public void run() {
    System.out.println("would run tile here");
    grabTiles();
  }

  /**
   * Turns "world units" into degrees?
   */
  private double pXtoLon(double pX) { 
    return (180.0 / PI) * lon_range * (2.0 * pX / widthOfWorld);
  }

  /**
   * @return x from lon
   */
  public double x(double l) {
    return ((l * PI * widthOfWorld) / (360.0 * lon_range)) - centerX + (windowWidth / 2);
  }
  public double lon(double x) {
    return -(360.0 * lon_range * (-centerX + (windowWidth / 2) - x)) / (PI * widthOfWorld);
  }

  private double pYtoLat(double pY) {
    // the mercator y value found from inverse of line 78
    double merc_y = lat_range * (2.0 * pY / heightOfWorld);
    // transform merc_y back to latitude in degrees
    return (180.0 / PI) * (2.0 * Math.atan(Math.exp(merc_y)) - PI / 2.0);
  }

  /**
   * y from lat
   */
  public double y(double l) {
    return centerY
      + (windowHeight / 2.0)
      - ((heightOfWorld * Math.log(Math.tan((90.0 + l) * PI / 360.0))) / (2.0 * lat_range));
  }

  /**
   * lat from y
   */
  public double lat(double y) {
    return (180.0 * ((2.0 * Math.atan(Math.exp((lat_range * (2.0 * centerY + windowHeight - 2.0 * y)) / heightOfWorld))) - PI / 2)) / PI;
  }

  public Point getTopLeft() {
    return new Point(lat(0), lon(0));
  }

  public Point getBottomRight() {
    return new Point(lat(windowHeight), lon(windowWidth));
  }

  public synchronized void draw() {
    // System.out.println("Drawing tiles...");
    applet.background(100);

    int x_offset = 0;//128 * -1;

    //    int y_y = lat_to_yahoo(lat(300));
    //  int y_x = lon_to_yahoo(lon(300));
    for(int a = y_x; a < y_x_max; a++) {
      for(int b = y_y;  b > y_y_max; b--) {

        PImage p = getImage("yahoo_" + a + "," + b);

        int x = (int)x(yahoo_to_lon(a));
        int y = (int)y(yahoo_to_lat(b));

        int width =  (int)x(yahoo_to_lon(a + 1))-x;
        int height =  (int)y(yahoo_to_lat(b - 1)) - y;

        int y_offset = height * -1;

        if (p != null) {
          //          System.out.println("showing image at " + (x+x_offset) + "," + (y+y_offset) + " with height " + width + "x" + height);
          applet.image(p, x+x_offset,y+y_offset, width, height);
        }
      }
    }
    /*
       for (long x = leftX; x < rightX + 1; x++) {
       for (long y = topY; y < botY + 1; y++) {
       int c = 0;
       for (int i = wmsURL.length - 1; i >= 0; i--) {
       PImage p = getImage(i + "_" + x + "," + y);
       if (p != null) {
       c++;
       applet.image(p, (x * tileWidth) - (long)centerX + (windowWidth / 2),
       windowHeight - ((y * tileHeight) - (long)centerY + (windowHeight / 2)));
       }
       }
       if (c == 0) {
       applet.stroke(255);
       applet.fill(255);
       applet.text("Loading tile...", (int)(((x + .5) * tileWidth) - (long)centerX + (windowWidth / 2)),
       (int)(windowHeight - (((y - .5) * tileHeight) - (long)centerY + (windowHeight / 2))));
       }
       }
       }
     */
  }

  public float metersPerPixel() {
    return (float)((40008.0 / 360.0) * 45.0
        * (float)Math.pow(2.0, -6 - (double)zoom))
      * 1000.0f;
  }

  public synchronized ImBundle getEle() {
    Object[] t = imv.toArray();
    Arrays.sort(t, new IMBComparator(
          (rightX + leftX) / 2.0,
          (botY + topY) / 2.0));

    ImBundle ib = (ImBundle)t[0];
    imv.remove(ib);

    // System.out.println("getEle " + ib.key);
    return ib;
  }

  private void zoom() {
    // call this after modifying the zoom level
    //
    // the zoom functions should be synchronized? it causes the applet to hang :-/

    updateChange();

    applet.recalcStrokeWeight();

    widthOfWorld = 512 * (1 << zoom);
    heightOfWorld = 512 * (1 << zoom);

    centerX = (lon / lon_range * (widthOfWorld / 2));
    centerY = (lat / lat_range * (heightOfWorld / 2));

    recalc();

    images.clear();
    applet.reProject();
    grabTiles();

    applet.redraw();
  }

  public void zoomin() {
	boolean doZoom = true;
	zoom++;
	if(zoom > 18) {
		zoom = 18;
		doZoom = false;
	}
	if(!doZoom) {
		MsgBox.msg("You can't zoom in any further");
	} else {
		// Do the zoom in
		zoom();
	}
  }

  public void zoomout() {
	boolean doZoom = true;
	System.out.print("Zoom was " + zoom + ", now " + (zoom-1));
	zoom--;
	if(zoom < 8) {
		zoom = 8;
		doZoom = false;
	}
	if(!doZoom) {
		MsgBox.msg("You can't zoom out any further");
	} else {
		zoom();
	}
  }

  public long getZoom() {
    return zoom;
  }

  private void updateChange() {
    timeChanged = System.currentTimeMillis();
    viewChanged = true;
  } // updateChange

} // Tile

class ImFetch extends Thread {

  Tile tiles;

  public ImFetch(Tile t) {
    tiles = t;
  } // QueueThread

  public void run() {
    while (true) {
      wait(1000);
      while (!tiles.imv.isEmpty()) {
        if (!tiles.imv.isEmpty()) {
          ImBundle s = tiles.getEle();
          tiles.downloadImage(s);
        }

      }
    }
  }

  public void wait(int milliseconds) {
    try {
      sleep(milliseconds);
    } catch (Exception e) {
    }
  }

  public synchronized void remove(String s) {
    for (Enumeration it = tiles.imv.elements(); it.hasMoreElements();) {
      ImBundle ib = (ImBundle)it.nextElement();
      if (ib.key.equals(s)) {
        tiles.imv.remove(ib);
      }
    }
  } // remove

} // ImFetch

class ImBundle {
  private long x,y;

  String s;
  String key;
  String type;

  public ImBundle(long xx, long yy) {
    x = xx;
    y = yy;
    key = x + "," + y;
  } // ImBundle

  public ImBundle(long xx, long yy, String ss, String t) {
    x = xx;
    y = yy;
    s = ss;
    type = t;
    key = t + "_" + x + "," + y;
  } // ImBundle

  public boolean equals(ImBundle other) {
    return x == other.x && y == other.y && type.equals(other.type);
  }

  /**
   * @return The squared distance between the this and the point given by the two values.
   */
  public double distanceSquared(double xx, double yy) {
    return (x-xx)*(x-xx) + (y-yy)*(y-yy);
  }
} // ImBundle

class IMBComparator implements java.util.Comparator {
  double cx, cy;

  public IMBComparator(double x, double y) {
    cx = x;
    cy = y;
  }

  public int compare(Object a, Object b) {
    ImBundle aa = (ImBundle)a;
    ImBundle bb = (ImBundle)b;

    int ai = aa.key.charAt(0);
    int bi = bb.key.charAt(0);

    if (ai < bi) {
      return -1;
    } else if (ai > bi) {
      return 1;
    }

    double ad = aa.distanceSquared(cx, cy);
    double bd = bb.distanceSquared(cx, cy);

    return Double.compare(ad, bd);
  }
}

/**
 * Watcher Thread, that will detect when changes have been made to 
 *  the tiles shown on the screen, and fetch some matching OSM data
 *  to go with it.
 */
class VFetch extends Thread {
	private Tile tiles;
	public VFetch(Tile t) {tiles = t;}

	public void run() {
		while (true) {
			// Sleep for 1 second between checks
			try {sleep(1000);} catch (Exception e) {}
			
			// If the tiles have changed, and they changed more than 6
			//  seconds ago, then do a re-draw
			if (tiles.viewChanged && 
			(tiles.timeChanged < System.currentTimeMillis() - 6000)) {
				tiles.viewChanged = false;
				tiles.applet.lines.clear();
				tiles.applet.nodes.clear();
				tiles.applet.ways.clear();

				tiles.applet.redraw();
				tiles.applet.osm.getNodesLinesWays(tiles.getTopLeft(), tiles.getBottomRight(), tiles);
				tiles.applet.redraw();
			}
		}
	}
}
