//    JOSM opendata plugin.
//    Copyright (C) 2011-2012 Don-vip
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
package org.openstreetmap.josm.plugins.opendata.core.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.downloadtasks.DownloadOsmTask;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler;
import org.openstreetmap.josm.plugins.opendata.core.datasets.DataSetUpdater;
import org.openstreetmap.josm.plugins.opendata.core.gui.AskLicenseAgreementDialog;
import org.openstreetmap.josm.plugins.opendata.core.io.NetworkReader;
import org.openstreetmap.josm.plugins.opendata.core.layers.OdDataLayer;
import org.openstreetmap.josm.plugins.opendata.core.licenses.License;
import org.openstreetmap.josm.plugins.opendata.core.modules.Module;
import org.openstreetmap.josm.plugins.opendata.core.modules.ModuleHandler;

public class DownloadDataTask extends DownloadOsmTask implements OdConstants {

	private AbstractDataSetHandler handler;
	
	//private static final PdfEditorKit pdfEditorKit = new PdfEditorKit();
	
	@Override
	public Future<?> download(boolean newLayer, Bounds downloadArea, ProgressMonitor progressMonitor) {
		return null;
	}

	@Override
	public Future<?> loadUrl(boolean newLayer, String url, ProgressMonitor progressMonitor) {
        downloadTask = new InternalDownloadTasK(newLayer, new NetworkReader(url, handler, true), progressMonitor);
        currentBounds = null;
        if (handler == null || !handler.hasLicenseToBeAccepted() || askLicenseAgreement(handler.getLicense())) {
        	return Main.worker.submit(downloadTask);
        } else {
        	return null;
        }
	}

	@Override
	public boolean acceptsUrl(String url) {
		this.handler = null;
		for (Module module : ModuleHandler.moduleList) {
			for (AbstractDataSetHandler handler : module.getNewlyInstanciatedHandlers()) {
				if (handler.acceptsUrl(url)) {
					this.handler = handler;
					return true;
				}
			}
		}
		for (String ext : new String[]{ZIP_EXT, CSV_EXT, KML_EXT, KMZ_EXT, XLS_EXT, ODS_EXT, SHP_EXT, MIF_EXT, TAB_EXT}) {
			if (Pattern.compile(".*\\."+ext, Pattern.CASE_INSENSITIVE).matcher(url).matches()) {
				return true;
			}
		}
		return false;
	}
	
	protected class InternalDownloadTasK extends DownloadTask {

		public InternalDownloadTasK(boolean newLayer, NetworkReader reader, ProgressMonitor progressMonitor) {
			super(newLayer, reader, progressMonitor);
		}

		/* (non-Javadoc)
		 * @see org.openstreetmap.josm.actions.downloadtasks.DownloadOsmTask.DownloadTask#createNewLayer(java.lang.String)
		 */
		@Override
		protected OsmDataLayer createNewLayer(String layerName) {
            File associatedFile = ((NetworkReader)reader).getReadFile();
            String filename = ((NetworkReader)reader).getReadFileName();
            if (layerName == null || layerName.isEmpty()) {
            	if (associatedFile != null) {
            		layerName = associatedFile.getName();
            	} else if (filename != null && !filename.isEmpty()) {
            		layerName = filename;
            	} else {
            		layerName = OsmDataLayer.createNewName();
            	}
            }
    		DataSetUpdater.updateDataSet(dataSet, handler, associatedFile);
    		return new OdDataLayer(dataSet, layerName, associatedFile, handler);
		}
	}
	
    /**
     * returns true if the user accepts the license, false if they refuse
     */
    protected final boolean askLicenseAgreement(License license) {
    	if (license == null || (license.getURL() == null && license.getSummaryURL() == null)) {
    		return true;
    	}
    	try {
	        return new AskLicenseAgreementDialog(license).showDialog().getValue() == 1;
	        
		} catch (IOException e) {
            JOptionPane.showMessageDialog(Main.parent, tr("License URL not available: {0}", license.toString()));
            return false;
		}
    }
}
