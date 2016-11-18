package org.openstreetmap.josm.plugins.imagery_cachexport;

import org.apache.commons.jcs.access.CacheAccess;
import org.openstreetmap.josm.data.cache.BufferedImageCacheEntry;
import org.openstreetmap.josm.gui.layer.TMSLayer;

/**
 * TMS specifics of the imagery tile export action.
 */
public class TMSImageryCacheExportAction extends AbstractImageryCacheExportAction {
    /**
     * Get the cache object of the TMS layer.
     *
     * @return Cache object of the TMS layer.
     */
    @Override
    protected CacheAccess<String, BufferedImageCacheEntry> getCache() {
        return TMSLayer.getCache();
    }

    /**
     * Get file name for a cache key.
     *
     * @param key Tile cache key.  That is the full cache key with the key
     * 		  prefix removed.
     *
     * @return File name for tile.
     */
    @Override
    protected String getFilename(String key) {
        /* key examples:
         * http://www.bing.com/maps/14/8816/5480
         * http://{switch:a,b,c}.tiles.mapbox.com/v4/openstreetmap.map-inh7ifmo/{zoom}/{x}/{y}.png?access_token=abc/14/8816/5479
         */
        final String[] segs = key.split("/");
        if (segs.length >= 3) {
            return segs[segs.length - 3] + "_" +
                segs[segs.length - 2] + "_" +
                segs[segs.length - 1] + ".jpg";
        }
        return null;
    }
}
