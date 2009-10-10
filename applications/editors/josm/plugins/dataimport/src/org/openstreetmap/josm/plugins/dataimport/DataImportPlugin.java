/**
 * This plugin leverages JOSM to import files.
 */
package org.openstreetmap.josm.plugins.dataimport;

import java.io.IOException;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.io.TangoGPS;
import org.openstreetmap.josm.io.Tcx;
import org.openstreetmap.josm.plugins.Plugin;

public class DataImportPlugin extends Plugin {

    /**
     * Add new File import filter into open dialog
     */
    public DataImportPlugin() throws IOException{
        super();

        ExtensionFileFilter.importers.add(new TangoGPS());
        ExtensionFileFilter.importers.add(new Tcx());
    }
}
