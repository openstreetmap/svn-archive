/**
 * 
 */
package at.dallermassl.josm.plugin.surveyor;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collection;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.layer.RawGpsLayer.GpsPoint;
import org.openstreetmap.josm.gui.layer.markerlayer.Marker;
import org.openstreetmap.josm.io.XmlWriter;

/**
 * Gpx writer that writes tracks and markers as waypoints.
 *  
 * @author cdaller
 *
 */
public class GpxTrackMarkerWriter implements XmlWriter.OsmWriterInterface {
	
	private final Collection<Collection<GpsPoint>> gpsData;
	private final Collection<Marker> markers;
	private static final SimpleDateFormat gpxDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	
	public GpxTrackMarkerWriter(Collection<Collection<GpsPoint>> gpsData, Collection<Marker> markers) {
		this.gpsData = gpsData;
		this.markers = markers;
	}

	public void header(PrintWriter out) {
		out.println("<gpx version='1.1' creator='JOSM' xmlns='http://www.topografix.com/GPX/1/1'>");
	}

	public void write(PrintWriter out) {
		// calculate bounds
		Bounds b = new Bounds(new LatLon(Double.MAX_VALUE, Double.MAX_VALUE), new LatLon(Double.MIN_VALUE, Double.MIN_VALUE));
		b = extendBounds(b);
		
		out.println("  <metadata>");
		out.println("    <bounds minlat='"+b.min.lat()+"' minlon='"+b.min.lon()+"' maxlat='"+b.max.lat()+"' maxlon='"+b.max.lon()+"' />");
		out.println("  </metadata>");

		if(gpsData != null && gpsData.size() > 0) {
			out.println("  <trk>");
			for (Collection<GpsPoint> c : gpsData) {
				out.println("    <trkseg>");
				LatLon last = null;
				for (GpsPoint p : c) {
					// skip double entries
					if (p.latlon.equals(last))
						continue;
					last =  p.latlon;
					LatLon ll = p.latlon;
					out.print("      <trkpt lat='"+ll.lat()+"' lon='"+ll.lon()+"'");
					if (p.time != null && p.time.length()!=0) {
						out.println(">");
						out.println("        <time>" + p.time + "</time>");
						out.println("      </trkpt>");
					} else
						out.println(" />");
				}
				out.println("    </trkseg>");
			}
			out.println("  </trk>");
		}
		
		if(markers != null && markers.size() > 0) {
			LatLon latLon;
			for(Marker marker : markers) {
				latLon = Main.proj.eastNorth2latlon(marker.eastNorth);
				out.print("  <wpt");
				out.print(" lat='" + latLon.lat() + "'");
				out.print(" lon='" + latLon.lon() + "'");
				out.println(">");
				out.println("  <name>" + marker.text + "</name>");
				out.println("</wpt>");
			}
		}
	}

	/**
     * @param bounds
     */
    public Bounds extendBounds(Bounds bounds) {
    	if(gpsData != null) {
    		for (Collection<GpsPoint> c : gpsData) {
    			for (GpsPoint p : c) {
    				bounds.extend(p.latlon);
    			}
    		}
    	}
    	if(markers != null) {
    		LatLon latLon;
    		for(Marker marker : markers) {
    			latLon = Main.proj.eastNorth2latlon(marker.eastNorth);
    			bounds.extend(latLon);
    		}
    	}
        return bounds;
    }

	public void footer(PrintWriter out) {
		out.println("</gpx>");
    }
}

