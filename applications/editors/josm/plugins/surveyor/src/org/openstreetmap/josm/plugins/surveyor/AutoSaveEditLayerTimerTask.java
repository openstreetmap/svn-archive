// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.surveyor;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.TimerTask;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.io.OsmWriter;
import org.openstreetmap.josm.io.OsmWriterFactory;
import org.openstreetmap.josm.tools.Logging;

/**
 * @author cdaller
 *
 */
public class AutoSaveEditLayerTimerTask extends TimerTask {
    private File file;

    public AutoSaveEditLayerTimerTask(String filename) {
        file = new File(filename);
    }

    @Override
    public void run() {
        OsmDataLayer layer = MainApplication.getLayerManager().getEditLayer();
        if (layer == null) {
            return;
        }
        try {
            DataSet dataset = layer.data;

            // write to temporary file, on success, rename tmp file to target file:
            File tmpFile = new File(file.getAbsoluteFile()+".tmp");
            Logging.info("AutoSaving osm data to file " + file.getAbsolutePath());
            synchronized (SurveyorLock.class) {
                OsmWriter w = OsmWriterFactory.createOsmWriter(new PrintWriter(new OutputStreamWriter(
                        new FileOutputStream(tmpFile), StandardCharsets.UTF_8)), false, dataset.getVersion());
                w.header();
                w.writeDataSources(dataset);
                w.writeContent(dataset);
                w.footer();
            }
            tmpFile.renameTo(file);
        } catch (IOException ex) {
            Logging.error(ex);
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(),
                tr("Error while exporting {0}: {1}", file.getAbsoluteFile(), ex.getMessage()),
                tr("Error"),
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
