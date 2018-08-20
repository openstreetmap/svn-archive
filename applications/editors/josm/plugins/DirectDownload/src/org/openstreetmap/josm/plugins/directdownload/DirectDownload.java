// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.directdownload;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.gpx.GpxConstants;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.data.gpx.GpxTrack;
import org.openstreetmap.josm.data.gpx.ImmutableGpxTrack;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.markerlayer.MarkerLayer;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.spi.preferences.Config;

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
        MainMenu.add(MainApplication.getMenu().gpsMenu, openaction);
    }

    static class DownloadAction extends JosmAction {
        DownloadAction() {
            super(tr("Download Track ..."), "DownloadAction",
                    tr("Download GPX track from openstreetmap.org"), null, false);
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            DownloadDataGui go = new DownloadDataGui();
            go.setVisible(true);

            ArrayList<UserTrack> tracks = go.getSelectedUserTracks();

            if (!((go.getValue() == 1) && (tracks != null))) {
                return;
            }

            for (UserTrack track: tracks) {

                GpxData data = new GpxServerReader().loadGpx(Long.parseLong(track.id));
                if (data == null) {
                    return;
                }
                GpxData dataNew = new GpxData();

                for (GpxTrack trk : data.getTracks()) {
                    HashMap<String, Object> attrib = new HashMap<>(trk.getAttributes());
                    if (!trk.getAttributes().containsKey(GpxConstants.GPX_NAME)) {
                        attrib.put(GpxConstants.GPX_NAME, track.filename);
                    }
                    if (!trk.getAttributes().containsKey(GpxConstants.GPX_DESC)) {
                        attrib.put(GpxConstants.GPX_DESC, track.description);
                    }
                    // replace the existing trace in the unmodifiable tracks
                    dataNew.addTrack(new ImmutableGpxTrack(new ArrayList<>(trk.getSegments()), attrib));
                }

                data = dataNew;

                final GpxLayer gpxLayer = new GpxLayer(data, (track.filename + " " + track.description).trim());

                if (data.hasRoutePoints() || data.hasTrackPoints()) {
                    MainApplication.getLayerManager().addLayer(gpxLayer);
                }

                if (Config.getPref().getBoolean("marker.makeautomarkers", true) && !data.waypoints.isEmpty()) {
                    MarkerLayer ml = new MarkerLayer(data, tr("Markers from {0}", track.filename), null, gpxLayer);
                    if (ml.data.size() > 0) {
                        MainApplication.getLayerManager().addLayer(ml);
                    }
                }
            }
        }
    }
}
