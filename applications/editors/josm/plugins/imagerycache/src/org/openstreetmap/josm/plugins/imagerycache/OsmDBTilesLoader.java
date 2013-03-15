package org.openstreetmap.josm.plugins.imagerycache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.openstreetmap.gui.jmapviewer.JobDispatcher;
import org.openstreetmap.gui.jmapviewer.OsmTileLoader;
import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.interfaces.TileJob;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoaderListener;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource.TileUpdate;

/**
 * 
 * @author Alexei Kasatkin, based on OsmFileCacheTileLoader by @author Jan Peter Stotz, @author Stefan Zeller
 */
class OsmDBTilesLoader extends OsmTileLoader {
    
    
    private static final Logger log = Logger.getLogger(OsmDBTilesLoader.class.getName());
    public static final long FILE_AGE_ONE_DAY = 1000 * 60 * 60 * 24;
    public static final long FILE_AGE_ONE_WEEK = FILE_AGE_ONE_DAY * 7;

    
    
    static class TileDAOMapDB {
        protected HashMap<String, DB> dbs = new HashMap<String, DB>();
        protected HashMap<String, Map<Long,DBTile>> storages  = new HashMap<String, Map<Long,DBTile>>();
        private final File cacheFolder;
        
        /**
         * Lazy creation of DB object associated to * @param source
         * or returning from cache
         */
        private synchronized DB getDB(String source) {
            DB db = dbs.get(source);
            if (db==null) {
                try {
                db = DBMaker
                    .newFileDB(new File(cacheFolder, source.replaceAll("[\\\\/:*?\"<>| ]", "_")))
                    .randomAccessFileEnableIfNeeded()
                    .journalDisable()
                    .closeOnJvmShutdown()
                    .make();
                dbs.put(source, db);
                } catch (Exception e) {
                    log.warning("Error: Can not create MapDB file");
                    e.printStackTrace();
                }
            }
            return db;
        }

        private synchronized Map<Long,DBTile> getStorage(String source) {
            Map<Long, DBTile> m = storages.get(source);
            if (m == null) {
                try {
                    DB d = getDB(source);
                    m = d.getHashMap("tiles");
                    storages.put(source, m);
                    log.log(Level.FINEST, "Created storage {0}", source);
                } catch (Exception e) {
                    log.severe("Error: Can not create HashMap in MapDB storage");
                    e.printStackTrace();
                }
            }
            return m;
        }
        
        public TileDAOMapDB(File cacheFolder) {
            this.cacheFolder = cacheFolder;
        }
        
                
        DBTile getById(String source, long id) {
            return getStorage(source).get(id);
        }

        protected void updateModTime(String source, long id, DBTile dbTile) {
            log.finest("Updating modification time");
            getStorage(source).put(id, dbTile);
        }

        protected void updateTile(String source, long id, DBTile dbTile) {
            log.finest("Updating tile in base");
            getStorage(source).put(id, dbTile);
        }

        protected void deleteTile(String source, long id) {
            getStorage(source).remove(id);
        }


    }
            
    TileDAOMapDB dao;
                        
   
    protected long maxCacheFileAge = FILE_AGE_ONE_WEEK;
    protected long recheckAfter = FILE_AGE_ONE_DAY;

    
    public OsmDBTilesLoader(TileLoaderListener smap, File cacheFolder) {
        super(smap);
        dao = new TileDAOMapDB(cacheFolder);
    }
    
    @Override
    public TileJob createTileLoaderJob(final Tile tile) {
        return new DatabaseLoadJob(tile);
    }
    
    static class DBTile implements Serializable {
        byte data[];
        Map<String, String> metaData;
        long lastModified;
    }

    protected class DatabaseLoadJob implements TileJob {

        private final Tile tile;
        File tileCacheDir;
        DBTile dbTile = null;
        long fileAge = 0;
        boolean fileTilePainted = false;
        
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
            if (fileTilePainted) {
                TileJob job = new TileJob() {
                    public void run() {
                        loadOrUpdateTile();
                    }
                    public Tile getTile() {
                        return tile;
                    }
                };
                JobDispatcher.getInstance().addJob(job);
            } else {
                loadOrUpdateTile();
            }
        }

        private boolean loadTileFromFile() {
            ByteArrayInputStream bin = null;
            try {
                dbTile = dao.getById(sourceName, id);
                
                if (dbTile == null) return false;

                if ("no-tile".equals(tile.getValue("tile-info")))
                {
                    tile.setError("No tile at this zoom level");
                    if (dbTile!=null) {
                        dao.deleteTile(sourceName, id);
                    }
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
                    fileTilePainted = true;
                    return true;
                }
                listener.tileLoadingFinished(tile, true);
                fileTilePainted = true;
            } catch (Exception e) {
                try {
                    if (bin != null) {
                        bin.close();
                        dao.deleteTile(sourceName, id);
                    }
                } catch (Exception e1) {
                }
                dbTile = null;
                fileAge = 0;
            }
            return false;
        }

        long getLastModTime() {
            return System.currentTimeMillis() - maxCacheFileAge + recheckAfter;
        }
                
        private void loadOrUpdateTile() {
            
            try {
                URLConnection urlConn = loadTileFromOsm(tile);
                final TileUpdate tileUpdate = tile.getSource().getTileUpdate();
                if (dbTile != null) {
                    switch (tileUpdate) {
                    case IfModifiedSince:   // (1)
                        urlConn.setIfModifiedSince(fileAge);
                        break;
                    case LastModified:      // (2)
                        if (!isOsmTileNewer(fileAge)) {
                            log.finest("LastModified test: local version is up to date: " + tile);
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
                    log.finest("Answer from HTTP: 304 / ETag test: local version is up to date: " + tile);
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
                        log.log(Level.FINE, "Loading from OSM{0}", tile);
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
                    log.log(Level.SEVERE, "Failed loading {0}: {1}", new Object[]{tile.getUrl(), e.getMessage()});
                    e.printStackTrace();
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

    }
    
}
