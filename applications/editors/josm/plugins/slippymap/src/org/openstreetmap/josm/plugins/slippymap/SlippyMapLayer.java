package org.openstreetmap.josm.plugins.slippymap;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.LinkedList;
import java.util.HashSet;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.RenameLayerAction;
import org.openstreetmap.josm.data.Preferences.PreferenceChangedListener;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.ImageProvider;

import org.openstreetmap.gui.jmapviewer.*;
import org.openstreetmap.gui.jmapviewer.interfaces.*;
import org.openstreetmap.gui.jmapviewer.Tile;

/**
 * Class that displays a slippy map layer.
 *
 * @author Frederik Ramm <frederik@remote.org>
 * @author LuVar <lubomir.varga@freemap.sk>
 * @author Dave Hansen <dave@sr71.net>
 *
 */
public class SlippyMapLayer extends Layer implements PreferenceChangedListener, ImageObserver,
    TileLoaderListener {
    boolean debug = false;
    void out(String s)
    {
        Main.debug(s);
    }

    protected MemoryTileCache tileCache;
    protected TileSource tileSource;
    protected TileLoader tileLoader;
    JobDispatcher jobDispatcher = JobDispatcher.getInstance();

    HashSet<Tile> tileRequestsOutstanding = new HashSet<Tile>();
    public synchronized void tileLoadingFinished(Tile tile, boolean success)
    {
        tile.setLoaded(true);
        needRedraw = true;
        Main.map.repaint(100);
        tileRequestsOutstanding.remove(tile);
        if (debug)
            out("tileLoadingFinished() tile: " + tile + " success: " + success);
    }
    public TileCache getTileCache()
    {
        return tileCache;
    }
    void clearTileCache()
    {
        if (debug)
            out("clearing tile storage");
        tileCache = new MemoryTileCache();
        tileCache.setCacheSize(2000);
    }

    /**
     * Actual zoom lvl. Initial zoom lvl is set to
     * {@link SlippyMapPreferences#getMinZoomLvl()}.
     */
    public int currentZoomLevel;

    LatLon lastTopLeft;
    LatLon lastBotRight;
    private Image bufferImage;
    private Tile clickedTile;
    private boolean needRedraw;
    private JPopupMenu tileOptionMenu;
    JCheckBoxMenuItem autoZoomPopup;
    Tile showMetadataTile;

    void redraw()
    {
        needRedraw = true;
        Main.map.repaint();
    }

    void newTileStorage()
    {
        int origZoom = currentZoomLevel;
        tileSource = SlippyMapPreferences.getMapSource();
        // The minimum should also take care of integer parsing
        // errors which would leave us with a zoom of -1 otherwise
        if (tileSource.getMaxZoom() < currentZoomLevel)
            currentZoomLevel = tileSource.getMaxZoom();
        if (tileSource.getMinZoom() > currentZoomLevel)
            currentZoomLevel = tileSource.getMinZoom();
        if (currentZoomLevel != origZoom) {
            out("changed currentZoomLevel loading new tile store from " + origZoom + " to " + currentZoomLevel);
            out("tileSource.getMinZoom(): " + tileSource.getMinZoom());
            out("tileSource.getMaxZoom(): " + tileSource.getMaxZoom());
            SlippyMapPreferences.setLastZoom(currentZoomLevel);
        }
        clearTileCache();
        //tileLoader = new OsmTileLoader(this);
        tileLoader = new OsmFileCacheTileLoader(this);
    }

    @SuppressWarnings("serial")
    public SlippyMapLayer() {
        super(tr("Slippy Map"));

        setBackgroundLayer(true);
        this.setVisible(true);

        currentZoomLevel = SlippyMapPreferences.getLastZoom();
        newTileStorage();

        tileOptionMenu = new JPopupMenu();

        autoZoomPopup = new JCheckBoxMenuItem();
        autoZoomPopup.setAction(new AbstractAction(tr("Auto Zoom")) {
            public void actionPerformed(ActionEvent ae) {
                boolean new_state = !SlippyMapPreferences.getAutozoom();
                SlippyMapPreferences.setAutozoom(new_state);
            }
        });
        autoZoomPopup.setSelected(SlippyMapPreferences.getAutozoom());
        tileOptionMenu.add(autoZoomPopup);

        tileOptionMenu.add(new JMenuItem(new AbstractAction(tr("Load Tile")) {
            public void actionPerformed(ActionEvent ae) {
                if (clickedTile != null) {
                    loadTile(clickedTile);
                    redraw();
                }
            }
        }));

        tileOptionMenu.add(new JMenuItem(new AbstractAction(
                tr("Show Tile Info")) {
            public void actionPerformed(ActionEvent ae) {
                out("info tile: " + clickedTile);
                if (clickedTile != null) {
                    showMetadataTile = clickedTile;
                    redraw();
                }
            }
        }));

        tileOptionMenu.add(new JMenuItem(new AbstractAction(
                tr("Request Update")) {
            public void actionPerformed(ActionEvent ae) {
                if (clickedTile != null) {
                    //FIXME//clickedTile.requestUpdate();
                    redraw();
                }
            }
        }));

        tileOptionMenu.add(new JMenuItem(new AbstractAction(
                tr("Load All Tiles")) {
            public void actionPerformed(ActionEvent ae) {
                loadAllTiles(true);
                redraw();
            }
        }));

        // increase and decrease commands
        tileOptionMenu.add(new JMenuItem(
                new AbstractAction(tr("Increase zoom")) {
                    public void actionPerformed(ActionEvent ae) {
                        increaseZoomLevel();
                        redraw();
                    }
                }));

        tileOptionMenu.add(new JMenuItem(
                new AbstractAction(tr("Decrease zoom")) {
                    public void actionPerformed(ActionEvent ae) {
                        decreaseZoomLevel();
                        Main.map.repaint();
                    }
                }));

        tileOptionMenu.add(new JMenuItem(
                new AbstractAction(tr("Snap to tile size")) {
                    public void actionPerformed(ActionEvent ae) {
                        double new_factor = Math.sqrt(lastImageScale);
                        if (debug)
                            out("tile snap: scale was: " + lastImageScale + ", new factor: " + new_factor);
                        Main.map.mapView.zoomToFactor(new_factor);
                        redraw();
                    }
                }));
        // end of adding menu commands

        tileOptionMenu.add(new JMenuItem(
                new AbstractAction(tr("Flush Tile Cache")) {
                    public void actionPerformed(ActionEvent ae) {
                        System.out.print("flushing all tiles...");
                        clearTileCache();
                        System.out.println("done");
                    }
                }));
        // end of adding menu commands

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Main.map.mapView.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getButton() != MouseEvent.BUTTON3)
                            return;
                        clickedTile = getTileForPixelpos(e.getX(), e.getY());
                        tileOptionMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                });

                listeners.add(new LayerChangeListener() {
                    public void activeLayerChange(Layer oldLayer, Layer newLayer) {
                    }

                    public void layerAdded(Layer newLayer) {
                    }

                    public void layerRemoved(Layer oldLayer) {
                        Main.pref.listener.remove(SlippyMapLayer.this);
                    }
                });
            }
        });

        Main.pref.listener.add(this);
    }

    void zoomChanged()
    {
        if (debug)
            out("zoomChanged(): " + currentZoomLevel);
        needRedraw = true;
        jobDispatcher.cancelOutstandingJobs();
        tileRequestsOutstanding.clear();
        SlippyMapPreferences.setLastZoom(currentZoomLevel);
    }

    int getMaxZoomLvl()
    {
        int ret = SlippyMapPreferences.getMaxZoomLvl();
        if (tileSource.getMaxZoom() < ret)
            ret = tileSource.getMaxZoom();
        return ret;
    }

    int getMinZoomLvl()
    {
        int ret = SlippyMapPreferences.getMinZoomLvl();
        if (tileSource.getMinZoom() > ret)
            ret = tileSource.getMinZoom();
        return ret;
    }

    /**
     * Zoom in, go closer to map.
     *
     * @return    true, if zoom increasing was successfull, false othervise
     */
    public boolean zoomIncreaseAllowed()
    {
        boolean zia = currentZoomLevel < this.getMaxZoomLvl();
        if (debug)
            out("zoomIncreaseAllowed(): " + zia + " " + currentZoomLevel + " vs. " + this.getMaxZoomLvl() );
        return zia;
    }
    public boolean increaseZoomLevel()
    {
        lastImageScale = null;
        if (zoomIncreaseAllowed()) {
            currentZoomLevel++;
            if (debug)
                out("increasing zoom level to: " + currentZoomLevel);
            zoomChanged();
        } else {
            System.err.println("current zoom lvl ("+currentZoomLevel+") couldnt be increased. "+
                             "MaxZoomLvl ("+this.getMaxZoomLvl()+") reached.");
            return false;
        }
        return true;
    }

    /**
     * Zoom out from map.
     *
     * @return    true, if zoom increasing was successfull, false othervise
     */
    public boolean zoomDecreaseAllowed()
    {
        return currentZoomLevel > this.getMinZoomLvl();
    }
    public boolean decreaseZoomLevel() {
        int minZoom = this.getMinZoomLvl();
        lastImageScale = null;
        if (zoomDecreaseAllowed()) {
            if (debug)
                out("decreasing zoom level to: " + currentZoomLevel);
            currentZoomLevel--;
            zoomChanged();
        } else {
            System.err.println("current zoom lvl couldnt be decreased. MinZoomLvl("+minZoom+") reached.");
            return false;
        }
        return true;
    }

    synchronized Tile getOrCreateTile(int x, int y, int zoom) {
        Tile tile = getTile(x, y, zoom);
        if (tile == null) {
            tile = new Tile(tileSource, x, y, zoom);
            tileCache.addTile(tile);
            tile.loadPlaceholderFromCache(tileCache);
        }
        return tile;
    }

    /*
     * This can and will return null for tiles that are not
     * already in the cache.
     */
    synchronized Tile getTile(int x, int y, int zoom) {
        int max = (1 << zoom);
        if (x < 0 || x >= max || y < 0 || y >= max)
                return null;
        Tile tile = tileCache.getTile(tileSource, x, y, zoom);
        return tile;
    }

    synchronized boolean loadTile(Tile tile)
    {
        if (tile == null)
            return false;
        if (tile.hasError())
            return false;
        if (tile.isLoaded())
            return false;
        if (tile.isLoading())
            return false;
        if (tileRequestsOutstanding.contains(tile))
            return false;
        tileRequestsOutstanding.add(tile);
        jobDispatcher.addJob(tileLoader.createTileLoaderJob(tileSource,
                             tile.getXtile(), tile.getYtile(), tile.getZoom()));
        return true;
    }

    void loadAllTiles(boolean force) {
        MapView mv = Main.map.mapView;
        LatLon topLeft = mv.getLatLon(0, 0);
        LatLon botRight = mv.getLatLon(mv.getWidth(), mv.getHeight());

        TileSet ts = new TileSet(topLeft, botRight, currentZoomLevel);

        // if there is more than 18 tiles on screen in any direction, do not
        // load all tiles!
        if (ts.tilesSpanned() > (18*18)) {
            System.out.println("Not downloading all tiles because there is more than 18 tiles on an axis!");
            return;
        }
        ts.loadAllTiles(force);
    }

    /*
     * Attempt to approximate how much the image is being scaled. For instance,
     * a 100x100 image being scaled to 50x50 would return 0.25.
     */
    Image lastScaledImage = null;
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
        boolean done = ((infoflags & (ERROR | FRAMEBITS | ALLBITS)) != 0);
        needRedraw = true;
        Main.map.repaint(done ? 0 : 100);
        return !done;
    }
    boolean imageLoaded(Image i) {
        if (i == null)
            return false;
        int status = Toolkit.getDefaultToolkit().checkImage(i, -1, -1, this);
        if ((status & ALLBITS) != 0)
            return true;
        return false;
    }
    Image getLoadedTileImage(Tile tile)
    {
        if (!tile.isLoaded())
            return null;
        Image img = tile.getImage();
        if (!imageLoaded(img))
            return null;
        return img;
    }

    double getImageScaling(Image img, Point p0, Point p1) {
        int realWidth = -1;
        int realHeight = -1;
           if (img != null) {
            realWidth = img.getHeight(this);
               realWidth = img.getWidth(this);
        }
        if (realWidth == -1 || realHeight == -1) {
            /*
             * We need a good image against which to work. If
             * the current one isn't loaded, then try the last one.
             * Should be good enough. If we've never seen one, then
             * guess.
             */
            if (lastScaledImage != null) {
                return getImageScaling(lastScaledImage, p0, p1);
            }
            realWidth = 256;
            realHeight = 256;
        } else {
            lastScaledImage = img;
        }
        /*
         * If the zoom scale gets really, really off, these can get into
         * the millions, so make this a double to prevent integer
         * overflows.
         */
        double drawWidth = p1.x - p0.x;
        double drawHeight = p1.x - p0.x;
        // stem.out.println("drawWidth: " + drawWidth + " drawHeight: " +
        // drawHeight);

        double drawArea = drawWidth * drawHeight;
        double realArea = realWidth * realHeight;

        return drawArea / realArea;
    }

    LatLon tileLatLon(Tile t)
    {
        int zoom = t.getZoom();
        return new LatLon(tileYToLat(t.getYtile(), zoom),
                          tileXToLon(t.getXtile(), zoom));
    }

    int paintFromOtherZooms(Graphics g, Tile topLeftTile, Tile botRightTile)
    {
        LatLon topLeft  = tileLatLon(topLeftTile);
        LatLon botRight = tileLatLon(botRightTile);

        if (!autoZoomEnabled())
            return 0;
        if (!SlippyMapPreferences.getAutoloadTiles())
            return 0;

        /*
         * Go looking for tiles in zoom levels *other* than the current
         * one. Even if they might look bad, they look better than a
         * blank tile.
         *
         * Make darn sure that the tilesCache can either hold all of
         * these "fake" tiles or that they don't get inserted in it to
         * begin with.
         */
        //int otherZooms[] = {-5, -4, -3, 2, -2, 1, -1};
        int otherZooms[] = { -1, 1, -2, 2, -3, -4, -5};
        int painted = 0;;
        for (int zoomOff : otherZooms) {
            int zoom = currentZoomLevel + zoomOff;
            if ((zoom < this.getMinZoomLvl()) ||
                (zoom > this.getMaxZoomLvl())) {
                continue;
            }
            TileSet ts = new TileSet(topLeft, botRight, zoom);
            int zoom_painted = this.paintTileImages(g, ts, zoom);
            if (debug && zoom_painted > 0)
                out("painted " + zoom_painted + "/"+ ts.size() +
                    " tiles from zoom("+zoomOff+"): " + zoom);
            painted += zoom_painted;
            if (zoom_painted >= ts.size()) {
                if (debug)
                    out("broke after drawing " + zoom_painted + "/"+ ts.size() + " at zoomOff: " + zoomOff);
                break;
            }
        }
        /*
         * Save this for last since it will get painted over all the others
         */
        return painted;
    }
    // This function is called for several zoom levels, not just
    // the current one.  It should not trigger any tiles to be
    // downloaded.  It should also avoid polluting the tile cache
    // with any tiles since these tiles are not mandatory.
    Double lastImageScale = null;
    int paintTileImages(Graphics g, TileSet ts, int zoom) {
        int paintedTiles = 0;
        boolean imageScaleRecorded = false;
        for (Tile tile : ts.allTiles()) {
            /*
             * We need to get a box in which to draw, so advance by one tile in
             * each direction to find the other corner of the box.
             * Note: this somewhat pollutes the tile cache
             */
            Tile t2 = getOrCreateTile(tile.getXtile() + 1, tile.getYtile() + 1, zoom);
            Image img = getLoadedTileImage(tile);
            Point p = pixelPos(tile);
            Point p2 = pixelPos(t2);
            if (currentZoomLevel == zoom && img == null) {
                int painted = paintFromOtherZooms(g, tile, t2);
                if (painted > 0 && debug)
                    out("painted a total of " + painted);
                continue;
            }
            g.drawImage(img, p.x, p.y, p2.x - p.x, p2.y - p.y, this);
            paintedTiles++;
            if (!imageScaleRecorded && zoom == currentZoomLevel) {
                lastImageScale = new Double(getImageScaling(img, p, p2));
                imageScaleRecorded = true;
            }
            float fadeBackground = SlippyMapPreferences.getFadeBackground();
            if (fadeBackground != 0f) {
                // dimm by painting opaque rect...
                g.setColor(new Color(1f, 1f, 1f, fadeBackground));
                g.fillRect(p.x, p.y, p2.x - p.x, p2.y - p.y);
            }
        }// end of for
        return paintedTiles;
    }

    void paintTileText(TileSet ts, Tile tile, Graphics g, MapView mv, int zoom, Tile t) {
        int fontHeight = g.getFontMetrics().getHeight();
        if (tile == null)
            return;
        Point p = pixelPos(t);
        int texty = p.y + 2 + fontHeight;

        if (SlippyMapPreferences.getDrawDebug()) {
            g.drawString("x=" + t.getXtile() + " y=" + t.getYtile() + " z=" + zoom + "", p.x + 2, texty);
            texty += 1 + fontHeight;
            if ((t.getXtile() % 32 == 0) && (t.getYtile() % 32 == 0)) {
                g.drawString("x=" + t.getXtile() / 32 + " y=" + t.getYtile() / 32 + " z=7", p.x + 2, texty);
                texty += 1 + fontHeight;
            }
        }// end of if draw debug

        if (tile == showMetadataTile) {
            String md = tile.toString();
            if (md != null) {
                g.drawString(md, p.x + 2, texty);
                texty += 1 + fontHeight;
            }
        }

        String tileStatus = tile.getStatus();
        if (!tile.isLoaded()) {
            g.drawString(tr("image " + tileStatus), p.x + 2, texty);
            texty += 1 + fontHeight;
        }
        /*
        We already do repaint when the images load
        we don't need to poll like this
        */
        if (!tile.isLoaded())
            redraw();

        if (SlippyMapPreferences.getDrawDebug()) {
            if (ts.leftTile(t)) {
                if (t.getYtile() % 32 == 31) {
                    g.fillRect(0, p.y - 1, mv.getWidth(), 3);
                } else {
                    g.drawLine(0, p.y, mv.getWidth(), p.y);
                }
            }
        }// /end of if draw debug
    }

    public Point pixelPos(Tile t) {
            double lon = tileXToLon(t.getXtile(), t.getZoom());
            LatLon tmpLL = new LatLon(tileYToLat(t.getYtile(), t.getZoom()), lon);
            MapView mv = Main.map.mapView;
            return mv.getPoint(tmpLL);
        }
    private class TileSet {
        int z12x0, z12x1, z12y0, z12y1;
        int zoom;
        int tileMax = -1;

        TileSet(LatLon topLeft, LatLon botRight, int zoom) {
            this.zoom = zoom;
            z12x0 = lonToTileX(topLeft.lon(), zoom) - 1;
            z12y0 = latToTileY(topLeft.lat(),  zoom) - 1;
            z12x1 = lonToTileX(botRight.lon(), zoom) + 1;
            z12y1 = latToTileY(botRight.lat(), zoom) + 1;
            if (z12x0 > z12x1) {
                int tmp = z12x0;
                z12x0 = z12x1;
                z12x1 = tmp;
            }
            if (z12y0 > z12y1) {
                int tmp = z12y0;
                z12y0 = z12y1;
                z12y1 = tmp;
            }
            tileMax = (int)Math.pow(2.0, zoom);
            if (z12x0 < 0) z12x0 = 0;
            if (z12y0 < 0) z12y0 = 0;
            if (z12x1 > tileMax) z12x1 = tileMax;
            if (z12y1 > tileMax) z12y1 = tileMax;
        }
        double tilesSpanned() {
            return Math.sqrt(1.0 * this.size());
        }

        int size() {
            int x_span = z12x1 - z12x0 + 1;
            int y_span = z12y1 - z12y0 + 1;
            return x_span * y_span;
        }

        /*
         * Get all tiles represented by this TileSet that are
         * already in the tileCache.
         */
        List<Tile> allTiles()
        {
            return this.allTiles(false);
        }
        private List<Tile> allTiles(boolean create)
        {
            List<Tile> ret = new ArrayList<Tile>();
            for (int x = z12x0; x <= z12x1; x++) {
                for (int y = z12y0; y <= z12y1; y++) {
                    Tile t;
                    if (create)
                        t = getOrCreateTile(x % tileMax, y % tileMax, zoom);
                    else
                        t = getTile(x % tileMax, y % tileMax, zoom);
                    if (t != null)
                        ret.add(t);
                }
            }
            return ret;
        }
        void loadAllTiles(boolean force)
        {
            List<Tile> tiles = this.allTiles(true);
            boolean autoload = SlippyMapPreferences.getAutoloadTiles();
            if (!autoload && !force)
               return;
            int nr_queued = 0;
            for (Tile t : tiles) {
                if (loadTile(t))
                    nr_queued++;
            }
            if (debug)
                if (nr_queued > 0)
                    out("queued to load: " + nr_queued + "/" + tiles.size() + " tiles at zoom: " + zoom);
        }
        boolean topTile(Tile t) {
            if (t.getYtile() == z12y0 )
                return true;
            return false;
        }

        boolean leftTile(Tile t) {
            if (t.getXtile() == z12x0 )
                return true;
            return false;
        }
    }

    boolean autoZoomEnabled()
    {
        return autoZoomPopup.isSelected();
    }
    /**
     */
    @Override
    public void paint(Graphics g, MapView mv) {
        long start = System.currentTimeMillis();
        LatLon topLeft = mv.getLatLon(0, 0);
        LatLon botRight = mv.getLatLon(mv.getWidth(), mv.getHeight());
        Graphics oldg = g;

        if (botRight.lon() == 0.0 || botRight.lat() == 0) {
            Main.debug("still initializing??");
            // probably still initializing
            return;
        }
        if (lastTopLeft != null && lastBotRight != null && topLeft.equalsEpsilon(lastTopLeft)
                && botRight.equalsEpsilon(lastBotRight) && bufferImage != null
                && mv.getWidth() == bufferImage.getWidth(null) && mv.getHeight() == bufferImage.getHeight(null)
                && !needRedraw) {

            if (debug)
                out("drawing buffered image");
            g.drawImage(bufferImage, 0, 0, null);
            return;
        }

        needRedraw = false;
        lastTopLeft = topLeft;
        lastBotRight = botRight;
        bufferImage = mv.createImage(mv.getWidth(), mv.getHeight());
        g = bufferImage.getGraphics();

        int zoom = currentZoomLevel;
        TileSet ts = new TileSet(topLeft, botRight, zoom);

        if (autoZoomEnabled()) {
            if (zoomDecreaseAllowed() && (ts.tilesSpanned() > 18)) {
                if (debug)
                    out("too many tiles, decreasing zoom from " + currentZoomLevel);
                if (decreaseZoomLevel())
                    this.paint(oldg, mv);
                return;
            }
            if (zoomIncreaseAllowed() && (ts.tilesSpanned() < 1.0)) {
                if (debug)
                    out("doesn't even cover one tile (" + ts.tilesSpanned()
                        + "), increasing zoom from " + currentZoomLevel);
                if (increaseZoomLevel())
                     this.paint(oldg, mv);
                return;
            }
        }

        // Too many tiles... refuse to draw
        if (ts.tilesSpanned() > 18)
            return;

        ts.loadAllTiles(false);

        int fontHeight = g.getFontMetrics().getHeight();

        g.setColor(Color.DARK_GRAY);

        this.paintTileImages(g, ts, currentZoomLevel);
        g.setColor(Color.red);

        // The current zoom tileset is guaranteed to have all of
        // its tiles
        for (Tile t : ts.allTiles()) {
            // This draws the vertical lines for the entire
            // column. Only draw them for the top tile in
            // the column.
            if (ts.topTile(t)) {
                Point p = pixelPos(t);
                if (SlippyMapPreferences.getDrawDebug()) {
                    if (t.getXtile() % 32 == 0) {
                        // level 7 tile boundary
                        g.fillRect(p.x - 1, 0, 3, mv.getHeight());
                    } else {
                        g.drawLine(p.x, 0, p.x, mv.getHeight());
                    }
                }
            }
            this.paintTileText(ts, t, g, mv, currentZoomLevel, t);
        }
        float fadeBackground = SlippyMapPreferences.getFadeBackground();
        oldg.drawImage(bufferImage, 0, 0, null);

        if (lastImageScale != null) {
            // If each source image pixel is being stretched into > 3
            // drawn pixels, zoom in... getting too pixelated
            if (lastImageScale > 3 && zoomIncreaseAllowed()) {
                if (autoZoomEnabled()) {
                    if (debug)
                        out("autozoom increase: scale: " + lastImageScale);
                    increaseZoomLevel();
                    this.paint(oldg, mv);
                }
            // If each source image pixel is being squished into > 0.32
            // of a drawn pixels, zoom out.
            } else if ((lastImageScale < 0.45) && (lastImageScale > 0) && zoomDecreaseAllowed()) {
                if (autoZoomEnabled()) {
                    if (debug)
                        out("autozoom decrease: scale: " + lastImageScale);
                    decreaseZoomLevel();
                    this.paint(oldg, mv);
                }
            }
        }
        g.setColor(Color.black);
        g.drawString("currentZoomLevel=" + currentZoomLevel, 120, 120);
    }// end of paint method

    /**
     * This isn't very efficient, but it is only used when the
     * user right-clicks on the map.
     */
    Tile getTileForPixelpos(int px, int py) {
        if (debug)
            out("getTileForPixelpos("+px+", "+py+")");
        MapView mv = Main.map.mapView;
        Point clicked = new Point(px, py);
        LatLon topLeft = mv.getLatLon(0, 0);
        LatLon botRight = mv.getLatLon(mv.getWidth(), mv.getHeight());
        int z = currentZoomLevel;
        TileSet ts = new TileSet(topLeft, botRight, z);

        ts.loadAllTiles(false); // make sure there are tile objects for all tiles
        Tile clickedTile = null;
        Point p1 = null, p2 = null;
        for (Tile t1 : ts.allTiles()) {
            Tile t2 = getOrCreateTile(t1.getXtile()+1, t1.getYtile()+1, z);
            Rectangle r = new Rectangle(pixelPos(t1));
            r.add(pixelPos(t2));
            if (debug)
                out("r: " + r + " clicked: " + clicked);
            if (!r.contains(clicked))
                continue;
            clickedTile  = t1;
            break;
        }
        if (clickedTile == null)
             return null;
        System.out.println("clicked on tile: " + clickedTile.getXtile() + " " + clickedTile.getYtile() +
                           " scale: " + lastImageScale + " currentZoomLevel: " + currentZoomLevel);
        return clickedTile;
    }

    @Override
    public Icon getIcon() {
        return ImageProvider.get("slippymap");
    }

    @Override
    public Object getInfoComponent() {
        return null;
    }

    @Override
    public Component[] getMenuEntries() {
        return new Component[] {
                new JMenuItem(LayerListDialog.getInstance().createShowHideLayerAction(this)),
                new JMenuItem(LayerListDialog.getInstance().createDeleteLayerAction(this)),
                new JSeparator(),
                // color,
                new JMenuItem(new RenameLayerAction(this.getAssociatedFile(), this)),
                new JSeparator(),
                new JMenuItem(new LayerListPopup.InfoAction(this)) };
    }

    @Override
    public String getToolTipText() {
        return null;
    }

    @Override
    public boolean isMergable(Layer other) {
        return false;
    }

    @Override
    public void mergeFrom(Layer from) {
    }

    @Override
    public void visitBoundingBox(BoundingXYVisitor v) {
    }

    private int latToTileY(double lat, int zoom) {
        double l = lat / 180 * Math.PI;
        double pf = Math.log(Math.tan(l) + (1 / Math.cos(l)));
        return (int) (Math.pow(2.0, zoom - 1) * (Math.PI - pf) / Math.PI);
    }

    private int lonToTileX(double lon, int zoom) {
        return (int) (Math.pow(2.0, zoom - 3) * (lon + 180.0) / 45.0);
    }

    private double tileYToLat(int y, int zoom) {
        return Math.atan(Math.sinh(Math.PI
                - (Math.PI * y / Math.pow(2.0, zoom - 1))))
                * 180 / Math.PI;
    }

    private double tileXToLon(int x, int zoom) {
        return x * 45.0 / Math.pow(2.0, zoom - 3) - 180.0;
    }

    private static int nr_loaded = 0;
    private static int at_zoom = -1;

    /*
     * (non-Javadoc)
     *
     * @seeorg.openstreetmap.josm.data.Preferences.PreferenceChangedListener#
     * preferenceChanged(java.lang.String, java.lang.String)
     */
    public void preferenceChanged(String key, String newValue) {
        if (key.startsWith(SlippyMapPreferences.PREFERENCE_PREFIX)) {
            // System.err.println(this + ".preferenceChanged('" + key + "', '"
            // + newValue + "') called");
            // when fade background changed, no need to clear tile storage
            // TODO move this code to SlippyMapPreferences class.
            if (!key.equals(SlippyMapPreferences.PREFERENCE_FADE_BACKGROUND)) {
                autoZoomPopup.setSelected(SlippyMapPreferences.getAutozoom());
            }
            if (key.equals(SlippyMapPreferences.PREFERENCE_TILE_SOURCE)) {
                newTileStorage();
            }
            redraw();
        }
    }

    @Override
    public void destroy() {
        Main.pref.listener.remove(SlippyMapLayer.this);
    }
}
