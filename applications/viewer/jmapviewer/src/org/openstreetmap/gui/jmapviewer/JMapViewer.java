package org.openstreetmap.gui.jmapviewer;

//License: GPL. Copyright 2008 by Jan Peter Stotz

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openstreetmap.gui.jmapviewer.JobDispatcher.JobThread;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import org.openstreetmap.gui.jmapviewer.interfaces.TileCache;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoader;

/**
 * 
 * Provides a simple panel that displays pre-rendered map tiles loaded from the
 * OpenStreetMap project.
 * 
 * @author Jan Peter Stotz
 * 
 */
public class JMapViewer extends JPanel {

	private static final long serialVersionUID = 1L;

	/**
	 * Vectors for clock-wise tile painting
	 */
	protected static final Point[] move =
			{ new Point(1, 0), new Point(0, 1), new Point(-1, 0), new Point(0, -1) };

	public static final int MAX_ZOOM = 18;
	public static final int MIN_ZOOM = 0;

	protected TileLoader tileLoader;
	protected TileCache tileCache;
	protected List<MapMarker> mapMarkerList;
	protected boolean mapMarkersVisible;
	protected boolean tileGridVisible;

	/**
	 * x- and y-position of the center of this map-panel on the world map
	 * denoted in screen pixel regarding the current zoom level.
	 */
	protected Point center;

	/**
	 * Current zoom level
	 */
	protected int zoom;

	protected JSlider zoomSlider;
	protected JButton zoomInButton;
	protected JButton zoomOutButton;

	/**
	 * Hourglass image that is displayed until a map tile has been loaded
	 */
	protected BufferedImage loadingImage;

	JobDispatcher jobDispatcher;

	/**
	 * Creates a standard {@link JMapViewer} instance that can be controlled via
	 * mouse: hold right mouse button for moving, double click left mouse button
	 * or use mouse wheel for zooming. Loaded tiles are stored the
	 * {@link MemoryTileCache} and the tile loader uses 4 parallel threads for
	 * retrieving the tiles.
	 */
	public JMapViewer() {
		this(new MemoryTileCache(), 4);
		new DefaultMapController(this);
	}

	public JMapViewer(TileCache tileCache, int downloadThreadCount) {
		super();
		tileLoader = new OsmTileLoader(this);
		this.tileCache = tileCache;
		jobDispatcher = new JobDispatcher(downloadThreadCount);
		mapMarkerList = new LinkedList<MapMarker>();
		mapMarkersVisible = true;
		tileGridVisible = false;
		setLayout(null);
		initializeZoomSlider();
		setMinimumSize(new Dimension(Tile.WIDTH, Tile.HEIGHT));
		setPreferredSize(new Dimension(400, 400));
		try {
			loadingImage =
					ImageIO.read(JMapViewer.class.getResourceAsStream("images/hourglass.png"));
		} catch (Exception e1) {
			loadingImage = null;
		}
		setDisplayPositionByLatLon(50, 9, 3);
	}

