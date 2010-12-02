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
    public static final int DEFAULT_MAX_ZOOM = 20;
    public static final int DEFAULT_MIN_ZOOM = 2;

    public static final BooleanProperty PROP_DEFAULT_AUTOZOOM = new BooleanProperty(PREFERENCE_PREFIX + ".default_autozoom", true);
    public static final BooleanProperty PROP_DEFAULT_AUTOLOAD = new BooleanProperty(PREFERENCE_PREFIX + ".default_autoload", true);
    public static final IntegerProperty PROP_MIN_ZOOM_LVL = new IntegerProperty(PREFERENCE_PREFIX + ".min_zoom_lvl", DEFAULT_MIN_ZOOM);
    public static final IntegerProperty PROP_MAX_ZOOM_LVL = new IntegerProperty(PREFERENCE_PREFIX + ".max_zoom_lvl", DEFAULT_MAX_ZOOM);
    public static final IntegerProperty PROP_FADE_BACKGROUND = new IntegerProperty(PREFERENCE_PREFIX + ".fade_background", 0);
    public static final BooleanProperty PROP_DRAW_DEBUG = new BooleanProperty(PREFERENCE_PREFIX + ".draw_debug", false);

    public static void setFadeBackground(float fadeBackground) {
        PROP_FADE_BACKGROUND.put(Math.round(fadeBackground*100));
    }

    /**
     *
     * @return  number between 0 and 1, inclusive
     */
    public static float getFadeBackground() {
        float parsed = (PROP_FADE_BACKGROUND.get())/100.0f;
        if(parsed < 0f) {
            parsed = 0f;
        } else {
            if(parsed > 1f) {
                parsed = 1f;
            }
        }
        return parsed;
    }

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
        if (ts != null && ts.getMaxZoom() < PROP_MIN_ZOOM_LVL.get()) {
            System.err.println("decreasing maxZoomLvl to match new tile source");
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
        if (ts != null && ts.getMinZoom() > PROP_MIN_ZOOM_LVL.get()) {
            System.err.println("increasomg minZoomLvl to match new tile source");
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
