package org.openstreetmap.josm.plugins.imagery.wms;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.data.preferences.BooleanProperty;
import org.openstreetmap.josm.data.preferences.IntegerProperty;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.io.CacheFiles;
import org.openstreetmap.josm.plugins.imagery.ImageryInfo.ImageryType;
import org.openstreetmap.josm.plugins.imagery.wms.io.WMSLayerExporter;
import org.openstreetmap.josm.plugins.imagery.wms.io.WMSLayerImporter;

// WMSPlugin-specific functions
public class WMSAdapter {
    CacheFiles cache = new CacheFiles("wmsplugin");

    public final IntegerProperty PROP_SIMULTANEOUS_CONNECTIONS = new IntegerProperty("wmsplugin.simultaneousConnections", 3);
    public final BooleanProperty PROP_OVERLAP = new BooleanProperty("wmsplugin.url.overlap", false);
    public final IntegerProperty PROP_OVERLAP_EAST = new IntegerProperty("wmsplugin.url.overlapEast", 14);
    public final IntegerProperty PROP_OVERLAP_NORTH = new IntegerProperty("wmsplugin.url.overlapNorth", 4);

    protected void initExporterAndImporter() {
        ExtensionFileFilter.exporters.add(new WMSLayerExporter());
        ExtensionFileFilter.importers.add(new WMSLayerImporter());
    }

    public WMSAdapter() {
        cache.setExpire(CacheFiles.EXPIRE_MONTHLY, false);
        cache.setMaxSize(70, false);
        initExporterAndImporter();
    }

    public Grabber getGrabber(MapView mv, WMSLayer layer){
        if(layer.info.getImageryType() == ImageryType.HTML)
            return new HTMLGrabber(mv, layer, cache);
        else if(layer.info.getImageryType() == ImageryType.WMS)
            return new WMSGrabber(mv, layer, cache);
        else throw new AssertionError();
    }

}