	protected void initializeZoomSlider() {
		zoomSlider = new JSlider(MIN_ZOOM, MAX_ZOOM);
		zoomSlider.setOrientation(JSlider.VERTICAL);
		zoomSlider.setBounds(10, 10, 30, 150);
		zoomSlider.setOpaque(false);
		zoomSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setZoom(zoomSlider.getValue());
			}
		});
		add(zoomSlider);
		int size = 18;
		try {
			ImageIcon icon = new ImageIcon(getClass().getResource("images/plus.png"));
			zoomInButton = new JButton(icon);
		} catch (Exception e) {
			zoomInButton = new JButton("+");
			zoomInButton.setFont(new Font("sansserif", Font.BOLD, 9));
			zoomInButton.setMargin(new Insets(0, 0, 0, 0));
		}
		zoomInButton.setBounds(4, 155, size, size);
		zoomInButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				zoomIn();
			}
		});
		add(zoomInButton);
		try {
			ImageIcon icon = new ImageIcon(getClass().getResource("images/minus.png"));
			zoomOutButton = new JButton(icon);
		} catch (Exception e) {
			zoomOutButton = new JButton("-");
			zoomOutButton.setFont(new Font("sansserif", Font.BOLD, 9));
			zoomOutButton.setMargin(new Insets(0, 0, 0, 0));
		}
		zoomOutButton.setBounds(8 + size, 155, size, size);
		zoomOutButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				zoomOut();
			}
		});
		add(zoomOutButton);
	}

	/**
	 * Changes the map pane so that it is centered on the specified coordinate
	 * at the given zoom level.
	 * 
	 * @param lat
	 *            latitude of the specified coordinate
	 * @param lon
	 *            longitude of the specified coordinate
	 * @param zoom
	 *            {@link #MIN_ZOOM} <= zoom level <= {@link #MAX_ZOOM}
	 */
	public void setDisplayPositionByLatLon(double lat, double lon, int zoom) {
		setDisplayPositionByLatLon(new Point(getWidth() / 2, getHeight() / 2), lat, lon, zoom);
	}

	/**
	 * Changes the map pane so that the specified coordinate at the given zoom
	 * level is displayed on the map at the screen coordinate
	 * <code>mapPoint</code>.
	 * 
	 * @param mapPoint
	 *            point on the map denoted in pixels where the coordinate should
	 *            be set
	 * @param lat
	 *            latitude of the specified coordinate
	 * @param lon
	 *            longitude of the specified coordinate
	 * @param zoom
	 *            {@link #MIN_ZOOM} <= zoom level <= {@link #MAX_ZOOM}
	 */
	public void setDisplayPositionByLatLon(Point mapPoint, double lat, double lon, int zoom) {
		int x = OsmMercator.LonToX(lon, zoom);
		int y = OsmMercator.LatToY(lat, zoom);
		setDisplayPosition(mapPoint, x, y, zoom);
	}

	public void setDisplayPosition(int x, int y, int zoom) {
		setDisplayPosition(new Point(getWidth() / 2, getHeight() / 2), x, y, zoom);
	}

	public void setDisplayPosition(Point mapPoint, int x, int y, int zoom) {
		if (zoom > MAX_ZOOM || zoom < MIN_ZOOM)
			return;

		// Get the plain tile number
		Point p = new Point();
		p.x = x - mapPoint.x + getWidth() / 2;
		p.y = y - mapPoint.y + getHeight() / 2;
		center = p;
		setIgnoreRepaint(true);
		try {
			int oldZoom = this.zoom;
			this.zoom = zoom;
			if (oldZoom != zoom)
				zoomChanged(oldZoom);
			if (zoomSlider.getValue() != zoom)
				zoomSlider.setValue(zoom);
		} finally {
			setIgnoreRepaint(false);
			repaint();
		}
	}

	/**
	 * Sets the displayed map pane and zoom level so that all map markers are
	 * visible.
	 */
	public void setDisplayToFitMapMarkers() {
		if (mapMarkerList == null || mapMarkerList.size() == 0)
			return;
		int x_min = Integer.MAX_VALUE;
		int y_min = Integer.MAX_VALUE;
		int x_max = Integer.MIN_VALUE;
		int y_max = Integer.MIN_VALUE;
		for (MapMarker marker : mapMarkerList) {
			int x = OsmMercator.LonToX(marker.getLon(), MAX_ZOOM);
			int y = OsmMercator.LatToY(marker.getLat(), MAX_ZOOM);
			x_max = Math.max(x_max, x);
			y_max = Math.max(y_max, y);
			x_min = Math.min(x_min, x);
			y_min = Math.min(y_min, y);
		}
		int height = Math.max(0, getHeight());
		int width = Math.max(0, getWidth());
		// System.out.println(x_min + " < x < " + x_max);
		// System.out.println(y_min + " < y < " + y_max);
		// System.out.println("tiles: " + width + " " + height);
		int newZoom = MAX_ZOOM;
		int x = x_max - x_min;
		int y = y_max - y_min;
		while (x > width || y > height) {
			// System.out.println("zoom: " + zoom + " -> " + x + " " + y);
			newZoom--;
			x >>= 1;
			y >>= 1;
		}
		x = x_min + (x_max - x_min) / 2;
		y = y_min + (y_max - y_min) / 2;
		int z = 1 << (MAX_ZOOM - newZoom);
		x /= z;
		y /= z;
		setDisplayPosition(x, y, newZoom);
	}

	public Point2D.Double getPosition() {
		double lon = OsmMercator.XToLon(center.x, zoom);
		double lat = OsmMercator.YToLat(center.y, zoom);
		return new Point2D.Double(lat, lon);
	}

	public Point2D.Double getPosition(Point mapPoint) {
		int x = center.x + mapPoint.x - getWidth() / 2;
		int y = center.y + mapPoint.y - getHeight() / 2;
		double lon = OsmMercator.XToLon(x, zoom);
		double lat = OsmMercator.YToLat(y, zoom);
		return new Point2D.Double(lat, lon);
	}

	/**
	 * Calculates the position on the map of a given coordinate
	 * 
	 * @param lat
	 * @param lon
	 * @return point on the map or <code>null</code> if the point is not visible
	 */
	public Point getMapPosition(double lat, double lon) {
		int x = OsmMercator.LonToX(lon, zoom);
		int y = OsmMercator.LatToY(lat, zoom);
		x -= center.x - getWidth() / 2;
		y -= center.y - getHeight() / 2;
		if (x < 0 || y < 0 || x > getWidth() || y > getHeight())
			return null;
		return new Point(x, y);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		int iMove = 0;

		int tilex = center.x / Tile.WIDTH;
		int tiley = center.y / Tile.HEIGHT;
		int off_x = (center.x % Tile.WIDTH);
		int off_y = (center.y % Tile.HEIGHT);

		int posx = getWidth() / 2 - off_x;
		int posy = getHeight() / 2 - off_y;

		int diff_left = off_x;
		int diff_right = Tile.WIDTH - off_x;
		int diff_top = off_y;
		int diff_bottom = Tile.HEIGHT - off_y;

		boolean start_left = diff_left < diff_right;
		boolean start_top = diff_top < diff_bottom;

		if (start_top) {
			if (start_left)
				iMove = 2;
			else
				iMove = 3;
		} else {
			if (start_left)
				iMove = 1;
			else
				iMove = 0;
		} // calculate the visibility borders
		int x_min = -Tile.WIDTH;
		int y_min = -Tile.HEIGHT;
		int x_max = getWidth();
		int y_max = getHeight();

		boolean painted = true;
		int x = 0;
		while (painted) {
			painted = false;
			for (int y = 0; y < 4; y++) {
				if (y % 2 == 0)
					x++;
				for (int z = 0; z < x; z++) {
					if (x_min <= posx && posx <= x_max && y_min <= posy && posy <= y_max) { // tile
						// is
						// visible
						Tile tile = getTile(tilex, tiley, zoom);
						if (tile != null) {
							painted = true;
							tile.paint(g, posx, posy);
							if (tileGridVisible)
								g.drawRect(posx, posy, Tile.WIDTH, Tile.HEIGHT);
						}
					}
					Point p = move[iMove];
					posx += p.x * Tile.WIDTH;
					posy += p.y * Tile.HEIGHT;
					tilex += p.x;
					tiley += p.y;
				}
				iMove = (iMove + 1) % move.length;
			}
		}
		// g.drawString("Tiles in cache: " + tileCache.getTileCount(), 50, 20);
		if (!mapMarkersVisible || mapMarkerList == null)
			return;
		for (MapMarker marker : mapMarkerList) {
			Point p = getMapPosition(marker.getLat(), marker.getLon());
			// System.out.println(marker + " -> " + p);
			if (p != null)
				marker.paint(g, p);
		}
	}

	/**
	 * Moves the visible map pane.
	 * 
	 * @param x
	 *            horizontal movement in pixel.
	 * @param y
	 *            vertical movement in pixel
	 */
	public void moveMap(int x, int y) {
		center.x += x;
		center.y += y;
		repaint();
	}

	/**
	 * @return the current zoom level
	 */
	public int getZoom() {
		return zoom;
	}

	/**
	 * Increases the current zoom level by one
	 */
	public void zoomIn() {
		setZoom(zoom + 1);
	}

	/**
	 * Increases the current zoom level by one
	 */
	public void zoomIn(Point mapPoint) {
		setZoom(zoom + 1, mapPoint);
	}

	/**
	 * Decreases the current zoom level by one
	 */
	public void zoomOut() {
		setZoom(zoom - 1);
	}

	/**
	 * Decreases the current zoom level by one
	 */
	public void zoomOut(Point mapPoint) {
		setZoom(zoom - 1, mapPoint);
	}

	public void setZoom(int zoom, Point mapPoint) {
		if (zoom > MAX_ZOOM || zoom == this.zoom)
			return;
		Point2D.Double zoomPos = getPosition(mapPoint);
		jobDispatcher.cancelOutstandingJobs(); // Clearing outstanding load
		// requests
		setDisplayPositionByLatLon(mapPoint, zoomPos.x, zoomPos.y, zoom);
	}

	public void setZoom(int zoom) {
		setZoom(zoom, new Point(getWidth() / 2, getHeight() / 2));
	}

	/**
	 * retrieves a tile from the cache. If the tile is not present in the cache
	 * a load job is added to the working queue of {@link JobThread}.
	 * 
	 * @param tilex
	 * @param tiley
	 * @param zoom
	 * @return specified tile from the cache or <code>null</code> if the tile
	 *         was not found in the cache.
	 */
	protected Tile getTile(final int tilex, final int tiley, final int zoom) {
		int max = (1 << zoom);
		if (tilex < 0 || tilex >= max || tiley < 0 || tiley >= max)
			return null;
		Tile tile = tileCache.getTile(tilex, tiley, zoom);
		if (tile == null) {
			tile = new Tile(tilex, tiley, zoom, loadingImage);
			tileCache.addTile(tile);
			tile.loadPlaceholderFromCache(tileCache);
		}
		if (!tile.isLoaded()) {
			jobDispatcher.addJob(tileLoader.createTileLoaderJob(tilex, tiley, zoom));
		}
		return tile;
	}

	/**
	 * Every time the zoom level changes this method is called. Override it in
	 * derived implementations for adapting zoom dependent values. The new zoom
	 * level can be obtained via {@link #getZoom()}.
	 * 
	 * @param oldZoom
	 *            the previous zoom level
	 */
	protected void zoomChanged(int oldZoom) {
	}

	public boolean isTileGridVisible() {
		return tileGridVisible;
	}

	public void setTileGridVisible(boolean tileGridVisible) {
		this.tileGridVisible = tileGridVisible;
		repaint();
	}

	public boolean getMapMarkersVisible() {
		return mapMarkersVisible;
	}

	/**
	 * Enables or disables painting of the {@link MapMarker}
	 * 
	 * @param mapMarkersVisible
	 * @see #addMapMarker(MapMarker)
	 * @see #getMapMarkerList()
	 */
	public void setMapMarkerVisible(boolean mapMarkersVisible) {
		this.mapMarkersVisible = mapMarkersVisible;
		repaint();
	}

	public void setMapMarkerList(List<MapMarker> mapMarkerList) {
		this.mapMarkerList = mapMarkerList;
		repaint();
	}

	public List<MapMarker> getMapMarkerList() {
		return mapMarkerList;
	}

	public void addMapMarker(MapMarker marker) {
		mapMarkerList.add(marker);
	}

	public void setZoomContolsVisible(boolean visible) {
		zoomSlider.setVisible(visible);
		zoomInButton.setVisible(visible);
		zoomOutButton.setVisible(visible);
	}

	public boolean getZoomContolsVisible() {
		return zoomSlider.isVisible();
	}

	public TileCache getTileCache() {
		return tileCache;
	}

	public TileLoader getTileLoader() {
		return tileLoader;
	}

	public void setTileLoader(TileLoader tileLoader) {
		this.tileLoader = tileLoader;
	}

}
