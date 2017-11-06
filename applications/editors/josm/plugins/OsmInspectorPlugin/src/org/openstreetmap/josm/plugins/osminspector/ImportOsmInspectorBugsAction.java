package org.openstreetmap.josm.plugins.osminspector;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.NoSuchElementException;

import javax.swing.ProgressMonitor;

import org.opengis.referencing.FactoryException;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Shortcut;

import com.vividsolutions.jts.io.ParseException;

public class ImportOsmInspectorBugsAction extends JosmAction {
	OsmInspectorPlugin plugin;
	/**
	 * 
	 */
	private static final long serialVersionUID = -6484182416189079287L;

	public ImportOsmInspectorBugsAction(OsmInspectorPlugin thePlugin) {
		super(tr("Import Osm Inspector Bugs..."), "importosmibugs",
				tr("Import Osm Inspector Bugs..."), Shortcut.registerShortcut("importosmibugs",
						tr("Edit: {0}", tr("Import Osm Inspector Bugs...")),
						KeyEvent.VK_O, Shortcut.ALT_CTRL), true);
		putValue("help", ht("/Action/ImportOsmInspectorBugs"));
		plugin = thePlugin;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (isEnabled()) {
		    MapView mapView = MainApplication.getMap().mapView;
			ProgressMonitor monitor = new ProgressMonitor(mapView,
					"Querying WFS Geofabrik", "Dowloading features", 0, 100);

			try {
				Bounds bounds = mapView.getLatLonBounds(mapView.getBounds());

				Logging.info("OSMI View bounds" + bounds);

				monitor.setProgress(10);

				OsmInspectorLayer inspector = plugin.getLayer();
				if (inspector == null) {
					GeoFabrikWFSClient wfs = new GeoFabrikWFSClient(bounds);
					wfs.initializeDataStore();
					inspector = new OsmInspectorLayer(wfs, monitor);
					MainApplication.getLayerManager().addLayer(inspector);
					plugin.setLayer(inspector);
				} else {
					GeoFabrikWFSClient wfs = new GeoFabrikWFSClient(bounds);
					wfs.initializeDataStore();
					inspector.loadFeatures(wfs);

				}
			} catch (IOException | IndexOutOfBoundsException | NoSuchElementException | FactoryException | ParseException e) {
				Logging.error(e);
			} finally {
				monitor.close();
				if (plugin.getLayer() != null) {
					plugin.getLayer().updateView();
				}
			}
		} else {
		    Logging.warn("Osm Inspector Action not enabled");
		}
	}

}
