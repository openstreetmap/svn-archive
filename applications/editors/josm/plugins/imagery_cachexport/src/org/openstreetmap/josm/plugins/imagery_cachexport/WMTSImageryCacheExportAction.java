package org.openstreetmap.josm.plugins.imagery_cachexport;

import org.apache.commons.jcs.access.CacheAccess;
import org.openstreetmap.josm.data.cache.BufferedImageCacheEntry;
import org.openstreetmap.josm.gui.layer.WMTSLayer;

/**
 * WMTS specifics of the imagery tile export action.
 */
public class WMTSImageryCacheExportAction extends AbstractImageryCacheExportAction {
    /**
     * Get the cache object of the WMTS layer.
     *
     * @return Cache object of the WMTS layer.
     */
    @Override
    protected CacheAccess<String, BufferedImageCacheEntry> getCache() {
        return WMTSLayer.getCache();
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
         * http://mapy.geoportal.gov.pl/wss/service/WMTS/guest/wmts/ORTO?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=ORTOFOTOMAPA&STYLE=default&FORMAT=image/jpeg&tileMatrixSet=EPSG:4326&tileMatrix=EPSG:4326:13&tileRow=4861&tileCol=18765EPSG:4326
         */
        //System.out.println("WMTS key: " + key);
        return null;
    }
}
