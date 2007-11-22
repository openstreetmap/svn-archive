package org.openstreetmap.josm.plugins.slippymap;

import org.openstreetmap.josm.Main;

/**
 * Preferences for Slippy Map Tiles
 * 
 * @author Hakan Tandogan <hakan@gurkensalat.com>
 * 
 */
public class SlippyMapPreferences
{
    public static String PREFERENCE_PREFIX   = "slippymap";

    public static String PREFERENCE_TILE_URL = PREFERENCE_PREFIX + ".tile_url";

    public static String getMapUrl()
    {
        String url = Main.pref.get(PREFERENCE_TILE_URL);

        if (url == null || "".equals(url))
        {
            url = "http://tah.openstreetmap.org/Tiles/tile"; // t@h
            // url = "http://tile.openstreetmap.org"; // mapnik 
            // url = "http://hypercube.telascience.org/tiles/1.0.0/coastline" //
            Main.pref.put(PREFERENCE_TILE_URL, url);
        }

        return url;
    }
}
