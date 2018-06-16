// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pbf.action;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.concurrent.Future;

import org.openstreetmap.josm.actions.downloadtasks.DownloadOsmTask;
import org.openstreetmap.josm.actions.downloadtasks.DownloadParams;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.plugins.pbf.PbfConstants;
import org.openstreetmap.josm.plugins.pbf.io.PbfServerReader;

/**
 * Task allowing to download remote PBF files.
 */
public class DownloadPbfTask extends DownloadOsmTask implements PbfConstants {

    @Override
    public Future<?> download(boolean newLayer, Bounds downloadArea, ProgressMonitor progressMonitor) {
        return null;
    }

    @Override
    public Future<?> loadUrl(DownloadParams settings, String url, ProgressMonitor progressMonitor) {
        downloadTask = new DownloadTask(settings, new PbfServerReader(url), progressMonitor);
        // We need submit instead of execute so we can wait for it to finish and get the error
        // message if necessary. If no one calls getErrorMessage() it just behaves like execute.
        return MainApplication.worker.submit(downloadTask);
    }

    @Override
    public String[] getPatterns() {
        return new String[]{".*\\."+EXTENSION};
    }

    @Override
    public String getTitle() {
        return tr("Download PBF");
    }
}
