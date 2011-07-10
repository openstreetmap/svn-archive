package org.openstreetmap.josm.plugins.walkingpapers;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.ImageObserver;
import java.net.URL;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;

import javax.swing.Action;
import javax.swing.Icon;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Class that displays a slippy map layer. Adapted from SlippyMap plugin for Walking Papers use.
 *
 * @author Frederik Ramm <frederik@remote.org>
 * @author LuVar <lubomir.varga@freemap.sk>
 * @author Dave Hansen <dave@sr71.net>
 *
 */
public class WalkingPapersLayer extends Layer implements ImageObserver {
    /**
     * Actual zoom lvl. Initial zoom lvl is set to
     * {@link WalkingPapersPreferences#getMinZoomLvl()}.
     */
    private int currentZoomLevel;
    private HashMap<WalkingPapersKey, WalkingPapersTile> tileStorage = null;

    private Point[][] pixelpos = new Point[21][21];
    private LatLon lastTopLeft;
    private LatLon lastBotRight;
    private int viewportMinX, viewportMaxX, viewportMinY, viewportMaxY;
    private Image bufferImage;
    private boolean needRedraw;

    private int minzoom, maxzoom;
    private Bounds printBounds;
    private String tileUrlTemplate;
    private String walkingPapersId;

    @SuppressWarnings("serial")
    public WalkingPapersLayer(String id, String tile, Bounds b, int minz, int maxz) {
        super(tr("Walking Papers: {0}", id));
        setBackgroundLayer(true);
        walkingPapersId = id;

        tileUrlTemplate = tile;
        this.printBounds = b;
        this.minzoom = minz; this.maxzoom = maxz;
        currentZoomLevel = minz;

        clearTileStorage();

        MapView.addLayerChangeListener(new LayerChangeListener() {
            public void activeLayerChange(Layer oldLayer, Layer newLayer) {
                // if user changes to a walking papers layer, zoom there just as if
                // it was newly added
                layerAdded(newLayer);
            }

            public void layerAdded(Layer newLayer) {
                // only do something if we are affected
                if (newLayer != WalkingPapersLayer.this) return;
                BoundingXYVisitor bbox = new BoundingXYVisitor();
                bbox.visit(printBounds);
                Main.map.mapView.recalculateCenterScale(bbox);
                needRedraw = true;
            }

            public void layerRemoved(Layer oldLayer) {
                if (oldLayer == WalkingPapersLayer.this) {
                    MapView.removeLayerChangeListener(this);
                }
            }
        });
    }

    /**
     * Zoom in, go closer to map.
     */
    public void increaseZoomLevel() {
        if (currentZoomLevel < maxzoom) {
            currentZoomLevel++;
            needRedraw = true;
        }
    }

    /**
     * Zoom out from map.
     */
    public void decreaseZoomLevel() {
        if (currentZoomLevel > minzoom) {
            currentZoomLevel--;
            needRedraw = true;
        }
    }

    public void clearTileStorage() {
        tileStorage = new HashMap<WalkingPapersKey, WalkingPapersTile>();
        checkTileStorage();
    }

    static class TileTimeComp implements Comparator<WalkingPapersTile> {
        public int compare(WalkingPapersTile s1, WalkingPapersTile s2) {
            long t1 = s1.access_time();
            long t2 = s2.access_time();
            if (s1 == s2) return 0;
            if (t1 == t2) {
                t1 = s1.hashCode();
                t2 = s2.hashCode();
            }
            if (t1 < t2) return -1;
            return 1;
        }
    }

    long lastCheck = 0;
    /**
     * <p>
     * Check if tiles.size() is not more than max_nr_tiles. If yes, oldest tiles by timestamp
     * are fired out from cache.
     * </p>
     */
    public void checkTileStorage() {
        long now = System.currentTimeMillis();
        if (now - lastCheck < 1000) return;
        lastCheck = now;
        TreeSet<WalkingPapersTile> tiles = new TreeSet<WalkingPapersTile>(new TileTimeComp());
        tiles.addAll(tileStorage.values());
        int max_nr_tiles = 100;
        if (tiles.size() < max_nr_tiles) {
            return;
        }
        int dropCount = tiles.size() - max_nr_tiles;;
        for (WalkingPapersTile t : tiles) {
            if (dropCount <= 0)
                break;
            t.dropImage();
            dropCount--;
        }
    }

    void loadSingleTile(WalkingPapersTile tile) {
        tile.loadImage();
        this.checkTileStorage();
    }

