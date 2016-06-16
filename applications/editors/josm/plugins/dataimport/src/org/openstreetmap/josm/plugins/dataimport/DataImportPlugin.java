/**
 * This plugin leverages JOSM to import files.
 */
package org.openstreetmap.josm.plugins.dataimport;

import java.io.IOException;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.dataimport.io.TangoGPS;
import org.openstreetmap.josm.plugins.dataimport.io.Tcx;

/**
 * Data import plugin.
 */
public class DataImportPlugin extends Plugin {

    /**
     * Add new File import filter into open dialog
     * @param info plugin information
     */
    public DataImportPlugin(PluginInformation info) throws IOException{
        super(info);

        ExtensionFileFilter.addImporter(new TangoGPS());
        ExtensionFileFilter.addImporter(new Tcx());
    }
}
