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

import java.io.File;
import java.util.concurrent.Future;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.downloadtasks.DownloadOsmTask;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.AbstractReader;
import org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler;
import org.openstreetmap.josm.plugins.opendata.core.datasets.DataSetUpdater;
import org.openstreetmap.josm.plugins.opendata.core.io.NetworkReader;
import org.openstreetmap.josm.plugins.opendata.core.layers.OdDataLayer;
import org.openstreetmap.josm.plugins.opendata.core.modules.Module;
import org.openstreetmap.josm.plugins.opendata.core.modules.ModuleHandler;

public class DownloadDataTask extends DownloadOsmTask {

	private AbstractDataSetHandler handler;
	
	@Override
	public Future<?> download(boolean newLayer, Bounds downloadArea, ProgressMonitor progressMonitor) {
		return null;
	}

	@Override
	public Future<?> loadUrl(boolean newLayer, String url, ProgressMonitor progressMonitor) {
		Class<? extends AbstractReader> readerClass = null; // TODO
        downloadTask = new InternDownloadTasK(newLayer, new NetworkReader(url, handler, readerClass), progressMonitor);
        currentBounds = null;
        // Extract .osm filename from URL to set the new layer name
        //Matcher matcher = Pattern.compile("http://.*/(.*\\.osm)").matcher(url);
        //newLayerName = matcher.matches() ? matcher.group(1) : null;
        return Main.worker.submit(downloadTask);
	}

	@Override
	public boolean acceptsUrl(String url) {
		for (Module module : ModuleHandler.moduleList) {
			for (AbstractDataSetHandler handler : module.getHandlers()) {
				if (handler != null && handler.getDataURL() != null && url.equals(handler.getDataURL().toString())) {
					this.handler = handler;
					return true;
				}
			}
		}
		return false;
	}
	
	protected class InternDownloadTasK extends DownloadTask {

		public InternDownloadTasK(boolean newLayer, NetworkReader reader, ProgressMonitor progressMonitor) {
			super(newLayer, reader, progressMonitor);
		}

		/* (non-Javadoc)
		 * @see org.openstreetmap.josm.actions.downloadtasks.DownloadOsmTask.DownloadTask#createNewLayer(java.lang.String)
		 */
		@Override
		protected OsmDataLayer createNewLayer(String layerName) {
            File associatedFile = ((NetworkReader)reader).getReadFile();
            if (layerName == null || layerName.isEmpty()) {
                layerName = associatedFile == null ? OsmDataLayer.createNewName() : associatedFile.getName();
            }
    		DataSetUpdater.updateDataSet(dataSet, handler, associatedFile);
    		return new OdDataLayer(dataSet, layerName, associatedFile, handler);
		}
	}
}
