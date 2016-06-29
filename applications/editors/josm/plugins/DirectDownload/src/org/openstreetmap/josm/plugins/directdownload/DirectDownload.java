package org.openstreetmap.josm.plugins.directdownload;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.markerlayer.MarkerLayer;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class DirectDownload extends Plugin {
    private DownloadAction openaction;

    /**
     * Will be invoked by JOSM to bootstrap the plugin
     *
     * @param info  information about the plugin and its local installation
     */
    public DirectDownload(PluginInformation info) {
        super(info);

        openaction = new DownloadAction();
        MainMenu.add(Main.main.menu.gpsMenu, openaction);
    }

    class DownloadAction extends JosmAction {
        public DownloadAction() {
            super(tr("Download Track ..."), "DownloadAction",
                    tr("Download GPX track from openstreetmap.org"), null, false);
        }

        public void actionPerformed(ActionEvent event) {
            DownloadDataGui go = new DownloadDataGui();
            go.setVisible(true);

            UserTrack track = go.getSelectedUserTrack();

            if (!((go.getValue() == 1) && (track != null))) {
                return;
            }

            final GpxData data = new GpxServerReader().loadGpx(Long.parseLong(track.id));
            if (data == null) {
                return;
            }
            final GpxLayer gpxLayer = new GpxLayer(data);

            if (data.hasRoutePoints() || data.hasTrackPoints()) {
                Main.getLayerManager().addLayer(gpxLayer);
            }

            if (Main.pref.getBoolean("marker.makeautomarkers", true) && !data.waypoints.isEmpty()) {
                MarkerLayer ml = new MarkerLayer(data, tr("Markers from {0}", track.filename), null, gpxLayer);
                if (ml.data.size() > 0) {
                    Main.getLayerManager().addLayer(ml);
                }
            }
        }
    }
}
