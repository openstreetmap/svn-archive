package org.openstreetmap.josm.plugins.imagery_cachexport;

import org.openstreetmap.josm.gui.layer.TMSLayer;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * Main class for the imagery cache export plugin.  The plugin exports the
 * images from the internal imagery layer store into the file system.
 */
public class ImageryCacheExportPlugin extends Plugin {

   /**
    * Will be invoked by JOSM to bootstrap the plugin.
    *
    * @param info  Information about the plugin and its local installation.
    */
    public ImageryCacheExportPlugin(PluginInformation info) {
        super(info);
        TMSLayer.registerMenuAddition(new TMSImageryCacheExportAction(), TMSLayer.class);
        //WMSLayer.registerMenuAddition(new WMSImageryCacheExportAction(), WMSLayer.class);
        //WMTSLayer.registerMenuAddition(new WMTSImageryCacheExportAction(), WMTSLayer.class);
    }
}
