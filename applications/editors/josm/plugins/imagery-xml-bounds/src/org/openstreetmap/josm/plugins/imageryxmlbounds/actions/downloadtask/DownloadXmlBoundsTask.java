// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.imageryxmlbounds.actions.downloadtask;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Optional;
import java.util.concurrent.Future;

import org.openstreetmap.josm.actions.downloadtasks.DownloadOsmTask;
import org.openstreetmap.josm.actions.downloadtasks.DownloadParams;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.MainApplication;
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
    public Future<?> download(DownloadParams settings, Bounds downloadArea,
            ProgressMonitor progressMonitor) {
        return null;
    }

    @Override
    public Future<?> loadUrl(DownloadParams settings, String url,
            ProgressMonitor progressMonitor) {
        downloadTask = new InternalDownloadTask(settings,
                new JosmServerLocationReader(url), progressMonitor);
        // We need submit instead of execute so we can wait for it to finish and get the error
        // message if necessary. If no one calls getErrorMessage() it just behaves like execute.
        return MainApplication.worker.submit(downloadTask);
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

    protected class InternalDownloadTask extends DownloadTask {

        /**
         * Constructs a new {@code DownloadTask}.
         * @param settings download settings
         * @param reader server reader
         * @param progressMonitor progress monitor
         */
        public InternalDownloadTask(DownloadParams settings, OsmServerReader reader, ProgressMonitor progressMonitor) {
            super(settings, reader, progressMonitor);
        }

        @Override
        protected OsmDataLayer getEditLayer() {
            OsmDataLayer editLayer = super.getEditLayer();
            return editLayer instanceof XmlBoundsLayer ? editLayer : null;
        }

        @Override
        protected long getNumModifiableDataLayers() {
            long count = 0;
            if (!MainApplication.isDisplayingMapView()) return 0;
            for (Layer layer : MainApplication.getLayerManager().getLayers()) {
                if (layer instanceof XmlBoundsLayer) {
                    count++;
                }
            }
            return count;
        }

        @Override
        protected OsmDataLayer getFirstModifiableDataLayer() {
            if (!MainApplication.isDisplayingMapView()) return null;
            for (Layer layer : MainApplication.getLayerManager().getLayers()) {
                if (layer instanceof XmlBoundsLayer)
                    return (XmlBoundsLayer) layer;
            }
            return null;
        }

        @Override
        protected OsmDataLayer createNewLayer(DataSet ds, Optional<String> layerName) {
            return new XmlBoundsLayer(ds, layerName.orElseGet(this::generateLayerName), null);
        }
    }
}
