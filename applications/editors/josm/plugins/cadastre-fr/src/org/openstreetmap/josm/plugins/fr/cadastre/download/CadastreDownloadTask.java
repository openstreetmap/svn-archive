// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.download;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.concurrent.Future;

import org.openstreetmap.josm.actions.downloadtasks.DownloadOsmTask;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.gui.MainApplication;
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
    public Future<?> loadUrl(boolean newLayer, String url, ProgressMonitor progressMonitor) {
        downloadTask = new DownloadTask(newLayer, new CadastreServerReader(url), progressMonitor);
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
}
