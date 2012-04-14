// License: GPL. For details, see LICENSE file.
package mirrored_download;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.GridBagConstraints;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;

import org.openstreetmap.josm.actions.JosmAction;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.downloadtasks.DownloadOsmTask;
import org.openstreetmap.josm.actions.downloadtasks.PostDownloadHandler;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.DataSource;
import org.openstreetmap.josm.gui.download.DownloadDialog;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.gui.widgets.AbstractTextComponentValidator;
import org.openstreetmap.josm.gui.widgets.HistoryComboBox;
import org.openstreetmap.josm.io.BoundingBoxDownloader;
import org.openstreetmap.josm.io.OsmTransferException;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.Shortcut;

public class MirroredDownloadAction extends JosmAction {

    static final String XAPI_QUERY_HISTORY_KEY = "plugin.mirrored_download.query-history";
    static final String XAPI_QUERY_TOOLTIP = tr("XAPI query, e.g., '''' (to download all data), ''[highway=*]'', or ''[[network=VRR][ref=603|613]''");

    public MirroredDownloadAction() {
        super(tr("Download from OSM mirror..."), (String)null, tr("Download map data from the OSM server."),
                Shortcut.registerShortcut("mirror:download", tr("File: {0}", tr("Download from OSM mirror...")), KeyEvent.VK_DOWN, Shortcut.ALT_SHIFT),
                true, "mirroreddownload/download", true);
        putValue("help", ht("/Action/MirroredDownload"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        MirroredDownloadDialog dialog = MirroredDownloadDialog.getInstance();
        dialog.restoreSettings();
        dialog.setVisible(true);
        if (!dialog.isCanceled()) {
            dialog.rememberSettings();
            Bounds area = dialog.getSelectedDownloadArea();
            DownloadOsmTask task = new DownloadOsmTask();
            Future<?> future = task.download(
                    new MirroredDownloadReader(area, dialog.getOverpassType(), dialog.getOverpassQuery()),
                    dialog.isNewLayerRequired(), area, null);
            Main.worker.submit(new PostDownloadHandler(task, future));
        }
    }

    static class XAPIQueryValidator extends AbstractTextComponentValidator {

        static final Pattern pattern = Pattern.compile("^(\\[([^=]+=[^=]*)\\])*$");

        public XAPIQueryValidator(JTextComponent tc) throws IllegalArgumentException {
            super(tc);
        }

        @Override
        public void validate() {
            if (pattern.matcher(getComponent().getText().trim()).matches()) {
                feedbackValid(XAPI_QUERY_TOOLTIP);
            } else {
                feedbackInvalid(tr("This XAPI query seems to be invalid, please doublecheck"));
            }
        }

        @Override
        public boolean isValid() {
            return pattern.matcher(getComponent().getText().trim()).matches();
        }
    }

    static class MirroredDownloadDialog extends DownloadDialog {

        protected JComboBox/*<String>*/ overpassType;
        protected HistoryComboBox overpassQuery;
        private static MirroredDownloadDialog instance;

        private MirroredDownloadDialog(Component parent) {
            super(parent);
            cbDownloadOsmData.setEnabled(false);
            cbDownloadGpxData.setEnabled(false);
            cbStartup.setEnabled(false);
        }

        static public MirroredDownloadDialog getInstance() {
            if (instance == null) {
                instance = new MirroredDownloadDialog(Main.parent);
            }
            return instance;
        }

        @Override
        protected void buildMainPanelAboveDownloadSelections(JPanel pnl) {
            overpassType = new JComboBox/*<String>*/(new String[]{"*", "node", "way", "relation"});
            pnl.add(new JLabel(tr("Object type: ")), GBC.std().insets(5, 5, 5, 5));
            pnl.add(overpassType, GBC.eol().fill(GridBagConstraints.HORIZONTAL));
            overpassType.setToolTipText(tr("OSM object type to download (''*'' stands for any)"));
            overpassQuery = new HistoryComboBox();
            pnl.add(new JLabel(tr("XAPI query: ")), GBC.std().insets(5, 5, 5, 5));
            new XAPIQueryValidator((JTextComponent) overpassQuery.getEditor().getEditorComponent());
            pnl.add(overpassQuery, GBC.eol().fill(GridBagConstraints.HORIZONTAL));
            overpassQuery.setToolTipText(XAPI_QUERY_TOOLTIP);
        }

        public String getOverpassQuery() {
            return overpassQuery.getText();
        }

        public String getOverpassType() {
            return (String)overpassType.getItemAt(overpassType.getSelectedIndex());
        }

        @Override
        public void restoreSettings() {
            super.restoreSettings();
            overpassQuery.setPossibleItems(
                    Main.pref.getCollection(XAPI_QUERY_HISTORY_KEY, new LinkedList<String>()));
        }

        @Override
        public void rememberSettings() {
            super.rememberSettings();
            overpassQuery.addCurrentItemToHistory();
            Main.pref.putCollection(XAPI_QUERY_HISTORY_KEY, overpassQuery.getHistory());
        }
    }

    static class MirroredDownloadReader extends BoundingBoxDownloader {

        final String overpassType;
        final String overpassQuery;

        public MirroredDownloadReader(Bounds downloadArea, String overpassType, String overpassQuery) {
            super(downloadArea);
            this.overpassType = overpassType;
            this.overpassQuery = overpassQuery.trim();
        }

        @Override
        protected String getBaseUrl() {
            return MirroredDownloadPlugin.getDownloadUrl();
        }

        @Override
        protected String getRequestForBbox(double lon1, double lat1, double lon2, double lat2) {
            return overpassQuery.isEmpty() && "*".equals(overpassType)
                    ? super.getRequestForBbox(lon1, lat1, lon2, lat2)
                    : overpassType + "[bbox=" + lon1 + "," + lat1 + "," + lon2 + "," + lat2 + "][@meta]" + overpassQuery;
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
