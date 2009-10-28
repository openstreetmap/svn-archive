package org.openstreetmap.josm.plugins.slippymap;

import org.openstreetmap.josm.Main;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.openstreetmap.gui.jmapviewer.*;
import org.openstreetmap.gui.jmapviewer.interfaces.*;

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

    public static final String PREFERENCE_TILE_CUSTOM_SOURCE = PREFERENCE_PREFIX + ".custom_tile_source_";
    public static final String PREFERENCE_TILE_SOURCE = PREFERENCE_PREFIX + ".tile_source";
    public static final String PREFERENCE_AUTOZOOM = PREFERENCE_PREFIX + ".autozoom";
    public static final String PREFERENCE_AUTOLOADTILES = PREFERENCE_PREFIX + ".autoload_tiles";
    public static final String PREFERENCE_MIN_ZOOM_LVL = PREFERENCE_PREFIX + ".min_zoom_lvl";
    public static final String PREFERENCE_MAX_ZOOM_LVL = PREFERENCE_PREFIX + ".max_zoom_lvl";
    public static final String PREFERENCE_LAST_ZOOM = PREFERENCE_PREFIX + ".last_zoom_lvl";
    public static final String PREFERENCE_FADE_BACKGROUND = PREFERENCE_PREFIX + ".fade_background";
    public static final String PREFERENCE_DRAW_DEBUG = PREFERENCE_PREFIX + ".draw_debug";

    public static final int MAX_ZOOM = 30;
    public static final int MIN_ZOOM = 2;
    public static final int DEFAULT_MAX_ZOOM = 20;
    public static final int DEFAULT_MIN_ZOOM = 2;

    public static TileSource getMapSource()
    {
        String name = Main.pref.get(PREFERENCE_TILE_SOURCE);
        List<TileSource> sources = SlippyMapPreferences.getAllMapSources();
        TileSource source = sources.get(0);
        if (name == null || "".equals(name)) {
            name = source.getName();
            Main.pref.put(PREFERENCE_TILE_SOURCE, name);
        }
        for (TileSource s : sources) {
            if (!name.equals(s.getName()))
                continue;
            source = s;
            break;
        }
        return source;
    }

    public static void setMapSource(TileSource source) {
    	Main.pref.put(SlippyMapPreferences.PREFERENCE_TILE_SOURCE, source.getName());
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

    private static int getIntPref(String prefName, int def)
    {
        int pref;
        try {
        	//Should we use Main.pref.getInteger(str)?
        	pref = Main.pref.getInteger(prefName, def);
        } catch (Exception ex) {
            String str = Main.pref.get(prefName);
            Main.pref.put(prefName, null);
        	throw new RuntimeException("Problem while converting string to int. "
                                       + "Converting value of preferences "
                                       + prefName + ". Value=\"" + str
                                       + "\". Should be an integer. Error: "
                                       + ex.getMessage(), ex);
        }
        return pref;
    }

    static int checkMaxZoomLvl(int maxZoomLvl)
    {
    	if(maxZoomLvl > MAX_ZOOM) {
    		System.err.println("MaxZoomLvl shouldnt be more than 30! Setting to 30.");
    		maxZoomLvl = MAX_ZOOM;
    	}
    	if(maxZoomLvl < SlippyMapPreferences.__getMinZoomLvl()) {
    		System.err.println("maxZoomLvl shouldnt be more than minZoomLvl! Setting to minZoomLvl.");
    		maxZoomLvl = SlippyMapPreferences.__getMinZoomLvl();
    	}
        return maxZoomLvl;
    }

    public static int getMaxZoomLvl()
    {
        int maxZoomLvl = getIntPref(PREFERENCE_MAX_ZOOM_LVL, DEFAULT_MAX_ZOOM);
        return checkMaxZoomLvl(maxZoomLvl);
    }

    public static void setMaxZoomLvl(int maxZoomLvl) {
        maxZoomLvl = checkMaxZoomLvl(maxZoomLvl);
    	Main.pref.put(SlippyMapPreferences.PREFERENCE_MAX_ZOOM_LVL, "" + maxZoomLvl);
    }

    static int checkMinZoomLvl(int minZoomLvl)
    {
        if(minZoomLvl < MIN_ZOOM) {
    		System.err.println("minZoomLvl shouldnt be lees than "+MIN_ZOOM+"! Setting to that.");
    		minZoomLvl = MIN_ZOOM;
    	}
    	if(minZoomLvl > SlippyMapPreferences.getMaxZoomLvl()) {
    		System.err.println("minZoomLvl shouldnt be more than maxZoomLvl! Setting to maxZoomLvl.");
    		minZoomLvl = SlippyMapPreferences.getMaxZoomLvl();
    	}
        return minZoomLvl;
    }

    private static int __getMinZoomLvl()
    {
        // We can use this internally
        return getIntPref(PREFERENCE_MIN_ZOOM_LVL, DEFAULT_MIN_ZOOM);
    }
    public static int getMinZoomLvl()
    {
        return checkMinZoomLvl(__getMinZoomLvl());
    }

    public static void setMinZoomLvl(int minZoomLvl) {
        minZoomLvl = checkMinZoomLvl(minZoomLvl);
    	Main.pref.put(SlippyMapPreferences.PREFERENCE_MIN_ZOOM_LVL, "" + minZoomLvl);
    }

    public static class Coastline extends OsmTileSource.AbstractOsmTileSource {
        public Coastline() {
            super("Coastline", "http://hypercube.telascience.org/tiles/1.0.0/coastline");
        }
        public TileUpdate getTileUpdate() {
            return TileUpdate.IfNoneMatch;
        }
    }
    public static class FreeMapySk extends OsmTileSource.AbstractOsmTileSource {
        public FreeMapySk() {
            super("freemapy.sk", "http://www.freemap.sk/layers/allinone/?");
        }
        public TileUpdate getTileUpdate() {
            return TileUpdate.IfNoneMatch;
        }
    }
    public static class FreeMapySkPokus extends OsmTileSource.AbstractOsmTileSource {
        public FreeMapySkPokus() {
            super("freemapy.sk pokus 2", "http://www.freemap.sk/layers/tiles/?");
        }
        public TileUpdate getTileUpdate() {
            return TileUpdate.IfNoneMatch;
        }
    }

    public static class Custom extends OsmTileSource.AbstractOsmTileSource {
        public Custom(String name, String url) {
            super(name, url);
        }
        public Custom(String name, String url, String extension) {
            super(name, url);
            this.extension = extension;
        }
        String extension;
        @Override
        public String getExtension() {
            if (extension == null)
                return super.getExtension();
            return extension;
        }
        public TileUpdate getTileUpdate() {
            return TileUpdate.IfNoneMatch;
        }
    }

    public static List<TileSource> getCustomSources()
    {
        List<TileSource> ret = new ArrayList<TileSource>();
        Map<String, String> customSources = Main.pref.getAllPrefix(PREFERENCE_TILE_CUSTOM_SOURCE);
        for (String key : customSources.keySet()) {
            String short_key = key.replaceFirst(PREFERENCE_TILE_CUSTOM_SOURCE, "");
            // slippymap.custom_tile_source_1.name=OOC layer
            // slippymap.custom_tile_source_1.url=http://a.ooc.openstreetmap.org/npe
            // slippymap.custom_tile_source_1.ext=png

            if (!(short_key.endsWith("name")))
                continue;
            String url_key = short_key.replaceFirst("name","url");
            String ext_key = short_key.replaceFirst("name","ext");
            String name = customSources.get(key);
            String url = customSources.get(PREFERENCE_TILE_CUSTOM_SOURCE + url_key);
            String ext = customSources.get(PREFERENCE_TILE_CUSTOM_SOURCE + ext_key);
            // ext may be null, but that's OK
            System.out.println("found new tile source: '" +name+"' url:'"+url+"'"+"' ext:'"+ext+"'");
            ret.add(new Custom(name, url, ext));
        }
        return ret;
    }

    public static ArrayList<TileSource> sources = null;
    public static List<TileSource> getAllMapSources()
    {
        if (sources != null)
            return sources;
        sources = new ArrayList<TileSource>();
        // first here is the default if the user does not set one
        sources.add(new OsmTileSource.Mapnik());
        sources.add(new OsmTileSource.CycleMap());
        sources.add(new OsmTileSource.TilesAtHome());
        sources.add(new Coastline());
        sources.add(new FreeMapySkPokus());
        sources.add(new FreeMapySk());
        sources.addAll(getCustomSources());
        // Probably need to either add these or let users add them somehow
        //      "http://hypercube.telascience.org/tiles/1.0.0/coastline", // coastline
        //      "http://www.freemap.sk/layers/allinone/?", //freemapy.sk
        //      "http://www.freemap.sk/layers/tiles/?", //freemapy.sk pokus 2
        return sources;
    }

    public static TileSource getSourceNamed(String name)
    {
        for (TileSource s : getAllMapSources())
            if (s.getName().equals(name))
                return s;
        return null;
    }
}
