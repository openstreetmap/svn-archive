/**
 * 
 */
package at.dallermassl.josm.plugin.surveyor;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.TimerTask;

import javax.swing.JOptionPane;

import livegps.LiveGpsLock;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.RawGpsLayer;
import org.openstreetmap.josm.io.GpxWriter;
import org.openstreetmap.josm.io.XmlWriter;

/**
 * TimerTask that writes the data of a {@link RawGpsLayer} to a gpx file.
 * Every time the task is run, the layer is retrieved from the map view so it even works
 * if the layer does not exist at the start of the timer task. If the layer does not exist,
 * the file is not written.
 * 
 * @author cdaller
 *
 */
public class AutoSaveGpsLayerTimerTask extends TimerTask {
    private String gpsLayerName;
    private File file;
    
    /**
     * Constructor using the file to write to and the name of the layer.
     * @param filename the file to write to.
     * @param gpsLayername the name of the layer holding the gps data.
     */
    public AutoSaveGpsLayerTimerTask(String filename, String layerName) {
        super();
        this.gpsLayerName = layerName;
        this.file = new File(filename);
    }
    
    /**
     * @return the gpsLayerName
     */
    public String getGpsLayerName() {
        return this.gpsLayerName;
    }


    /* (non-Javadoc)
     * @see java.util.TimerTask#run()
     */
    @Override
    public void run() {
        System.out.println("AutoSaving data to file " + file.getAbsolutePath());

        try {
            XmlWriter.OsmWriterInterface writer = getXmlWriter();
            if(writer != null) {
                // synchronize on layer to prevent concurrent adding of data to the layer
                // quite a hack, but no other object to sync available :-(
                // @see LiveGpsLayer
                synchronized(LiveGpsLock.class) {
                    XmlWriter.output(new FileOutputStream(file), writer);
                }                
            }
        } catch (IOException x) {
            x.printStackTrace();
            JOptionPane.showMessageDialog(Main.parent, 
                tr("Error while exporting {0}", file.getAbsoluteFile())+":\n" + x.getMessage(), 
                tr("Error"), 
                JOptionPane.ERROR_MESSAGE);
        }       
    }
    
    /**
     * Returns the layer with the given name and type from the map view or <code>null</code>.
     * @param <LayerType> the type of the layer.
     * @param layerName the name of the layer.
     * @param layerType the type of the layer.
     * @return the layer or <code>null</code>.
     */
    public <LayerType extends Layer> LayerType findGpsLayer(String layerName, Class<LayerType> layerType) {
        LayerType result = null;
        if(Main.map != null && Main.map.mapView != null) {
            for(Layer layer : Main.map.mapView.getAllLayers()) {
                if(layerName.equals(layer.name) && layerType.isAssignableFrom(layer.getClass())) {
                    result = (LayerType) layer;
                    break;
                }
            }
        }
        return result;
    }
    
    
    /**
     * Returns the writer that writes the data. Override this method, if another type
     * of writer should be used.
     * @return the writer.
     */
    public XmlWriter.OsmWriterInterface getXmlWriter() {
        RawGpsLayer gpsLayer = findGpsLayer(gpsLayerName, RawGpsLayer.class);
        if(gpsLayer == null) {
            return null;
        }
        return new GpxWriter.Trk(gpsLayer.data);
    }


}
