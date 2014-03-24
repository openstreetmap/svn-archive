// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.imageryxmlbounds.io;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.OsmServerReader;
import org.openstreetmap.josm.io.OsmTransferException;

public class JosmServerLocationReader extends OsmServerReader {

    private String url;
    
    public JosmServerLocationReader(String url) {
        this.url = url;
    }

    @Override
    public DataSet parseOsm(ProgressMonitor progressMonitor)
            throws OsmTransferException {
        try {
            progressMonitor.beginTask(tr("Contacting Server...", 10));
            return new XmlBoundsImporter().parseDataSet(url);
        } catch (Exception e) {
            throw new OsmTransferException(e);
        } finally {
            progressMonitor.finishTask();
        }
    }
}
