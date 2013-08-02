// License: GPL. For details, see LICENSE file.
package mirrored_download;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.actions.downloadtasks.DownloadOsmTask;
import org.openstreetmap.josm.actions.downloadtasks.PostDownloadHandler;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.DataSource;
import org.openstreetmap.josm.gui.download.DownloadDialog;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.gui.widgets.AbstractTextComponentValidator;
import org.openstreetmap.josm.io.BoundingBoxDownloader;
import org.openstreetmap.josm.io.OsmTransferException;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.Shortcut;

public class OverpassDownloadAction extends JosmAction {

    public OverpassDownloadAction() {
        super(tr("Download from Overpass API ..."), "download_mirror", tr("Download map data from Overpass API server."),
                /*Shortcut.registerShortcut("overpass:download", tr("File: {0}", tr("Download from Overpass API ...")), KeyEvent.VK_DOWN, Shortcut.ALT_SHIFT)*/null,
                true, "overpassdownload/download", true);
        putValue("help", ht("/Action/OverpassDownload"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        OverpassDownloadDialog dialog = OverpassDownloadDialog.getInstance();
        dialog.restoreSettings();
        dialog.setVisible(true);
        if (!dialog.isCanceled()) {
            dialog.rememberSettings();
            Bounds area = dialog.getSelectedDownloadArea();
            DownloadOsmTask task = new DownloadOsmTask();
            Future<?> future = task.download(
                    new OverpassDownloadReader(area, dialog.getOverpassQuery()),
                    dialog.isNewLayerRequired(), area, null);
            Main.worker.submit(new PostDownloadHandler(task, future));
        }
    }

    static class OverpassDownloadDialog extends DownloadDialog {

        protected JTextArea overpassQuery;
        private static OverpassDownloadDialog instance;

        private OverpassDownloadDialog(Component parent) {
            super(parent);
            cbDownloadOsmData.setEnabled(false);
            cbDownloadGpxData.setEnabled(false);
            cbStartup.setEnabled(false);
        }

        static public OverpassDownloadDialog getInstance() {
            if (instance == null) {
                instance = new OverpassDownloadDialog(Main.parent);
            }
            return instance;
        }

        @Override
        protected void buildMainPanelAboveDownloadSelections(JPanel pnl) {
            overpassQuery = new JTextArea("[timeout:15];", 8, 80);
            pnl.add(new JLabel(tr("Overpass query: ")), GBC.std().insets(5, 5, 5, 5));
            pnl.add(new JScrollPane(overpassQuery), GBC.eol().fill(GridBagConstraints.BOTH));
        }

        public String getOverpassQuery() {
            return overpassQuery.getText();
        }
    }

    static class OverpassDownloadReader extends BoundingBoxDownloader {

        final String overpassQuery;

        public OverpassDownloadReader(Bounds downloadArea, String overpassQuery) {
            super(downloadArea);
            this.overpassQuery = overpassQuery.trim();
        }

        @Override
        protected String getBaseUrl() {
            String url = MirroredDownloadPlugin.getDownloadUrl();
            if ("xapi?".equals(url.substring(url.length() - 5, url.length())))
                return url.substring(0, url.length() - 5);
            else
                return url;
        }
        
        private String completeOverpassQuery(String query)
        {
            int firstColon = query.indexOf(";");
            if (firstColon == -1)
                return "[bbox];" + query;
            int bboxPos = query.indexOf("[bbox");
            if (bboxPos > -1 && bboxPos < firstColon)
                return query;
                
            int bracketCount = 0;
            int pos = 0;
            for (; pos < firstColon; ++pos)
            {
                if (query.charAt(pos) == '[')
                    ++bracketCount;
                else if (query.charAt(pos) == '[')
                    --bracketCount;
                else if (bracketCount == 0)
                {
                    if (!Character.isWhitespace(query.charAt(pos)))
                        break;
                }
            }
            
            if (pos < firstColon)
                // We start with a statement, not with declarations
                return "[bbox];" + query;
                
            // We start with declarations. Add just one more declaration in this case.
            return "[bbox]" + query;
        }

        @Override
        protected String getRequestForBbox(double lon1, double lat1, double lon2, double lat2) {
            if (overpassQuery.isEmpty())
                return "" + super.getRequestForBbox(lon1, lat1, lon2, lat2);
            else
            {
                String realQuery = completeOverpassQuery(overpassQuery);
                String url = "interpreter?data=" + realQuery + "&bbox=" + lon1 + "," + lat1 + "," + lon2 + "," + lat2;
                try
                {
                    url = "interpreter?data=" + URLEncoder.encode(realQuery, "UTF-8") + "&bbox=" + lon1 + "," + lat1 + "," + lon2 + "," + lat2;
                }
                catch (UnsupportedEncodingException e) {}
                return url;
            }
        }

        @Override
        public DataSet parseOsm(ProgressMonitor progressMonitor) throws OsmTransferException {

            DataSet ds = super.parseOsm(progressMonitor);

            // add bounds if necessary (note that Overpass API does not return bounds in the response XML)
            if (ds != null && ds.dataSources.isEmpty()) {
                if (crosses180th) {
                    Bounds bounds = new Bounds(lat1, lon1, lat2, 180.0);
                    DataSource src = new DataSource(bounds, MirroredDownloadPlugin.getDownloadUrl());
                    ds.dataSources.add(src);

                    bounds = new Bounds(lat1, -180.0, lat2, lon2);
                    src = new DataSource(bounds, MirroredDownloadPlugin.getDownloadUrl());
                    ds.dataSources.add(src);
                } else {
                    Bounds bounds = new Bounds(lat1, lon1, lat2, lon2);
                    DataSource src = new DataSource(bounds, MirroredDownloadPlugin.getDownloadUrl());
                    ds.dataSources.add(src);
                }
            }

            return ds;
        }
    }
}
