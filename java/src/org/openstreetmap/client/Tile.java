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

import org.openstreetmap.gui.MsgBox;
import org.openstreetmap.processing.OsmApplet;
import org.openstreetmap.util.Point;
import org.openstreetmap.util.Releaseable;

import processing.core.PImage;

/**
 * Manages the background image tiles - identifying the ones needed (based on
 * mouse drags / zoom through drag() and zoom()), queuing/requesting/caching
 * them and also handles the redrawing of the tile background.
 * 
 * Thread synchronisation (some significant changes mid-Feb 07):
 * 
 * The major data structures that require internal synchronisation, are the
 * <code>images</code> map (cached images) and the <code>imv</code> vector
 * (images awaiting download).
 * 
 * Access to the images map and the instance draw() method is synchronized (on
 * the instance). The <code>imv</code> vector was inconsistently synchronized,
 * but has been moved within the ImFetch instance, <code>imf</code>, (and
 * synchronized on imf).
 * 
 * LOCK ACQUISITION ORDER: images and imf (ImFetch instance) Should lock on (1)
 * images then (2) imf.
 * 
 * NB: This is necessary whenever processing occurs to populate imf (queue new
 * tiles for download) - which needs to check if it's in the image cache and/or
 * the download queue.
 * 
 * NB: Currently tiles that are already downloading and are currently required
 * under a zoom/drag update may be requeued and downloaded twice. The
 * inefficiency does not appear massive (see TODO Data structures review below)
 * 
 * TODO Simplify locking: Perhaps, for code simplicity, <code>imv</code> and
 * <code>images</code> could be synced on same object without significant
 * performance penalty. This would remove need for care w.r.t. lock acquisition
 * order.
 * 
 * TODO Data structures review: Perhaps, more correctly, data structures should
 * be reviewed/ changed to accomodate tracking of those images currently being
 * downloaded by an ImFetch helper thread (currently these are known only to the
 * download thread(s) and are not present in images or imv). Would also be good
 * to need to check only one, hashed, data structure in order to determine tiles
 * that need queueing for download: i.e. perhaps one images map, keyed on
 * imbundle, entries being an entry that holds image ref and status of QUEUED,
 * DOWNLOADING or IN CACHE.
 * 
 */
public class Tile implements Projection, Releaseable {

  private static final double PI = 3.14159265358979323846;

  private static final double lat_range = PI;

  private static final double lon_range = PI;

  /** 
   * Minimum delay in miliseconds allowed between last draw being completed,
   * and enacting new drag.
   */
  private static final long DRAG_MINIMUM_DELAY_MS = 20;

  /** 
   * App zoom level. 
   */ 
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
  // private String wmsURL =
  // "http://www.openstreetmap.org/tile/0.1/wms?map=/usr/lib/cgi-bin/steve/wms.map&service=WMS&WMTVER=1.0.0&REQUEST=map&STYLES=&TRANSPARENT=TRUE";
  private String[] wmsURL;

  /** yahoo x component of westmost screen tile */
  public int y_x = 65480;

  /** yahoo y component of southmost screen tile */
  public int y_y = 21963;

  /** yahoo x component of tile one beyond eastmost screen tile */
  public int y_x_max = 65480;

  /** yahoo y component of tile one beyond northmost screen tile */
  public int y_y_max = 21963;

  OsmApplet applet;

  Map images = new HashMap();

  ImFetch imf;
  public VFetch vf;

  private boolean viewChanged = false;

  private long timeChanged;

  private int skippedx = 0;
  private int skippedy = 0;

  private static final int MAX_TILE_ZOOM_OFFSET = 2;
  private static final int MIN_TILE_ZOOM_OFFSET = -2;

  /**
   * Modifier used to convert applet zoom level into appropriate satellite
   * image tile zoom/resolution level.
   */
  private int tileZoomOffset = 1;

  /**
   * The resolution level for the satellite images to use as
   * background tiles.
   * 
   * The highest resolution tiles are at level 1, decreasing in res to 
   * level 12.
   * 
   * The level is calculated relative to the applet zoom level (starts
   * at 15 for street level, increases with zooming in.
   */
  private int yahooZoom = 1; // NB should always be accessed from sync(this) block

  public Tile(OsmApplet p, String url, double la, double lo, int wW, int wH,
      int z, int imageFetchThreads) {

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

    // TODO exposing this in constructor - potentially unsafe
    imf = new ImFetch(this, imageFetchThreads);

    // TODO exposing this in constructor - potentially unsafe
    vf = new VFetch(this);

    recalc();
    queueTiles();
  } // tile
  
  /**
   * Used to start tile instance's helper threads (to avoid having
   * to unsafely start in constructor.
   */
  public void start() {
    // FIXedME remove test call
    //ImFetch.testVectorContains();
    
    imf.start();
    vf.start();
  }

