/**
 * This plugin leverages JOSM to import TangoGPS files.
 */
package org.openstreetmap.josm.plugins;

import java.io.IOException;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.io.TangoGPS;
import org.openstreetmap.josm.io.Tcx;

public class DataImport extends Plugin {

    /**
     * Add new File import filter into open dialog
     */
    public DataImport() throws IOException{
        super();

        ExtensionFileFilter.importers.add(new TangoGPS());
        ExtensionFileFilter.importers.add(new Tcx());
    }


}
