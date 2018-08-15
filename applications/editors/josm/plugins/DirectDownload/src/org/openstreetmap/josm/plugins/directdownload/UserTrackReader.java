// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.directdownload;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.openstreetmap.josm.io.OsmApi;
import org.openstreetmap.josm.io.OsmConnection;
import org.openstreetmap.josm.io.OsmTransferException;
import org.openstreetmap.josm.tools.HttpClient;
import org.openstreetmap.josm.tools.Logging;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class UserTrackReader extends OsmConnection {

    public List<UserTrack> getTrackList() {
        final String urlString = OsmApi.getOsmApi().getBaseUrl() + "user/gpx_files";

        try {
            final HttpClient client = HttpClient.create(new URL(urlString));
            addAuth(client);

            SAXParserFactory spf = SAXParserFactory.newInstance();
            TrackListHandler handler = new TrackListHandler();

            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            try (InputStream in = client.connect().getContent()) {
                sp.parse(in, handler);
            }

            return handler.getResult();
        } catch (MalformedURLException e) {
            Logging.error(e);
            JOptionPane.showMessageDialog(null, tr("Invalid URL {0}", urlString));
        } catch (IOException | OsmTransferException e) {
            Logging.error(e);
            JOptionPane.showMessageDialog(null, tr("Error fetching URL {0}", urlString));
        } catch (SAXException | ParserConfigurationException e) {
            Logging.error(e);
            JOptionPane.showMessageDialog(null, tr("Error parsing data from URL {0}", urlString));
        }

        return new LinkedList<>();
    }

    private static class TrackListHandler extends DefaultHandler {
        private LinkedList<UserTrack> data = new LinkedList<>();

        private String cdata = "";

        @Override
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
            if (qName.equals("gpx_file")) {
                UserTrack track = new UserTrack();

                track.id = atts.getValue("id");
                track.filename = atts.getValue("name");
                track.datetime = atts.getValue("timestamp").replaceAll("[TZ]", " "); // TODO: do real parsing and time zone conversion

                data.addFirst(track);
            }
            cdata = "";
        }

        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {
            cdata += new String(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equals("description")) {
                data.getFirst().description = cdata;
            } else if (qName.equals("tag")) {
                data.getFirst().tags = cdata;
                cdata = new String();
            }

        }

        public List<UserTrack> getResult() {
            return data;
        }
    }

}
