/**
 * This program is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU General Public License as published by the 
 * Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.openstreetmap.josm.plugins.columbusCSV;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.io.IOException;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.AutoScaleAction;
import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.markerlayer.MarkerLayer;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.FileImporter;
import org.openstreetmap.josm.io.IllegalDataException;

/**
 * 
 * @author Oliver Wieland <oliver.wieland@online.de>
 * 
 */
public class ColumbusCSVImporter extends FileImporter {
    public static final String COLUMBUS_FILE_EXT = "csv";
    public static final String COLUMBUS_FILE_EXT_DOT = "." + COLUMBUS_FILE_EXT;

    public ColumbusCSVImporter() {
	super(new ExtensionFileFilter(COLUMBUS_FILE_EXT, COLUMBUS_FILE_EXT,
		tr("Columbus V-900 CSV Files") + " (*" + COLUMBUS_FILE_EXT_DOT
			+ ")"));
    }

    @Override
    public boolean acceptFile(File pathname) {
	try {
	    // do some deep packet inspection...
	    boolean ok = super.acceptFile(pathname)
		    && ColumbusCSVReader.isColumbusFile(pathname);
	    return ok;
	} catch (Exception ex) {
	    return false;
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openstreetmap.josm.io.FileImporter#importData(java.io.File,
     * org.openstreetmap.josm.gui.progress.ProgressMonitor)
     */
    @Override
    public void importData(File file, ProgressMonitor progressMonitor)
	    throws IOException, IllegalDataException {
	String fn = file.getPath();

	if (progressMonitor == null) { // make sure that there is a progress
				       // monitor...
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
		assert (gpxData != null);
		progressMonitor.setTicksCount(1);

		r.dropBufferLists();

		progressMonitor.setTicksCount(2);

		GpxLayer gpxLayer = new GpxLayer(gpxData, file.getName());
		assert (gpxLayer != null);

		// add layer to show way points
		Main.main.addLayer(gpxLayer);

		progressMonitor.setTicksCount(3);

		// ... and scale view appropriately - if wished by user
		if (ColumbusCSVPreferences.zoomAfterImport()) {
		    AutoScaleAction action = new AutoScaleAction("data");
		    action.autoScale();
		}
		progressMonitor.setTicksCount(4);

		if (Main.pref.getBoolean("marker.makeautomarkers", true)) {
		    try {
			MarkerLayer ml = new MarkerLayer(gpxData,
				tr("Markers of ") + file.getName(), file,
				gpxLayer);

			assert (ml != null);
			assert (ml.data != null);

			System.out.println("Layer: " + ml);
			System.out.println("Data: " + ml.data != null);
			System.out.println("Data size: " + ml.data.size());

			Main.main.addLayer(ml);
			if (ml.data.size() > 0) {

			} else {
			    System.out
				    .println("Warning: File contains no markers.");
			}
		    } catch (Exception exLayer) {
			System.out.println(exLayer);
			exLayer.printStackTrace(System.err);
		    }
		} else {
		    System.out
			    .println("Warning: Option 'marker.makeautomarkers' is not set; audio marker layer is not created.");
		}
	    } catch (Exception e) {
		// catch and forward exception
		e.printStackTrace(System.err);
		throw new IllegalDataException(e);
	    } finally { // take care of monitor...
		progressMonitor.finishTask();
	    }
	} else {
	    throw new IOException(
		    tr(String
			    .format("Unsupported file extension (file '%s' does not end with '%s')!",
				    file.getName(), COLUMBUS_FILE_EXT)));
	}
    }
}
