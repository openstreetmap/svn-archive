package poly;

import java.util.concurrent.Future;
import org.openstreetmap.josm.Main;
import static org.openstreetmap.josm.tools.I18n.tr;
import org.openstreetmap.josm.actions.downloadtasks.DownloadOsmTask;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.DataSet;
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
    public Future<?> download( boolean newLayer, Bounds downloadArea, ProgressMonitor progressMonitor ) {
        return null;
    }

    @Override
    public Future<?> loadUrl( boolean new_layer, String url, ProgressMonitor progressMonitor ) {
        downloadTask = new DownloadTask(new_layer, new ServerPolyReader(url), progressMonitor);
        return Main.worker.submit(downloadTask);
    }

    @Override
    public String[] getPatterns() {
        return new String[]{"http://.*/.*\\.poly"};
    }

    @Override
    public String getTitle() {
        return tr("Download Osmosis poly");
    }

    public class ServerPolyReader extends OsmServerReader {
        private String url;

        public ServerPolyReader( String url ) {
            this.url = url;
        }

        @Override
        public DataSet parseOsm( ProgressMonitor progressMonitor ) throws OsmTransferException {
            try {
                progressMonitor.beginTask(tr("Contacting Server...", 10));
                return new PolyImporter().parseDataSet(url);
            } catch( Exception e ) {
                throw new OsmTransferException(e);
            } finally {
                progressMonitor.finishTask();
            }
        }
    }
}
