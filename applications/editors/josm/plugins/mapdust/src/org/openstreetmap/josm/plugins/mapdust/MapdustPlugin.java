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
import javax.swing.SwingUtilities;
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
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.mapdust.gui.MapdustGUI;
import org.openstreetmap.josm.plugins.mapdust.gui.component.dialog.CreateBugDialog;
import org.openstreetmap.josm.plugins.mapdust.gui.observer.MapdustBugObserver;
import org.openstreetmap.josm.plugins.mapdust.gui.observer.MapdustUpdateObserver;
import org.openstreetmap.josm.plugins.mapdust.gui.value.MapdustPluginState;
import org.openstreetmap.josm.plugins.mapdust.service.MapdustServiceHandler;
import org.openstreetmap.josm.plugins.mapdust.service.MapdustServiceHandlerException;
import org.openstreetmap.josm.plugins.mapdust.service.value.BoundingBox;
import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustBug;
import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustBugFilter;
import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustRelevance;
import org.openstreetmap.josm.tools.Shortcut;


/**
 * This is the main class of the MapDust plug-in. Defines the MapDust plug-in
 * main functionality.
 *
 * @author Bea
 */
public class MapdustPlugin extends Plugin implements LayerChangeListener,
        ZoomChangeListener, PreferenceChangedListener, MouseListener,
        MapdustUpdateObserver, MapdustBugObserver {

    /** The graphical user interface of the plug-in */
    private MapdustGUI mapdustGUI;

    /** The layer of the MapDust plug-in */
    private MapdustLayer mapdustLayer;

    /** The <code>CreateIssueDialog</code> object */
    private CreateBugDialog dialog;

    /** The JOSM user identity manager, it is used for obtaining the user name */
    private final JosmUserIdentityManager userIdentityManager;

    /** The list of <code>MapdustBug</code> objects */
    private List<MapdustBug> mapdustBugList;

    /** The bounding box from where the MapDust bugs are down-loaded */
    private BoundingBox bBox;

    /**
     * The <code>MapdustBugFilter</code> object representing the selected
     * filters
     */
    private MapdustBugFilter filter;

    /** Specifies if there was or not an error down-loading the data */
    protected boolean wasError = false;

    /**
     * Builds a new <code>MapDustPlugin</code> object based on the given
     * arguments.
     *
     * @param info The <code>MapDustPlugin</code> object
     */
    public MapdustPlugin(PluginInformation info) {
        super(info);
        this.userIdentityManager = JosmUserIdentityManager.getInstance();
        this.filter = null;
        this.bBox = null;
        initializePlugin();
    }

    /**
     * Initialize the <code>MapdustPlugin</code> object. Creates the
     * <code>MapdustGUI</code> and initializes the following variables with
     * default values: 'mapdust.pluginState', 'mapdust.nickname',
     * 'mapdust.showError', 'mapdust.version' and 'mapdust.localVersion'.
     */
    private void initializePlugin() {
        /* create MapDust GUI */
        Shortcut shortcut = Shortcut.registerShortcut("MapDust", tr("Toggle: {0}", tr("Open MapDust")),
                KeyEvent.VK_0, Shortcut.GROUP_LAYER, Shortcut.SHIFT_DEFAULT);
        String name = "MapDust bug reports";
        String tooltip = "Activates the MapDust bug reporter plugin";
        mapdustGUI = new MapdustGUI(tr(name), "mapdust_icon.png", tr(tooltip),
                shortcut, 150, this);
        /* add default values for static variables */
        Main.pref.put("mapdust.pluginState", MapdustPluginState.ONLINE.getValue());
        Main.pref.put("mapdust.nickname", "");
        Main.pref.put("mapdust.showError", true);
        Main.pref.put("mapdust.version", getPluginInformation().version);
        Main.pref.put("mapdust.localVersion",getPluginInformation().localversion);
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
                /* add MapdustGUI */
                mapdustGUI.setBounds(newMapFrame.getBounds());
                mapdustGUI.addObserver(this);
                newMapFrame.addToggleDialog(mapdustGUI);
                /* add Listeners */
                NavigatableComponent.addZoomChangeListener(this);
                MapView.addLayerChangeListener(this);
                Main.map.mapView.addMouseListener(this);
                Main.pref.addPreferenceChangeListener(this);
                /* put username to preferences */
                Main.pref.put("mapdust.josmUserName",
                        userIdentityManager.getUserName());
            }
        }
    }

    /**
     * Listens for the events of type <code>PreferenceChangeEvent</code> . If
     * the event key is 'osm-server.username' then if the user name was changed
     * in Preferences and it was used as 'nickname'( the user did not changed
     * this completed nickname to something else ) for submitting changes to
     * MapDust , re-set the 'mapdust.josmUserName' and 'mapdust.nickname'
     * properties.
     *
     * @param event The <code>PreferenceChangeEvent</code> object
     */
    @Override
    public void preferenceChanged(PreferenceChangeEvent event) {
        if (mapdustGUI.isShowing() && !wasError && mapdustLayer != null
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
                        /* user name was used for nickname, and was not changed */
                        Main.pref.put("mapdust.josmUserName", newUserName);
                        Main.pref.put("mapdust.nickname", newUserName);
                    } else {
                        /* user name was used for nickname, and was changed */
                        Main.pref.put("mapdust.josmUserName", newUserName);
                    }
                }
            }
        }
    }

    /**
     * Updates the map and the MapDust bugs list with the given
     * <code>MapdustBug</code> object. If the bug is already contained in the
     * list and map, then this object will be updated with the new properties.
     * If the filter settings does not allow this new bug to be shown in the map
     * and list, then it will be removed from the map and list.
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
                MapdustBug oldBug = null;
                for (MapdustBug bug : mapdustBugList) {
                    if (bug.getId().equals(mapdustBug.getId())) {
                        oldBug = bug;
                    }
                }
                boolean showBug = shouldDisplay(mapdustBug);
                if (oldBug != null) {
                    /* remove, add */
                    if (showBug) {
                        mapdustBugList.remove(oldBug);
                        mapdustBugList.add(0, mapdustBug);
                    } else {
                        mapdustBugList.remove(oldBug);
                    }
                } else {
                    /* new add */
                    if (showBug) {
                        mapdustBugList.add(0, mapdustBug);
                    }
                }
                mapdustGUI.update(mapdustBugList, this);
                mapdustLayer.setMapdustGUI(mapdustGUI);
                if (showBug) {
                    mapdustGUI.setSelectedBug(mapdustBugList.get(0));
                } else {
                    mapdustLayer.setBugSelected(null);
                    mapdustGUI.enableBtnPanel(true);
                    Main.map.mapView.repaint();
                    String title = "MapDust";
                    String message = "The operation was successful.";
                    JOptionPane.showMessageDialog(Main.parent, message, title,
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
    }

    /**
     * Verifies if the given <code>MapdustBug</code> object should be displayed
     * on the map and on the bugs list. A <code>MapdustBug</code> will be
     * displayed on the map only if it is permitted by the selected filter
     * settings (statuses, types, description and relevance filter).
     *
     * @param mapdustBug The <code>MapdustBug</code> object
     * @return true if the given bug should be displayed false otherwise
     */
    private boolean shouldDisplay(MapdustBug mapdustBug) {
        boolean result = true;
        if (filter != null) {
            boolean containsStatus = false;
            if (filter.getStatuses() != null && !filter.getStatuses().isEmpty()) {
                Integer statusKey = mapdustBug.getStatus().getKey();
                if (filter.getStatuses().contains(statusKey)) {
                    containsStatus = true;
                }
            } else {
                containsStatus = true;
            }
            boolean containsType = false;
            if (filter.getTypes() != null && !filter.getTypes().isEmpty()) {
                String typeKey = mapdustBug.getType().getKey();
                if (filter.getTypes().contains(typeKey)) {
                    containsType = true;
                }
            } else {
                containsType = true;
            }
            if (filter.getDescr() != null && filter.getDescr()) {
                /* show only bugs with isDefaultDescription = false */
                if (mapdustBug.getIsDefaultDescription()) {
                    result = false;
                } else {
                    result = containsStatus && containsType;
                }
            } else {
                result = containsStatus && containsType;
            }
            /* check relevance filter settings */
            boolean containsMinRelevance = false;
            if (filter.getMinRelevance() != null) {
                MapdustRelevance minRel = filter.getMinRelevance();
                MapdustRelevance bugRel = mapdustBug.getRelevance();
                if (minRel.equals(bugRel)) {
                    containsMinRelevance = true;
                } else {
                    if (bugRel.compareTo(minRel) == 1) {
                        containsMinRelevance = true;
                    }
                }
            }
            boolean containsMaxRelevance = false;
            if (filter.getMaxRelevance() != null) {
                MapdustRelevance maxRel = filter.getMaxRelevance();
                MapdustRelevance bugRel = mapdustBug.getRelevance();
                if (maxRel.equals(bugRel)) {
                    containsMaxRelevance = true;
                } else {
                    if (bugRel.compareTo(maxRel) == -1) {
                        containsMaxRelevance = true;
                    }
                }
                result = result && (containsMinRelevance && containsMaxRelevance);
            }
        }
        return result;
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
     * and selecting a bug from the map. A bug can be added if the plug-in is
     * the only active plug-in and you double click on the map. You can select
     * a bug from the map by clicking on it.
     *
     * @param event The <code>MouseEvent</code> object
     */
    @Override
    public void mouseClicked(MouseEvent event) {
        if (mapdustLayer != null && mapdustLayer.isVisible()) {
            if (event.getButton() == MouseEvent.BUTTON1) {
                if (event.getClickCount() == 2 && !event.isConsumed()) {
                    if (Main.map.mapView.getActiveLayer() == getMapdustLayer()) {
                        /* show add bug dialog */
                        MapdustBug bug = mapdustGUI.getSelectedBug();
                        if (bug != null) {
                            Main.pref.put("selectedBug.status", bug.getStatus()
                                    .getValue());
                        } else {
                            Main.pref.put("selectedBug.status", "create");
                        }
                        /* disable MapdustButtonPanel */
                        mapdustGUI.getPanel().disableBtnPanel();
                        /* create and show dialog */
                        dialog = new CreateBugDialog(event.getPoint(), this);
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
                        mapdustGUI.setSelectedBug(nearestBug);
                        Main.map.mapView.repaint();
                    }
                    return;
                }
            }
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
     * No need to implement this.
     */
    @Override
    public void activeLayerChange(Layer arg0, Layer arg1) {}

    /**
     * Adds the <code>MapdustLayer</code> to the JOSM editor. If the list of
     * <code>MapdustBug</code>s is null then down-loads the data from the
     * MapDust Service and updates the editor with this new data.
     *
     * @param layer The <code>Layer</code> which will be added to the JOSM
     * editor
     */
    @Override
    public void layerAdded(Layer layer) {}

    /**
     * Removes the <code>MapdustLayer</code> from the JOSM editor. Also closes
     * the MapDust plug-in window.
     *
     * @param layer The <code>Layer</code> which will be removed from the JOSM
     * editor
     */
    @Override
    public void layerRemoved(Layer layer) {
        if (layer instanceof MapdustLayer) {
            /* remove the layer */
            Main.pref.put("mapdust.pluginState",
                    MapdustPluginState.ONLINE.getValue());
            NavigatableComponent.removeZoomChangeListener(this);
            Main.map.mapView.removeLayer(layer);
            Main.map.remove(mapdustGUI);
            if (mapdustGUI != null) {
                mapdustGUI.destroy();
            }
            mapdustLayer = null;
            filter = null;
            mapdustBugList = null;
        }
    }

    /**
     * Listens for the zoom change event. If the zoom was changed, then it will
     * down-load the MapDust bugs data from the current view. The new data will
     * be down-loaded only if the current bounding box is different from the
     * previous one.
     */
    @Override
    public void zoomChanged() {
        if (mapdustGUI.isShowing() && !wasError) {
            boolean download = true;
            BoundingBox curentBBox = getBBox();
            if (bBox != null) {
                if (bBox.equals(curentBBox)) {
                    download = false;
                }
            }
            bBox = curentBBox;
            if (download) {
                updatePluginData();
            }
        }
    }

    /**
     * Updates the plug-in with a new MapDust bugs data. If the filters are set
     * then the MapDust data will be filtered. If initialUpdate flag is true
     * then the plug-in is updated for the first time with the MapDust data. By
     * default the first time there is no filter applied to the MapDust data.
     *
     * @param filter The <code>MapdustBugFilter</code> containing the filter
     * settings
     * @param initialUpdate If true then there will be no filter applied.
     */
    @Override
    public void update(MapdustBugFilter filter, boolean initialUpdate) {
        bBox = getBBox();
        if (initialUpdate) {
            updatePluginData();
        } else {
            if (filter != null) {
                this.filter = filter;
            }
            if (mapdustGUI.isShowing() && !wasError) {
                updatePluginData();
            }
        }
    }

    /**
     * Returns the current bounding box. If the bounding box values are not in
     * the limits, then it will normalized.
     *
     * @return A <code>BoundingBox</code>
     */
    private BoundingBox getBBox() {
        MapView mapView = Main.map.mapView;
        Bounds bounds = new Bounds(mapView.getLatLon(0, mapView.getHeight()),
                mapView.getLatLon(mapView.getWidth(), 0));
        return new BoundingBox(bounds.getMin().lon(), bounds.getMin().lat(),
                bounds.getMax().lon(), bounds.getMax().lat());
    }

    /**
     * Updates the <code>MapdustPlugin</code> data. Down-loads the
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
     * Updates the MapDust plug-in data. Down-loads the list of
     * <code>MapdustBug</code> objects for the given area, and updates the map
     * and the MapDust layer with the new data.
     */
    protected synchronized void updateMapdustData() {
        if (Main.map != null && Main.map.mapView != null) {
            /* Down-loads the MapDust data */
            try {
                MapdustServiceHandler handler = new MapdustServiceHandler();
                mapdustBugList = handler.getBugs(bBox, filter);
                wasError = false;
            } catch (MapdustServiceHandlerException e) {
                wasError = true;
                mapdustBugList = new ArrayList<MapdustBug>();
            }
            /* update the view */
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    synchronized (this) {
                        updateView();
                        if (wasError) {
                            handleError();
                        }
                    }
                }
            });

        }
    }


    /**
     * Updates the current view ( map and MapDust bug list), with the given list
     * of <code>MapdustBug</code> objects.
     */
    protected void updateView() {
        if (Main.map != null && Main.map.mapView != null) {
            /* update the MapdustLayer */
            boolean needRepaint = false;
            if (!containsMapdustLayer()) {
                /* first start or layer was deleted */
                if (mapdustGUI.isDownloaded()) {
                    mapdustGUI.update(mapdustBugList, this);
                    /* create and add the layer */
                    mapdustLayer = new MapdustLayer("MapDust", mapdustGUI,
                            mapdustBugList);
                    Main.main.addLayer(this.mapdustLayer);
                    Main.map.mapView.moveLayer(this.mapdustLayer, 0);
                    Main.map.mapView.addMouseListener(this);
                    NavigatableComponent.addZoomChangeListener(this);
                    needRepaint = true;
                }
            } else {
                if (mapdustLayer != null) {
                    /* MapDust data was changed */
                    mapdustGUI.update(mapdustBugList, this);
                    mapdustLayer.destroy();
                    mapdustLayer.update(mapdustGUI, mapdustBugList);
                    needRepaint = true;
                }
            }
            if (needRepaint) {
                /* force repaint */
                mapdustGUI.revalidate();
                Main.map.mapView.revalidate();
                Main.map.repaint();
            }
        }
    }

    /**
     * Verifies if the <code>MapView</code> contains or not the
     * <code>MapdustLayer</code> layer.
     *
     * @return true if the <code>MapView</code> contains the
     * <code>MapdustLayer</code> false otherwise
     */
    private boolean containsMapdustLayer() {
        boolean contains = false;
        List<Layer> all = Main.map.mapView.getAllLayersAsList();
        if (mapdustLayer != null && all.contains(mapdustLayer)) {
            contains = true;
        }
        return contains;
    }

    /**
     * Handles the <code>MapdustServiceHandlerException</code> error.
     */
    protected void handleError() {
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
     * Returns the MapDust bug filter
     *
     * @return the filter
     */
    public MapdustBugFilter getFilter() {
        return filter;
    }

}
