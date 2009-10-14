package org.openstreetmap.osmolt.slippymap;

//This code has been adapted and copied from code that has been written by Tim Haussmann, Immanuel Scholz and others for JOSM.
//License: GPL. Copyright 2007 by Josias Polchau

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Vector;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.MemoryTileCache;
import org.openstreetmap.gui.jmapviewer.OsmFileCacheTileLoader;
import org.openstreetmap.gui.jmapviewer.OsmMercator;
import org.openstreetmap.gui.jmapviewer.OsmTileSource;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

/**
 * JComponent that displays the slippy map tiles
 * 
 * @author Josias Polchau
 * 
 */
public class SlippyMapBBoxChooser extends JMapViewer {
  
  private static final long serialVersionUID = -5474551439404012348L;
  
  private TileSource[] sources = { new OsmTileSource.Mapnik(), new OsmTileSource.TilesAtHome(),
      new OsmTileSource.CycleMap() };
  
  private SourceButton iSourceButton = new SourceButton();
  
  // standard dimension
  private Dimension iSurrouningDimension;
  
  OsmMapControl mapControl;
  
  SlippyMapCaller gui;
  
  BBox bbox;
  
  // upper left and lower right corners of the selection rectangle (x/y on
  // ZOOM_MAX)
  Point iSelectionRectStart;
  
  Point iSelectionRectEnd;
  
  public SlippyMapBBoxChooser(SlippyMapCaller gui) {
    super();
    this.gui = gui;
    setZoomContolsVisible(false);
    setMapMarkerVisible(false);
    
    ((MemoryTileCache) getTileCache()).setCacheSize(1000);
    setTileLoader(new OsmFileCacheTileLoader(this));
    bbox = gui.getBBox();
    this.setTileSource(sources[0]);
    mapControl = new OsmMapControl(this, gui.getSlipyyMapSurroundingPane(), iSourceButton);
    iSurrouningDimension = gui.getSlipyyMapSurroundingPane().getSize();
  }
  
  /**
   * Draw the map.
   */
  @Override
  public void paint(Graphics g) {
    this.setBounds(0, 0, iSurrouningDimension.width, iSurrouningDimension.height);
    try {
      super.paint(g);
      // draw selection rectangle
      if (iSelectionRectStart != null && iSelectionRectEnd != null) {
        int zoomDiff = MAX_ZOOM - zoom;
        Point tlc = getTopLeftCoordinates();
        int x_min = (iSelectionRectStart.x >> zoomDiff) - tlc.x;
        int y_min = (iSelectionRectStart.y >> zoomDiff) - tlc.y;
        int x_max = (iSelectionRectEnd.x >> zoomDiff) - tlc.x;
        int y_max = (iSelectionRectEnd.y >> zoomDiff) - tlc.y;
        
        int w = x_max - x_min;
        int h = y_max - y_min;
        g.setColor(new Color(0.9f, 0.7f, 0.7f, 0.6f));
        g.fillRect(x_min, y_min, w, h);
        
        g.setColor(Color.BLACK);
        g.drawRect(x_min, y_min, w, h);
        
      }
      iSourceButton.paint(g);
    } catch (Exception e) {
      e.printStackTrace();
    }
    Dimension newSlipyyMapSurroundingPaneSize = gui.getSlipyyMapSurroundingPane().getSize();
    if ((iSurrouningDimension == null) || (!iSurrouningDimension.equals(newSlipyyMapSurroundingPaneSize))) {
      iSurrouningDimension = newSlipyyMapSurroundingPaneSize;
      this.setBounds(0, 0, iSurrouningDimension.width, iSurrouningDimension.height);
      // System.out.println("SlipyyMapSurroundingPane map:
      // "+newSlipyyMapSurroundingPaneSize);
      
    }
  }
  
  void resizeSlippyMap() {
    
    if ((iSurrouningDimension == null) || (!iSurrouningDimension.equals(gui.getSlipyyMapSurroundingPane().getSize()))) {
      iSurrouningDimension = gui.getSlipyyMapSurroundingPane().getSize();
      this.setBounds(0, 0, iSurrouningDimension.width, iSurrouningDimension.height);
    }
    
  }
  
  void setSelection(Point StartSelectionPoint, Point EndSelectionPoint) {
    
    if (StartSelectionPoint == null || EndSelectionPoint == null)
      return;
    Point p_max = new Point(Math.max(EndSelectionPoint.x, StartSelectionPoint.x), Math.max(EndSelectionPoint.y,
        StartSelectionPoint.y));
    Point p_min = new Point(Math.min(EndSelectionPoint.x, StartSelectionPoint.x), Math.min(EndSelectionPoint.y,
        StartSelectionPoint.y));
    
    Point tlc = getTopLeftCoordinates();
    int zoomDiff = MAX_ZOOM - zoom;
    Point pEnd = new Point(p_max.x + tlc.x, p_max.y + tlc.y);
    Point pStart = new Point(p_min.x + tlc.x, p_min.y + tlc.y);
    
    pEnd.x <<= zoomDiff;
    pEnd.y <<= zoomDiff;
    pStart.x <<= zoomDiff;
    pStart.y <<= zoomDiff;
    
    iSelectionRectStart = pStart;
    iSelectionRectEnd = pEnd;
    
    Point2D.Double l1 = getPosition(p_max);
    Point2D.Double l2 = getPosition(p_min);
    
    bbox.minlat = Math.min(l2.x, l1.x);
    bbox.minlon = Math.min(l1.y, l2.y);
    bbox.maxlat = Math.max(l2.x, l1.x);
    bbox.maxlon = Math.max(l1.y, l2.y);
    
    // gui.boundingBoxChanged(this);
    repaint();
    
  }
  
  protected Point getTopLeftCoordinates() {
    return new Point(center.x - (getWidth() / 2), center.y - (getHeight() / 2));
  }
  
  public void boundingBoxChanged() {
    // test if a bounding box has been set set
    if (bbox.minlat == 0.0 && bbox.minlon == 0.0 && bbox.maxlat == 0.0 && bbox.maxlon == 0.0)
      return;
    
    int y1 = OsmMercator.LatToY(bbox.minlat, MAX_ZOOM);
    int y2 = OsmMercator.LatToY(bbox.maxlat, MAX_ZOOM);
    int x1 = OsmMercator.LonToX(bbox.minlon, MAX_ZOOM);
    int x2 = OsmMercator.LonToX(bbox.maxlon, MAX_ZOOM);
    
    iSelectionRectStart = new Point(Math.min(x1, x2), Math.min(y1, y2));
    iSelectionRectEnd = new Point(Math.max(x1, x2), Math.max(y1, y2));
    
    // calc the screen coordinates for the new selection rectangle
    MapMarkerDot xmin_ymin = new MapMarkerDot(bbox.minlat, bbox.minlon);
    MapMarkerDot xmax_ymax = new MapMarkerDot(bbox.maxlat, bbox.maxlon);
    
    Vector<MapMarker> marker = new Vector<MapMarker>(2);
    marker.add(xmin_ymin);
    marker.add(xmax_ymax);
    setMapMarkerList(marker);
    setDisplayToFitMapMarkers();
    zoomOut();
  }
  
  public void toggleMapSource(int mapSource) {
    this.tileCache = new MemoryTileCache();
    if (mapSource == SourceButton.MAPNIK) {
      this.setTileSource(sources[0]);
    } else if (mapSource == SourceButton.CYCLEMAP) {
      this.setTileSource(sources[2]);
    } else {
      this.setTileSource(sources[1]);
    }
  }
  
}
