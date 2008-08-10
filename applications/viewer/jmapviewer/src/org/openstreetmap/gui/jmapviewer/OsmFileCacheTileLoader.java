package org.openstreetmap.gui.jmapviewer;

//License: GPL. Copyright 2008 by Jan Peter Stotz

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.openstreetmap.gui.jmapviewer.interfaces.Job;
import org.openstreetmap.gui.jmapviewer.interfaces.TileCache;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoader;

/**
 * A {@link TileLoader} implementation that loads tiles from OSM via HTTP and
 * saves all loaded files in a directory located in the the temporary directory.
 * If a tile is present in this file cache it will not be loaded from OSM again.
 * 
 * @author Jan Peter Stotz
 */
public class OsmFileCacheTileLoader extends OsmTileLoader {

	private static final String FILE_EXT = ".png";

	public static final long FILE_AGE_ONE_DAY = 1000 * 60 * 60 * 24;
	public static final long FILE_AGE_ONE_WEEK = FILE_AGE_ONE_DAY * 7;

	protected String tileCacheDir;

	protected long maxFileAge = FILE_AGE_ONE_WEEK;

	public OsmFileCacheTileLoader(JMapViewer map, String baseUrl) {
		super(map, baseUrl);
		String tempDir = System.getProperty("java.io.tmpdir");
		try {
			if (tempDir == null)
				throw new IOException();
			File cacheDir = new File(tempDir, "JMapViewerTiles");
			cacheDir = new File(cacheDir, Integer.toString(baseUrl.hashCode()));
			// System.out.println(cacheDir);
			if (!cacheDir.exists() && !cacheDir.mkdirs())
				throw new IOException();
			tileCacheDir = cacheDir.getAbsolutePath();
		} catch (Exception e) {
			tileCacheDir = "tiles";
		}
	}

	public OsmFileCacheTileLoader(JMapViewer map) {
		this(map, MAP_MAPNIK);
	}

	public Job createTileLoaderJob(final int tilex, final int tiley, final int zoom) {
		return new FileLoadJob(tilex, tiley, zoom);
	}

	protected class FileLoadJob implements Job {
		InputStream input = null;

		int tilex, tiley, zoom;

		public FileLoadJob(int tilex, int tiley, int zoom) {
			super();
			this.tilex = tilex;
			this.tiley = tiley;
			this.zoom = zoom;
		}

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
				long fileAge = 0;
				FileInputStream fin = null;
				File f = null;
				try {
					f = getTileFile(tile);
					fin = new FileInputStream(f);
					tile.loadImage(fin);
					fin.close();
					fileAge = f.lastModified();
					boolean oldTile = System.currentTimeMillis() - fileAge > maxFileAge;
					System.out.println("Loaded from file: " + tile);
					if (!oldTile) {
						tile.setLoaded(true);
						map.repaint();
						return;
					}
					// System.out.println("Cache hit for " + tile +
					// " but file age is high: "
					// + new Date(fileAge));
					map.repaint();
					// if (!isOsmTileNewer(tile, fileAge)) {
					// tile.setLoaded(true);
					// return;
					// }
				} catch (Exception e) {
					try {
						if (fin != null) {
							fin.close();
							f.delete();
						}
					} catch (Exception e1) {
					}
				}
				// Thread.sleep(500);
				// System.out.println("Loading tile from OSM: " + tile);
				HttpURLConnection urlConn = loadTileFromOsm(tile);
				// if (fileAge > 0)
				// urlConn.setIfModifiedSince(fileAge);
				//
				// if (urlConn.getResponseCode() == 304) {
				// System.out.println("Local version is up to date");
				// tile.setLoaded(true);
				// return;
				// }
				byte[] buffer = loadTileInBuffer(urlConn);
				tile.loadImage(new ByteArrayInputStream(buffer));
				tile.setLoaded(true);
				map.repaint();
				input = null;
				saveTileToFile(tile, buffer);
			} catch (Exception e) {
				if (input == null /* || !input.isStopped() */)
					System.err.println("failed loading " + zoom + "/" + tilex
							+ "/" + tiley + " " + e.getMessage());
			} finally {
				tile.loading = false;
			}
		}

		protected byte[] loadTileInBuffer(URLConnection urlConn)
				throws IOException {
			input = urlConn.getInputStream();
			ByteArrayOutputStream bout = new ByteArrayOutputStream(input
					.available());
			byte[] buffer = new byte[2048];
			boolean finished = false;
			do {
				int read = input.read(buffer);
				if (read >= 0)
					bout.write(buffer, 0, read);
				else
					finished = true;
			} while (!finished);
			return bout.toByteArray();
		}

		/**
		 * Performs a <code>HEAD</code> request for retrieving the
		 * <code>LastModified</code> header value.
		 * 
		 * Note: This does only work with servers providing the
		 * <code>LastModified</code> header:
		 * <ul>
		 * <li>{@link OsmTileLoader#MAP_OSMA} - supported</li>
		 * <li>{@link OsmTileLoader#MAP_MAPNIK} - not supported</li>
		 * </ul>
		 * 
		 * @param tile
		 * @param fileAge
		 * @return <code>true</code> if the tile on the server is newer than the
		 *         file
		 * @throws IOException
		 */
		protected boolean isOsmTileNewer(Tile tile, long fileAge)
				throws IOException {
			URL url;
			url = new URL(baseUrl + "/" + tile.getKey() + ".png");
			HttpURLConnection urlConn = (HttpURLConnection) url
					.openConnection();
			urlConn.setRequestMethod("HEAD");
			urlConn.setReadTimeout(30000); // 30 seconds read
			// System.out.println("Tile age: " + new
			// Date(urlConn.getLastModified()) + " / "
			// + new Date(fileAge));
			long lastModified = urlConn.getLastModified();
			if (lastModified == 0)
				return true;
			return (lastModified > fileAge);
		}

		protected File getTileFile(Tile tile) throws IOException {
			return new File(tileCacheDir + "/" + tile.getZoom() + "_"
					+ tile.getXtile() + "_" + tile.getYtile() + FILE_EXT);
		}

		protected void saveTileToFile(Tile tile, byte[] rawData) {
			try {
				FileOutputStream f = new FileOutputStream(tileCacheDir + "/"
						+ tile.getZoom() + "_" + tile.getXtile() + "_"
						+ tile.getYtile() + FILE_EXT);
				f.write(rawData);
				f.close();
				// System.out.println("Saved tile to file: " + tile);
			} catch (Exception e) {
				System.err.println("Failed to save tile content: "
						+ e.getLocalizedMessage());
			}
		}

		public void stop() {
		}
	}

	public long getMaxFileAge() {
		return maxFileAge;
	}

	/**
	 * Sets the maximum age of the local cached tile in the file system.
	 * 
	 * @param maxFileAge
	 *            maximum age in milliseconds
	 * @see #FILE_AGE_ONE_DAY
	 * @see #FILE_AGE_ONE_WEEK
	 */
	public void setMaxFileAge(long maxFileAge) {
		this.maxFileAge = maxFileAge;
	}

	public String getTileCacheDir() {
		return tileCacheDir;
	}

	public void setTileCacheDir(String tileCacheDir) {
		File dir = new File(tileCacheDir);
		dir.mkdirs();
		this.tileCacheDir = dir.getAbsolutePath();
	}

}
