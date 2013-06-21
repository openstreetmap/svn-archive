package org.openstreetmap.josm.plugins.imagerycache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.Random;
import org.openstreetmap.gui.jmapviewer.JobDispatcher;
import org.openstreetmap.gui.jmapviewer.OsmTileLoader;
import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.interfaces.TileJob;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoaderListener;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource.TileUpdate;
import org.openstreetmap.josm.data.preferences.BooleanProperty;

/**
 * 
 * @author Alexei Kasatkin, based on OsmFileCacheTileLoader by @author Jan Peter Stotz, @author Stefan Zeller
 */
class OsmDBTilesLoader extends OsmTileLoader {
    
    
    public static final long FILE_AGE_ONE_DAY = 1000 * 60 * 60 * 24;
    public static final long FILE_AGE_ONE_WEEK = FILE_AGE_ONE_DAY * 7;
    
    public static final boolean debug = new BooleanProperty("imagerycache.debug", false).get();
            
    TileDAOMapDB dao;
   
    protected long maxCacheFileAge = FILE_AGE_ONE_WEEK;
    protected long recheckAfter = FILE_AGE_ONE_DAY;

    
    public OsmDBTilesLoader(TileLoaderListener smap, File cacheFolder) {
        super(smap);
        dao = TileDAOMapDB.getInstance();
        dao.setCacheFolder(cacheFolder);
    }
    
    @Override
    public TileJob createTileLoaderJob(final Tile tile) {
        return new DatabaseLoadJob(tile);
    }

    protected class DatabaseLoadJob implements TileJob {

        private final Tile tile;
        File tileCacheDir;
        
        /**
         * Stores the tile loaded from database, null if nothing found. 
         */
        DBTile dbTile = null;
        long fileAge = 0;
        
        long id;
        String sourceName;
        
        public DatabaseLoadJob(Tile tile) {
            this.tile = tile;
            id = 0x01000000L * tile.getZoom() + 0x00200000L *tile.getXtile() +tile.getYtile();
            sourceName = tile.getSource().getName();
        }

        @Override
        public Tile getTile() {
            return tile;
        }

        @Override
        public void run() {
            synchronized (tile) {
                if ((tile.isLoaded() && !tile.hasError()) || tile.isLoading())
                    return;
                tile.initLoading();
            }
            if (loadTileFromFile()) {
                return;
            }
            if (dbTile != null) {
                TileJob job = new TileJob() {
                    @Override public void run() {
                        loadOrUpdateTileFromServer();
                    }
                    @Override public Tile getTile() {
                        return tile;
                    }
                };
                JobDispatcher.getInstance().addJob(job);
            } else {
                loadOrUpdateTileFromServer();
            }
        }

        /**
         * Loads tile from database. 
         * There can be dbTile != null but the tile is outdated and reload is still needed
         * @return true if no loading from server is needed.
         */
        private boolean loadTileFromFile() {
            ByteArrayInputStream bin = null;
            try {
                dbTile = dao.getById(sourceName, id);
                
                if (dbTile == null) return false;
                
                loadMetadata(); 
                if (debug) System.out.println(id+": found in cache, metadata ="+dbTile.metaData);

                if ("no-tile".equals(tile.getValue("tile-info")))
                {
                    tile.setError("No tile at this zoom level");
                    dao.deleteTile(sourceName, id);
                } else {
                    bin = new ByteArrayInputStream(dbTile.data);
                    if (bin.available() == 0)
                        throw new IOException("Data empty");
                    tile.loadImage(bin);
                    bin.close();
                }

                fileAge = dbTile.lastModified;
                boolean oldTile = System.currentTimeMillis() - fileAge > maxCacheFileAge;
                if (!oldTile) {
                    tile.setLoaded(true);
                    listener.tileLoadingFinished(tile, true);
                    return true; // tile loaded
                } else {
                    listener.tileLoadingFinished(tile, true);
                    return false; // Tile is loaded, but too old. Should be reloaded from server
                }
            } catch (Exception e) {
                System.out.println("Error: Can not load tile from database: "+sourceName+":"+id);
                e.printStackTrace(System.out);
                try {
                    if (bin != null) {
                        bin.close();
                        dao.deleteTile(sourceName, id);
                    }
                } catch (Exception e1) {   }
                dbTile = null;
                fileAge = 0;
                return false; // tile is not because of some error (corrupted database, etc.)
            } catch (Error e) { // this is bad, bat MapDB throws it
                System.out.println("Serious database error: Can not load tile from database: "+sourceName+":"+id);
                e.printStackTrace(System.out);
                dbTile = null;  fileAge = 0;  return false;                                            
            }
        }

        long getLastModTime() {
            return System.currentTimeMillis() - maxCacheFileAge + recheckAfter;
        }
                
