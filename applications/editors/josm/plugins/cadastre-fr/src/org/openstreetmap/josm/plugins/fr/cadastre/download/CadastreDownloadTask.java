// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.download;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.concurrent.Future;

import org.openstreetmap.josm.actions.downloadtasks.DownloadOsmTask;
import org.openstreetmap.josm.actions.downloadtasks.DownloadParams;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;

/**
 * Cadastre download task.
 */
public class CadastreDownloadTask extends DownloadOsmTask {

    @Override
    public Future<?> download(boolean newLayer, Bounds downloadArea, ProgressMonitor progressMonitor) {
        return null;
    }

    @Override
    public Future<?> loadUrl(DownloadParams settings, String url, ProgressMonitor progressMonitor) {
        downloadTask = new InternalDownloadTask(settings, url, progressMonitor);
        currentBounds = null;
        return MainApplication.worker.submit(downloadTask);
    }

    @Override
    public String[] getPatterns() {
        return new String[]{"https?://.*edigeo.*.tar.bz2"};
    }

    @Override
    public String getTitle() {
        return tr("Download cadastre data");
    }

    class InternalDownloadTask extends DownloadTask {

        private final String url;

        InternalDownloadTask(DownloadParams settings, String url, ProgressMonitor progressMonitor) {
            super(settings, new CadastreServerReader(url), progressMonitor);
            this.url = url;
        }

        @Override
        protected OsmDataLayer createNewLayer(String layerName) {
            return super.createNewLayer(layerName != null ? layerName : url.substring(url.lastIndexOf('/')+1));
        }
    }
}
