package org.openstreetmap.gui.jmapviewer;

//License: GPL. Copyright 2008 by Jan Peter Stotz

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.imageio.ImageIO;

import org.openstreetmap.gui.jmapviewer.interfaces.Job;
import org.openstreetmap.gui.jmapviewer.interfaces.TileCache;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoader;

/**
 * A {@link TileLoader} implementation that loads tiles from OSM via HTTP.
 * 
 * @author Jan Peter Stotz
 */
public class OsmTileLoader implements TileLoader {

	public static final String MAP_MAPNIK = "http://tile.openstreetmap.org";
	public static final String MAP_OSMA = "http://tah.openstreetmap.org/Tiles/tile";

	protected String baseUrl;

	protected JMapViewer map;

	public OsmTileLoader(JMapViewer map) {
		this(map, MAP_MAPNIK);
	}

	public OsmTileLoader(JMapViewer map, String baseUrl) {
		this.map = map;
		this.baseUrl = baseUrl;
	}

	public void addLoadRequest(final int tilex, final int tiley, final int zoom) {
		map.jobDispatcher.addJob(new Job() {

			InputStream input = null;

			public void run() {
				TileCache cache = map.getTileCache();
				Tile tile;
				synchronized (cache) {
					tile = cache.getTile(tilex, tiley, zoom);
					if (tile == null || tile.isLoaded() || tile.loading)
						return;
					tile.loading = true;
				}
				try {
					// Thread.sleep(500);
					input = loadTileFromOsm(tile).getInputStream();
					tile.setImage(ImageIO.read(input));
					tile.setLoaded(true);
					map.repaint();
					input.close();
					input = null;
				} catch (Exception e) {
					if (input == null /* || !input.isStopped() */)
						System.err.println("failed loading " + zoom + "/" + tilex + "/" + tiley
								+ " " + e.getMessage());
				} finally {
					tile.loading = false;
				}
			}

			/**
			 * Terminating all transfers that are currently in progress
			 */
			public void stop() {

				try {
					// if (input != null)
					// input.stop();
				} catch (Exception e) {
				}
			}
		});
	}

	protected HttpURLConnection loadTileFromOsm(Tile tile) throws IOException {
		URL url;
		url = new URL(baseUrl + "/" + tile.getKey() + ".png");
		HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
		urlConn.setReadTimeout(30000); // 30 seconds read
		// timeout
		return urlConn;
	}
}
