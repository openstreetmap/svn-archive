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
        LatLon topLeft = mv.getLatLon(0, 0);
        LatLon botRight = mv.getLatLon(mv.getWidth(), mv.getHeight());

        TileSet ts = new TileSet(topLeft, botRight, currentZoomLevel);
        
        // if there is more than 18 tiles on screen in any direction, do not
        // load all tiles!
        if (ts.tilesSpanned() > (18*18)) {
        	System.out.println("Not downloading all tiles because there is more than 18 tiles on an axis!");
        	return;
        }

        for (Tile t : ts.allTiles()) {
        	SlippyMapTile tile = getOrCreateTile(t.x, t.y, currentZoomLevel);
        	if (tile.getImage() == null) {
        		this.loadSingleTile(tile);
        	}
        }//end of for Tile t
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

	boolean imageLoaded(Image i) {
		if (i == null)
			return false;

		int status = Toolkit.getDefaultToolkit().checkImage(i, -1, -1, this);
		if ((status & ALLBITS) != 0)
			return true;
		return false;
	}

	Double paintTileImages(Graphics g, TileSet ts, int zoom) {
		Double imageScale = null;
		Image img = null;
		for (Tile t : ts.allTiles()) {
			SlippyMapTile tile = getTile(t.x, t.y, zoom);
			if (tile == null) {
				// Don't trigger tile loading if this isn't
				// the exact zoom level we're looking for
				if (zoom != currentZoomLevel)
					continue;
				tile = getOrCreateTile(t.x, t.y, zoom);
				if (SlippyMapPreferences.getAutoloadTiles())
					loadSingleTile(tile);
			}
			img = tile.getImage();
			if (img == null)
				continue;
			if ((zoom != currentZoomLevel) && !tile.isDownloaded())
				continue;
			Point p = t.pixelPos(zoom);
			/*
			 * We need to get a box in which to draw, so advance by one tile in
			 * each direction to find the other corner of the box
			 */
			Tile t2 = new Tile(t.x + 1, t.y + 1);
			Point p2 = t2.pixelPos(zoom);

			g.drawImage(img, p.x, p.y, p2.x - p.x, p2.y - p.y, this);
			if (img == null)
				continue;
			if (imageScale == null && zoom == currentZoomLevel)
				imageScale = new Double(getImageScaling(img, p, p2));
			float fadeBackground = SlippyMapPreferences.getFadeBackground();
			if (fadeBackground != 0f) {
				// dimm by painting opaque rect...
				g.setColor(new Color(1f, 1f, 1f, fadeBackground));
				g.fillRect(p.x, p.y, p2.x - p.x, p2.y - p.y);
			}
		}// end of for
		return imageScale;
	}

	void paintTileText(TileSet ts, Graphics g, MapView mv, int zoom, Tile t) {
		int fontHeight = g.getFontMetrics().getHeight();

		SlippyMapTile tile = getTile(t.x, t.y, zoom);
		if (tile == null) {
			return;
		}
		if (tile.getImage() == null) {
			loadSingleTile(tile);
		}
		Point p = t.pixelPos(zoom);
		int texty = p.y + 2 + fontHeight;

		if (SlippyMapPreferences.getDrawDebug()) {
			g.drawString("x=" + t.x + " y=" + t.y + " z=" + zoom + "", p.x + 2, texty);
			texty += 1 + fontHeight;
			if ((t.x % 32 == 0) && (t.y % 32 == 0)) {
				g.drawString("x=" + t.x / 32 + " y=" + t.y / 32 + " z=7", p.x + 2, texty);
				texty += 1 + fontHeight;
			}
		}// end of if draw debug

		String md = tile.getMetadata();
		if (md != null) {
			g.drawString(md, p.x + 2, texty);
			texty += 1 + fontHeight;
		}

		Image tileImage = tile.getImage();
		if (tileImage == null) {
			g.drawString(tr("image not loaded"), p.x + 2, texty);
			texty += 1 + fontHeight;
		}
		if (!imageLoaded(tileImage)) {
			g.drawString(tr("image loading..."), p.x + 2, texty);
			needRedraw = true;
			Main.map.repaint(100);
			texty += 1 + fontHeight;
		}

		if (SlippyMapPreferences.getDrawDebug()) {
			if (ts.leftTile(t)) {
				if (t.y % 32 == 31) {
					g.fillRect(0, p.y - 1, mv.getWidth(), 3);
				} else {
					g.drawLine(0, p.y, mv.getWidth(), p.y);
				}
			}
		}// /end of if draw debug
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
		if (lastTopLeft != null && lastBotRight != null && topLeft.equalsEpsilon(lastTopLeft)
				&& botRight.equalsEpsilon(lastBotRight) && bufferImage != null
				&& mv.getWidth() == bufferImage.getWidth(null) && mv.getHeight() == bufferImage.getHeight(null)
				&& !needRedraw) {

			g.drawImage(bufferImage, 0, 0, null);
			return;
		}

		needRedraw = false;
		lastTopLeft = topLeft;
		lastBotRight = botRight;
		bufferImage = mv.createImage(mv.getWidth(), mv.getHeight());
		g = bufferImage.getGraphics();

		TileSet ts = new TileSet(topLeft, botRight, currentZoomLevel);
		int zoom = currentZoomLevel;

		if (ts.tilesSpanned() > (18*18)) {
			System.out.println("too many tiles, decreasing zoom from " + currentZoomLevel);
			if (decreaseZoomLevel()) {
				this.paint(oldg, mv);
			}
			return;
		}//end of if more than 18*18
		
		if (ts.tilesSpanned() <= 0) {
			System.out.println("doesn't even cover one tile, increasing zoom from " + currentZoomLevel);
			if (increaseZoomLevel()) {
				this.paint(oldg, mv);
			}
			return;
		}

		int fontHeight = g.getFontMetrics().getHeight();

		g.setColor(Color.DARK_GRAY);

		float fadeBackground = SlippyMapPreferences.getFadeBackground();

		/*
		  * Go looking for tiles in zoom levels *other* than the current
		  * one.  Even if they might look bad, they look better than a
		  *  blank tile.
		  */
		int otherZooms[] = {2, -2, 1, -1};
		for (int zoomOff : otherZooms) {
			int zoom2 = currentZoomLevel + zoomOff;
			if ((zoom <= 0) || (zoom > SlippyMapPreferences.getMaxZoomLvl())) {
				continue;
			}
			TileSet ts2 = new TileSet(topLeft, botRight, zoom2);
			this.paintTileImages(g, ts2, zoom2);
			}
		/*
		 * Save this for last since it will get painted over all the others
		 */
		Double imageScale = this.paintTileImages(g, ts, currentZoomLevel);
		g.setColor(Color.red);
		
		for (Tile t : ts.allTiles()) {
			// This draws the vertical lines for the entire
			// column.  Only draw them for the top tile in
			// the column.
			if (ts.topTile(t)) {
				Point p = t.pixelPos(currentZoomLevel);
				if (SlippyMapPreferences.getDrawDebug()) {
					if (t.x % 32 == 0) {
						// level 7 tile boundary
						g.fillRect(p.x - 1, 0, 3, mv.getHeight());
					} else {
						g.drawLine(p.x, 0, p.x, mv.getHeight());
					}
				}
			}
			this.paintTileText(ts, g, mv, currentZoomLevel, t);
		}

		oldg.drawImage(bufferImage, 0, 0, null);

		if (imageScale != null) {
			// If each source image pixel is being stretched into > 3
			// drawn pixels, zoom in... getting too pixelated
			if (imageScale > 3) {
				if (SlippyMapPreferences.getAutozoom()) {
					Main.debug("autozoom increase: scale: " + imageScale);
					increaseZoomLevel();
				}
				this.paint(oldg, mv);
			}

			// If each source image pixel is being squished into > 0.32
			// of a drawn pixels, zoom out.
			if (imageScale < 0.32) {
				if (SlippyMapPreferences.getAutozoom()) {
					Main.debug("autozoom decrease: scale: " + imageScale);
					decreaseZoomLevel();
				}
				this.paint(oldg, mv);
			}
		}
		g.setColor(Color.black);
		g.drawString("currentZoomLevel=" + currentZoomLevel, 120, 120);
	}// end of paint method

    /**
     * This isn't very efficient, but it is only used when the
     * user right-clicks on the map.
     */
    SlippyMapTile getTileForPixelpos(int px, int py) {
    	MapView mv = Main.map.mapView;
    	Point clicked = new Point(px, py);
    	LatLon topLeft = mv.getLatLon(0, 0);
    	LatLon botRight = mv.getLatLon(mv.getWidth(), mv.getHeight());
    	TileSet ts = new TileSet(topLeft, botRight, currentZoomLevel);
    	int z = currentZoomLevel;
    	
    	Tile clickedTile = null;
        for (Tile t1 : ts.allTiles()) {
            Tile t2 = new Tile(t1.x+1, t1.y+1);
            Point p1 = t1.pixelPos(z);
            Point p2 = t2.pixelPos(z);
            Rectangle r = new Rectangle(p1,new Dimension(p2.x, p2.y));
            if (!r.contains(clicked))
                continue;
            clickedTile  = t1;
            break;
        }
        if (clickedTile == null)
             return null;
        System.out.println("clicked on tile: " + clickedTile.x + " " + clickedTile.y);
        SlippyMapTile tile = getOrCreateTile(clickedTile.x, clickedTile.y, currentZoomLevel);
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

    private SlippyMapTile imgToTile(Image img) {
		// we use the enumeration to avoid ConcurrentUpdateExceptions
		// with other users of the tileStorage
		Enumeration<SlippyMapTile> e = tileStorage.elements();
		while (e.hasMoreElements()) {
			SlippyMapTile t = e.nextElement();
			if (t.getImageNoTimestamp() != img) {
				continue;
			}
			return t;
		}
		return null;
	}
    
    private static int nr_loaded = 0;
    private static int at_zoom = -1;
    
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
		boolean done = ((infoflags & (ERROR | FRAMEBITS | ALLBITS)) != 0);
		SlippyMapTile imageTile = imgToTile(img);
		if (imageTile == null) {
			return false;
		}

		if ((infoflags & ERROR) != 0) {
			String url; // = "unknown";
			url = imageTile.getImageURL().toString();
			System.err.println("imageUpdate(" + img + ") error " + url + ")");
		}
		if (((infoflags & ALLBITS) != 0)) {
			int z = imageTile.getZoom();
			if (z == at_zoom) {
				nr_loaded++;
			} else {
				System.out.println("downloaded " + nr_loaded + " at: " + at_zoom + " now going to " + z);
				nr_loaded = 0;
				at_zoom = z;
			}
			imageTile.markAsDownloaded();
		}
		if ((infoflags & SOMEBITS) != 0) {
			// if (y%100 == 0)
			//System.out.println("imageUpdate("+img+") SOMEBITS ("+x+","+y+")");
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
