// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.dataimport;

import java.io.IOException;

import javax.xml.bind.JAXBException;

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
     * @throws IOException in case of I/O error
     * @throws JAXBException if JAXB cannot be initialized
     */
    public DataImportPlugin(PluginInformation info) throws IOException, JAXBException {
        super(info);

        ExtensionFileFilter.addImporter(new TangoGPS());
        ExtensionFileFilter.addImporter(new Tcx());
    }
}
