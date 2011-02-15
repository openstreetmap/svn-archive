/* Copyright (c) 2010, skobbler GmbH
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the project nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.openstreetmap.josm.plugins.mapdust;


import static org.openstreetmap.josm.tools.I18n.tr;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.Preferences.PreferenceChangeEvent;
import org.openstreetmap.josm.data.Preferences.PreferenceChangedListener;
import org.openstreetmap.josm.gui.JosmUserIdentityManager;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.NavigatableComponent;
import org.openstreetmap.josm.gui.NavigatableComponent.ZoomChangeListener;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.mapdust.gui.MapdustGUI;
import org.openstreetmap.josm.plugins.mapdust.gui.component.dialog.CreateIssueDialog;
import org.openstreetmap.josm.plugins.mapdust.gui.observer.MapdustBugObserver;
import org.openstreetmap.josm.plugins.mapdust.gui.observer.MapdustInitialUpdateObserver;
import org.openstreetmap.josm.plugins.mapdust.gui.observer.MapdustRefreshObserver;
import org.openstreetmap.josm.plugins.mapdust.gui.value.MapdustPluginState;
import org.openstreetmap.josm.plugins.mapdust.service.MapdustServiceHandler;
import org.openstreetmap.josm.plugins.mapdust.service.MapdustServiceHandlerException;
import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustBug;
import org.openstreetmap.josm.tools.Shortcut;


/**
 * This is the main class of the MapDust plug-in. Defines the MapDust plug-in
 * main functionality.
 * 
 * @author Bea
 * 
 */
