// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.io.OsmImporter;
import org.openstreetmap.josm.io.OsmImporter.OsmImporterData;

/**
 * This class provides functionality used by multiple test classes of pt_assistant plugin.
 */
public final class ImportUtils {

    private ImportUtils() {
        // private constructor for utils classes
    }

    public static DataSet importOsmFile(File file, String layerName) {

        OsmImporter importer = new OsmImporter();
        ProgressMonitor progressMonitor = NullProgressMonitor.INSTANCE;

        try {
            InputStream in = new FileInputStream(file);
            OsmImporterData oid = importer.loadLayer(in, file, layerName, progressMonitor);
            OsmDataLayer layer = oid.getLayer();
            return layer.data;

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalDataException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }
}
