/**
 * 
 */
package at.dallermassl.josm.plugin.openvisible;

import java.awt.geom.Rectangle2D;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
        
    public static void main(String[] args) {
        if(args.length < 5) {
            printHelp();
            return;
        }
        double minLat = Double.parseDouble(args[0]);
        double maxLat = Double.parseDouble(args[1]);
        double minLon = Double.parseDouble(args[2]);
        double maxLon = Double.parseDouble(args[3]);
        String[] files = new String[args.length - 4];
        System.arraycopy(args, 4, files, 0, args.length - 4);
            
        try {    
            File file;
            for(String fileName : files) {
                file = new File(fileName);
                if(!file.isDirectory() 
                  && (file.getName().endsWith("gpx") || file.getName().endsWith("osm"))) {
                    OsmGpxBounds parser = new OsmGpxBounds();
                    parser.parse(new BufferedInputStream(new FileInputStream(file)));
                    if(parser.intersects(minLat, maxLat, minLon, maxLon)) {
                        System.out.println(file.getAbsolutePath()); // + "," + parser.minLat + "," + parser.maxLat + "," + parser.minLon + "," + parser.maxLon);                        
                    }
//                    System.out.println(parser.intersects(47.0555, 47.09, 15.406, 15.4737));
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     */
    private static void printHelp() {
        System.out.println(OsmGpxBounds.class.getName() + " <minLat> <maxLat> <minLon> <maxLon> <files+>");
        
    }

}
