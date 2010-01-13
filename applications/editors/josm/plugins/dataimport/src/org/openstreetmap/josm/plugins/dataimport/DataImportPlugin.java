/**
 * This plugin leverages JOSM to import files.
 */
package org.openstreetmap.josm.plugins.dataimport;

import java.io.IOException;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.plugins.dataimport.io.TangoGPS;
import org.openstreetmap.josm.plugins.dataimport.io.Tcx;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class DataImportPlugin extends Plugin {

    /**
     * Add new File import filter into open dialog
     */
    public DataImportPlugin(PluginInformation info) throws IOException{
        super(info);

        ExtensionFileFilter.importers.add(new TangoGPS());
        ExtensionFileFilter.importers.add(new Tcx());
    }
}