  /**
   * Common recalculations between zoom, drag and resolution change.
   * 
   * Assumed called on event thread.
   * Should be called within sync(this) block around any other calculations. 
   */
  synchronized private void recalc() {
    yahooZoom = 18 - (int) zoom - tileZoomOffset;
    if (yahooZoom < 1) yahooZoom = 1;
    // debug("tileZoomOffset: " + tileZoomOffset + ", yahooZoom: " + yahooZoom);
    
    // used in getEle() for distance comparison to select priority downloads...
    // (was broken, change Feb 07)
    leftX = lon_to_yahoo(lon(0), yahooZoom );
    rightX = lon_to_yahoo(lon(windowWidth), yahooZoom);
    topY = lat_to_yahoo(lat(0), yahooZoom);
    botY = lat_to_yahoo(lat(windowHeight), yahooZoom);
  } // recalc

  /**
   * Handler for each pan of the screen. Assumed called on event thread.
   * 
   * NB: The processing here has changed Feb 07.  Was doing full recalc and
   * queueTiles() and redraw() for every mouse event.  queueTiles() call was
   * pretty much removing all outstanding download links and then re-adding
   * them again.  Doing all this on the AWT event thread means that it's not 
   * getting much chance to update the screen before the next repaint is queued 
   * (perhaps this was why, when continuously dragging screen, draw() is running 
   * regularly but the screen doesn't update).  As a result:
   * 
   * A (crude) algorithm is used to ignore drag events that are very close
   * together (timewise), to prevent hogging event thread and spending all our
   * CPU drawing, but never getting to see the results of it.
   *  
   * @param dx
   * @param dy
   */
  public void drag(int dx, int dy) {
    // TODO should improve this to queue and collapse events for drag update thread 
    // - i.e. make sure that final drag event gets done eventually
    long timeDrawn = applet.getLastDrawTime();
    if ((System.currentTimeMillis() - timeDrawn) < DRAG_MINIMUM_DELAY_MS) {
      skippedx += dx;
      skippedy += dy;
      //debug("skipping recalc/redraw");
      return;
    }
    else {
      dx += skippedx;
      dy += skippedy;
      skippedx = skippedy = 0;
    }

    synchronized (this) {
      updateChange();
      centerX += dx;
      centerY += dy;

      // lat = Math.log(Math.tan( (PI / 4.0) + (PI * la / 360.0) ) );
      // lon = PI * lo / 180.0;

      lat = Math.log(Math.tan((PI / 4.0) + (PI * lat(windowHeight / 2) / 360.0)));
      lon = PI * lon(windowWidth / 2) / 180.0;

      recalc();
      removeUnusedTiles();
    }
    
    // NB: NOT in sync(this) - see comment at start of zoom() 
    applet.reProject();
    queueTiles();
    
    applet.osm.abortMapGet();

    //debug(lat + "," + lon);
    applet.redraw();
  } // drag

  private int lon_to_yahoo(double lon, int yzoom) {
    return (int) Math.floor(((((lon + 180) / 360) * 131072) / (1 << (yzoom - 1))));
  } // lon_to_yahoo

  private double yahoo_to_lon(int yahoo, int yzoom) {
    return -45.0 * (65536.0 - (double) (yahoo * (1 << (yzoom - 1)))) / 16384.0;
  }

  private int lat_to_yahoo(double lat, int yzoom) {
    return (int) Math.floor((((Math.log(Math.tan((PI / 4)
        + (lat * PI / 180.0 / 2)))) / PI * 131072 / 2) / (1 << (yzoom-1))));
  } // lat_to_yahoo

  private double yahoo_to_lat(int yahoo, int yzoom) {
    return -90.0
        * (Math.PI - (4.0 * java.lang.Math.atan(java.lang.Math.exp(Math.PI
            * (double) (yahoo * (1 << (yzoom - 1))) / 65536)))) / Math.PI;
  }

  public void downloadImage(ImBundle ib) {
    debug("Downloading image " + ib.getUrl());
    PImage i = applet.loadImage(ib.getUrl());
    
    if (i == null || i.width == 0 || i.height == 0) {
      debug("BAD IMAGE: " + ib.getUrl());
    }
    else {
      // may have to wait for tile draw, but only a few ms...
      addImage(ib.getKey(), i);
      applet.redraw();
    }
  } // getImage

  private synchronized void addImage(String key, PImage img) {
    //debug("images.add (" + key + ") - sync on Tile instance");
    images.put(key, img);
  } // addImage

  /**
   * Determines whether the specified map tile is contained in either the
   * downloaded image cache (images) or the download queue (imb) - or not.
   * 
   * i.e. whether it will need adding to download queue.
   * 
   * NB: THREADS-BEWARE.  Multiple locks held here, so always in order:
   *   lock images on this Tile
   *   lock download queue on ImFetch instance
   * 
   * @param ib The map tile identifier.
   * @return <code>true</code> if the image is in the cache or the queue, or
   *   <code>false</code> if it isn't in either.
   */
  private synchronized boolean contains(ImBundle ib) {
    //debug("images.containsKey(" + ib.key + ") test - sync on Tile instance");

    // is the image already downloaded 
    if (images.containsKey(ib.getKey())) {
      return true;
    }

    // ...or in the queue?
    if (imf.contains(ib)) {
      return true;
    }
    
    return false;
  } // contains

