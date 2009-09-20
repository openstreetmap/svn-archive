/**
 *
 */
package org.openstreetmap.josm.io;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.data.gpx.GpxTrack;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.markerlayer.MarkerLayer;

/**
 * @author dmuecke Data import for TangoGPS file format.
 */
public class TangoGPS extends FileImporter {

	public TangoGPS() {
		super(new ExtensionFileFilter("log", "log",tr("TangoGPS Files (*.log)")));
	}

	/**
	 * @author cbrill
	 * This function imports data from file and adds trackpoints
	 *         to a layer.
	 * Read a log file from TangoGPS. These are simple text files in the
	 * form: <lat>,<lon>,<elevation>,<speed>,<course>,<hdop>,<datetime>
	 */
	@Override
	public void importData(File file) throws IOException {
		// create the data tree
		GpxData data = new GpxData();
		GpxTrack currentTrack = new GpxTrack();
		data.tracks.add(currentTrack);
		ArrayList<WayPoint> currentTrackSeg = new ArrayList<WayPoint>();

		int imported = 0;
		int failure = 0;

		BufferedReader rd = null;
		try {
			InputStream source = new FileInputStream(file);
			rd = new BufferedReader(new InputStreamReader(source));

			String line;
			while ((line = rd.readLine()) != null) {
				failure++;
				String[] lineElements = line.split(",");
				if (lineElements.length == 7) {
					try {
						WayPoint currentWayPoint = new WayPoint(
								parseLatLon(lineElements));
						currentWayPoint.attr.put("ele", lineElements[2]);
						currentWayPoint.attr.put("time", lineElements[6]);
						currentWayPoint.setTime();
						currentTrackSeg.add(currentWayPoint);
						imported++;
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
				}
			}
			failure = failure - imported;
			if(imported > 0) {
				currentTrack.trackSegs.add(currentTrackSeg);
				data.recalculateBounds();
				data.storageFile = file;
				GpxLayer gpxLayer = new GpxLayer(data, file.getName());
				Main.main.addLayer(gpxLayer);
				if (Main.pref.getBoolean("marker.makeautomarkers", true)) {
					MarkerLayer ml = new MarkerLayer(data, tr("Markers from {0}", file.getName()), file, gpxLayer);
					if (ml.data.size() > 0) {
						Main.main.addLayer(ml);
					}
				}
			}
			showInfobox(imported,failure);
		} finally {
			if (rd != null)
				rd.close();
		}
	}

	private double parseCoord(String s) {
		return Double.parseDouble(s);
	}

	private LatLon parseLatLon(String[] lineElements) {
		if (lineElements.length < 2)
			return null;
		return new LatLon(parseCoord(lineElements[0]),
				parseCoord(lineElements[1]));
	}

    private void showInfobox(int success,int failure) {
        String msg = tr("Coordinates imported: ") + success + " " + tr("Format errors: ") + failure + "\n";
        if (success > 0) {
            JOptionPane.showMessageDialog(Main.parent, msg, tr("TangoGPS import success"), JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(Main.parent, msg, tr("TangoGPS import failure!"), JOptionPane.ERROR_MESSAGE);
        }
    }


}
