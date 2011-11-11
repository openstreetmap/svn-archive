// This code has been adapted and copied from code that has been written by Immanuel Scholz and others for JOSM.
// License: GPL. Copyright 2007 by Tim Haussmann

// Adapted for JTileDownloader by Sven Strickroth <email@cs-ware.de>, 2009 - 2010

package org.openstreetmap.fma.jtiledownloader.views.main.slippymap;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;

import org.openstreetmap.fma.jtiledownloader.datatypes.TileProviderIf;
import org.openstreetmap.fma.jtiledownloader.views.main.inputpanel.BBoxLatLonPanel;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.JTileDownloaderTileLoader;
import org.openstreetmap.gui.jmapviewer.JTileDownloaderTileSourceWrapper;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.MemoryTileCache;
import org.openstreetmap.gui.jmapviewer.OsmMercator;
import org.openstreetmap.gui.jmapviewer.OsmTileLoader;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoader;

/**
 * JComponent that displays the slippy map tiles
 * 
 * @author Tim Haussmann
 * 
 */
public class SlippyMapChooser
    extends JMapViewer
{
    private final static Logger log = Logger.getLogger(SlippyMapChooser.class.getName());
    // upper left and lower right corners of the selection rectangle (x/y on
    // ZOOM_MAX)
    Point iSelectionRectStart;
    Point iSelectionRectEnd;

    // standard dimension
    private Dimension iDownloadDialogDimension;
    // screen size
    private Dimension iScreenSize;

    private BBoxLatLonPanel bboxlatlonpanel;

    JTileDownloaderTileLoader cachedLoader;
    TileLoader uncachedLoader;
    JPanel slippyMapTabPanel;

    /**
     * Create the chooser component.
     */
    public SlippyMapChooser(BBoxLatLonPanel bboxlatlonpanel, String tileDirectory, TileProviderIf tileProvider)
    {
        super();
        cachedLoader = new JTileDownloaderTileLoader(this, tileDirectory);
        uncachedLoader = new OsmTileLoader(this);
        setZoomContolsVisible(true);
        setMapMarkerVisible(false);
        setMinimumSize(new Dimension(350, 350 / 2));
        // We need to set an initial size - this prevents a wrong zoom selection for 
        // the area before the component has been displayed the first time   
        setBounds(new Rectangle(getMinimumSize()));
        setFileCacheEnabled(true);
        setMaxTilesInMemory(1000);

        tileSource = new JTileDownloaderTileSourceWrapper(tileProvider);

        new OsmMapControl(this).initialize(slippyMapTabPanel);
        this.bboxlatlonpanel = bboxlatlonpanel;
        boundingBoxChanged();
        bboxlatlonpanel.setChangeListener(this);
    }

    public void setMaxTilesInMemory(int tiles)
    {
        ((MemoryTileCache) getTileCache()).setCacheSize(tiles);
    }

    public void setFileCacheEnabled(boolean enabled)
    {
        if (enabled)
            setTileLoader(cachedLoader);
        else
            setTileLoader(uncachedLoader);
    }

    protected Point getTopLeftCoordinates()
    {
        return new Point(center.x - (getWidth() / 2), center.y - (getHeight() / 2));
    }

    /**
     * Draw the map.
     */
    @Override
    public void paint(Graphics g)
    {
        try
        {
            super.paint(g);

            // draw selection rectangle
            if (iSelectionRectStart != null && iSelectionRectEnd != null)
            {

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
        }
        catch (Exception e)
        {
            log.log(Level.SEVERE, "Error painting tile", e);
        }
    }

    public void boundingBoxChanged()
    {

        // test if a bounding box has been set set
        if (bboxlatlonpanel.getMinLat() == 0.0 && bboxlatlonpanel.getMinLon() == 0.0 && bboxlatlonpanel.getMaxLat() == 0.0 && bboxlatlonpanel.getMaxLon() == 0.0)
            return;

        int y1 = OsmMercator.LatToY(bboxlatlonpanel.getMinLat(), MAX_ZOOM);
        int y2 = OsmMercator.LatToY(bboxlatlonpanel.getMaxLat(), MAX_ZOOM);
        int x1 = OsmMercator.LonToX(bboxlatlonpanel.getMinLon(), MAX_ZOOM);
        int x2 = OsmMercator.LonToX(bboxlatlonpanel.getMaxLon(), MAX_ZOOM);

        iSelectionRectStart = new Point(Math.min(x1, x2), Math.min(y1, y2));
        iSelectionRectEnd = new Point(Math.max(x1, x2), Math.max(y1, y2));

        // calc the screen coordinates for the new selection rectangle
        MapMarkerDot xmin_ymin = new MapMarkerDot(bboxlatlonpanel.getMinLat(), bboxlatlonpanel.getMinLon());
        MapMarkerDot xmax_ymax = new MapMarkerDot(bboxlatlonpanel.getMaxLat(), bboxlatlonpanel.getMaxLon());

        ArrayList<MapMarker> marker = new ArrayList<MapMarker>(2);
        marker.add(xmin_ymin);
        marker.add(xmax_ymax);
        setMapMarkerList(marker);
        setDisplayToFitMapMarkers();
        zoomOut();
    }

    /**
     * Callback for the OsmMapControl. (Re-)Sets the start and end point of the
     * selection rectangle.
     * 
     * @param aStart
     * @param aEnd
     */
    public void setSelection(Point aStart, Point aEnd)
    {
        if (aStart == null || aEnd == null)
            return;
        Point p_max = new Point(Math.max(aEnd.x, aStart.x), Math.max(aEnd.y, aStart.y));
        Point p_min = new Point(Math.min(aEnd.x, aStart.x), Math.min(aEnd.y, aStart.y));

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

        bboxlatlonpanel.setCoordinates(Math.min(l2.x, l1.x), Math.min(l1.y, l2.y), Math.max(l2.x, l1.x), Math.max(l1.y, l2.y));

        repaint();
    }

    /**
     * Performs resizing of the DownloadDialog in order to enlarge or shrink the
     * map.
     */
    public void resizeSlippyMap()
    {
        if (iScreenSize == null)
        {
            Component c = this.getParent().getParent().getParent().getParent().getParent().getParent().getParent().getParent().getParent();
            // remember the initial set screen dimensions
            iDownloadDialogDimension = c.getSize();
            // retrive the size of the display
            iScreenSize = Toolkit.getDefaultToolkit().getScreenSize();
        }

        // resize
        Component co = this.getParent().getParent().getParent().getParent().getParent().getParent().getParent().getParent().getParent();
        Dimension currentDimension = co.getSize();

        // enlarge
        if (currentDimension.equals(iDownloadDialogDimension))
        {
            // make the each dimension 90% of the absolute display size and
            // center the DownloadDialog
            int w = iScreenSize.width * 90 / 100;
            int h = iScreenSize.height * 90 / 100;
            co.setBounds((iScreenSize.width - w) / 2, (iScreenSize.height - h) / 2, w, h);

        }
        // shrink
        else
        {
            // set the size back to the initial dimensions and center the
            // DownloadDialog
            int w = iDownloadDialogDimension.width;
            int h = iDownloadDialogDimension.height;
            co.setBounds((iScreenSize.width - w) / 2, (iScreenSize.height - h) / 2, w, h);

        }

        repaint();
    }

    private void clearCache()
    {
        this.tileCache = new MemoryTileCache();
    }

    /**
     * @param outputFolder
     */
    public void setDirectory(String outputFolder)
    {
        cachedLoader.setTileCacheDir(outputFolder);
        clearCache();
    }

    /**
     * @param selectedTileProvider
     */
    public void setTileProvider(TileProviderIf selectedTileProvider)
    {
        tileSource = new JTileDownloaderTileSourceWrapper(selectedTileProvider);
        clearCache();
    }

    /**
     * @param noDownload
     */
    public void setNoDownload(boolean noDownload)
    {
        cachedLoader.setNoDownload(noDownload);
        clearCache();
    }

    public void setSaveTiles(boolean saveTiles)
    {
        cachedLoader.setSaveTiles(saveTiles);
    }
}