  private synchronized PImage getImage(String key) {
    // debug("images.get(" + key + ") - sync on Tile instance");
    return (PImage) images.get(key);
  }

  
  /**
   * Implemented by algorithms that want to act on a number of ImBundle instances,
   * reusing traversal algorithms.
   */
  interface ImBundleVisitor {
    void visit(ImBundle imb);
  }

  /**
   * Traverse screen tile placements that are required at current
   * zoom, location and screen size.
   * 
   * @param visitor The procedure to run on each tile placement handle
   * @param offscreenMargin The extent of placements to visit outside
   *   the current screen, in number of tiles.
   */
  private void visitRequiredTilePlacements(ImBundleVisitor visitor, int offscreenMargin) {
    
    y_y = lat_to_yahoo(lat(0), yahooZoom) + offscreenMargin;
    y_x = lon_to_yahoo(lon(0), yahooZoom) - offscreenMargin;

    y_y_max = lat_to_yahoo(lat(windowHeight), yahooZoom) - offscreenMargin - 1;
    y_x_max = lon_to_yahoo(lon(windowWidth), yahooZoom) + offscreenMargin + 1;
    
    // go through all tiles we'd use in current screen
    for (int a = y_x; a < y_x_max; a++) {
      for (int b = y_y; b > y_y_max; b--) {
        ImBundle imb = new ImBundle(a, b, ImBundle.TYPE_YAHOO, yahooZoom);
        visitor.visit(imb);
      }
    }

    //debug("OOOOOOOO=======>    " + y_x + "," + y_y);
  }

  /**
   * Implements logic for deciding which image tiles to queue for download
   */
  private class QueueTilesVisitor implements ImBundleVisitor {
    public void visit(ImBundle imb) {
      if (!contains(imb)) {
        imf.add(imb);
      }
    }
  }

  /**
   * Queues tiles for download.
   */
  private void queueTiles() {
    imf.clear();  // don't try to be too clever - clear queue and (re-)add
                  // the required tiles below...

    // critical section of image and imv containment check, then
    // addition on result, needs lock on both images and imf, respectively
    // NB: images then imf locks held, always in that order
    synchronized (this) {
      synchronized (imf) {
        visitRequiredTilePlacements(new QueueTilesVisitor(), 0);
        //debug("" + imf.size() + " tiles queued for download.");
      }
    }
  } // queueTiles

  /**
   * Implements logic for deciding which image tiles are unused
   * 
   * TODO could hold onto tiles between zooms and re-use at new zoom view
   * level until replacement tiles downloaded.  However this is nice and
   * simple, i.e. it works!
   */
  private class RemoveUnusedTilesVisitor implements ImBundleVisitor {
    private Map map = new HashMap();
    Map getMap() {
      return map;
    }

    public void visit(ImBundle imb) {
      String mykey = imb.getKey();
      if (images.containsKey(mykey)) { // tile in use - copy to new images map
        PImage pi = (PImage) images.get(mykey);
        map.put(mykey, pi);
      }
    }
  }
  
  /**
   * Discards images that are not needed.
   */
  private synchronized void removeUnusedTiles() {
    RemoveUnusedTilesVisitor v = new RemoveUnusedTilesVisitor();
    visitRequiredTilePlacements(v, 1); // keep hold of slightly wider range
                                       // of tiles
    
    // Feb 07 Spurious imf.remove() logic deleted - 
    // instead now we just clear the image download queue at 
    // start of queueTiles().

    images = v.getMap();
    //debug("images remove unused - end sync (request tiles still in queue=" + imv.size() + ")");
  }

  public String toString() {
    return "[tile.java lat,long = (" + lat + "," + lon
        + ") world width,height = (" + widthOfWorld + "," + heightOfWorld
        + ") center = (" + centerX + "," + centerY + ") tile bounds: (" + leftX
        + " -> " + rightX + ", " + topY + " -> " + botY + ")]";
  }

  /**
   * Turns "world units" into degrees?
   */
  private double pXtoLon(double pX) {
    return (180.0 / PI) * lon_range * (2.0 * pX / widthOfWorld);
  }

  /* (non-Javadoc)
   * @see org.openstreetmap.client.Projection#x(double)
   */
  public double x(double l) {
    return ((l * PI * widthOfWorld) / (360.0 * lon_range)) - centerX
        + (windowWidth / 2);
  }