    /*
     * Attempt to approximate how much the image is
     * being scaled.  For instance, a 100x100 image
     * being scaled to 50x50 would return 0.25.
     */
    Double getImageScaling(Image img, Point p0, Point p1) {
        int realWidth = img.getWidth(this);
        int realHeight = img.getHeight(this);
        if (realWidth == -1 || realHeight == -1)
                return null;
        int drawWidth = p1.x - p0.x;
        int drawHeight = p1.x - p0.x;

        double drawArea = drawWidth * drawHeight;
        double realArea = realWidth * realHeight;

        return drawArea / realArea;
    }

    /**
     */
    @Override
    public void paint(Graphics2D g, MapView mv, Bounds bounds) {
        LatLon topLeft = mv.getLatLon(0, 0);
        LatLon botRight = mv.getLatLon(mv.getWidth(), mv.getHeight());
        Graphics2D oldg = g;

        if (botRight.lon() == 0.0 || botRight.lat() == 0) {
                // probably still initializing
                return;
        }
        if (lastTopLeft != null && lastBotRight != null
                && topLeft.equalsEpsilon(lastTopLeft)
                && botRight.equalsEpsilon(lastBotRight) && bufferImage != null
                && mv.getWidth() == bufferImage.getWidth(null)
                && mv.getHeight() == bufferImage.getHeight(null) && !needRedraw) {

            g.drawImage(bufferImage, 0, 0, null);
            return;
        }

        needRedraw = false;
        lastTopLeft = topLeft;
        lastBotRight = botRight;
        bufferImage = mv.createImage(mv.getWidth(), mv.getHeight());
        g = (Graphics2D) bufferImage.getGraphics();

        if (!LatLon.isValidLat(topLeft.lat())  ||
            !LatLon.isValidLat(botRight.lat()) ||
            !LatLon.isValidLon(topLeft.lon())  ||
            !LatLon.isValidLon(botRight.lon()))
            return;

        viewportMinX = lonToTileX(topLeft.lon());
        viewportMaxX = lonToTileX(botRight.lon());
        viewportMinY = latToTileY(topLeft.lat());
        viewportMaxY = latToTileY(botRight.lat());

        if (viewportMinX > viewportMaxX) {
            int tmp = viewportMinX;
            viewportMinX = viewportMaxX;
            viewportMaxX = tmp;
        }
        if (viewportMinY > viewportMaxY) {
            int tmp = viewportMinY;
            viewportMinY = viewportMaxY;
            viewportMaxY = tmp;
        }

        if (viewportMaxX-viewportMinX > 18) return;
        if (viewportMaxY-viewportMinY > 18) return;

        for (int x = viewportMinX - 1; x <= viewportMaxX + 1; x++) {
            double lon = tileXToLon(x);
            for (int y = viewportMinY - 1; y <= viewportMaxY + 1; y++) {
                LatLon tmpLL = new LatLon(tileYToLat(y), lon);
                pixelpos[x - viewportMinX + 1][y - viewportMinY + 1] = mv.getPoint(Main.getProjection()
                        .latlon2eastNorth(tmpLL));
            }
        }

        g.setColor(Color.DARK_GRAY);

        Double imageScale = null;
        int count = 0;

        for (int x = viewportMinX-1; x <= viewportMaxX; x++) {

            for (int y = viewportMinY-1; y <= viewportMaxY; y++) {
                WalkingPapersKey key = new WalkingPapersKey(currentZoomLevel, x, y);
                WalkingPapersTile tile;
                tile = tileStorage.get(key);
                if (!key.valid) continue;
                if (tile == null) {
                    // check if tile is in range
                    Bounds tileBounds = new Bounds(new LatLon(tileYToLat(y+1), tileXToLon(x)),
                        new LatLon(tileYToLat(y), tileXToLon(x+1)));
                    if (!tileBounds.asRect().intersects(printBounds.asRect())) continue;
                    tile = new WalkingPapersTile(x, y, currentZoomLevel, this);
                    tileStorage.put(key, tile);
                    loadSingleTile(tile);
                    checkTileStorage();
                }
                Image img = tile.getImage();

                if (img != null) {
                    Point p = pixelpos[x - viewportMinX + 1][y - viewportMinY + 1];
                    Point p2 = pixelpos[x - viewportMinX + 2][y - viewportMinY + 2];
                    g.drawImage(img, p.x, p.y, p2.x - p.x, p2.y - p.y, this);
                    if (imageScale == null)
                        imageScale = getImageScaling(img, p, p2);
                    count++;
                }
            }
        }

        if (count == 0)
        {
            //System.out.println("no images on " + walkingPapersId + ", return");
            return;
        }

        oldg.drawImage(bufferImage, 0, 0, null);

        if (imageScale != null) {
            // If each source image pixel is being stretched into > 3
            // drawn pixels, zoom in... getting too pixelated
            if (imageScale > 3) {
                increaseZoomLevel();
                this.paint(oldg, mv, bounds);
            }

            // If each source image pixel is being squished into > 0.32
            // of a drawn pixels, zoom out.
            else if (imageScale < 0.32) {
                decreaseZoomLevel();
                this.paint(oldg, mv, bounds);
            }
        }
    }// end of paint metod

