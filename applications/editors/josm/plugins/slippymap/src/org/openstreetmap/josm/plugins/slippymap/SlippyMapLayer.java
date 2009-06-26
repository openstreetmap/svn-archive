package org.openstreetmap.josm.plugins.slippymap;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JMenuItem;
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

/**
 * Class that displays a slippy map layer.
 *
 * @author Frederik Ramm <frederik@remote.org>
 * @author LuVar <lubomir.varga@freemap.sk>
 * @author Dave Hansen <dave@sr71.net>
 * @author Dave Hansen <dave@linux.vnet.ibm.com>
 * 
 */
public class SlippyMapLayer extends Layer implements ImageObserver,
    PreferenceChangedListener {
    /**
     * Actual zoom lvl. Initial zoom lvl is set to
     * {@link SlippyMapPreferences#getMinZoomLvl()}.
     */
    public int currentZoomLevel = SlippyMapPreferences.getMinZoomLvl();
    private Hashtable<SlippyMapKey, SlippyMapTile> tileStorage = null;

    Point[][] pixelpos = new Point[21][21];
    LatLon lastTopLeft;
    LatLon lastBotRight;
    int z12x0, z12x1, z12y0, z12y1;
    private Image bufferImage;
    private SlippyMapTile clickedTile;
    private boolean needRedraw;
    private JPopupMenu tileOptionMenu;

    @SuppressWarnings("serial")
    public SlippyMapLayer() {
        super(tr("Slippy Map"));
        background = true;

        clearTileStorage();

        tileOptionMenu = new JPopupMenu();
        tileOptionMenu.add(new JMenuItem(new AbstractAction(tr("Load Tile")) {
            public void actionPerformed(ActionEvent ae) {
                if (clickedTile != null) {
                    loadSingleTile(clickedTile);
                    needRedraw = true;
                    Main.map.repaint();
                }
            }
        }));

        tileOptionMenu.add(new JMenuItem(new AbstractAction(
                tr("Show Tile Status")) {
            public void actionPerformed(ActionEvent ae) {
                if (clickedTile != null) {
                    clickedTile.loadMetadata();
                    needRedraw = true;
                    Main.map.repaint();
                }
            }
        }));

        tileOptionMenu.add(new JMenuItem(new AbstractAction(
                tr("Request Update")) {
            public void actionPerformed(ActionEvent ae) {
                if (clickedTile != null) {
                    clickedTile.requestUpdate();
                    needRedraw = true;
                    Main.map.repaint();
                }
            }
        }));

        tileOptionMenu.add(new JMenuItem(new AbstractAction(
                tr("Load All Tiles")) {
            public void actionPerformed(ActionEvent ae) {
                loadAllTiles();
                needRedraw = true;
                Main.map.repaint();
            }
        }));

        // increase and decrease commands
        tileOptionMenu.add(new JMenuItem(
                new AbstractAction(tr("Increase zoom")) {
                    public void actionPerformed(ActionEvent ae) {
                        increaseZoomLevel();
                        needRedraw = true;
                        Main.map.repaint();
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
        		new AbstractAction(tr("Flush Tile Cache")) {
        			public void actionPerformed(ActionEvent ae) {
        				System.out.print("flushing all tiles...");
        				for (SlippyMapTile t : tileStorage.values()) {
        					t.dropImage();
        					}
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
                        tileOptionMenu.show(e.getComponent(), e.getX(), e
                                .getY());
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

    /**
     * Zoom in, go closer to map.
     * 
     * @return	true, if zoom increasing was successfull, false othervise
     */
    public boolean increaseZoomLevel() {
        if (currentZoomLevel < SlippyMapPreferences.getMaxZoomLvl()) {
            currentZoomLevel++;
            Main.debug("increasing zoom level to: " + currentZoomLevel);
            needRedraw = true;
        } else {
            System.err.println("current zoom lvl ("+currentZoomLevel+") couldnt be increased. "+
                             "MaxZoomLvl ("+SlippyMapPreferences.getMaxZoomLvl()+") reached.");
            return false;
            }
        return true;
    }

    /**
     * Zoom out from map.
     * 
     * @return	true, if zoom increasing was successfull, false othervise
     */
    public boolean decreaseZoomLevel() {
        if (currentZoomLevel > SlippyMapPreferences.getMinZoomLvl()) {
            Main.debug("decreasing zoom level to: " + currentZoomLevel);
            currentZoomLevel--;
            needRedraw = true;
        } else {
            System.err.println("current zoom lvl couldnt be decreased. MinZoomLvl reached.");
            return false;
        }
        return true;
    }

    public void clearTileStorage() {
        // when max zoom lvl is begin saved, this method is called and probably
        // the setting isnt saved yet.
        int maxZoom = 30; // SlippyMapPreferences.getMaxZoomLvl();
        tileStorage = new Hashtable<SlippyMapKey, SlippyMapTile>();

        checkTileStorage();
    }

    class TileTimeComp implements Comparator<SlippyMapTile> {
            public int compare(SlippyMapTile s1, SlippyMapTile s2) {
                    long t1 = s1.access_time();
                    long t2 = s2.access_time();
                    if (s1 == s2)
                            return 0;
                    if (t1 == t2) {
                            t1 = s1.hashCode();
                            t2 = s2.hashCode();
                    }
                    if (t1 < t2)
                            return -1;
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
        int maxZoom = 30; // SlippyMapPreferences.getMaxZoomLvl();
        long now = System.currentTimeMillis();
        if (now - lastCheck < 1000)
                return;
        lastCheck = now;
        TreeSet<SlippyMapTile> tiles = new TreeSet<SlippyMapTile>(new TileTimeComp());
        tiles.addAll(tileStorage.values());
        int max_nr_tiles = 100;
        if (tiles.size() < max_nr_tiles) {
            Main.debug("total of " + tiles.size() + " loaded tiles, size OK " + now);
            return;
        }
        int nr_to_drop = tiles.size() - max_nr_tiles;;
        Main.debug("total of " + tiles.size() + " tiles, need to flush " + nr_to_drop + " tiles");
        for (SlippyMapTile t : tiles) {
            if (nr_to_drop <= 0)
                    break;
            t.dropImage();
            nr_to_drop--;
        }
    }

    void loadSingleTile(SlippyMapTile tile)
    {
        tile.loadImage();
        this.checkTileStorage();
    }

    synchronized SlippyMapTile getTile(int x, int y, int zoom) {
		SlippyMapKey key = new SlippyMapKey(x, y, zoom);
		if (!key.valid) {
			key = null;
		}
		return tileStorage.get(key);
	}

	synchronized SlippyMapTile putTile(SlippyMapTile tile, int x, int y, int zoom) {
		SlippyMapKey key = new SlippyMapKey(x, y, zoom);
		if (!key.valid) {
			key = null;
		}
		return tileStorage.put(key, tile);
	}

	synchronized SlippyMapTile getOrCreateTile(int x, int y, int zoom) {
		SlippyMapTile tile = getTile(x, y, zoom);
		if (tile != null) {
			return tile;
		}
		tile = new SlippyMapTile(x, y, zoom);
		putTile(tile, x, y, zoom);
		return tile;
	}
    
    void loadAllTiles() {
        MapView mv = Main.map.mapView;
        int zoom = currentZoomLevel;
        LatLon topLeft = mv.getLatLon(0, 0);
        LatLon botRight = mv.getLatLon(mv.getWidth(), mv.getHeight());
        z12x0 = lonToTileX(topLeft.lon(), zoom);
        z12x1 = lonToTileX(botRight.lon(), zoom);
        z12y0 = latToTileY(topLeft.lat(), zoom);
        z12y1 = latToTileY(botRight.lat(), zoom);
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
        // if there is more than 18 tiles on screen in any direction, do not
        // load all tiles!
        if (z12x1 - z12x0 > 18) {
            System.out
                    .println("Not downloading all tiles because there is more than 18 tiles on X axis!");
            return;
        }
        if (z12y1 - z12y0 > 18) {
            System.out
                    .println("Not downloading all tiles because there is more than 18 tiles on Y axis!");
            return;
        }

        for (int x = z12x0 - 1; x <= z12x1; x++) {
            for (int y = z12y0 - 1; y <= z12y1; y++) {
                SlippyMapKey key = new SlippyMapKey(x, y, currentZoomLevel);
                SlippyMapTile tile = tileStorage.get(key);
                if (!key.valid) {
                        System.out.println("paint-1() made invalid key");
                        continue;
                }
                if (tile == null)
                    tileStorage.put(key,
                            tile = new SlippyMapTile(x, y, currentZoomLevel));
                if (tile.getImage() == null) {
                    this.loadSingleTile(tile);
                }
            }
        }
    }

	/*
	 * Attempt to approximate how much the image is being scaled. For instance,
	 * a 100x100 image being scaled to 50x50 would return 0.25.
	 */
	Image lastScaledImage = null;

	double getImageScaling(Image img, Point p0, Point p1) {
		int realWidth = img.getWidth(this);
		int realHeight = img.getHeight(this);
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

	private class Tile {
		public int x;
		public int y;

		Tile(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public Point pixelPos(int zoom) {
			double lon = tileXToLon(this.x, zoom);
			LatLon tmpLL = new LatLon(tileYToLat(this.y, zoom), lon);
			MapView mv = Main.map.mapView;
			return mv.getPoint(Main.proj.latlon2eastNorth(tmpLL));
		}
	}

	private class TileSet {
		int z12x0, z12x1, z12y0, z12y1;
		int zoom;

		TileSet(LatLon topLeft, LatLon botRight, int zoom) {
			this.zoom = zoom;
			z12x0 = lonToTileX(topLeft.lon(), zoom);
			z12x1 = lonToTileX(botRight.lon(), zoom);
			z12y0 = latToTileY(topLeft.lat(), zoom);
			z12y1 = latToTileY(botRight.lat(), zoom);
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
		}

		int tilesSpanned() {
			int x_span = z12x1 - z12x0;
			int y_span = z12y1 - z12y0;
			return x_span * y_span;
		}

		/*
		 * This is pretty silly. Should probably just be implemented as an
		 * iterator to keep from having to instantiate all these tiles.
		 */
		List<Tile> allTiles()
        {
            List<Tile> ret = new ArrayList<Tile>();
            for (int x = z12x0 - 1; x <= z12x1 + 1; x++) {
                if (x < 0)
                    continue;
                for (int y = z12y0 - 1; y <= z12y1 + 1; y++) {
                    if (y < 0)
                        continue;
                    Tile t = new Tile(x, y);
                    ret.add(t);
                }
            }
            return ret;
        }

		boolean topTile(Tile t) {
			if (t.y == z12y0 - 1)
				return true;
			return false;
		}

		boolean leftTile(Tile t) {
			if (t.x == z12x0 - 1)
				return true;
			return false;
		}
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
        g = bufferImage.getGraphics();

        int zoom = currentZoomLevel;
        z12x0 = lonToTileX(topLeft.lon(), zoom);
        z12x1 = lonToTileX(botRight.lon(), zoom);
        z12y0 = latToTileY(topLeft.lat(), zoom);
        z12y1 = latToTileY(botRight.lat(), zoom);

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

        if (z12x1 - z12x0 > 18)
            return;
        if (z12y1 - z12y0 > 18)
            return;

        for (int x = z12x0 - 1; x <= z12x1 + 1; x++) {
            double lon = tileXToLon(x, zoom);
            for (int y = z12y0 - 1; y <= z12y1 + 1; y++) {
                LatLon tmpLL = new LatLon(tileYToLat(y, zoom), lon);
                pixelpos[x - z12x0 + 1][y - z12y0 + 1] = mv.getPoint(Main.proj
                        .latlon2eastNorth(tmpLL));
            }
        }

        int fontHeight = g.getFontMetrics().getHeight();

        g.setColor(Color.DARK_GRAY);

        float fadeBackground = SlippyMapPreferences.getFadeBackground();

        Double imageScale = null;
        int count = 0;
        for (int x = z12x0 - 1; x <= z12x1; x++) {
            for (int y = z12y0 - 1; y <= z12y1; y++) {
                SlippyMapKey key = new SlippyMapKey(x, y, currentZoomLevel);
                SlippyMapTile tile;
                tile = tileStorage.get(key);
                if (!key.valid) {
                        System.out.println("loadAllTiles() made invalid key");
                        continue;
                }
                if (tile == null) {
                    tile = new SlippyMapTile(x, y, currentZoomLevel);
                    tileStorage.put(key, tile);
                    if (SlippyMapPreferences.getAutoloadTiles()) {
                        // TODO probably do on background
                        loadSingleTile(tile);
                    }
                    checkTileStorage();
                }
                Image img = tile.getImage();

                if (img != null) {
                    Point p = pixelpos[x - z12x0 + 1][y - z12y0 + 1];
                    Point p2 = pixelpos[x - z12x0 + 2][y - z12y0 + 2];
                    g.drawImage(img, p.x, p.y, p2.x - p.x, p2.y - p.y, this);
                    if (imageScale == null)
                        imageScale = new Double(getImageScaling(img, p, p2));
                    count++;
                    if (fadeBackground != 0f) {
                        // dimm by painting opaque rect...
                        g.setColor(new Color(1f, 1f, 1f, fadeBackground));
                        g.fillRect(p.x, p.y, p2.x - p.x, p2.y - p.y);
                    }// end of if dim != 0
                }// end of if img != null
            }// end of for
        }// end of for
        g.setColor(Color.red);
        for (int x = z12x0 - 1; x <= z12x1; x++) {
            Point p = pixelpos[x - z12x0 + 1][0];

            if(SlippyMapPreferences.getDrawDebug()) {
                if (x % 32 == 0) {
                    // level 7 tile boundary
                    g.fillRect(p.x - 1, 0, 3, mv.getHeight());
                } else {
                    g.drawLine(p.x, 0, p.x, mv.getHeight());
                }
            }//end of if draw debug

            for (int y = z12y0 - 1; y <= z12y1; y++) {
                SlippyMapKey key = new SlippyMapKey(currentZoomLevel, x, y);
                int texty = p.y + 2 + fontHeight;
                SlippyMapTile tile = tileStorage.get(key);
                if (tile == null) {
                    continue;
                }
                if (!key.valid) {
                        System.out.println("paint-0() made invalid key");
                        continue;
                }
                if (tile.getImage() == null) {
                        loadSingleTile(tile);
                }
                p = pixelpos[x - z12x0 + 1][y - z12y0 + 2];

                if(SlippyMapPreferences.getDrawDebug()) {
                    g.drawString("x=" + x + " y=" + y + " z=" + currentZoomLevel
                            + "", p.x + 2, texty);
                    texty += 1 + fontHeight;
                    if ((x % 32 == 0) && (y % 32 == 0)) {
                        g.drawString("x=" + x / 32 + " y=" + y / 32 + " z=7",
                                p.x + 2, texty);
                        texty += 1 + fontHeight;
                    }
                }//end of if draw debug

                String md = tile.getMetadata();
                if (md != null) {
                    g.drawString(md, p.x + 2, texty);
                    texty += 1 + fontHeight;
                }

                if (tile.getImage() == null) {
                    g.drawString(tr("image not loaded"), p.x + 2, texty);
                    texty += 1 + fontHeight;
                }

                if(SlippyMapPreferences.getDrawDebug()) {
                    if (x == z12x0 - 1) {
                        if (y % 32 == 31) {
                            g.fillRect(0, p.y - 1, mv.getWidth(), 3);
                        } else {
                            g.drawLine(0, p.y, mv.getWidth(), p.y);
                        }
                    }
                }//end of if draw debug
            } // end of for
        }

        oldg.drawImage(bufferImage, 0, 0, null);

        if (imageScale != null) {
            // If each source image pixel is being stretched into > 3
            // drawn pixels, zoom in... getting too pixelated
            if (imageScale > 3) {
                if (SlippyMapPreferences.getAutozoom()) {
                    Main.debug("autozoom increase: "+z12x1+" " + z12x0 + " " + z12y1 + " " + z12y0
                                    + topLeft + " " + botRight + " scale: " + imageScale);
                    increaseZoomLevel();
                }
                this.paint(oldg, mv);
            }

            // If each source image pixel is being squished into > 0.32
            // of a drawn pixels, zoom out.
            if (imageScale < 0.32) {
                if (SlippyMapPreferences.getAutozoom()) {
                    Main.debug("autozoom decrease: "+z12x1+" " + z12x0 + " " + z12y1 + " " + z12y0
                                    + topLeft + " " + botRight + " scale: " + imageScale);
                    decreaseZoomLevel();
                }
                this.paint(oldg, mv);
            }
        }
        g.setColor(Color.black);
        g.drawString("currentZoomLevel=" + currentZoomLevel, 120, 120);
    }// end of paint metod

    SlippyMapTile getTileForPixelpos(int px, int py) {
        int tilex = z12x1;
        int tiley = z12y1;
        for (int x = z12x0; x <= z12x1; x++) {

            if (pixelpos[x - z12x0 + 1][0].x > px) {
                tilex = x - 1;
                break;
            }
        }
        if (tilex == -1)
            return null;
        for (int y = z12y0; y <= z12y1; y++) {

            if (pixelpos[0][y - z12y0 + 1].y > py) {
                tiley = y - 1;
                break;
            }
        }
        if (tiley == -1) {
            return null;
        }

        SlippyMapKey key = new SlippyMapKey(tilex, tiley, currentZoomLevel);
        if (!key.valid) {
            System.err.println("getTileForPixelpos("+px+","+py+") made invalid key");
            return null;
        }
        SlippyMapTile tile = tileStorage.get(key);
        if (tile == null)
            tileStorage.put(key, tile = new SlippyMapTile(tilex, tiley, currentZoomLevel));
        checkTileStorage();
        return tile;
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
                new JMenuItem(new LayerListDialog.ShowHideLayerAction(this)),
                new JMenuItem(new LayerListDialog.DeleteLayerAction(this)),
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

    public boolean imageUpdate(Image img, int infoflags, int x, int y,
            int width, int height) {
        boolean done = ((infoflags & (ERROR | FRAMEBITS | ALLBITS)) != 0);
        if ((infoflags & ERROR) != 0) {
                String url = "unknown";
                for (SlippyMapTile tile : tileStorage.values()) {
                        if (tile.getImage() != img)
                                continue;
                        url = tile.getImageURL().toString();
                }
                System.err.println("imageUpdate(" + img + ") error " + url +")");
        }
        if ((infoflags & SOMEBITS) != 0) {
                //if (y%100 == 0)
                //    System.out.println("imageUpdate("+img+") SOMEBITS ("+x+","+y+")");
        }
        // Repaint immediately if we are done, otherwise batch up
        // repaint requests every 100 milliseconds
        needRedraw = true;
        Main.map.repaint(done ? 0 : 100);
        return !done;
    }

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
                clearTileStorage();
            }
        }
    }

    @Override
    public void destroy() {
        Main.pref.listener.remove(SlippyMapLayer.this);
    }
}
