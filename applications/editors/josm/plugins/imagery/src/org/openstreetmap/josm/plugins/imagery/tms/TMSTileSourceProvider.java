package org.openstreetmap.josm.plugins.imagery.tms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.josm.gui.bbox.SlippyMapBBoxChooser.TileSourceProvider;
import org.openstreetmap.josm.plugins.imagery.ImageryInfo;
import org.openstreetmap.josm.plugins.imagery.ImageryPlugin;

/**
 * TMS TileSource provider for the slippymap chooser
 * @author Upliner
 */
public class TMSTileSourceProvider implements TileSourceProvider {
    static final HashSet<String> existingSlippyMapUrls = new HashSet<String>();
    static {
        // Urls that already exist in the slippymap chooser
        existingSlippyMapUrls.add("http://tile.openstreetmap.org/");
        existingSlippyMapUrls.add("http://tah.openstreetmap.org/Tiles/");
        existingSlippyMapUrls.add("http://tile.opencyclemap.org/cycle/");
    }

    @Override
    public List<TileSource> getTileSources() {
        if (!TMSPreferences.PROP_ADD_TO_SLIPPYMAP_CHOOSER.get()) return Collections.<TileSource>emptyList();
        List<TileSource> sources = new ArrayList<TileSource>();
        for (ImageryInfo info : ImageryPlugin.instance.info.getLayers()) {
            if (existingSlippyMapUrls.contains(info.getURL())) continue;
            TileSource source = TMSPreferences.getTileSource(info);
            if (source != null) sources.add(source);
        }
        return sources;
    }

    public static void addExistingSlippyMapUrl(String url) {
        existingSlippyMapUrls.add(url);
    }
}