        private void loadOrUpdateTileFromServer() {
            
            try {
                URLConnection urlConn = loadTileFromOsm(tile);
                final TileUpdate tileUpdate = tile.getSource().getTileUpdate();
                if (dbTile != null) {
                    // MapDB wants simmutable entities
                    dbTile = new DBTile(dbTile);
                    switch (tileUpdate) {
                    case IfModifiedSince:   // (1)
                        urlConn.setIfModifiedSince(fileAge);
                        break;
                    case LastModified:      // (2)
                        if (!isOsmTileNewer(fileAge)) {
                            System.out.println("Tile "+id+": LastModified test: local version is up to date");
                            dbTile.lastModified = getLastModTime();
                            dao.updateModTime(sourceName, id, dbTile);
                            return;
                        }
                        break;
                    }
                } else {
                    dbTile = new DBTile();
                }
                
                if (tileUpdate == TileSource.TileUpdate.ETag || tileUpdate == TileSource.TileUpdate.IfNoneMatch) {
                    String fileETag = tile.getValue("etag");
                    if (fileETag != null) {
                        switch (tileUpdate) {
                        case IfNoneMatch:   // (3)
                            urlConn.addRequestProperty("If-None-Match", fileETag);
                            break;
                        case ETag:          // (4)
                            if (hasOsmTileETag(fileETag)) {
                                dbTile.lastModified = getLastModTime();
                                dao.updateModTime(sourceName, id, dbTile);
                                return;
                            }
                        }
                    }
                    tile.putValue("etag", urlConn.getHeaderField("ETag"));
                }
                if (urlConn instanceof HttpURLConnection && ((HttpURLConnection)urlConn).getResponseCode() == 304) {
                    // If we are isModifiedSince or If-None-Match has been set
                    // and the server answers with a HTTP 304 = "Not Modified"
                    if (debug) System.out.println("Tile "+id+": Answer from HTTP=304 / ETag test: local version is up to date");
                    dbTile.lastModified = getLastModTime();
                    dao.updateModTime(sourceName, id, dbTile);
                    return;
                }

                loadTileMetadata(tile, urlConn);
                dbTile.metaData = tile.getMetadata();
                
                if ("no-tile".equals(tile.getValue("tile-info")))
                {
                    tile.setError("No tile at this zoom level");
                    listener.tileLoadingFinished(tile, true);
                } else {
                    for(int i = 0; i < 5; ++i) {
                        if (urlConn instanceof HttpURLConnection && ((HttpURLConnection)urlConn).getResponseCode() == 503) {
                            Thread.sleep(5000+(new Random()).nextInt(5000));
                            continue;
                        }
                        if (debug) System.out.println("Tile "+id+": Loading from OSM, "+ tile);
                        byte[] buffer = loadTileInBuffer(urlConn);
                        if (buffer != null) {
                            tile.loadImage(new ByteArrayInputStream(buffer));
                            tile.setLoaded(true);
                            dbTile.data = buffer;
                            dbTile.lastModified = System.currentTimeMillis();
                            dao.updateTile(sourceName, id, dbTile);
                            listener.tileLoadingFinished(tile, true);
                            break;
                        }
                    }
                }
                
            } catch (Exception e) {
                tile.setError(e.getMessage());
                listener.tileLoadingFinished(tile, false);
                try {
                    System.out.println("Error: Tile "+id+" can not be loaded from"+tile.getUrl());
                    e.printStackTrace(System.out);
                } catch(IOException i) {
                }
            } finally {
                tile.finishLoading();
            }
        }
        
        
        protected byte[] loadTileInBuffer(URLConnection urlConn) throws IOException {
            InputStream input = urlConn.getInputStream();
            ByteArrayOutputStream bout = new ByteArrayOutputStream(input.available());
            byte[] buffer = new byte[2048];
            boolean finished = false;
            do {
                int read = input.read(buffer);
                if (read >= 0) {
                    bout.write(buffer, 0, read);
                } else {
                    finished = true;
                }
            } while (!finished);
            if (bout.size() == 0)
                return null;
            return bout.toByteArray();
        }

        /**
         * Performs a <code>HEAD</code> request for retrieving the
         * <code>LastModified</code> header value.
         *
         * Note: This does only work with servers providing the
         * <code>LastModified</code> header:
         * <ul>
         * <li>{@link tilesources.OsmTileSource.CycleMap} - supported</li>
         * <li>{@link tilesources.OsmTileSource.Mapnik} - not supported</li>
         * </ul>
         *
         * @param fileAge time of the 
         * @return <code>true</code> if the tile on the server is newer than the
         *         file
         * @throws IOException
         */
        protected boolean isOsmTileNewer(long fileAge) throws IOException {
            URL url;
            url = new URL(tile.getUrl());
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            prepareHttpUrlConnection(urlConn);
            urlConn.setRequestMethod("HEAD");
            urlConn.setReadTimeout(30000); // 30 seconds read timeout
            // System.out.println("Tile age: " + new
            // Date(urlConn.getLastModified()) + " / "
            // + new Date(fileAge));
            long lastModified = urlConn.getLastModified();
            if (lastModified == 0)
                return true; // no LastModified time returned
            return (lastModified > fileAge);
        }

        protected boolean hasOsmTileETag(String eTag) throws IOException {
            URL url;
            url = new URL(tile.getUrl());
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            prepareHttpUrlConnection(urlConn);
            urlConn.setRequestMethod("HEAD");
            urlConn.setReadTimeout(30000); // 30 seconds read timeout
            // System.out.println("Tile age: " + new
            // Date(urlConn.getLastModified()) + " / "
            // + new Date(fileAge));
            String osmETag = urlConn.getHeaderField("ETag");
            if (osmETag == null)
                return true;
            return (osmETag.equals(eTag));
        }

        /**
         * Loads attribute map from dbTile to tile
         */
        private void loadMetadata() {
            Map<String,String> m = dbTile.metaData;
            if (m==null) return;
            for (String k: m.keySet()) {
                tile.putValue(k, m.get(k));
            }
        }
    }
}