  /* (non-Javadoc)
   * @see org.openstreetmap.client.Projection#lon(double)
   */
  public double lon(double x) {
    return -(360.0 * lon_range * (-centerX + (windowWidth / 2) - x))
        / (PI * widthOfWorld);
  }

  private double pYtoLat(double pY) {
    // the mercator y value found from inverse of line 78
    double merc_y = lat_range * (2.0 * pY / heightOfWorld);
    // transform merc_y back to latitude in degrees
    return (180.0 / PI) * (2.0 * Math.atan(Math.exp(merc_y)) - PI / 2.0);
  }

  /* (non-Javadoc)
   * @see org.openstreetmap.client.Projection#y(double)
   */
  public double y(double l) {
    return centerY
        + (windowHeight / 2.0)
        - ((heightOfWorld * Math.log(Math.tan((90.0 + l) * PI / 360.0))) / (2.0 * lat_range));
  }

  /* (non-Javadoc)
   * @see org.openstreetmap.client.Projection#lat(double)
   */
  public double lat(double y) {
    return (180.0 * ((2.0 * Math.atan(Math.exp((lat_range * (2.0 * centerY
        + windowHeight - 2.0 * y))
        / heightOfWorld))) - PI / 2))
        / PI;
  }

  /**
   * @return Lat, long point for top left of visible map.
   */
  public Point getTopLeft() {
    return new Point(lat(0), lon(0));
  }

  /**
   * @return Lat, long point for bottom right of visible map.
   */
  public Point getBottomRight() {
    return new Point(lat(windowHeight), lon(windowWidth));
  }
  
  /**
   * Top left point of OSM vector data download bbox.
   * @return Corner point or <code>null</code> if not downloaded or currently downloading.
   */
  public Point getDataDownloadTopLeft() {
    return vf.getTopLeft();
  }

  /**
   * Bottom right point of OSM vector data download bbox.
   * @return Corner point or <code>null</code> if not downloaded or currently downloading.
   */
  public Point getDataDownloadBottomRight() {
    return vf.getBottomRight();
  }

  /**
   * Draws the available background satellite image tiles.
   * 
   * NB: Always called on applet thread that disappeared into noLoop(), which
   * already has sync lock on applet instance.
   */
  public synchronized void draw() {
    //long tileDrawStart = System.currentTimeMillis(); // debug("tiles.draw()...");
    applet.background(100);

    int x_offset = 0;// 128 * -1;

    // NB: Should make sure that updates to the field values used here
    // on should be similarly synced on the tile instance:
    //  o  yahooZoom
    //  o  widthOfWorld, centerX (in x())
    //  o  heightOfWorld, centerY (in y())
    for (int a = y_x; a < y_x_max; a++) {
      for (int b = y_y; b > y_y_max; b--) {

        PImage p = getImage("yahoo_" + a + "," + b + "," + yahooZoom);

        int x = (int) x(yahoo_to_lon(a, yahooZoom));
        int y = (int) y(yahoo_to_lat(b, yahooZoom));

        int width = (int) x(yahoo_to_lon(a + 1, yahooZoom)) - x;
        int height = (int) y(yahoo_to_lat(b - 1, yahooZoom)) - y;

        int y_offset = height * -1;

        if (p != null) {
          // System.out.println("showing image at " + (x+x_offset) +
          // "," + (y+y_offset) + " with height " + width + "x" +
          // height);
          applet.image(p, x + x_offset, y + y_offset, width, height);
        }
        else if (false) { // used for debugging...
          applet.strokeWeight(1);
          applet.textSize(8 << (zoom + yahooZoom - 16));
          applet.color(0, 0, 0);
          applet.text("downloading...", x + x_offset + width / 2, y + y_offset + height /2);
        }
      }
    }
    
    // long timeDrawn = System.currentTimeMillis(); 
    // debug("tiles.draw() done in "  + (timeDrawn - tileDrawStart) + "ms.");
    
    /*
     * for (long x = leftX; x < rightX + 1; x++) { for (long y = topY; y < botY +
     * 1; y++) { int c = 0; for (int i = wmsURL.length - 1; i >= 0; i--) {
     * PImage p = getImage(i + "_" + x + "," + y); if (p != null) { c++;
     * applet.image(p, (x * tileWidth) - (long)centerX + (windowWidth / 2),
     * windowHeight - ((y * tileHeight) - (long)centerY + (windowHeight / 2))); } }
     * if (c == 0) { applet.stroke(255); applet.fill(255); applet.text("Loading
     * tile...", (int)(((x + .5) * tileWidth) - (long)centerX + (windowWidth /
     * 2)), (int)(windowHeight - (((y - .5) * tileHeight) - (long)centerY +
     * (windowHeight / 2)))); } } }
     */
  }

  public float metersPerPixel() {
    return (float) ((40008.0 / 360.0) * 45.0 * (float) Math.pow(2.0, -6
        - (double) zoom)) * 1000.0f;
  }
  
