package org.openstreetmap.josm.plugins.rasterfilters;

import java.awt.Container;
import java.io.File;
import java.io.IOException;

import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.layer.ImageryLayer;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerAddEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerOrderChangeEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerRemoveEvent;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeEvent;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeListener;
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
public class RasterFiltersPlugin extends Plugin implements LayerChangeListener, ActiveLayerChangeListener {

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
			Main.getLayerManager().addLayerChangeListener(this);
            Main.getLayerManager().addActiveLayerChangeListener(this);
		}
	}

	@Override
	public void activeOrEditLayerChanged(ActiveLayerChangeEvent e) {
		if (!(Main.getLayerManager().getActiveLayer() instanceof ImageryLayer)) {
			filterButton.setEnabled(false);
		} else {
			filterButton.setEnabled(true);
		}
	}

	@Override
	public void layerAdded(LayerAddEvent e) {

		if (filterButton == null) {

			// filter reading and adding to the collections of FilterDownloader
			FiltersDownloader.downloadFiltersInfoList();
			FiltersDownloader.initFilters();

			if (action == null) {
				action = new ShowLayerFiltersDialog();
			}

			if (e.getAddedLayer() instanceof ImageryLayer) {
				filterButton = new SideButton(action, false);
				filterButton.setEnabled(true);
			} else {
				filterButton = new SideButton(action, false);
				filterButton.setEnabled(false);
			}

			LayerListDialog dialog = LayerListDialog.getInstance();

			JPanel buttonRowPanel = (JPanel) ((JPanel) dialog.getComponent(2)).getComponent(0);
			buttonRowPanel.add(filterButton);
		}

		if (e.getAddedLayer() instanceof ImageryLayer) {
			FiltersDialog dialog = new FiltersDialog((ImageryLayer) e.getAddedLayer());
			action.addFiltersDialog(dialog);
		}

	}

	@Override
	public void layerRemoving(LayerRemoveEvent e) {

		if (e.getRemovedLayer() instanceof ImageryLayer) {
			FiltersDialog dialog = action.getDialogByLayer(e.getRemovedLayer());
			((ImageryLayer) e.getRemovedLayer()).removeImageProcessor(dialog.getFiltersManager());
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
    public void layerOrderChanged(LayerOrderChangeEvent e) {
        // Do nothing
    }

    @Override
	public PreferenceSetting getPreferenceSetting() {
		if (setting == null) {
			setting = new RasterFiltersPreferences();
		}

		return setting;
	}
}
