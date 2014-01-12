package org.openstreetmap.josm.plugins.imagerycache;

import java.io.File;
import org.openstreetmap.gui.jmapviewer.OsmTileLoader;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoaderListener;
import org.openstreetmap.josm.gui.layer.TMSLayer;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * @author Alexei Kasatkin
 */
public class ImageryCachePlugin extends Plugin {
    
    TMSLayer.TileLoaderFactory factory = new TMSLayer.TileLoaderFactory() {
        @Override
        public OsmTileLoader makeTileLoader(TileLoaderListener listener) {
            String cachePath = TMSLayer.PROP_TILECACHE_DIR.get();
            try {
                new File(cachePath).mkdirs();
            } catch (Exception e) {
                cachePath=".";
            }
            
            if (cachePath != null && !cachePath.isEmpty()) {
                return new OsmDBTilesLoader(listener, new File(cachePath));
            }
            return null;
        }
    };

    public ImageryCachePlugin(PluginInformation info) {
        super(info);
        TMSLayer.setCustomTileLoaderFactory(factory);
    }
    
    public static void main(String[] args) {
        System.out.println("Debugging code for ImageryAdjust plugin");
    }
}
