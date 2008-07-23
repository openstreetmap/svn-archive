package org.openstreetmap.gui.jmapviewer;

//License: GPL. Copyright 2008 by Jan Peter Stotz

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;

/**
 * Holds one map tile. Additionally the code for loading the tile image and
 * painting it is also included in this class.
 * 
 * @author Jan Peter Stotz
 */
public class Tile {

	protected int xtile;
	protected int ytile;
	protected int zoom;
	protected BufferedImage image;
	protected String key;
	protected boolean loaded = false;
	public static final int WIDTH = 256;
	public static final int HEIGHT = 256;
	public static final int WIDTH_HALF = 128;
	public static final int HEIGHT_HALF = 128;

	/**
	 * Creates a tile with empty image.
	 * 
	 * @param xtile
	 * @param ytile
	 * @param zoom
	 */
	public Tile(int xtile, int ytile, int zoom) {
		super();
		this.xtile = xtile;
		this.ytile = ytile;
		this.zoom = zoom;
		this.image = null;
		this.key = getTileKey(xtile, ytile, zoom);
	}

	public Tile(int xtile, int ytile, int zoom, BufferedImage image) {
		this(xtile, ytile, zoom);
		this.image = image;
	}

	/**
	 * Tries to get tiles of a lower or higher zoom level (one or two level
	 * difference) from cache and use it as a placeholder until the tile has
	 * been loaded.
	 */
	public void loadPlaceholderFromCache(TileCache cache) {
		BufferedImage tmpImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) tmpImage.getGraphics();
		// g.drawImage(image, 0, 0, null);
		for (int zoomDiff = 1; zoomDiff < 3; zoomDiff++) {
			// first we check if there are already the 2^x tiles
			// of a higher detail level
			int zoom_high = zoom + zoomDiff;
			if (zoom_high <= JMapViewer.MAX_ZOOM) {
				int factor = 1 << zoomDiff;
				int xtile_high = xtile << zoomDiff;
				int ytile_high = ytile << zoomDiff;
				double scale = 1.0 / factor;
				g.setTransform(AffineTransform.getScaleInstance(scale, scale));
				int paintedTileCount = 0;
				for (int x = 0; x < factor; x++) {
					for (int y = 0; y < factor; y++) {
						Tile tile = cache.getTile(xtile_high + x, ytile_high + y, zoom_high);
						if (tile != null && tile.isLoaded()) {
							paintedTileCount++;
							tile.paint(g, x * WIDTH, y * HEIGHT);
						}
					}
				}
				if (paintedTileCount == factor * factor) {
					image = tmpImage;
					return;
				}
			}

			int zoom_low = zoom - zoomDiff;
			if (zoom_low >= JMapViewer.MIN_ZOOM) {
				int xtile_low = xtile >> zoomDiff;
				int ytile_low = ytile >> zoomDiff;
				int factor = (1 << zoomDiff);
				double scale = (double) factor;
				AffineTransform at = new AffineTransform();
				int translate_x = (xtile % factor) * WIDTH;
				int translate_y = (ytile % factor) * HEIGHT;
				at.setTransform(scale, 0, 0, scale, -translate_x, -translate_y);
				g.setTransform(at);
				Tile tile = cache.getTile(xtile_low, ytile_low, zoom_low);
				if (tile != null && tile.isLoaded()) {
					tile.paint(g, 0, 0);
					image = tmpImage;
					return;
				}
			}
		}
	}

	/**
	 * @return tile number on the x axis of this tile
	 */
	public int getXtile() {
		return xtile;
	}

	/**
	 * @return tile number on the y axis of this tile
	 */
	public int getYtile() {
		return ytile;
	}

	/**
	 * @return zoom level of this tile
	 */
	public int getZoom() {
		return zoom;
	}

	public BufferedImage getImage() {
		return image;
	}

	/**
	 * @return key that identifies a tile
	 */
	public String getKey() {
		return key;
	}

	public boolean isLoaded() {
		return loaded;
	}

	public synchronized void loadTileImage() throws IOException {
		if (loaded)
			return;
		URL url;
		URLConnection urlConn;
		DataInputStream input;
		url = new URL("http://tile.openstreetmap.org/" + zoom + "/" + xtile + "/" + ytile + ".png");
		// System.out.println(url);
		urlConn = url.openConnection();
		// urlConn.setUseCaches(false);
		input = new DataInputStream(urlConn.getInputStream());
		image = ImageIO.read(input);
		input.close();
		loaded = true;
	}

	/**
	 * Paints the tile-image on the {@link Graphics} <code>g</code> at the
	 * position <code>x</code>/<code>y</code>.
	 * 
	 * @param g
	 * @param x
	 *            x-coordinate in <code>g</code>
	 * @param y
	 *            y-coordinate in <code>g</code>
	 */
	public void paint(Graphics g, int x, int y) {
		if (image == null)
			return;
		g.drawImage(image, x, y, null);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Tile))
			return false;
		Tile tile = (Tile) obj;
		return (xtile == tile.xtile) && (ytile == tile.ytile) && (zoom == tile.zoom);
	}

	public static String getTileKey(int xtile, int ytile, int zoom) {
		return zoom + "/" + xtile + "/" + ytile;
	}

}