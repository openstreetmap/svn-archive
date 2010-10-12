package org.openstreetmap.josm.plugins.slippymap;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.gui.jmapviewer.OsmTileSource.CycleMap;
import org.openstreetmap.gui.jmapviewer.OsmTileSource.Mapnik;
import org.openstreetmap.gui.jmapviewer.OsmTileSource.TilesAtHome;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Preferences.PreferenceChangeEvent;
import org.openstreetmap.josm.data.Preferences.PreferenceChangedListener;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.bbox.SlippyMapBBoxChooser;
import org.openstreetmap.josm.gui.bbox.SlippyMapBBoxChooser.TileSourceProvider;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * Main class for the slippy map plugin.
 *
 * @author Frederik Ramm <frederik@remote.org>
 *
 */
public class SlippyMapPlugin extends Plugin implements PreferenceChangedListener
{
	public SlippyMapPlugin(PluginInformation info)
	{
		super(info);
		Main.pref.addPreferenceChangeListener(this);
		SlippyMapBBoxChooser.addTileSourceProvider(new TileSourceProvider() {
			public List<TileSource> getTileSources() {
				List<TileSource> result = new ArrayList<TileSource>();
				for (TileSource ts: SlippyMapPreferences.getAllMapSources()) {
					if (ts instanceof Mapnik || ts instanceof CycleMap || ts instanceof TilesAtHome) {
						continue; // Already included in default list
					}
					result.add(ts);
				}
				return result;
			}
		});
	}

	@Override
	public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame)
	{
		if (newFrame != null && SlippyMapPreferences.getMapSource() != SlippyMapPreferences.NO_DEFAULT_TILE_SOURCE) {
			SlippyMapLayer smlayer;
			smlayer = new SlippyMapLayer();
			Main.main.addLayer(smlayer);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.openstreetmap.josm.plugins.Plugin#getPreferenceSetting()
	 */
	@Override
	public PreferenceSetting getPreferenceSetting()
	{
		return new SlippyMapPreferenceSetting();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seeorg.openstreetmap.josm.data.Preferences.PreferenceChangedListener#
	 * preferenceChanged(java.lang.String, java.lang.String)
	 */
	public void preferenceChanged(PreferenceChangeEvent event) {
		if (!Main.isDisplayingMapView()) {
			return;
		}
		List<SlippyMapLayer> layes = Main.map.mapView.getLayersOfType(SlippyMapLayer.class);
		assert layes.size() <= 1;
		SlippyMapLayer layer = layes.isEmpty()?null:layes.get(0);

		if (event.getKey().equals(SlippyMapPreferences.PREFERENCE_TILE_SOURCE)) {
			if (layer == null && SlippyMapPreferences.getMapSource() != SlippyMapPreferences.NO_DEFAULT_TILE_SOURCE) {
				Main.map.mapView.addLayer(new SlippyMapLayer());
			} else if (layer != null && SlippyMapPreferences.getMapSource() == SlippyMapPreferences.NO_DEFAULT_TILE_SOURCE) {
				Main.map.mapView.removeLayer(layer);
			} else if (layer == null && SlippyMapPreferences.getMapSource() == SlippyMapPreferences.NO_DEFAULT_TILE_SOURCE) {
				// Do nothing
			} else {
				layer.newTileStorage();
			}
		} else  if (event.getKey().startsWith(SlippyMapPreferences.PREFERENCE_PREFIX) && layer != null) {
			// System.err.println(this + ".preferenceChanged('" + key + "', '"
			// + newValue + "') called");
			// when fade background changed, no need to clear tile storage
			// TODO move this code to SlippyMapPreferences class.
			if (!event.getKey().equals(SlippyMapPreferences.PREFERENCE_FADE_BACKGROUND)) {
				layer.autoZoomPopup.setSelected(SlippyMapPreferences.getAutozoom());
			}
			layer.redraw();
		}
	}

}
