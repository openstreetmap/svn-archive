package org.openstreetmap.josm.plugins.gpxpoints;

import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.markerlayer.MarkerLayer;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * Main plugin class.
 * Registers handles for layerRemoved, layerAded to process newly added or removed waypoints.
 * @author larry0ua
 *
 */
public class GpxPointsPlugin extends Plugin{

	private final static GpxPointsPanel togglePanel = new GpxPointsPanel();

	public GpxPointsPlugin(PluginInformation info) {
		super(info);
		MapView.addLayerChangeListener(new MapView.LayerChangeListener() {

			@Override
			public void layerRemoved(Layer oldLayer) {
				if (oldLayer instanceof MarkerLayer) {
					// remove points given by deleted layer
					togglePanel.removePoints(((MarkerLayer) oldLayer).data);
				}
			}

			@Override
			public void layerAdded(Layer newLayer) {
				if (newLayer instanceof MarkerLayer) {
					// if added layer contains gpx data - add waypoints from this layer to TableModel
					togglePanel.addPoints(((MarkerLayer) newLayer).data);
				}
			}

			@Override
			public void activeLayerChange(Layer oldLayer, Layer newLayer) {
				// empty method to instantiate LayerChangeListener correctly
			}
		});
	}

	@Override
	public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
		super.mapFrameInitialized(oldFrame, newFrame);
		if (newFrame != null) {
			newFrame.addToggleDialog(togglePanel);
		}
	}

}
