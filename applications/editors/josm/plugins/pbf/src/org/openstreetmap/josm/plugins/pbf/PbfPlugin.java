// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pbf;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.io.OverpassDownloadReader;
import org.openstreetmap.josm.io.OverpassDownloadReader.OverpassOutpoutFormat;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.pbf.action.DownloadPbfTask;
import org.openstreetmap.josm.plugins.pbf.io.PbfExporter;
import org.openstreetmap.josm.plugins.pbf.io.PbfImporter;
import org.openstreetmap.josm.plugins.pbf.io.PbfReader;

/**
 * PBF plugin.
 * @author Don-vip
 */
public class PbfPlugin extends Plugin {

    /**
     * Constructs a new {@code PbfPlugin}.
     * @param info plugin information
     */
    public PbfPlugin(PluginInformation info) {
        super(info);
        // Allow JOSM to import *.osm.pbf files
        ExtensionFileFilter.addImporter(new PbfImporter());
        // Allow JOSM to export *.osm.pbf files
        ExtensionFileFilter.addExporter(new PbfExporter());
        // Allow JOSM to download remote *.osm.pbf files
        Main.main.menu.openLocation.addDownloadTaskClass(DownloadPbfTask.class);
        // Alow JOSM to download PBF data from Overpass API
        OverpassDownloadReader.registerOverpassOutpoutFormatReader(OverpassOutpoutFormat.PBF, PbfReader.class);
    }
}
