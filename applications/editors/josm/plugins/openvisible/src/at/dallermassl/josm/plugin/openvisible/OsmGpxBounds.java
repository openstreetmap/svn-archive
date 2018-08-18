/**
 *
 */
package at.dallermassl.josm.plugin.openvisible;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author cdaller
 *
 */
public class OsmGpxBounds extends DefaultHandler {
    private double minLat = 180.0;
    private double maxLat = -180.0;
    private double minLon = 90.0;
    private double maxLon = -90.0;

    public OsmGpxBounds() {

    }

    /**
     * Parses the given input stream (gpx or osm file).
     * @param in the stream to parse.
     * @throws IOException if the file cannot be read.
     * @throws SAXException if the file could not be parsed.
     */
    public void parse(InputStream in) throws IOException, SAXException {
        try {
            SAXParserFactory.newInstance().newSAXParser().parse(in, this);
        } catch (ParserConfigurationException e1) {
            e1.printStackTrace(); // broken SAXException chaining
            throw new SAXException(e1);
        }
    }

    @Override
    public void startElement(String ns, String lname, String qname, Attributes a) {
        if (qname.equals("node") || qname.equals("trkpt")) {
            double lat = Double.parseDouble(a.getValue("lat"));
            double lon = Double.parseDouble(a.getValue("lon"));
            minLat = Math.min(minLat, lat);
            minLon = Math.min(minLon, lon);
            maxLat = Math.max(maxLat, lat);
            maxLon = Math.max(maxLon, lon);
        }
    }

    /**
     * Returns <code>true</code>, if the given coordinates intersect with the
     * parsed min/max latitude longitude.
     * @param minLat the minimum latitude.
     * @param maxLat the maximum latitude.
     * @param minLon the minimum longitude.
     * @param maxLon the maximum longitude.
     * @return <code>true</code> if the given rectangle intersects with the parsed min/max.
     */
    public boolean intersects(double minLat, double maxLat, double minLon, double maxLon) {
        double lat1 = Math.max(this.minLat, minLat);
        double lon1 = Math.max(this.minLon, minLon);
        double lat2 = Math.min(this.maxLat, maxLat);
        double lon2 = Math.min(this.maxLon, maxLon);
        return ((lat2-lat1) > 0) && ((lon2-lon1) > 0);
    }
}
