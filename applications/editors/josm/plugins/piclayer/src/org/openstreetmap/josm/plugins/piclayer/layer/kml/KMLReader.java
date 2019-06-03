// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.piclayer.layer.kml;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import org.openstreetmap.josm.tools.Logging;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class KMLReader {

    private List<KMLGroundOverlay> groundOverlays;

    private File file;
    public KMLReader(File file) {
        this.file = file;
    }

    public void process() {
        KMLHandler handler = new KMLHandler();
        try {
            XMLReader xr = XMLReaderFactory.createXMLReader();
            xr.setContentHandler(handler);
            xr.parse(new InputSource(Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)));
        } catch (SAXException | IOException e) {
            Logging.error(e);
        }
        groundOverlays = handler.getResult();
    }

    public List<KMLGroundOverlay> getGroundOverlays() {
        return groundOverlays;
    }
}
