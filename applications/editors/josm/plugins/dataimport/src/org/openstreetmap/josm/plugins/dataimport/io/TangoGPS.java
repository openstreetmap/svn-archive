// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.dataimport.io;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.data.gpx.ImmutableGpxTrack;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.markerlayer.MarkerLayer;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.FileImporter;

/**
 * Data import for TangoGPS file format.
 * @author dmuecke
 */
public class TangoGPS extends FileImporter {

    public TangoGPS() {
        super(new ExtensionFileFilter("log", "log", tr("TangoGPS Files (*.log)")));
    }

    /**
     * @author cbrill
     * This function imports data from file and adds trackpoints to a layer.
     * Read a log file from TangoGPS. These are simple text files in the
     * form: {@code <lat>,<lon>,<elevation>,<speed>,<course>,<hdop>,<datetime>}
     */
    @Override
    public void importData(File file, ProgressMonitor progressMonitor) throws IOException {
        // create the data tree
        List<WayPoint> currentTrackSeg = new ArrayList<>();

        int imported = 0;
        int failure = 0;

        try (
            InputStream source = new FileInputStream(file);
            BufferedReader rd = new BufferedReader(new InputStreamReader(source));
         ) {
            String line;
            while ((line = rd.readLine()) != null) {
                failure++;
                String[] lineElements = line.split(",");
                if (lineElements.length >= 7) {
                    try {
                        WayPoint currentWayPoint = new WayPoint(
                                parseLatLon(lineElements));
                        currentWayPoint.attr.put("ele", lineElements[2]);
                        currentWayPoint.attr.put("time", lineElements[6]);
                        currentWayPoint.setTime();
                        currentTrackSeg.add(currentWayPoint);
                        imported++;
                    } catch (NumberFormatException e) {
                        Main.error(e);
                    }
                }
            }
            failure = failure - imported;
            if (imported > 0) {
                GpxData data = new GpxData();
                data.tracks.add(new ImmutableGpxTrack(Collections.singleton(currentTrackSeg), Collections.<String, Object>emptyMap()));
                data.recalculateBounds();
                data.storageFile = file;
                GpxLayer gpxLayer = new GpxLayer(data, file.getName());
                Main.getLayerManager().addLayer(gpxLayer);
                if (Main.pref.getBoolean("marker.makeautomarkers", true)) {
                    MarkerLayer ml = new MarkerLayer(data, tr("Markers from {0}", file.getName()), file, gpxLayer);
                    if (ml.data.size() > 0) {
                        Main.getLayerManager().addLayer(ml);
                    }
                }
            }
            showInfobox(imported, failure);
        }
    }

    private double parseCoord(String s) {
        return Double.parseDouble(s);
    }

    private LatLon parseLatLon(String[] lineElements) {
        if (lineElements.length < 2)
            return null;
        return new LatLon(parseCoord(lineElements[0]), parseCoord(lineElements[1]));
    }

    private void showInfobox(int success, int failure) {
        String msg = tr("Coordinates imported: ") + success + " " + tr("Format errors: ") + failure + "\n";
        if (success > 0) {
            JOptionPane.showMessageDialog(Main.parent, msg, tr("TangoGPS import success"), JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(Main.parent, msg, tr("TangoGPS import failure!"), JOptionPane.ERROR_MESSAGE);
        }
    }
}
