package org.openstreetmap.josm.plugins.slippymap;

import org.openstreetmap.josm.Main;

/**
 * Preferences for Slippy Map Tiles
 * 
 * @author Hakan Tandogan <hakan@gurkensalat.com>
 * @author LuVar <lubomir.varga@freemap.sk>
 * 
 */
public class SlippyMapPreferences
{
    public static final String PREFERENCE_PREFIX   = "slippymap";

    public static final String PREFERENCE_TILE_URL = PREFERENCE_PREFIX + ".tile_url";
    public static final String PREFERENCE_AUTOZOOM = PREFERENCE_PREFIX + ".autozoom";
    public static final String PREFERENCE_AUTOLOADTILES = PREFERENCE_PREFIX + ".autoload_tiles";
    public static final String PREFERENCE_MIN_ZOOM_LVL = PREFERENCE_PREFIX + ".min_zoom_lvl";
    public static final String PREFERENCE_MAX_ZOOM_LVL = PREFERENCE_PREFIX + ".max_zoom_lvl";
    public static final String PREFERENCE_LAST_ZOOM = PREFERENCE_PREFIX + ".last_zoom_lvl";
    public static final String PREFERENCE_FADE_BACKGROUND = PREFERENCE_PREFIX + ".fade_background";
    public static final String PREFERENCE_DRAW_DEBUG = PREFERENCE_PREFIX + ".draw_debug";
    
    public static String getMapUrl()
    {
        String url = Main.pref.get(PREFERENCE_TILE_URL);

        if (url == null || "".equals(url))
        {
            url = "http://tah.openstreetmap.org/Tiles/tile"; // t@h
            Main.pref.put(PREFERENCE_TILE_URL, url);
        }

        return url;
    }
    
    public static void setMapUrl(String mapUrl) {
    	Main.pref.put(SlippyMapPreferences.PREFERENCE_TILE_URL, mapUrl);
    }
    
    public static boolean getAutozoom()
    {
        String autozoom = Main.pref.get(PREFERENCE_AUTOZOOM);

        if (autozoom == null || "".equals(autozoom))
        {
        	autozoom = "true";
            Main.pref.put(PREFERENCE_AUTOZOOM, autozoom);
        }

        return Boolean.parseBoolean(autozoom);
    }
    
    public static void setAutozoom(boolean autozoom) {
    	Main.pref.put(SlippyMapPreferences.PREFERENCE_AUTOZOOM, autozoom);
    }
    
    public static void setDrawDebug(boolean drawDebug) {
    	Main.pref.put(SlippyMapPreferences.PREFERENCE_DRAW_DEBUG, drawDebug);
    }
    
    public static void setLastZoom(int zoom) {
    	Main.pref.put(SlippyMapPreferences.PREFERENCE_LAST_ZOOM, ""+zoom);
    }
    public static int getLastZoom() {
    	int ret = -1;
        String pref = Main.pref.get(SlippyMapPreferences.PREFERENCE_LAST_ZOOM);
        try {
            ret = Integer.parseInt(pref);
        } catch (NumberFormatException e) {
        }
        return ret;
    }

    public static boolean getDrawDebug()
    {
        String drawDebug = Main.pref.get(PREFERENCE_DRAW_DEBUG);

        if (drawDebug == null || "".equals(drawDebug))
        {
        	drawDebug = "false";
            Main.pref.put(PREFERENCE_DRAW_DEBUG, drawDebug);
        }

        return Boolean.parseBoolean(drawDebug);
    }
    
    public static boolean getAutoloadTiles()
    {
        String autoloadTiles = Main.pref.get(PREFERENCE_AUTOLOADTILES);

        if (autoloadTiles == null || "".equals(autoloadTiles))
        {
        	autoloadTiles = "true";
            Main.pref.put(PREFERENCE_AUTOLOADTILES, autoloadTiles);
        }

        return Boolean.parseBoolean(autoloadTiles);
    }
    
    public static void setFadeBackground(float fadeBackground) {
    	Main.pref.put(SlippyMapPreferences.PREFERENCE_FADE_BACKGROUND, fadeBackground + "");
    }

    /**
     * 
     * @return	number between 0 and 1, inclusive
     */
    public static float getFadeBackground() {
        String fadeBackground = Main.pref.get(PREFERENCE_FADE_BACKGROUND);

        if (fadeBackground == null || "".equals(fadeBackground))
        {
        	fadeBackground = "0.0";
            Main.pref.put(PREFERENCE_FADE_BACKGROUND, fadeBackground);
        }
        
        float parsed;
        try {
        	parsed = Float.parseFloat(fadeBackground);
        } catch (Exception ex) {
        	setFadeBackground(0.1f);
        	System.out.println("Error while parsing setting fade background to float! returning 0.1, because of error:");
        	ex.printStackTrace(System.out);
        	return 0.1f;
        }
        if(parsed < 0f) {
        	parsed = 0f;
        } else {
        	if(parsed > 1f) {
            	parsed = 1f;
            }
        }
        return parsed;
    }
    