public class MapdustPlugin extends Plugin implements LayerChangeListener,
        ZoomChangeListener, PreferenceChangedListener, MouseListener,
        MapdustRefreshObserver, MapdustBugObserver,
        MapdustInitialUpdateObserver {
    
    /** The graphical user interface of the plug-in */
    private MapdustGUI mapdustGUI;
    
    /** The layer of the MapDust plug-in */
    private MapdustLayer mapdustLayer;
    
    /** The list of <code>MapdustBug</code> objects */
    private List<MapdustBug> mapdustBugList;
    
    /** The <code>CreateIssueDialog</code> object */
    private CreateIssueDialog dialog;
    
    /** Specifies if there was or not an error downloading the data */
    private boolean wasError = false;
    
    /** The JOSM user identity manager, it is used for obtaining the username */
    private final JosmUserIdentityManager userIdentityManager;
    
    /**
     * Builds a new <code>MapDustPlugin</code> object based on the given
     * arguments.
     * 
     * @param info The <code>MapDustPlugin</code> object
     */
    public MapdustPlugin(PluginInformation info) {
        super(info);
        /* create instance for JOSM user identity manager */
        userIdentityManager = JosmUserIdentityManager.getInstance();
        /* initialize the plugin */
        initializePlugin();
    }
    
    /**
     * Initialize the <code>MapdustPlugin</code> object. Creates the
     * <code>MapdustGUI</code> and initializes the following variables with
     * default values: 'mapdust.pluginState', 'mapdust.nickname',
     * 'mapdust.showError'.
     */
    private void initializePlugin() {
        /* create MapDust GUI */
        Shortcut shortcut = Shortcut.registerShortcut("mapdust", tr("Toggle: {0}", 
                tr("Open MapDust")), KeyEvent.VK_0, Shortcut.GROUP_MENU, 
                Shortcut.SHIFT_DEFAULT);
        String name = "MapDust bug reports";
        String tooltip = "Activates the MapDust bug reporter plugin";
        mapdustGUI = new MapdustGUI(tr(name), "mapdust_icon.png", tr(tooltip),
                shortcut, 150, this);
        /* add default values for static variables */
        Main.pref.put("mapdust.pluginState",
                MapdustPluginState.ONLINE.getValue());
        Main.pref.put("mapdust.nickname", "");
        Main.pref.put("mapdust.showError", true);
        Main.pref.put("mapdust.version", getPluginInformation().version);
        Main.pref.put("mapdust.localVersion",
                getPluginInformation().localversion);
    }
    
    /**
     * Initializes the new <code>MapFrame</code>. Adds the
     * <code>MapdustGUI</code> to the new <code>MapFrame</code> and sets the
     * observers/listeners.
     * 
     * @param oldMapFrame The old <code>MapFrame</code> object
     * @param newMapFrame The new <code>MapFrame</code> object
     */
    @Override
    public void mapFrameInitialized(MapFrame oldMapFrame, MapFrame newMapFrame) {
        if (newMapFrame == null) {
            /* if new MapFrame is null, remove listener */
            MapView.removeLayerChangeListener(this);
            NavigatableComponent.removeZoomChangeListener(this);
        } else {
            /* add MapDust dialog window */
            if (Main.map != null && Main.map.mapView != null) {
                /* set bounds for MapdustGUI */
                mapdustGUI.setBounds(newMapFrame.getBounds());
                /* add observer */
                mapdustGUI.addObserver(this);
                /* add dialog to new MapFrame */
                newMapFrame.addToggleDialog(mapdustGUI);
                /* add ZoomChangeListener */
                NavigatableComponent.addZoomChangeListener(this);
                /* add LayerChangeListener */
                MapView.addLayerChangeListener(this);
                /* add MouseListener */
                Main.map.mapView.addMouseListener(this);
                Main.pref.addPreferenceChangeListener(this);
                /* put username to preferences */
                Main.pref.put("mapdust.josmUserName", 
                        userIdentityManager.getUserName());
            }
        }
    }
    
    /**
     * Refreshes the MapDust data. Downloads the data from the given area and
     * updates the map and the MapDust list with this new data. This method is
     * called whenever the 'Refresh' button is pressed by the user.
     */
    @Override
    public void refreshData() {
        if (containsOsmDataLayer() && mapdustGUI.isShowing()) {
            updatePluginData();
        }
    }
    
    /**
     * Downloads the MapDust bugs from the current map view, and updates the
     * plugin data with the new downloaded data. This method is called only
     * once, before first showing the MapDust bugs.
     */
    @Override
    public void initialUpdate() {
        if (containsOsmDataLayer()) {
            updatePluginData();
        }
    }
    
    /**
     * Updates the given <code>MapdustBug</code> object from the map and from
     * the MapDust bugs list.
     * 
     * @param mapdustBug The <code>MapdustBug</code> object
     */
    @Override
    public synchronized void changedData(MapdustBug mapdustBug) {
        if (mapdustBugList == null) {
            mapdustBugList = new ArrayList<MapdustBug>();
        }
        if (getMapdustGUI().isDialogShowing()) {
            if (Main.map != null && Main.map.mapView != null) {
                /* if the layer was active , should be active after the update */
                boolean wasActive = false;
                if (Main.map.mapView.getActiveLayer() == getMapdustLayer()) {
                    wasActive = true;
                }
                /* update the list with the modified MapDust bug */
                updateMapdustBugList(mapdustBug);
                /* destroy the layer */
                mapdustLayer.destroy();
                Main.main.removeLayer(mapdustLayer);
                mapdustLayer = null;
                /* update the view, and activate the layer */
                updateView();
                if (wasActive) {
                    Main.map.mapView.setActiveLayer(getMapdustLayer());
                }
            }
        }
    }
    
    /**
     * If the zoom was changed, download the bugs from the current map view.
     * This method is called whenever the zoom was changed.
     */
    @Override
    public void zoomChanged() {
        if (containsOsmDataLayer() && this.mapdustGUI.isShowing() && !wasError) {
            updatePluginData();
        }
    }
    
    /**
     * No need to implement this.
     */
    @Override
    public void activeLayerChange(Layer arg0, Layer arg1) {}
    
    /**
     * Adds the <code>MapdustLayer</code> to the JOSM editor. If the list of
     * <code>MapdustBug</code>s is null then downloads the data from the MapDust
     * Service and updates the editor with this new data.
     * 
     * @param layer The <code>Layer</code> which will be added to the JOSM
     * editor
     */
    @Override
    public void layerAdded(Layer layer) {
        if (layer instanceof MapdustLayer) {
            /* download the MapDust bugs and update the plugin */
            if (mapdustBugList == null) {
                updateMapdustData();
            }
        }
    }
    
    /**
     * Removes the <code>MapdustLayer</code> from the JOSM editor. Also closes
     * the MapDust plugin window.
     * 
     * @param layer The <code>Layer</code> which will be removed from the JOSM
     * editor
     */
    @Override
    public void layerRemoved(Layer layer) {
        if (layer instanceof MapdustLayer) {
            /* remove the layer */
            MapView.removeLayerChangeListener(this);
            NavigatableComponent.removeZoomChangeListener(this);
            Main.map.mapView.removeLayer(layer);
            Main.map.remove(mapdustGUI);
            if (mapdustGUI != null) {
                mapdustGUI.update(new ArrayList<MapdustBug>(), this);
                mapdustGUI.setVisible(false);
            }
            mapdustLayer = null;
        }
    }
    
    /**
     * No need to implement this.
     */
    @Override
    public void mouseEntered(MouseEvent event) {}
    
    /**
     * No need to implement this.
     */
    @Override
    public void mouseExited(MouseEvent arg0) {}
    
    /**
     * No need to implement this.
     */
    @Override
    public void mousePressed(MouseEvent event) {}
    
    /**
     * No need to implement this.
     */
    @Override
    public void mouseReleased(MouseEvent arg0) {}
    
    /**
     * At mouse click the following two actions can be done: adding a new bug,
     * and selecting a bug from the map. A bug can be added if the plugin is the
     * only active plugin and you double click on the map. You can select a bug
     * from the map by clicking on it.
     * 
     * @event The <code>MouseEvent</code> object
     */
    @Override
    public void mouseClicked(MouseEvent event) {
        if (mapdustLayer != null && mapdustLayer.isVisible()) {
            if (event.getButton() == MouseEvent.BUTTON1) {
                if (event.getClickCount() == 2 && !event.isConsumed()) {
                    if (Main.map.mapView.getActiveLayer() == getMapdustLayer()) {
                        /* show add bug dialog */
                        MapdustBug bug = mapdustGUI.getPanel().getSelectedBug();
                        if (bug != null) {
                            Main.pref.put("selectedBug.status", bug.getStatus()
                                    .getValue());
                        } else {
                            Main.pref.put("selectedBug.status", "create");
                        }
                        /* disable MapdustButtonPanel */
                        mapdustGUI.disableBtnPanel();
                        /* create and show dialog */
                        dialog = new CreateIssueDialog(event.getPoint(), this);
                        dialog.showDialog();
                        event.consume();
                        return;
                    }
                }
                if (event.getClickCount() == 1 && !event.isConsumed()) {
                    /* allow click on the bug icon on the map */
                    Point p = event.getPoint();
                    MapdustBug nearestBug = getNearestBug(p);
                    if (nearestBug != null) {
                        mapdustLayer.setBugSelected(nearestBug);
                        /* set also in the list of bugs the element */
                        mapdustGUI.getPanel().setSelectedBug(nearestBug);
                        Main.map.mapView.repaint();
                    }
                    return;
                }
            }
        }
    }
    
    /**
     * Listens for the events of type <code>PreferenceChangeEvent</code> . If
     * the event key is 'osm-server.username' then if the username was changed
     * in Preferences and it was used as 'nickname'( the user did not changed
     * this completed nickname to someting else ) for submitting changes to
     * MapDust , re-set the 'mapdust.josmUserName' and 'mapdust.nickname'
     * properties.
     * 
     * @param event The <code>PreferenceChangeEvent</code> obejct
     */
    @Override
    public void preferenceChanged(PreferenceChangeEvent event) {
        if (this.mapdustGUI.isShowing() && !wasError && mapdustLayer != null
                && mapdustLayer.isVisible()) {
            if (event.getKey().equals("osm-server.username")) {
                String newUserName = userIdentityManager.getUserName();
                String oldUserName = Main.pref.get("mapdust.josmUserName");
                String nickname = Main.pref.get("mapdust.nickname");
                if (nickname.isEmpty()) {
                    /* nickname was not completed */
                    Main.pref.put("mapdust.josmUserName", newUserName);
                    Main.pref.put("mapdust.nickname", newUserName);
                } else {
                    if (nickname.equals(oldUserName)) {
                        /* username was used for nickname, and was not changed */
                        Main.pref.put("mapdust.josmUserName", newUserName);
                        Main.pref.put("mapdust.nickname", newUserName);
                    } else {
                        /* username was used for nickname, and was changed */
                        Main.pref.put("mapdust.josmUserName", newUserName);
                    }
                }
            }
        }
    }
    
    /**
     * Updates the <code>MapdustPlugin</code> data. Downloads the
     * <code>MapdustBug</code> objects from the current view, and updates the
     * <code>MapdustGUI</code> and the map with the new data.
     */
    private void updatePluginData() {
        Main.worker.execute(new Runnable() {
            @Override
            public void run() {
                updateMapdustData();
            }
        });
    }
    
    /**
     * Returns the bounds of the current <code>MapView</code>.
     * 
     * @return bounds
     */
    private Bounds getBounds() {
        MapView mapView = Main.map.mapView;
        Bounds bounds = new Bounds(mapView.getLatLon(0, mapView.getHeight()),
                mapView.getLatLon(mapView.getWidth(), 0));
        return bounds;
    }
    
    /**
     * Updates the MapDust plugin data. Downloads the list of
     * <code>MapdustBug</code> objects for the given area, and updates the map
     * and the MapDust layer with the new data.
     */
    private synchronized void updateMapdustData() {
        if (Main.map != null && Main.map.mapView != null) {
            /* download the MapDust data */
            try {
                Bounds bounds = getBounds();
                MapdustServiceHandler handler = new MapdustServiceHandler();
                mapdustBugList = handler.getBugs(bounds.getMin().lon(), 
                        bounds.getMin().lat(), bounds.getMax().lon(), 
                        bounds.getMax().lat());
                wasError = false;
            } catch (MapdustServiceHandlerException e) {
                wasError = true;
                mapdustBugList = new ArrayList<MapdustBug>();
                updateView();
                handleError();
            }
            /* update the view */
            if (!wasError) {
                updateView();
            }
        }
    }
    
    /**
     * Updates the current view ( map and MapDust bug list), with the given list
     * of <code>MapdustBug</code> objects.
     */
    private void updateView() {
        /* update the dialog with the new data */
        mapdustGUI.update(mapdustBugList, this);
        mapdustGUI.setVisible(true);
        mapdustGUI.revalidate();
        /* update the MapdustLayer */
        if (mapdustLayer == null) {
            /* create and add the layer */
            mapdustLayer = new MapdustLayer("MapDust", mapdustGUI, mapdustBugList);
            Main.main.addLayer(this.mapdustLayer);
            Main.map.mapView.moveLayer(this.mapdustLayer, 0);
            Main.map.mapView.addMouseListener(this);
            MapView.addLayerChangeListener(this);
            NavigatableComponent.addZoomChangeListener(this);
        } else {
            /* re-set the properties */
            mapdustLayer.destroy();
            mapdustLayer.setMapdustGUI(mapdustGUI);
            mapdustLayer.setMapdustBugList(mapdustBugList);
            mapdustLayer.setBugSelected(null);
        }
        /* repaint */
        Main.map.mapView.revalidate();
        Main.map.repaint();
    }
    
    /**
     * Updates the MapDust bugs list with the given <code>MapdustBug</code>
     * object.
     * 
     * @param mapdustBug The <code>MapdustBug</code> object
     */
    private void updateMapdustBugList(MapdustBug mapdustBug) {
        MapdustBug oldBug = null;
        for (MapdustBug bug : this.mapdustBugList) {
            if (bug.getId().equals(mapdustBug.getId())) {
                oldBug = bug;
            }
        }
        if (oldBug != null) {
            /* remove, add */
            this.mapdustBugList.remove(oldBug);
            this.mapdustBugList.add(0, mapdustBug);
        } else {
            /* new add */
            this.mapdustBugList.add(0, mapdustBug);
        }
    }
    
    /**
     * Returns the nearest <code>MapdustBug</code> object to the given point on
     * the map.
     * 
     * @param p A <code>Point</code> object
     * @return A <code>MapdustBug</code> object
     */
    private MapdustBug getNearestBug(Point p) {
        double snapDistance = 10;
        double minDistanceSq = Double.MAX_VALUE;
        MapdustBug nearestBug = null;
        for (MapdustBug bug : mapdustBugList) {
            Point sp = Main.map.mapView.getPoint(bug.getLatLon());
            double dist = p.distanceSq(sp);
            if (minDistanceSq > dist && p.distance(sp) < snapDistance) {
                minDistanceSq = p.distanceSq(sp);
                nearestBug = bug;
            } else if (minDistanceSq == dist) {
                nearestBug = bug;
            }
        }
        return nearestBug;
    }
    
    /**
     * Verifies if the <code>OsmDataLayer</code> layer has been added to the
     * list of layers.
     * 
     * @return true if the <code>OsmDataLayer</code> layer has been added false
     * otherwise
     */
    private boolean containsOsmDataLayer() {
        boolean contains = false;
        List<Layer> l = Main.map.mapView.getAllLayersAsList();
        for (Layer ll : l) {
            if (ll instanceof OsmDataLayer) {
                contains = true;
            }
        }
        return contains;
    }
    
    /**
     * Handles the <code>MapdustServiceHandlerException</code> error.
     * 
     */
    private void handleError() {
        String showMessage = Main.pref.get("mapdust.showError");
        Boolean showErrorMessage = Boolean.parseBoolean(showMessage);
        if (showErrorMessage) {
            /* show errprMessage, and remove the layer */
            Main.pref.put("mapdust.showError", false);
            String errorMessage = "There was a Mapdust service error.";
            errorMessage += " Please try later.";
            JOptionPane.showMessageDialog(Main.parent, tr(errorMessage));
        }
    }
    
    /**
     * Returns the <code>MapdustGUI</code> object
     * 
     * @return the mapdustGUI
     */
    public MapdustGUI getMapdustGUI() {
        return mapdustGUI;
    }
    
    /**
     * Sets the <code>MapdustGUI</code> object.
     * 
     * @param mapdustGUI the mapdustGUI to set
     */
    public void setMapdustGUI(MapdustGUI mapdustGUI) {
        this.mapdustGUI = mapdustGUI;
    }
    
    /**
     * Returns the <code>MapdustLayer</code> object.
     * 
     * @return the mapdustLayer
     */
    public MapdustLayer getMapdustLayer() {
        return mapdustLayer;
    }
    
    /**
     * Sets the <code>MapdustLayer</code> object.
     * 
     * @param mapdustLayer the mapdustLayer to set
     */
    public void setMapdustLayer(MapdustLayer mapdustLayer) {
        this.mapdustLayer = mapdustLayer;
    }
    
    /**
     * Returns the list of <code>MapdustBug</code> objects
     * 
     * @return the mapdustBugList
     */
    public List<MapdustBug> getMapdustBugList() {
        return mapdustBugList;
    }
    
    /**
     * Sets the list of <code>MapdustBug</code> objects
     * 
     * @param mapdustBugList the mapdustBugList to set
     */
    public void setMapdustBugList(List<MapdustBug> mapdustBugList) {
        this.mapdustBugList = mapdustBugList;
    }
    
}
