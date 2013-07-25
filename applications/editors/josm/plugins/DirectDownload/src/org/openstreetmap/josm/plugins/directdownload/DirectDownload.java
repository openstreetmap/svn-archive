package org.openstreetmap.josm.plugins.directdownload;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.io.InputStream;
import java.net.URL;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.markerlayer.MarkerLayer;
import org.openstreetmap.josm.io.GpxReader;
import org.openstreetmap.josm.io.OsmApi;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.xml.sax.SAXException;

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

            if ((go.getValue() == 1) && (track != null)) {
                String urlString = OsmApi.getOsmApi().getBaseUrl() + "gpx/" + track.id + "/data";

                try {
                    URL trackDataUrl = new URL(urlString);
                    InputStream is = trackDataUrl.openStream();

                    GpxReader r = new GpxReader(is);
                    boolean parsedProperly = r.parse(true);
                    GpxLayer gpxLayer = new GpxLayer(r.getGpxData(), track.filename, true);

                    if (r.getGpxData().hasRoutePoints() || r.getGpxData().hasTrackPoints()) {
                        Main.main.addLayer(gpxLayer);
                    }

                    if (Main.pref.getBoolean("marker.makeautomarkers", true) && !r.getGpxData().waypoints.isEmpty()) {
                        MarkerLayer ml = new MarkerLayer(r.getGpxData(), tr("Markers from {0}", track.filename), null, gpxLayer);
                        if (ml.data.size() > 0) {
                            Main.main.addLayer(ml);
                        }
                    }
                    if (!parsedProperly) {
                        JOptionPane.showMessageDialog(Main.parent,
                            tr("Error occurred while parsing gpx file {0}. Only a part of the file will be available.", urlString));
                    }

                } catch (java.net.MalformedURLException e) {
                    JOptionPane.showMessageDialog(Main.parent,
                            tr("Invalid URL {0}", urlString));
                } catch (java.io.IOException e) {
                    JOptionPane.showMessageDialog(Main.parent,
                            tr("Error fetching URL {0}", urlString));
                } catch (SAXException e) {
                    JOptionPane.showMessageDialog(Main.parent,
                            tr("Error parsing data from URL {0}", urlString));
                }
            }
        }
    }
}