  /**
   * @return The next image bundle (map tile) to retrieve.
   */
  public ImBundle getEle() {
    //debug("getEle(): imv.sort() + remove() - " + imf.size() + " in queue - sync on Tile instance");
    Object[] t = imf.toArray();
    Arrays.sort(t, new IMBComparator((rightX + leftX) / 2.0,
        (botY + topY) / 2.0));

    ImBundle ib = (ImBundle) t[0];
    imf.remove(ib.getKey());

    //debug("getEle(): got " + ib.key);
    return ib;
  }

  /**
   * Modifies zoom level.
   * 
   * Assumed always called on event thread - synchronization may need
   * changing if this is not so.
   * 
   * @param dz
   */
  private void zoom(int dz) {
    // the zoom functions should be synchronized? it causes the applet to
    // hang :-/  
  
    // NB: this is probably due to the PApplet display thread having
    // already gained the lock on the applet instance i.e. if drawing (in
    // sync'ed tiles.draw()), the draw thread will have locked (1) the applet,
    // then (2) the Tile instance.  if you sync this method, it gets called
    // by AWT thread attempts to lock (1) this tile instance then (2) the
    // applet in applet.reProject() below.
    //
    // syncing applet.reProject() and applet.draw() together is probably
    // correct to keep all the nodes written in the same frame of
    // reference through draw.
    //
    // obviously we want to do a similar thing for tile rendering - make
    // sure that tile drawing is a critical section (as it is sync'ing on
    // tile instance), but also we want to make sure that the info it
    // depends on has also been synchronized on same object (so that its
    // information has been updated with similar consistency
    // 
  
    zoom += dz;
    updateChange();
    applet.recalcStrokeWeight();
    applet.osm.abortMapGet();
  
    synchronized (this) {
      widthOfWorld = 512 * (1 << zoom);
      heightOfWorld = 512 * (1 << zoom);
  
      centerX = (lon / lon_range * (widthOfWorld / 2));
      centerY = (lat / lat_range * (heightOfWorld / 2));
  
      recalc();
  
      // NB: images access always within sync(this) block
      images.clear();
    }
    
    // NB: NOT in sync(this) - see comment at start of method 
    applet.reProject();
    
    queueTiles();
  
    applet.redraw();
  }

  /**
   * Zooms in one level to max. Assumed called on event thread.
   */
  public void zoomin() {
    if (zoom >= 18) {
      zoom = 18;
      MsgBox.msg("You can't zoom in any further");
    }
    else {
      // Do the zoom in
      zoom(1);
    }
  }

  /**
   * Zooms out one level to max. Assumed called on event thread.
   */
  public void zoomout() {
    System.out.print("Zoom was " + zoom + ", now " + (zoom - 1));
    if (zoom <= 5) {
      zoom = 5;
      MsgBox.msg("You can't zoom out any further");
    }
    else {
      zoom(-1);
    }
  }

  public long getZoom() {
    return zoom;
  }

  /**
   * Increase tile resolution one notch (on top of relative change w/ zoom). 
   */
  public void resolutionUp() {
    if (++tileZoomOffset > MAX_TILE_ZOOM_OFFSET) tileZoomOffset = MAX_TILE_ZOOM_OFFSET;
    resolutionChanged();
  }
  /**
   * Decrease tile resolution one notch (on top of relative change w/ zoom). 
   */
  public void resolutionDown() {
    if (--tileZoomOffset < MIN_TILE_ZOOM_OFFSET) tileZoomOffset = MIN_TILE_ZOOM_OFFSET;
    resolutionChanged();
  }

  /**
   * Perform necessary updates based on resolution setting having changed.
   */
  private void resolutionChanged() {
    recalc();

    //debug("images.clear() on zoom, no sync");
    synchronized (this) {
      images.clear();
    }
    imf.clear();
    queueTiles();
    applet.redraw();
  }
  
  /**
   * @return The resolution of image tiles, as a small integer offset.  Lower res < 0 < higher res.
   */
  public int getResolution() {
    return tileZoomOffset;
  }
  
  /**
   * Critical section = update both at once.
   */
  synchronized void updateChange() {
    this.timeChanged = System.currentTimeMillis();
    setViewChanged(true);
  } // updateChange

  synchronized public boolean isViewChanged() {
    return viewChanged;
  }

  synchronized public void setViewChanged(boolean changed) {
    this.viewChanged = changed;
  }

  synchronized long getTimeChanged() {
    return timeChanged;
  }

  private void debug(String s) {
    applet.debug(s);
  }

  /* (non-Javadoc)
   * @see org.openstreetmap.util.Releaseable#release()
   */
  public void release() {
    imf.release();
    vf.release();
    applet = null; // release back-ref - otherwise tiles might persist
  }
} // Tile

/**
 * Encapsulates thread that fetches images.
 * 
 * This used to synchronize access to image fetch queue.
 */
