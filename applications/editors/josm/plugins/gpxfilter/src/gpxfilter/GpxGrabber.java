// License: GPL. Copyright 2007 by Immanuel Scholz and others
package gpxfilter;

import java.io.IOException;
import java.io.InputStream;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.GpxReader;
import org.openstreetmap.josm.io.OsmServerReader;
import org.openstreetmap.josm.io.OsmTransferException;
import org.xml.sax.SAXException;

public class GpxGrabber extends OsmServerReader {
    /**
     * The boundings of the desired map data.
     */
    private final double lat1;
    private final double lon1;
    private final double lat2;
    private final double lon2;
    
    private int page;

    public GpxGrabber(Bounds downloadArea) {
        this.lat1 = downloadArea.getMin().lat();
        this.lon1 = downloadArea.getMin().lon();
        this.lat2 = downloadArea.getMax().lat();
        this.lon2 = downloadArea.getMax().lon();
        
        page = 0;
    }

    /**
     * Retrieve raw gps waypoints from the server API.
     * @return A list of all primitives retrieved. Currently, the list of lists
     *      contain only one list, since the server cannot distinguish between
     *      ways.
     */
    public GpxData parseRawGps() throws IOException, SAXException,OsmTransferException {
        try {
            String url = "trackpoints?bbox="+lon1+","+lat1+","+lon2+","+lat2+"&page="+page;

            InputStream in = getInputStream(url, NullProgressMonitor.INSTANCE);
            GpxReader reader = new GpxReader(in);
            reader.parse(false);
            GpxData result = reader.getGpxData();
            in.close();
            result.fromServer = true;
            page++;
            return result;
        } catch (IllegalArgumentException e) {
            // caused by HttpUrlConnection in case of illegal stuff in the response
            if (cancel)
                return null;
            throw new SAXException("Illegal characters within the HTTP-header response.", e);
        } catch (IOException e) {
            if (cancel)
                return null;
            throw e;
        } catch (SAXException e) {
            throw e;
        } catch (OsmTransferException e) {
            throw e;
        } catch (RuntimeException e) {
            if (cancel)
                return null;
            throw e;
        }    }

    @Override
    public DataSet parseOsm(ProgressMonitor progressMonitor) throws OsmTransferException {
        return null;
    }
}
