package org.openstreetmap.josm.plugins.opendata.core.io.session;

import java.io.File;
import java.io.IOException;

import org.openstreetmap.josm.gui.io.importexport.OsmImporter;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.io.session.OsmDataSessionImporter;
import org.openstreetmap.josm.io.session.SessionLayerImporter;
import org.openstreetmap.josm.io.session.SessionReader.ImportSupport;
import org.openstreetmap.josm.plugins.opendata.OdPlugin;
import org.openstreetmap.josm.plugins.opendata.core.io.AbstractImporter;
import org.w3c.dom.Element;

public class OpenDataSessionImporter implements SessionLayerImporter {

    @Override
    public Layer load(Element elem, ImportSupport support, ProgressMonitor progressMonitor) throws IOException, IllegalDataException {
        OsmDataSessionImporter.checkMetaVersion(elem);
        String fileStr = OsmDataSessionImporter.extractFileName(elem, support);
        File pathname = new File(fileStr.startsWith("file:/") ? fileStr.replace("file:/", "") : fileStr);
        for (AbstractImporter importer : OdPlugin.getInstance().importers) {
            if (importer.acceptFile(pathname)) {
                importer.setFile(pathname);
                return OsmDataSessionImporter.importData(importer, support, fileStr, progressMonitor);
            }
        }
        return OsmDataSessionImporter.importData(new OsmImporter(), support, fileStr, progressMonitor);
    }
}
