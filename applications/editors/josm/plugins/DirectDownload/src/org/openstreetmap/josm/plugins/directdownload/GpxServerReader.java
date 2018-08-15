// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.directdownload;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.io.GpxReader;
import org.openstreetmap.josm.io.OsmApi;
import org.openstreetmap.josm.io.OsmConnection;
import org.openstreetmap.josm.io.OsmTransferException;
import org.openstreetmap.josm.tools.HttpClient;
import org.openstreetmap.josm.tools.Logging;
import org.xml.sax.SAXException;

public class GpxServerReader extends OsmConnection {

    public GpxData loadGpx(final long id) {
        final String urlString = OsmApi.getOsmApi().getBaseUrl() + "gpx/" + id + "/data";

        try {
            final HttpClient client = HttpClient.create(new URL(urlString));
            addAuth(client);

            try (InputStream in = client.connect().uncompressAccordingToContentDisposition(true).getContent()) {
                final GpxReader r = new GpxReader(in);
                boolean parsedProperly = r.parse(true);
                if (!parsedProperly) {
                    JOptionPane.showMessageDialog(MainApplication.getMainFrame(),
                            tr("Error occurred while parsing gpx file {0}. Only a part of the file will be available.", urlString));
                }
                return r.getGpxData();
            }
        } catch (IOException | OsmTransferException e) {
            Logging.warn(e);
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(), tr("Error fetching URL {0}", urlString));
            return null;
        } catch (SAXException e) {
            Logging.warn(e);
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(), tr("Error parsing data from URL {0}", urlString));
            return null;
        }
    }
}
