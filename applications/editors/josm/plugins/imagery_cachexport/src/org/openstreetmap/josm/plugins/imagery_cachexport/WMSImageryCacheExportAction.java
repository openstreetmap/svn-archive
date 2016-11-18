package org.openstreetmap.josm.plugins.imagery_cachexport;

import org.apache.commons.jcs.access.CacheAccess;
import org.openstreetmap.josm.data.cache.BufferedImageCacheEntry;
import org.openstreetmap.josm.gui.layer.WMSLayer;

/**
 * WMS specifics of the imagery tile export action.
 */
public class WMSImageryCacheExportAction extends AbstractImageryCacheExportAction {
    /**
     * Get the cache object of the WMS layer.
     *
     * @return Cache object of the WMS layer.
     */
    @Override
    protected CacheAccess<String, BufferedImageCacheEntry> getCache() {
        return WMSLayer.getCache();
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
         * http://irs.gis-lab.info/?layers=landsat&SRS=EPSG:3857&WIDTH=512&HEIGHT=512&BBOX=1027312.7548108,7611905.5587892,1037096.6940108,7621689.4979892EPSG:3857
         * http://kortforsyningen.kms.dk/dhm?login=OpenStreetMapDK2015&password=Gall4Peters&FORMAT=image/png&VERSION=1.1.1&SERVICE=WMS&REQUEST=GetMap&Layers=dhm_overflade_skyggekort&STYLES=&SRS=EPSG:3857&WIDTH=512&HEIGHT=512&BBOX=1037096.6940108,7514066.1667892,1056664.5724108,7533634.0451892EPSG:3857
         */
        //System.out.println("WMS key: " + key);
        return null;
    }
}
