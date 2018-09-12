// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.columbusCSV;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.io.IOException;

import org.openstreetmap.josm.actions.AutoScaleAction;
import org.openstreetmap.josm.actions.AutoScaleAction.AutoScaleMode;
import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.io.importexport.FileImporter;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.markerlayer.MarkerLayer;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.Logging;

/**
 * 
 * @author Oliver Wieland &lt;oliver.wieland@online.de&gt;
 * 
 */
public class ColumbusCSVImporter extends FileImporter {
    public static final String COLUMBUS_FILE_EXT = "csv";
    public static final String COLUMBUS_FILE_EXT_DOT = "." + COLUMBUS_FILE_EXT;

    /**
     * Constructs a new {@code ColumbusCSVImporter}.
     */
    public ColumbusCSVImporter() {
        super(new ExtensionFileFilter(COLUMBUS_FILE_EXT, COLUMBUS_FILE_EXT,
            tr("Columbus V-900 CSV Files") + " (*" + COLUMBUS_FILE_EXT_DOT + ")"));
    }

    @Override
    public boolean acceptFile(File pathname) {
        try {
            // do some deep packet inspection...
            return super.acceptFile(pathname)
                && ColumbusCSVReader.isColumbusFile(pathname);
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public void importData(File file, ProgressMonitor progressMonitor)
        throws IOException, IllegalDataException {
        String fn = file.getPath();
    
        if (progressMonitor == null) { // make sure that there is a progress monitor...
            progressMonitor = NullProgressMonitor.INSTANCE;
        }
    
        progressMonitor.beginTask(String.format(
            tr("Importing CSV file ''%s''..."), file.getName(), 4));
        progressMonitor.setTicksCount(1);
    
        if (fn.toLowerCase().endsWith(COLUMBUS_FILE_EXT_DOT)) {
            try {
                ColumbusCSVReader r = new ColumbusCSVReader();
        
                // transform CSV into GPX
                GpxData gpxData = r.transformColumbusCSV(fn);
                assert gpxData != null;
                progressMonitor.setTicksCount(1);
        
                r.dropBufferLists();
        
                progressMonitor.setTicksCount(2);
        
                GpxLayer gpxLayer = new GpxLayer(gpxData, file.getName());
                assert gpxLayer != null;
        
                // add layer to show way points
                MainApplication.getLayerManager().addLayer(gpxLayer);
        
                progressMonitor.setTicksCount(3);
        
                // ... and scale view appropriately - if wished by user
                if (ColumbusCSVPreferences.zoomAfterImport()) {
                    AutoScaleAction action = new AutoScaleAction(AutoScaleMode.DATA);
                    action.autoScale();
                }
                progressMonitor.setTicksCount(4);
        
                if (Config.getPref().getBoolean("marker.makeautomarkers", true)) {
                    try {
                        MarkerLayer ml = new MarkerLayer(gpxData,
                            tr("Markers of ") + file.getName(), file, gpxLayer);
            
                        assert ml != null;
                        assert ml.data != null;
            
                        Logging.info("Layer: " + ml);
                        Logging.info("Data size: " + ml.data.size());
            
                        MainApplication.getLayerManager().addLayer(ml);
                        if (ml.data.isEmpty()) {
                        	Logging.warn("File contains no markers.");
                        }
                    } catch (Exception exLayer) {
                    	Logging.error(exLayer);
                    }
                } else {
                	Logging.warn("Option 'marker.makeautomarkers' is not set; audio marker layer is not created.");
                }
            } catch (Exception e) {
                // catch and forward exception
            	Logging.error(e);
                throw new IllegalDataException(e);
            } finally { // take care of monitor...
                progressMonitor.finishTask();
            }
        } else {
            throw new IOException(
                tr(String.format("Unsupported file extension (file '%s' does not end with '%s')!",
                        file.getName(), COLUMBUS_FILE_EXT)));
        }
    }
}
