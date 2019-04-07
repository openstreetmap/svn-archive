// License: GPL. For details, see LICENSE file.
package poly;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.concurrent.Future;

import org.openstreetmap.josm.actions.downloadtasks.DownloadOsmTask;
import org.openstreetmap.josm.actions.downloadtasks.DownloadParams;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.OsmServerReader;
import org.openstreetmap.josm.io.OsmTransferException;

/**
 * Download task for remote control to download poly files.
 * 
 * @author zverik
 */
public class DownloadPolyTask extends DownloadOsmTask {

    @Override
    public Future<?> download(DownloadParams settings, Bounds downloadArea, ProgressMonitor progressMonitor) {
        return null;
    }

    @Override
    public Future<?> loadUrl(DownloadParams settings, String url, ProgressMonitor progressMonitor) {
        downloadTask = new DownloadTask(settings, new ServerPolyReader(url), progressMonitor);
        return MainApplication.worker.submit(downloadTask);
    }

    @Override
    public String[] getPatterns() {
        return new String[]{"https?://.*/.*\\.poly"};
    }

    @Override
    public String getTitle() {
        return tr("Download Osmosis poly");
    }

    /**
     * Class to download and parse a poly file.  
     */
    public static class ServerPolyReader extends OsmServerReader {
        private String url;

        /**
         * Create new {@link ServerPolyReader}
         * @param url
         */
        public ServerPolyReader(String url) {
            this.url = url;
        }

        @Override
        public DataSet parseOsm(ProgressMonitor progressMonitor) throws OsmTransferException {
            try {
                progressMonitor.beginTask(tr("Contacting Server..."));
                return new PolyImporter().parseDataSet(url);
            } catch (Exception e) {
                throw new OsmTransferException(e);
            } finally {
                progressMonitor.finishTask();
            }
        }
    }
}
