package org.openstreetmap.josm.plugins.directdownload;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.InputStream;
import java.io.IOException;

import java.net.URL;

import java.util.List;

import java.awt.event.ActionEvent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;

import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

import org.openstreetmap.josm.gui.download.DownloadSelection;

import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.markerlayer.MarkerLayer;

import org.openstreetmap.josm.io.GpxReader;

import javax.swing.JOptionPane;

import org.xml.sax.SAXException;

public class DirectDownload extends Plugin {
    DownloadAction openaction;

     /**
      * Will be invoked by JOSM to bootstrap the plugin
      *
      * @param info  information about the plugin and its local installation    
      */
      public DirectDownload(PluginInformation info) {
         super(info);

	 openaction = new DownloadAction();
	 Main.main.menu.fileMenu.add(openaction);
      }

    class DownloadAction extends JosmAction{
        public DownloadAction(){
            super(tr("Download Track ..."), "DownloadAction", tr("Download GPX track from openstreetmap.org"),
            null, false);
        }
        public void actionPerformed(ActionEvent event) {
            DownloadDataGui go = new DownloadDataGui();
            go.setVisible(true);

	    UserTrack track = go.getSelectedUserTrack();

	    if ((go.getValue() == 1) && (track != null)) {
		String apiBaseUrl = "http://api.openstreetmap.org/api/0.6/";
		String urlString = apiBaseUrl + "gpx/" + track.id + "/data";

		try {
		    URL trackDataUrl = new URL(urlString);
		    InputStream is = trackDataUrl.openStream();

		    GpxReader r = new GpxReader(is);
		    boolean parsedProperly = r.parse(true);
		    GpxLayer gpxLayer = new GpxLayer(r.data, track.filename, true);

                    if (r.data.hasRoutePoints() || r.data.hasTrackPoints()) {
                        Main.main.addLayer(gpxLayer);
                    }

                    if (Main.pref.getBoolean("marker.makeautomarkers", true) && !r.data.waypoints.isEmpty()) {
                        MarkerLayer ml = new MarkerLayer(r.data, tr("Markers from {0}", track.filename), null, gpxLayer);
                        if (ml.data.size() > 0) {
                            Main.main.addLayer(ml);
                        }
                    }
                    if (!parsedProperly) {
                        JOptionPane.showMessageDialog(null, tr("Error occurred while parsing gpx file {0}. Only a part of the file will be available.", urlString));
                    }

		} catch (java.net.MalformedURLException e) {
		    JOptionPane.showMessageDialog(null, tr("Invalid URL {0}", urlString));
		} catch (java.io.IOException e) {
		    JOptionPane.showMessageDialog(null, tr("Error fetching URL {0}", urlString));
		} catch (SAXException e) {
		    JOptionPane.showMessageDialog(null, tr("Error parsing data from URL {0}", urlString));
		}
	    }
        }
    }

}

