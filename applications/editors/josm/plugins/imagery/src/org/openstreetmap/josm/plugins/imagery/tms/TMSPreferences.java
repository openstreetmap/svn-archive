package org.openstreetmap.josm.plugins.imagery.tms;

import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.josm.data.preferences.BooleanProperty;
import org.openstreetmap.josm.data.preferences.IntegerProperty;

/**
 * Preferences for Slippy Map Tiles
 *
 * @author Hakan Tandogan <hakan@gurkensalat.com>
 * @author LuVar <lubomir.varga@freemap.sk>
 * @author Upliner <upliner@gmail.com>
 *
 */
public class TMSPreferences
{
    public static final String PREFERENCE_PREFIX   = "imagery.tms";

    public static final int MAX_ZOOM = 30;
    public static final int MIN_ZOOM = 2;
    public static final int DEFAULT_MAX_ZOOM = 18;
    public static final int DEFAULT_MIN_ZOOM = 2;

    public static final BooleanProperty PROP_DEFAULT_AUTOZOOM = new BooleanProperty(PREFERENCE_PREFIX + ".default_autozoom", true);
    public static final BooleanProperty PROP_DEFAULT_AUTOLOAD = new BooleanProperty(PREFERENCE_PREFIX + ".default_autoload", true);
    public static final IntegerProperty PROP_MIN_ZOOM_LVL = new IntegerProperty(PREFERENCE_PREFIX + ".min_zoom_lvl", DEFAULT_MIN_ZOOM);
    public static final IntegerProperty PROP_MAX_ZOOM_LVL = new IntegerProperty(PREFERENCE_PREFIX + ".max_zoom_lvl", DEFAULT_MAX_ZOOM);
    public static final BooleanProperty PROP_DRAW_DEBUG = new BooleanProperty(PREFERENCE_PREFIX + ".draw_debug", false);

    static int checkMaxZoomLvl(int maxZoomLvl, TileSource ts)
    {
        if(maxZoomLvl > MAX_ZOOM) {
            System.err.println("MaxZoomLvl shouldnt be more than 30! Setting to 30.");
            maxZoomLvl = MAX_ZOOM;
        }
        if(maxZoomLvl < PROP_MIN_ZOOM_LVL.get()) {
            System.err.println("maxZoomLvl shouldnt be more than minZoomLvl! Setting to minZoomLvl.");
            maxZoomLvl = PROP_MIN_ZOOM_LVL.get();
        }
        if (ts != null && ts.getMaxZoom() != 0) {
            maxZoomLvl = ts.getMaxZoom();
        }
        return maxZoomLvl;
    }

    public static int getMaxZoomLvl(TileSource ts)
    {
        return checkMaxZoomLvl(PROP_MAX_ZOOM_LVL.get(), ts);
    }

    public static void setMaxZoomLvl(int maxZoomLvl) {
        maxZoomLvl = checkMaxZoomLvl(maxZoomLvl, null);
        PROP_MAX_ZOOM_LVL.put(maxZoomLvl);
    }

    static int checkMinZoomLvl(int minZoomLvl, TileSource ts)
    {
        if(minZoomLvl < MIN_ZOOM) {
            System.err.println("minZoomLvl shouldnt be lees than "+MIN_ZOOM+"! Setting to that.");
            minZoomLvl = MIN_ZOOM;
        }
        if(minZoomLvl > PROP_MAX_ZOOM_LVL.get()) {
            System.err.println("minZoomLvl shouldnt be more than maxZoomLvl! Setting to maxZoomLvl.");
            minZoomLvl = getMaxZoomLvl(ts);
        }
        if (ts != null && ts.getMinZoom() > minZoomLvl) {
            System.err.println("increasomg minZoomLvl to match tile source");
            minZoomLvl = ts.getMinZoom();
        }
        return minZoomLvl;
    }

    public static int getMinZoomLvl(TileSource ts)
    {
        return checkMinZoomLvl(PROP_MIN_ZOOM_LVL.get(), ts);
    }

    public static void setMinZoomLvl(int minZoomLvl) {
        minZoomLvl = checkMinZoomLvl(minZoomLvl, null);
        PROP_MIN_ZOOM_LVL.put(minZoomLvl);
    }
}