    public static void setAutoloadTiles(boolean autoloadTiles) {
    	Main.pref.put(SlippyMapPreferences.PREFERENCE_AUTOLOADTILES, autoloadTiles);
    }
    
    public static int getMaxZoomLvl()
    {
        String maxZoomLvl = Main.pref.get(PREFERENCE_MAX_ZOOM_LVL);

        if (maxZoomLvl == null || "".equals(maxZoomLvl))
        {
        	maxZoomLvl = "17";
            Main.pref.put(PREFERENCE_MAX_ZOOM_LVL, maxZoomLvl);
        }

        int navrat;
        try {
        	navrat = Integer.parseInt(maxZoomLvl);
        } catch (Exception ex) {
        	throw new RuntimeException("Problem while converting string to int. Converting value of prefetrences " + PREFERENCE_MAX_ZOOM_LVL + ". Value=\"" + maxZoomLvl + "\". Should be an integer. Error: " + ex.getMessage(), ex);
        }
        if(navrat > 30) {
    		System.err.println("MaxZoomLvl shouldnt be more than 30! Setting to 30.");
    		navrat = 30;
    	}
        //if(navrat < SlippyMapPreferences.getMinZoomLvl()) {
    	//	System.err.println("maxZoomLvl shouldnt be more than minZoomLvl! Setting to minZoomLvl.");
    	//	navrat = SlippyMapPreferences.getMinZoomLvl();
    	//}
        return navrat;
    }
    
    public static void setMaxZoomLvl(int maxZoomLvl) {
    	if(maxZoomLvl > 30) {
    		System.err.println("MaxZoomLvl shouldnt be more than 30! Setting to 30.");
    		maxZoomLvl = 30;
    	}
    	if(maxZoomLvl < SlippyMapPreferences.getMinZoomLvl()) {
    		System.err.println("maxZoomLvl shouldnt be more than minZoomLvl! Setting to minZoomLvl.");
    		maxZoomLvl = SlippyMapPreferences.getMinZoomLvl();
    	}
    	Main.pref.put(SlippyMapPreferences.PREFERENCE_MAX_ZOOM_LVL, "" + maxZoomLvl);
    }
    
    public static int getMinZoomLvl()
    {
        String minZoomLvl = Main.pref.get(PREFERENCE_MIN_ZOOM_LVL);

        if (minZoomLvl == null || "".equals(minZoomLvl))
        {
        	minZoomLvl = "" + (SlippyMapPreferences.getMaxZoomLvl() - 4);
            Main.pref.put(PREFERENCE_MIN_ZOOM_LVL, minZoomLvl);
        }

        int navrat;
        try {
        	navrat = Integer.parseInt(minZoomLvl);
        } catch (Exception ex) {
        	throw new RuntimeException("Problem while converting string to int. Converting value of prefetrences " + PREFERENCE_MIN_ZOOM_LVL + ". Value=\"" + minZoomLvl + "\". Should be an integer. Error: " + ex.getMessage(), ex);
        }
        if(navrat < 2) {
    		System.err.println("minZoomLvl shouldnt be lees than 2! Setting to 2.");
    		navrat = 2;
    	}
        //if(navrat > SlippyMapPreferences.getMaxZoomLvl()) {
    	//	System.err.println("minZoomLvl shouldnt be more than maxZoomLvl! Setting to maxZoomLvl.");
    	//	navrat = SlippyMapPreferences.getMaxZoomLvl();
    	//}
        return navrat;
    }
    
    public static void setMinZoomLvl(int minZoomLvl) {
    	if(minZoomLvl < 2) {
    		System.err.println("minZoomLvl shouldnt be lees than 2! Setting to 2.");
    		minZoomLvl = 2;
    	}
    	if(minZoomLvl > SlippyMapPreferences.getMaxZoomLvl()) {
    		System.err.println("minZoomLvl shouldnt be more than maxZoomLvl! Setting to maxZoomLvl.");
    		minZoomLvl = SlippyMapPreferences.getMaxZoomLvl();
    	}
    	Main.pref.put(SlippyMapPreferences.PREFERENCE_MIN_ZOOM_LVL, "" + minZoomLvl);
    }
    
    public static String[] getAllMapUrls()
    {
        String[] defaultTileSources = new String[]
        {
                "http://tah.openstreetmap.org/Tiles/tile", // t@h
                "http://tah.openstreetmap.org/Tiles/maplint", // maplint
                "http://tile.openstreetmap.org", // mapnik
                "http://hypercube.telascience.org/tiles/1.0.0/coastline", // coastline
                "http://www.freemap.sk/layers/allinone/?", //freemapy.sk
                "http://www.freemap.sk/layers/tiles/?", //freemapy.sk pokus 2
        };
        return defaultTileSources;
    }
}
