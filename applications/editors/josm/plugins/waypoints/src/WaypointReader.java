package waypoints; 

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet; // NW
import org.openstreetmap.josm.data.osm.Node; // NW
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Read waypoints from a GPX file and convert to nodes.
 */
public class WaypointReader {

	private static class Parser extends DefaultHandler {
		/**
		 * Current track to be read. The last entry is the current trkpt.
		 * If in wpt-mode, it contain only one GpsPoint.
		 */
	    private DataSet dataSet;
		private LatLon currentLatLon;
		private String curWptName;
		private  boolean inName = false;

		// NW start	
		// data now has two components: the GPS points and an OsmDataLayer.
		// This is to allow us to convert waypoints straight to nodes.
		// The way this works is that waypoints with a name not beginning
		// with 0 - i.e. waypoints specially named - will be loaded in as
		// nodes, in addition to going into the gpx layer. Other waypoints will
		// only go into the gpx layer.
		public Parser()
		{
			dataSet = new DataSet();
		}
		// NW end

		@Override public void startElement(String namespaceURI, 
				String localName, String qName, Attributes atts) 
					throws SAXException {
			if (qName.equals("wpt")) {
				try {
	                double lat = Double.parseDouble(atts.getValue("lat"));
	                double lon = Double.parseDouble(atts.getValue("lon"));
	        		if (Math.abs(lat) > 90)
	        			throw new SAXException
						(tr("Data error: lat value \"{0}\" is out of bound.", 
							lat));
	        		if (Math.abs(lon) > 180)
	        			throw new SAXException
						(tr("Data error: lon value \"{0}\" is out of bound.", 
							lon));
	                currentLatLon = new LatLon(lat, lon);
                } catch (NumberFormatException e) {
                	e.printStackTrace();
	                throw new SAXException(e);
                }
			}
			else if (qName.equals("name")) {
				inName = true;
				curWptName = "";	
			}	
		}

		@Override public void characters(char[] ch, int start, int length) {
			// NW start
			if (inName) {
				curWptName = new String (ch,start,length);
			}
			// NW end
		}

		@Override public void endElement(String namespaceURI, String localName,
					   					String qName) {
			if (qName.equals("wpt") && curWptName!="") { 
				// create a new node from the latitude and longitude
				System.out.println("Found a waypoint to convert to a node: " 
										+ curWptName);
				Node node = new Node(currentLatLon);
				node.put("name",curWptName);
				dataSet.nodes.add(node);
			}
			else if (qName.equals("name")) {
				inName = false;
			}
        }
	}

	/**
	 * Parse and return the read data
	 */
	public static DataSet parse(InputStream source) 
			throws SAXException, IOException, ParserConfigurationException {
		Parser parser = new Parser();
		SAXParserFactory.newInstance().newSAXParser().parse(new InputSource(new InputStreamReader(source, "UTF-8")), parser);
		return parser.dataSet;
	}
}
