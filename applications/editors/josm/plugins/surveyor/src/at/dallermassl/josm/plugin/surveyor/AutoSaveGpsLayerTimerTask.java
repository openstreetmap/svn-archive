/**
 * Copyright by Christof Dallermassl
 * This program is free software and licensed under GPL.
 */
package at.dallermassl.josm.plugin.surveyor;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.TimerTask;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.io.GpxWriter;

import at.dallermassl.josm.plugin.surveyor.util.LayerUtil;

/**
 * TimerTask that writes the data of a {@link GpxLayer} to a gpx file.
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

        try {

            GpxLayer gpsLayer = LayerUtil.findGpsLayer(gpsLayerName, GpxLayer.class);
            if(gpsLayer == null) {
                return;
            }
            // write to temporary file, on success, rename tmp file to target file:
            File tmpFile = new File(file.getAbsoluteFile()+".tmp");
            System.out.println("AutoSaving data to file " + file.getAbsolutePath());
            // synchronize on layer to prevent concurrent adding of data to the layer
            // quite a hack, but no other object to sync available :-(
            // @see LiveGpsLayer
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(tmpFile)));
            GpxWriter gpxWriter = new GpxWriter(out);
            gpxWriter.write(gpsLayer.data);
            gpxWriter.close();
            tmpFile.renameTo(file);
        } catch (IOException ioExc) {
            ioExc.printStackTrace();
            JOptionPane.showMessageDialog(Main.parent,
                tr("Error while exporting {0}: {1}", file.getAbsoluteFile(), ioExc.getMessage()),
                tr("Error"),
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
