/*
 * Copyright (C) 2008 Innovant
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 *
 * For more information, please contact:
 *
 *  Innovant
 *   juangui@gmail.com
 *   vidalfree@gmail.com
 *
 *  http://public.grupoinnovant.com/blog
 *
 */

package com.innovant.josm.plugin.routing;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.event.AbstractDatasetChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataSetListenerAdapter;
import org.openstreetmap.josm.data.osm.event.DatasetEventManager;
import org.openstreetmap.josm.data.osm.event.DatasetEventManager.FireMode;
import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

import com.innovant.josm.plugin.routing.actions.AddRouteNodeAction;
import com.innovant.josm.plugin.routing.actions.MoveRouteNodeAction;
import com.innovant.josm.plugin.routing.actions.RemoveRouteNodeAction;
import com.innovant.josm.plugin.routing.gui.RoutingDialog;
import com.innovant.josm.plugin.routing.gui.RoutingMenu;
import com.innovant.josm.plugin.routing.gui.RoutingPreferenceDialog;

/**
 * The main class of the routing plugin
 * @author juangui
 * @author Jose Vidal
 * @author cdaller
 *
 * @version 0.3
 */
public class RoutingPlugin extends Plugin implements LayerChangeListener,DataSetListenerAdapter.Listener {
	/**
	 * Logger
	 */
	static Logger logger = Logger.getLogger(RoutingPlugin.class);

	/**
	 * The list of routing layers
	 */
	private final ArrayList<RoutingLayer> layers;

	/**
	 * The side dialog where nodes are listed
	 */
	private final RoutingDialog routingDialog;

	/**
	 * Preferences Settings Dialog.
	 */
	private final PreferenceSetting preferenceSettings;

	/**
	 * MapMode for adding route nodes.
	 * We use this field to enable or disable the mode automatically.
	 */
	private AddRouteNodeAction addRouteNodeAction;

	/**
	 * MapMode for removing route nodes.
	 * We use this field to enable or disable the mode automatically.
	 */
	private RemoveRouteNodeAction removeRouteNodeAction;

	/**
	 * MapMode for moving route nodes.
	 * We use this field to enable or disable the mode automatically.
	 */
	private MoveRouteNodeAction moveRouteNodeAction;

	/**
	 * IconToggleButton for adding route nodes, we use this field to show or hide the button.
	 */
	private IconToggleButton addRouteNodeButton;

	/**
	 * IconToggleButton for removing route nodes, we use this field to show or hide the button.
	 */
	private IconToggleButton removeRouteNodeButton;

	/**
	 * IconToggleButton for moving route nodes, we use this field to show or hide the button.
	 */
	private IconToggleButton moveRouteNodeButton;

	/**
	 * IconToggleButton for moving route nodes, we use this field to show or hide the button.
	 */
	private final RoutingMenu menu;

	/**
	 * Reference for the plugin class (as if it were a singleton)
	 */
	private static RoutingPlugin plugin;

	private final DataSetListenerAdapter datasetAdapter;

	/**
	 * Default Constructor
	 */
	public RoutingPlugin(PluginInformation info) {
		super(info);

		datasetAdapter = new DataSetListenerAdapter(this);
		plugin = this; // Assign reference to the plugin class
		if (new java.io.File("log4j.xml").exists()) {
			DOMConfigurator.configure("log4j.xml");
		} else {
			System.err.println("Routing plugin warning: log4j configuration not found");
		}
		logger.debug("Loading routing plugin...");
		preferenceSettings=new RoutingPreferenceDialog();
		// Create side dialog
		routingDialog = new RoutingDialog();
		// Initialize layers list
		layers = new ArrayList<RoutingLayer>();
		// Add menu
		menu = new RoutingMenu();
		// Register this class as LayerChangeListener
		MapView.addLayerChangeListener(this);
		DatasetEventManager.getInstance().addDatasetListener(datasetAdapter, FireMode.IN_EDT_CONSOLIDATED);
		logger.debug("Finished loading plugin");
	}

	/**
	 * Provides static access to the plugin instance, to enable access to the plugin methods
	 * @return the instance of the plugin
	 */
	public static RoutingPlugin getInstance() {
		return plugin;
	}

	/**
	 * Get the routing side dialog
	 * @return The instance of the routing side dialog
	 */
	public RoutingDialog getRoutingDialog() {
		return routingDialog;
	}