class ImFetch implements Runnable, Releaseable {
  private static volatile int threadCount = 0;
  private Vector imv = new Vector();

  Tile tiles;
  private int threadsToUse; // number of threads to run
  volatile private int threadsDownloading = 0;

  public ImFetch(Tile t, int threads) {
    tiles = t;
    this.threadsToUse = threads;
  } // QueueThread

  /**
   * @param ib  Image bundle definition to add to download queue.
   */
  synchronized public void add(ImBundle ib) {
    imv.add(ib);
    notify(); // wake up a waiting thread in run()
  }

  /**
   * @return Number of image bundles in download queue.
   */
  synchronized public long size() {
    return imv.size();
  }

  /**
   * @return An array of references to image bundles in download queue.
   */
  synchronized public Object[] toArray() {
    return imv.toArray();
  }

  /**
   * Remove all image bundles queued for download. 
   */
  synchronized public void clear() {
    imv.clear();
  }

  /**
   * Remove based on key value - TODO should really use an ordered
   * map and remove entry directly when needed.
   * 
   * @param key The key value of the image bundle to remove. 
   */
  synchronized public void remove(String key) {
    //debug("imv.remove(" + key + ") - sync on ImFetch instance");
    for (Enumeration it = imv.elements(); it.hasMoreElements();) {
      ImBundle ib = (ImBundle) it.nextElement();
      if (ib.getKey().equals(key)) {
        imv.remove(ib);
      }
    }
  } // remove

  /** Termination control flag for thread loop */
  private boolean stop = false;

  /* (non-Javadoc)
   * @see org.openstreetmap.util.Releaseable#release()
   */
  synchronized public void release() {
    stop = true;
    // TODO to prevent NPEs on exit, store refs to threads and join() before returning
  }

  public void run() {
    Thread.currentThread().setName("ImFetch_thr_" + threadCount++);
    ImBundle ib;
    boolean testedEmpty;
    while (!stop) {
      try {
        wait(5000);
        ib = null;
        testedEmpty = false;
  
        //debug("imv.isEmpty test() - no sync");
        while (!testedEmpty) {
          synchronized (this) { // sync on this ImFetch to check for any more in queue
            testedEmpty = imv.isEmpty();
            ib = testedEmpty ? null : tiles.getEle();
          }
          if (ib != null) {
            threadsDownloading++;
            tiles.downloadImage(ib); // don't worry about sync of ib, since is immutable
            --threadsDownloading;
            if (threadsDownloading == 0 && size() == 0) { // NB: not guaranteed accurate / once only
              debug("All tiles downloaded.");
            }
          }
        }
      }
      catch (OutOfMemoryError oome) {
        // hmm what to do here - well:
        // (A) profile app, may well be using too much heap, e.g. in getEle()
        // (B) probably due to images being much larger than requirements - 
        //      scale back to lower res images?
        // (C) shutdown a number of threads (e.g. don't:
        //       while(true);  
        //     but instead
        //       while(runningThreads < threads)
        // (D) expand JVM allocation - how to do this in applet?
        tiles.applet.status("WARNING OutOfMemory error - running gc()");
        System.gc();
        wait(3000);
        tiles.applet.status("WARNING OutOfMemory error - trying to continue thread...");
      }
    } // while
  }

  /**
   * Create and run the necessary threads.
   */
  public void start() {
    for (int i=0; i < threadsToUse; i++) {
      new Thread(this).start();
    }
    debug("ImFetch, started " + threadsToUse + " threads.");
  }
  
  synchronized public void wait(int milliseconds) {
    try {
      super.wait(milliseconds);
    }
    catch (Exception e) {
      // NOP
    }
  }

  private void debug(String s) {
    tiles.applet.debug(s);
  }

