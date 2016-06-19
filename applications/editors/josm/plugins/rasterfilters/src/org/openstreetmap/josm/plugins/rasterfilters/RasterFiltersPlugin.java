package org.openstreetmap.josm.plugins.rasterfilters;

import java.awt.Container;
import java.io.File;
import java.io.IOException;

import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.layer.ImageryLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.rasterfilters.actions.ShowLayerFiltersDialog;
import org.openstreetmap.josm.plugins.rasterfilters.gui.FiltersDialog;
import org.openstreetmap.josm.plugins.rasterfilters.preferences.FiltersDownloader;
import org.openstreetmap.josm.plugins.rasterfilters.preferences.RasterFiltersPreferences;
/**
 * Main Plugin class. This class embed new plugin button for adding filter and
 * subtab in Preferences menu
 *
 * @author Nipel-Crumple
 *
 */
public class RasterFiltersPlugin extends Plugin implements LayerChangeListener {

	private SideButton filterButton;
	private ShowLayerFiltersDialog action;
	private PreferenceSetting setting;

	public RasterFiltersPlugin(PluginInformation info) {
		super(info);
		Main.debug("Loading RasterFiltersPlugin");

		File file = new File(getPluginDir());
		if (file.mkdir()) {

			// opening file with last user's settings
			file = new File(file.getAbsoluteFile(), "urls.map");
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					Main.debug("Cannot create file" + file.getAbsolutePath() + "\n" + e.getMessage());
				}
			}
		}

		FiltersDownloader.setPluginDir(getPluginDir());
	}

	@Override
	public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
		if (Main.isDisplayingMapView()) {
			MapView.addLayerChangeListener(this);
		}
	}

	@Override
	public void activeLayerChange(Layer oldLayer, Layer newLayer) {
		if (!(newLayer instanceof ImageryLayer)) {
			filterButton.setEnabled(false);
		} else {
			filterButton.setEnabled(true);
		}

	}

	@Override
	public void layerAdded(Layer newLayer) {

		if (filterButton == null) {

			// filter reading and adding to the collections of
			// FilterDownloader
			FiltersDownloader.downloadFiltersInfoList();
			FiltersDownloader.initFilters();

			if (action == null) {
				action = new ShowLayerFiltersDialog();
			}

			if (newLayer instanceof ImageryLayer) {
				filterButton = new SideButton(action, false);
				filterButton.setEnabled(true);
			} else {
				filterButton = new SideButton(action, false);
				filterButton.setEnabled(false);
			}

			LayerListDialog dialog = LayerListDialog.getInstance();

			JPanel buttonRowPanel = (JPanel) ((JPanel) dialog.getComponent(2))
					.getComponent(0);
			buttonRowPanel.add(filterButton);
		}

		if (newLayer instanceof ImageryLayer) {
			FiltersDialog dialog = new FiltersDialog((ImageryLayer) newLayer);
			action.addFiltersDialog(dialog);
		}

	}

	@Override
	public void layerRemoved(Layer oldLayer) {

		if (oldLayer instanceof ImageryLayer) {
			FiltersDialog dialog = action.getDialogByLayer(oldLayer);
			((ImageryLayer) oldLayer).removeImageProcessor(dialog.getFiltersManager());
			dialog.closeFrame();
			action.removeFiltersDialog(dialog);
		}

		if (Main.getLayerManager().getLayers().isEmpty()) {

			Container container = filterButton.getParent();
			if (container != null)
				container.remove(filterButton);
			
			FiltersDownloader.destroyFilters();
			filterButton = null;

		}
	}

	@Override
	public PreferenceSetting getPreferenceSetting() {
		if (setting == null) {
			setting = new RasterFiltersPreferences();
		}

		return setting;
	}

}