	public void addLayer() {
		OsmDataLayer osmLayer = Main.map.mapView.getEditLayer();
		RoutingLayer layer = new RoutingLayer(tr("Routing") + " [" + osmLayer.getName() + "]", osmLayer);
		layers.add(layer);
		Main.main.addLayer(layer);
	}

	/*
	 * (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.Plugin#mapFrameInitialized(org.openstreetmap.josm.gui.MapFrame, org.openstreetmap.josm.gui.MapFrame)
	 */
	@Override
	public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
		if(newFrame != null) {
			// Create plugin map modes
			addRouteNodeAction = new AddRouteNodeAction(newFrame);
			removeRouteNodeAction = new RemoveRouteNodeAction(newFrame);
			moveRouteNodeAction = new MoveRouteNodeAction(newFrame);
			// Create plugin buttons and add them to the toolbar
			addRouteNodeButton = new IconToggleButton(addRouteNodeAction);
			removeRouteNodeButton = new IconToggleButton(removeRouteNodeAction);
			moveRouteNodeButton = new IconToggleButton(moveRouteNodeAction);
			addRouteNodeButton.setAutoHideDisabledButton(true);
			removeRouteNodeButton.setAutoHideDisabledButton(true);
			moveRouteNodeButton.setAutoHideDisabledButton(true);
			newFrame.addMapMode(addRouteNodeButton);
			newFrame.addMapMode(removeRouteNodeButton);
			newFrame.addMapMode(moveRouteNodeButton);
			// Enable menu
			menu.enableStartItem();
			newFrame.addToggleDialog(routingDialog);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.openstreetmap.josm.gui.layer.Layer.LayerChangeListener#activeLayerChange(org.openstreetmap.josm.gui.layer.Layer, org.openstreetmap.josm.gui.layer.Layer)
	 */
	public void activeLayerChange(Layer oldLayer, Layer newLayer) {
		if (newLayer instanceof RoutingLayer) {			/*   show Routing toolbar and dialog window  */
			menu.enableRestOfItems();
			routingDialog.showDialog();
			routingDialog.refresh();
		}else{											/*   hide Routing toolbar and dialog window  */
			menu.disableRestOfItems();
			routingDialog.hideDialog();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.openstreetmap.josm.gui.layer.Layer.LayerChangeListener#layerAdded(org.openstreetmap.josm.gui.layer.Layer)
	 */
	public void layerAdded(Layer newLayer) {
		// Add button(s) to the tool bar when the routing layer is added
		if (newLayer instanceof RoutingLayer) {
			menu.enableRestOfItems();
			// Set layer on top and select layer, also refresh toggleDialog to reflect selection
			Main.map.mapView.moveLayer(newLayer, 0);
			logger.debug("Added routing layer.");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.openstreetmap.josm.gui.layer.Layer.LayerChangeListener#layerRemoved(org.openstreetmap.josm.gui.layer.Layer)
	 */
	public void layerRemoved(Layer oldLayer) {
		if ((oldLayer instanceof RoutingLayer) & (layers.size()==1)) {
			// Remove button(s) from the tool bar when the last routing layer is removed
			addRouteNodeButton.setVisible(false);
			removeRouteNodeButton.setVisible(false);
			moveRouteNodeButton.setVisible(false);
			menu.disableRestOfItems();
			layers.remove(oldLayer);
			logger.debug("Removed routing layer.");
		} else if (oldLayer instanceof OsmDataLayer) {
			// Remove all associated routing layers
			// Convert to Array to prevent ConcurrentModificationException when removing layers from ArrayList
			// FIXME: can't remove associated routing layers without triggering exceptions in some cases
			RoutingLayer[] layersArray = layers.toArray(new RoutingLayer[0]);
			for (int i=0;i<layersArray.length;i++) {
				if (layersArray[i].getDataLayer().equals(oldLayer)) {
					try {
						// Remove layer
						Main.map.mapView.removeLayer(layersArray[i]);
					} catch (IllegalArgumentException e) {
					}
				}
			}
		}
		// Reload RoutingDialog table model
		routingDialog.refresh();
	}

	public void processDatasetEvent(AbstractDatasetChangedEvent event){


	}
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.Plugin#getPreferenceSetting()
	 */
	@Override
	public PreferenceSetting getPreferenceSetting() {
		return preferenceSettings;
	}
}
