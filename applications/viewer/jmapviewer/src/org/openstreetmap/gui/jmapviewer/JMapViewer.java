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
import java.io.IOException;
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

	public static final int MAX_ZOOM = 18;
	public static final int MIN_ZOOM = 0;

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
			loadingImage = ImageIO.read(getClass().getResourceAsStream("images/hourglass.png"));
		} catch (IOException e1) {
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

		// Substract the calculated values so that we get the x/y index of the
		// upper left tile again including fraction where in the tile is our
		// map origin point (0,0)

		// Get the plain tile number
		Point p = new Point();
		p.x = x - mapPoint.x + getWidth() / 2;
		p.y = y - mapPoint.y + getHeight() / 2;
		center = p;
		this.zoom = zoom;
		if (zoomSlider.getValue() != zoom)
			zoomSlider.setValue(zoom);
		repaint();
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
		int zoom = MAX_ZOOM;
		int x = x_max - x_min;
		int y = y_max - y_min;
		while (x > width || y > height) {
			// System.out.println("zoom: " + zoom + " -> " + x + " " + y);
			zoom--;
			x >>= 1;
			y >>= 1;
		}
		x = x_min + (x_max - x_min) / 2;
		y = y_min + (y_max - y_min) / 2;
		int z = 1 << (MAX_ZOOM - zoom);
		x /= z;
		y /= z;
		setDisplayPosition(x, y, zoom);
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

		// Optimization for loading the centered tile in a lower zoom first
		if (zoom > MIN_ZOOM) {
			int center_tx = center.x / Tile.WIDTH;
			int center_ty = center.y / Tile.HEIGHT;
			Tile centerTile = tileCache.getTile(center_tx, center_ty, zoom);
			if (centerTile == null || !centerTile.isLoaded()) {
				// tile in the center of the screen is not loaded, for faster
				// displaying anything in the center we first load a tile of a
				// lower zoom level
				getTile(center_tx / 2, center_ty / 2, zoom - 1);
			}
		}
		// Regular tile painting
		int left = center.x - getWidth() / 2;
		int top = center.y - getHeight() / 2;

		int tilex = left / Tile.WIDTH;
		int tiley = top / Tile.HEIGHT;
		int off_x = left % Tile.WIDTH;
		int off_y = top % Tile.HEIGHT;
		for (int x = -off_x; x < getWidth(); x += Tile.WIDTH) {
			int tiley_tmp = tiley;
			for (int y = -off_y; y < getHeight(); y += Tile.HEIGHT) {
				Tile tile = getTile(tilex, tiley_tmp, zoom);
				if (tile != null) {
					if (!tile.isLoaded()) {
						// Paint stretched preview from the next lower zoom
						// level (if already loaded)
						Tile parent = tile.getParentTile(tileCache);
						if (parent != null && parent.isLoaded()) {
							int parentx = tile.getXtile() % 2;
							int parenty = tile.getYtile() % 2;
							parent.parentPaint(g, x, y, parentx, parenty);
						} else
							tile.paint(g, x, y);
					} else
						tile.paint(g, x, y);
				}
				if (tileGridVisible)
					g.drawRect(x, y, Tile.WIDTH, Tile.HEIGHT);
				tiley_tmp++;
			}
			tilex++;
		}
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
	public void move(int x, int y) {
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
	 * Decreases the current zoom level by one
	 */
	public void zoomOut() {
		setZoom(zoom - 1);
	}

	public void setZoom(int zoom, Point mapPoint) {
		if (zoom > MAX_ZOOM || zoom == this.zoom)
			return;
		Point2D.Double zoomPos = getPosition(mapPoint);
		// addMapMarker(new MapMarkerDot(Color.RED, zoomPos.x, zoomPos.y));
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
		}
		if (!tile.isLoaded()) {
			jobDispatcher.addJob(new Runnable() {

				public void run() {
					Tile tile = tileCache.getTile(tilex, tiley, zoom);
					if (tile.isLoaded())
						return;
					try {
						// Thread.sleep(500);
						tile.loadTileImage();
						repaint();
					} catch (Exception e) {
						System.err.println("failed loading " + zoom + "/" + tilex + "/" + tiley
								+ " " + e.getMessage());
					}
				}
			});
		}
		return tile;
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

}
