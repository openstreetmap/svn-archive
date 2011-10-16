//    JOSM Imagery XML Bounds plugin.
//    Copyright (C) 2011 Don-vip
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
package org.openstreetmap.josm.plugins.imageryxmlbounds.actions.downloadtask;

import java.util.concurrent.Future;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.downloadtasks.DownloadOsmTask;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.OsmServerReader;
import org.openstreetmap.josm.plugins.imageryxmlbounds.XmlBoundsConstants;
import org.openstreetmap.josm.plugins.imageryxmlbounds.XmlBoundsLayer;
import org.openstreetmap.josm.plugins.imageryxmlbounds.io.JosmServerLocationReader;

public class DownloadXmlBoundsTask extends DownloadOsmTask implements XmlBoundsConstants {

    @Override
    public Future<?> download(boolean newLayer, Bounds downloadArea,
            ProgressMonitor progressMonitor) {
        return null;
    }

    @Override
    public Future<?> loadUrl(boolean newLayer, String url,
            ProgressMonitor progressMonitor) {
        downloadTask = new DownloadTask(newLayer,
                new JosmServerLocationReader(url), progressMonitor);
        // We need submit instead of execute so we can wait for it to finish and get the error
        // message if necessary. If no one calls getErrorMessage() it just behaves like execute.
        return Main.worker.submit(downloadTask);
    }

    @Override
    public boolean acceptsUrl(String url) {
        return url != null && (url.equals(XML_LOCATION) || url.matches("http://.*\\."+EXTENSION.replace(".", "\\.")));
    }

    protected class DownloadTask extends DownloadOsmTask.DownloadTask {
        public DownloadTask(boolean newLayer, OsmServerReader reader, ProgressMonitor progressMonitor) {
            super(newLayer, reader, progressMonitor);
        }
        
        /* (non-Javadoc)
         * @see org.openstreetmap.josm.actions.downloadtasks.DownloadOsmTask.DownloadTask#getEditLayer()
         */
        @Override
        protected OsmDataLayer getEditLayer() {
            OsmDataLayer editLayer = super.getEditLayer();
            return editLayer instanceof XmlBoundsLayer ? editLayer : null;
        }

        /* (non-Javadoc)
         * @see org.openstreetmap.josm.actions.downloadtasks.DownloadOsmTask.DownloadTask#getNumDataLayers()
         */
        @Override
        protected int getNumDataLayers() {
            int count = 0;
            if (!Main.isDisplayingMapView()) return 0;
            for (Layer layer : Main.map.mapView.getAllLayers()) {
                if (layer instanceof XmlBoundsLayer) {
                    count++;
                }
            }
            return count;
        }

        /* (non-Javadoc)
         * @see org.openstreetmap.josm.actions.downloadtasks.DownloadOsmTask.DownloadTask#getFirstDataLayer()
         */
        @Override
        protected OsmDataLayer getFirstDataLayer() {
            if (!Main.isDisplayingMapView()) return null;
            for (Layer layer : Main.map.mapView.getAllLayersAsList()) {
                if (layer instanceof XmlBoundsLayer)
                    return (XmlBoundsLayer) layer;
            }
            return null;
        }

        /* (non-Javadoc)
         * @see org.openstreetmap.josm.actions.downloadtasks.DownloadOsmTask.DownloadTask#createNewLayer()
         */
        @Override
        protected OsmDataLayer createNewLayer() {
            return new XmlBoundsLayer(dataSet, XmlBoundsLayer.createNewName(), null);
        }
    }
}
