/**
 * 
 */
package at.dallermassl.josm.plugin.surveyor;

import java.util.Collection;

import org.openstreetmap.josm.gui.layer.RawGpsLayer;
import org.openstreetmap.josm.gui.layer.markerlayer.Marker;
import org.openstreetmap.josm.gui.layer.markerlayer.MarkerLayer;
import org.openstreetmap.josm.io.XmlWriter.OsmWriterInterface;

/**
 * TimerTask that writes the data of a {@link RawGpsLayer} and from a {@link MarkerLayer} to a 
 * gpx file.
 * Every time the task is run, the layers are retrieved from the map view so it even works
 * if the layer does not exist at the start of the timer task. If the layer does not exist,
 * the file is not written.
 * 
 * @author cdaller
 *
 */
public class AutoSaveGpsAndMarkerLayerTimeTask extends AutoSaveGpsLayerTimerTask {
    private String markerLayerName;

    /**
     * Constructor using the file to write to and the names of the layers.
     * @param filename the file to write to.
     * @param gpsLayername the name of the layer holding the gps data.
     * @param markerLayerName the name of the layer holding markers. 
     */
    public AutoSaveGpsAndMarkerLayerTimeTask(String filename, String gpsLayername, String markerLayerName) {
        super(filename, gpsLayername);
        this.markerLayerName = markerLayerName;
    }

    /* (non-Javadoc)
     * @see at.dallermassl.josm.plugin.surveyor.AutoSaveGpsLayerTimerTask#getXmlWriter()
     */
    @Override
    public OsmWriterInterface getXmlWriter() {
        Collection<Collection<RawGpsLayer.GpsPoint>> gpsPoints = null;
        Collection<Marker> markers = null;
        
        RawGpsLayer gpsLayer = findGpsLayer(getGpsLayerName(), RawGpsLayer.class);
        if(gpsLayer != null) {
            gpsPoints = gpsLayer.data;
        }
        MarkerLayer markerLayer = findGpsLayer(markerLayerName, MarkerLayer.class);
        if(markerLayer != null) {
            markers = markerLayer.data;
        }
        return new GpxTrackMarkerWriter(gpsPoints, markers);
    }

}
