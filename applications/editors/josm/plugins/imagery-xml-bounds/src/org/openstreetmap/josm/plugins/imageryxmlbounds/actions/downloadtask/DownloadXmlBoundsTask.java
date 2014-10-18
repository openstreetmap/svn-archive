// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.imageryxmlbounds.actions.downloadtask;

import static org.openstreetmap.josm.tools.I18n.tr;

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

/**
 * Download imagery XML bounds from JOSM server.
 */
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
    public String[] getPatterns() {
        return new String[]{
                XML_LOCATION.replace(".", "\\."),
                "http://.*\\."+EXTENSION.replace(".", "\\.")
        };
    }

    @Override
    public String getTitle() {
        return tr("Download imagery XML bounds");
    }

    protected class DownloadTask extends DownloadOsmTask.DownloadTask {

        /**
         * Constructs a new {@code DownloadTask}.
         * @param newLayer if {@code true}, download to a new layer
         * @param reader server reader
         * @param progressMonitor progress monitor
         */
        public DownloadTask(boolean newLayer, OsmServerReader reader, ProgressMonitor progressMonitor) {
            super(newLayer, reader, progressMonitor);
        }

        @Override
        protected OsmDataLayer getEditLayer() {
            OsmDataLayer editLayer = super.getEditLayer();
            return editLayer instanceof XmlBoundsLayer ? editLayer : null;
        }

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

        @Override
        protected OsmDataLayer getFirstDataLayer() {
            if (!Main.isDisplayingMapView()) return null;
            for (Layer layer : Main.map.mapView.getAllLayersAsList()) {
                if (layer instanceof XmlBoundsLayer)
                    return (XmlBoundsLayer) layer;
            }
            return null;
        }

        @Override
        protected OsmDataLayer createNewLayer() {
            return new XmlBoundsLayer(dataSet, XmlBoundsLayer.createNewName(), null);
        }
    }
}
