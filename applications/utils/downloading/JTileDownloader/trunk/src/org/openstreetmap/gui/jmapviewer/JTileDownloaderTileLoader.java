package org.openstreetmap.gui.jmapviewer;

//License: GPL. Copyright 2008 by Jan Peter Stotz

// Adapted for JTileDownloader by Sven Strickroth <email@cs-ware.de>, 2010

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.logging.Logger;
import org.openstreetmap.gui.jmapviewer.interfaces.TileCache;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoader;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoaderListener;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

/**
 * A {@link TileLoader} implementation that loads tiles from OSM via HTTP and
 * saves all loaded files in a directory located in the the temporary directory.
 * If a tile is present in this file cache it will not be loaded from OSM again.
 * 
 * @author Jan Peter Stotz
 */
public class JTileDownloaderTileLoader
    extends OsmTileLoader
{
    private static final Logger log = Logger.getLogger(JTileDownloaderTileLoader.class.getName());

    protected String cacheDirBase;
    protected boolean noDownload = false;
    protected boolean saveTiles = false;

    public JTileDownloaderTileLoader(TileLoaderListener map, String cacheDirBase)
    {
        super(map);
        this.cacheDirBase = cacheDirBase;
    }

    @Override
    public Runnable createTileLoaderJob(final TileSource source, final int tilex, final int tiley, final int zoom)
    {
        return new FileLoadJob(source, tilex, tiley, zoom);
    }

    protected class FileLoadJob
        implements Runnable
    {
        InputStream input = null;

        int tilex, tiley, zoom;
        Tile tile;
        TileSource source;
        File tileCacheDir;
        File tileFile = null;

        public FileLoadJob(TileSource source, int tilex, int tiley, int zoom)
        {
            super();
            this.source = source;
            this.tilex = tilex;
            this.tiley = tiley;
            this.zoom = zoom;
        }

        public void run()
        {
            TileCache cache = listener.getTileCache();
            synchronized (cache)
            {
                tile = cache.getTile(source, tilex, tiley, zoom);
                if (tile == null || tile.isLoaded() || tile.loading)
                    return;
            }
            tileCacheDir = new File(cacheDirBase);
            if (tileCacheDir.exists() && loadTileFromFile())
                return;

            if (!noDownload)
            {
                try
                {
                    Thread.sleep(500);

                    byte[] buffer = loadTileInBuffer(loadTileFromOsm(tile).getInputStream());
                    if (buffer != null)
                    {
                        tile.loadImage(new ByteArrayInputStream(buffer));
                        tile.loading = false;
                        listener.tileLoadingFinished(tile, true);
                        if (saveTiles)
                        {
                            saveTileToFile(buffer);
                        }
                    }
                    tile.setLoaded(true);
                    return;
                }
                catch (Exception e)
                {
                    // some error occoured
                }
            }

            tile.setImage(Tile.ERROR_IMAGE);
            tile.setLoaded(true);
            listener.tileLoadingFinished(tile, true);
        }

        protected byte[] loadTileInBuffer(InputStream input) throws IOException
        {
            ByteArrayOutputStream bout = new ByteArrayOutputStream(input.available());
            byte[] buffer = new byte[2048];
            boolean finished = false;
            do
            {
                int read = input.read(buffer);
                if (read >= 0)
                    bout.write(buffer, 0, read);
                else
                    finished = true;
            }
            while (!finished);
            input.close();
            if (bout.size() == 0)
                return null;
            return bout.toByteArray();
        }

        protected void saveTileToFile(byte[] rawData)
        {
            try
            {
                File folder = new File(tileCacheDir + "/" + tile.getZoom() + "/" + tile.getXtile());
                if (!folder.exists())
                {
                    folder.mkdirs();
                }
                FileOutputStream f = new FileOutputStream(new File(folder, tile.getYtile() + "." + source.getTileType()));
                f.write(rawData);
                f.close();
                log.finest("Saved tile to file: " + tile);
            }
            catch (Exception e)
            {
                log.warning("Failed to save tile content: " + e.getLocalizedMessage());
            }
        }

        protected boolean loadTileFromFile()
        {
            FileInputStream fin = null;
            try
            {
                tileFile = getTileFile();
                fin = new FileInputStream(tileFile);
                if (fin.available() == 0)
                    throw new IOException("File empty");
                tile.loadImage(fin);
                fin.close();
                log.finest("Loaded from file: " + tile);
                tile.setLoaded(true);
                listener.tileLoadingFinished(tile, true);
                return true;
            }
            catch (Exception e)
            {
                try
                {
                    if (fin != null)
                    {
                        fin.close();
                        tileFile.delete();
                    }
                }
                catch (Exception e1)
                {
                }
                tileFile = null;
            }
            return false;
        }

        protected File getTileFile()
        {
            return new File(tileCacheDir + "/" + tile.getZoom() + "/" + tile.getXtile() + "/" + tile.getYtile() + "." + source.getTileType());
        }

    }

    public String getCacheDirBase()
    {
        return cacheDirBase;
    }

    public void setTileCacheDir(String tileCacheDir)
    {
        File dir = new File(tileCacheDir);
        dir.mkdirs();
        this.cacheDirBase = dir.getAbsolutePath();
    }

    /**
     * @param noDownload
     */
    public void setNoDownload(boolean noDownload)
    {
        this.noDownload = noDownload;
    }

    /**
     * Setter for saveTiles
     * @param saveTiles the saveTiles to set
     */
    public void setSaveTiles(boolean saveTiles)
    {
        this.saveTiles = saveTiles;
    }

}