  /**
   * Replacement for vector contains() call to test whether bundle in
   * the imv vector.
   * 
   * The standard contains() call seems to fail because of some gnarly
   * problem elucidated in commented out code below...
   * 
   * @param ib The image bundle definition to check for
   * @return <code>true</code> if the checked for image bundle
   *   is in the download queue.
   */
  synchronized public boolean contains(ImBundle ib) {
    //debug("imv.elements(), test for containment - sync on Tile instance");
    for (int i=0; i < imv.size(); i++) {
      ImBundle iother = (ImBundle) imv.get(i);
      
      // this test for containment always works correctly:
      if (iother.hashCode() == ib.hashCode() && iother.equals(ib)) {
        // only arrive here if imv not empty (which shouldn't occur now)
        // in order to get here, comment out imf.clear() call at top of
        // queueTiles() method body.
        
        // this one always seems to fail!  a simple test of vector contains()
        // function in testVectorContains() always works - looks like some obscure
        // threading problem - see commented out section below for further info
        if (!(imv.contains(ib))) { // NB! even happens if using iother not ib!
          debug("!!!!!!!!!!!! imv.contains() different to imv.get() search result");
        }
        else {
          // OK - it never seems to go down here...
          debug("!!!!!!!!!!!! imv.contains() result == imv.get() search result");
        }
        return true; // ignore results of imv.contains()
      }
    }

    // >> the following should really be replaced with imv.contains(ib)
    // >> but it wouldn't work for some reason -
    //
    // > (imi): I don't believe it does not work with imv.contains.
    // > Please proove the above statement with a test case!
    //
    // thought might be due to hashCode() values not being consistent
    // (collections allowed to use hashCode() shorcuts in contains()
    // implementations) but even fixing ImBundle's hashCode(), contains()
    // still doesn't work.

    /*  
    debug / tests made on above issues:
     
    for (Enumeration it = imv.elements(); it.hasMoreElements();) {
      ImBundle iother = (ImBundle) it.nextElement();
      if (iother.equals(ib)) { // giving false positive?   must be
        debug("imv.size()=" + imv.size());
        if (!imv.contains(ib)) {
          debug("ib    =" + ib.toString());
          debug("iother=" + iother.toString());
          debug("iother.equals(ib)=" + iother.equals(ib));
          debug("ib.equals(iother)=" + ib.equals(iother));
          
          for (int i=0; i < imv.size(); i++) {
            debug("imv[" + i + "]=" + imv.get(i));
          }
          debug("ib#    =" + ib.hashCode());
          debug("iother#=" + iother.hashCode());
          debug("ib@    =" + ib.getUrl());
          debug("iother@=" + iother.getUrl());
          
          //debug("ib.getUrl().equals(iother.getUrl())=" + ib.getUrl().equals(iother.getUrl()));
          
          // TODO should really work out what is going on here...
          
          // this definitely happens.  was surprised when i saw it as code seems fine
          // it possible could be an occurrence of this:
          // http://www.cs.umd.edu/~pugh/java/memoryModel/jsr-133-faq.html#finalWrong
          // or is it something about the equals()?
          
          // hmm - what if ib has been added by another thread
          // before getting here... now ib has another thread accessing
          // it, and it doesn't have sync protection.  i.e. would need to 
          // sync on equals test if really open to this final frailty
          
          // or could there be a problem with equals implementation, and it's
          // the order that matters - which is equal of which?
          debug("!!!!!!!!!!!! imv.contains() different to imv.elements() search (prob. thread contention)");
        }
        else {
          // OK - it never seems to go down here...
          debug("!!!!!!!!!!!! imv.contains() result == imv.elements() search result");
        }
        return true;
      }
    } */
    return false;
  }

  static public void testVectorContains() {
    long x = 10000;
    long y = 20000;
    int zoom = 10;
    ImBundle imb1 = new ImBundle(x, y, ImBundle.TYPE_YAHOO, zoom);
    ImBundle imb2 = new ImBundle(x, y, ImBundle.TYPE_YAHOO, zoom);
    Vector vector = new Vector();
    vector.add(imb1);
    vector.add(imb2);
    System.out.println("test vector.contains(imb1)=" + vector.contains(imb1));
    System.out.println("test vector.contains(imb2)=" + vector.contains(imb2));
  }
} // ImFetch

/**
 * Data-holder for unloaded image tile.
 * 
 * Holds remote url and formats a key (which is subsequently also used for
 * storage of downloaded images).
 * 
 * NB: Made immutable so no worries for multi-threading.
 * 
 * TODO probably originally intended that this class should decide on
 *   URL format based on provider type - maybe construct from subclass,
 *   e.q. YahooImBundle.  That would neaten this up a bit.
 */
class ImBundle {
  static public final String TYPE_YAHOO = "yahoo";
  private final long x, y;
  /** URL tile resides at. */
  private final String url;
  /** Unique key of image. */
  private final String key;
  private final String type;
  /** provider zoomlevel of tile */
  private final int imageZoom;

  /**
   * TODO remove? apparently unused (currently no url or provider specified)
   * 
   * @param xx
   * @param yy
   */
  public ImBundle(long xx, long yy) {
    x = xx;
    y = yy;
    type = "";
    url = "";
    key = x + "," + y;
    imageZoom = 1;
  } // ImBundle

  /**
   * Constructor for specific provider.
   * 
   * @param xx Provider-specific map tile x.
   * @param yy Provider-specific map tile y.
   * @param type Provider type constant, unique name per map provider, e.g. TYPE_YAHOO
   * @param z Provider image zoom level.
   */
  public ImBundle(long xx, long yy, String type, int z) {
    if (type != TYPE_YAHOO)  throw new IllegalArgumentException("Unknown ImBundle provider: '" + type + "'");
    x = xx;
    y = yy;
    this.type = type;
    this.imageZoom = z;
    key = type + "_" + x + "," + y + "," + z; // NB: key may not be unique across zoom levels
    url = "http://us.maps3.yimg.com/aerial.maps.yimg.com/tile?v=1.4&t=a&x="
      + x + "&y=" + y + "&z=" + z;
                                    // z=1 - so always getting most detailed zoom tiles...
                                    // should probably be changing based on zoom (might solve
                                    // OOM errors) or at least not throwing away image cache
                                    // unnecessarily.
  } // ImBundle