    WalkingPapersTile getTileForPixelpos(int px, int py) {
        int tilex = viewportMaxX;
        int tiley = viewportMaxY;
        for (int x = viewportMinX; x <= viewportMaxX; x++) {
            if (pixelpos[x - viewportMinX + 1][0].x > px) {
                tilex = x - 1;
                break;
            }
        }

        if (tilex == -1) return null;

        for (int y = viewportMinY; y <= viewportMaxY; y++) {
            if (pixelpos[0][y - viewportMinY + 1].y > py) {
                tiley = y - 1;
                break;
            }
        }

        if (tiley == -1) return null;

        WalkingPapersKey key = new WalkingPapersKey(currentZoomLevel, tilex, tiley);
        if (!key.valid) {
            System.err.println("getTileForPixelpos("+px+","+py+") made invalid key");
            return null;
        }
        WalkingPapersTile tile = tileStorage.get(key);
        if (tile == null)
            tileStorage.put(key, tile = new WalkingPapersTile(tilex, tiley, currentZoomLevel, this));
        checkTileStorage();
        return tile;
    }

    @Override
    public Icon getIcon() {
        return ImageProvider.get("walkingpapers");
    }

    @Override
    public Object getInfoComponent() {
        return getToolTipText();
    }

    @Override
    public Action[] getMenuEntries() {
        return new Action[] {
                LayerListDialog.getInstance().createShowHideLayerAction(),
                LayerListDialog.getInstance().createDeleteLayerAction(),
                SeparatorLayerAction.INSTANCE,
                // color,
                // new JMenuItem(new RenameLayerAction(associatedFile, this)),
                SeparatorLayerAction.INSTANCE,
                new LayerListPopup.InfoAction(this) };
    }

    @Override
    public String getToolTipText() {
        return tr("Walking Papers layer ({0}) in zoom {1}", this.getWalkingPapersId(), currentZoomLevel);
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
        if (printBounds != null)
            v.visit(printBounds);
    }

    private int latToTileY(double lat) {
        double l = lat / 180 * Math.PI;
        double pf = Math.log(Math.tan(l) + (1 / Math.cos(l)));
        return (int) (Math.pow(2.0, currentZoomLevel - 1) * (Math.PI - pf) / Math.PI);
    }

    private int lonToTileX(double lon) {
        return (int) (Math.pow(2.0, currentZoomLevel - 3) * (lon + 180.0) / 45.0);
    }

    private double tileYToLat(int y) {
        return Math.atan(Math.sinh(Math.PI
                - (Math.PI * y / Math.pow(2.0, currentZoomLevel - 1))))
                * 180 / Math.PI;
    }

    private double tileXToLon(int x) {
        return x * 45.0 / Math.pow(2.0, currentZoomLevel - 3) - 180.0;
    }

    public boolean imageUpdate(Image img, int infoflags, int x, int y,
            int width, int height) {
        boolean done = ((infoflags & (ERROR | FRAMEBITS | ALLBITS)) != 0);
        if ((infoflags & ERROR) != 0) return false;
        // Repaint immediately if we are done, otherwise batch up
        // repaint requests every 100 milliseconds
        needRedraw = true;
        Main.map.repaint(done ? 0 : 100);
        return !done;
    }

    public String getWalkingPapersId() {
        return walkingPapersId;
    }

    public URL formatImageUrl(int x, int y, int z) {
        String urlstr = tileUrlTemplate.
            replace("{x}", String.valueOf(x)).
            replace("{y}", String.valueOf(y)).
            replace("{z}", String.valueOf(z));
        try {
            return new URL(urlstr);
        } catch (Exception ex) {
            return null;
        }
    }

}
