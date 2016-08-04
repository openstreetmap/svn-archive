// License: GPL. For details, see LICENSE file.
package com.innovant.josm.plugin.routing;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
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
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerAddEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerOrderChangeEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerRemoveEvent;
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
public class RoutingPlugin extends Plugin implements LayerChangeListener, DataSetListenerAdapter.Listener {
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
    private RoutingDialog routingDialog;

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
        File log4jConfigFile = new java.io.File("log4j.xml");
        if (log4jConfigFile.exists()) {
            DOMConfigurator.configure(log4jConfigFile.getPath());
        } else {
            System.err.println("Routing plugin warning: log4j configuration not found");
        }
        logger.debug("Loading routing plugin...");
        preferenceSettings = new RoutingPreferenceDialog();
        // Initialize layers list
        layers = new ArrayList<>();
        // Add menu
        menu = new RoutingMenu();
        // Register this class as LayerChangeListener
        Main.getLayerManager().addLayerChangeListener(this);
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
        OsmDataLayer osmLayer = Main.getLayerManager().getEditLayer();
        if (osmLayer != null) {
            RoutingLayer layer = new RoutingLayer(tr("Routing") + " [" + osmLayer.getName() + "]", osmLayer);
            layers.add(layer);
            Main.getLayerManager().addLayer(layer);
        }
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (newFrame != null) {
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
            newFrame.addToggleDialog(routingDialog = new RoutingDialog());
        } else {
            addRouteNodeAction = null;
            removeRouteNodeAction = null;
            moveRouteNodeAction = null;
            addRouteNodeButton = null;
            removeRouteNodeButton = null;
            moveRouteNodeButton = null;
            routingDialog = null;
        }
    }

    public void activeLayerChange(Layer oldLayer, Layer newLayer) {
        if (newLayer instanceof RoutingLayer) {            /*   show Routing toolbar and dialog window  */
            menu.enableRestOfItems();
            if (routingDialog != null) {
                routingDialog.showDialog();
                routingDialog.refresh();
            }
        } else {                                           /*   hide Routing toolbar and dialog window  */
            menu.disableRestOfItems();
            if (routingDialog != null) {
                routingDialog.hideDialog();
            }
        }
    }

    @Override
    public void layerOrderChanged(LayerOrderChangeEvent e) {
        // Do nothing
    }

    @Override
    public void layerAdded(LayerAddEvent evt) {
        Layer newLayer = evt.getAddedLayer();
        // Add button(s) to the tool bar when the routing layer is added
        if (newLayer instanceof RoutingLayer) {
            menu.enableRestOfItems();
            // Set layer on top and select layer, also refresh toggleDialog to reflect selection
            Main.map.mapView.moveLayer(newLayer, 0);
            logger.debug("Added routing layer.");
        }
    }

    @Override
    public void layerRemoving(LayerRemoveEvent evt) {
        Layer oldLayer = evt.getRemovedLayer();
        if ((oldLayer instanceof RoutingLayer) & (layers.size() == 1)) {
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
            for (int i = 0; i < layersArray.length; i++) {
                if (layersArray[i].getDataLayer().equals(oldLayer)) {
                    try {
                        // Remove layer
                        Main.getLayerManager().removeLayer(layersArray[i]);
                    } catch (IllegalArgumentException e) {
                        Main.error(e);
                    }
                }
            }
        }
        // Reload RoutingDialog table model
        if (routingDialog != null) {
            routingDialog.refresh();
        }
    }

    @Override
    public void processDatasetEvent(AbstractDatasetChangedEvent event){

    }

    @Override
    public PreferenceSetting getPreferenceSetting() {
        return preferenceSettings;
    }
}