  public boolean equals(ImBundle other) {
    System.out.println(toString() + "=" + other.toString());
    return x == other.x && y == other.y && type.equals(other.type) && imageZoom == other.imageZoom;
  }
  
  public int hashCode() {
    return (int) (x + ((y & 0xFF) << 8) + imageZoom);
  }
  
  public String toString() {
    return type + imageZoom + x + y;
  }

  /**
   * @return The squared distance between the this and the point given by the
   *         two values.
   */
  public double distanceSquared(double xx, double yy) {
    return (x - xx) * (x - xx) + (y - yy) * (y - yy);
  }

  String getKey() {
    return key;
  }

  String getUrl() {
    return url;
  }
} // ImBundle

/**
 * Comparator that decides which of the outstanding image tiles to download we
 * should download first. 
 */
class IMBComparator implements java.util.Comparator {
  double cx, cy;

  /** Init to reference point */
  public IMBComparator(double x, double y) {
    cx = x;
    cy = y;
  }

  /**
   * Compares based on:
   *   (i) ascii value of first character of key
   * NB: first char of ImBundle keys is 'y' of yahoo...
   *   (ii) distance from ref point
   *   
   *  i.e. sort with this comparator should put nearest to screen centre 
   *  to top of list.  
   */
  public int compare(Object a, Object b) {
    ImBundle aa = (ImBundle) a;
    ImBundle bb = (ImBundle) b;

    int ai = aa.getKey().charAt(0);
    int bi = bb.getKey().charAt(0);

    if (ai < bi) {
      return -1;
    }
    else if (ai > bi) {
      return 1;
    }

    double ad = aa.distanceSquared(cx, cy);
    double bd = bb.distanceSquared(cx, cy);

    return Double.compare(ad, bd);
  }
}

/**
 * Watcher Thread, that will detect when changes have been made to the tiles
 * shown on the screen, and fetch some matching OSM data to go with it.
 * 
 * NB: Probably 'V' for 'Vector data'.
 */
class VFetch extends Thread implements Releaseable {
  private static final long FETCH_MAP_DELAY_MS = 2000;

  private static volatile int threadCount = 0;

  /** Termination control flag for thread loop */
  volatile private boolean stop = false;

  /* (non-Javadoc)
   * @see org.openstreetmap.util.Releaseable#release()
   */
  public void release() {
    stop = true;
    // TODO to prevent NPEs on exit, store refs to threads and join() before returning
  }

  private Tile tiles;
  private OsmApplet applet;
  volatile private Point topLeft = null;
  volatile private Point bottomRight = null;

  public VFetch(Tile t) {
    tiles = t;
    applet = t.applet;
  }

  public void run() {
    Thread.currentThread().setName("VFetch_thr_" + threadCount++);
    fetchData(); // initial fetch
    
    boolean doFetch = false;
    while (!stop) {
      // sleep between checks (could wait/notify for more responsiveness...)
      try {
        sleep(500);
      }
      catch (Exception e) {
        // NOP
      }
      
      // If the tiles have changed, and they changed more than a
      // certain time ago, then fetch map data again
      synchronized (tiles) { //
        if (tiles.isViewChanged() 
            && tiles.zoom >= 8 // retain old zoom limit for map requests    
            && (tiles.getTimeChanged() < System.currentTimeMillis() - FETCH_MAP_DELAY_MS)) {
          tiles.setViewChanged(false);
          doFetch = true;
        }
      }
      
      if (doFetch) {
        fetchData();
        doFetch = false;
      }
    }
    tiles = null; // release back-ref - otherwise tiles might persist
    applet = null;
  }

  private void fetchData() {
    topLeft = tiles.getTopLeft();
    bottomRight = tiles.getBottomRight();
    MapData map = applet.osm.getNodesLinesWays(getTopLeft(), getBottomRight(), tiles);
    if (map != null) {
      applet.updateMap(map); // updates applet's map with data just fetched
      applet.setStatus(OsmApplet.EDITABLE);    
    }
    else { // failed to update map - reset flags so that re-attempt will be made
      tiles.updateChange();
      topLeft = bottomRight = null;
    }
    applet.debug("fetchData() finished");
    applet.redraw();
  }

  /**
   * @return The top-left point of bbox data is downloading for, or 
   * <code>null</code> if not currently downloading/downloaded.
   */
  Point getTopLeft() {
    return topLeft;
  }

  /**
   * @return The br point of bbox data is downloading for, or 
   * <code>null</code> if not currently downloading/downloaded.
   */
  Point getBottomRight() {
    return bottomRight;
  }
}
