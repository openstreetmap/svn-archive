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
 * This is the main class of the MapDust plugin. Defines the MapDust plugin main
 * functionality.
 * 
 * @author Bea
 * 
 */
public class MapdustPlugin extends Plugin implements LayerChangeListener,
        MouseListener, MapdustRefreshObserver, MapdustBugObserver,
        MapdustInitialUpdateObserver {
    
    /** The graphical user interface of the plugin */
    private MapdustGUI mapdustGUI;
    
    /** The layer of the MapDust plugin */
    private MapdustLayer mapdustLayer;
    
    /** The list of <code>MapdustBug</code> objects */
    private List<MapdustBug> mapdustBugList;
    
    /**
     * Builds a new <code>MapDustPlugin</code> object based on the given
     * arguments.
     * 
     * @param info The <code>MapDustPlugin</code> object
     */
    public MapdustPlugin(PluginInformation info) {
        super(info);
        Main.pref.put("mapdust.pluginState",
                MapdustPluginState.ONLINE.getValue());
        Main.pref.put("mapdust.nickname", null);
        Main.pref.put("mapdust.modify", false);
        MapView.addLayerChangeListener(this);
        if (Main.map != null && Main.map.mapView != null) {
            Main.map.mapView.addMouseListener(this);
            MapView.addLayerChangeListener(this);
        }
    }
    
    /**
     * Initializes the new view with the MapDust Bugs from that area.
     * 
     * @param oldMapFrame The old <code>MapFrame</code> object
     * @param newMapFrame The new <code>MapFrame</code> object
     */
    @Override
    public void mapFrameInitialized(MapFrame oldMapFrame, MapFrame newMapFrame) {
        if (newMapFrame == null) {
            /* if new MapFrame is null, remove listener */
            MapView.removeLayerChangeListener(this);
        } else {
            /* add MapDust dialog window */
            Shortcut shortcut = Shortcut.registerShortcut("mapdust",
                    tr("Toggle: {0}", tr("Open MapDust")), KeyEvent.VK_0, 
                    Shortcut.GROUP_MENU, Shortcut.SHIFT_DEFAULT);
            String name = "MapDust bug reports";
            String tooltip = "Activates the MapDust bug reporter plugin";
            mapdustGUI = new MapdustGUI(tr(name), "mapdust_icon.png", tr(tooltip),
                    shortcut, 150, this);
            mapdustGUI.setBounds(newMapFrame.getBounds());
            mapdustGUI.addObserver(this);
            newMapFrame.addToggleDialog(mapdustGUI);
            NavigatableComponent
                    .addZoomChangeListener(new ZoomChangeListener() {
                        
                        @Override
                        public void zoomChanged() {
                            updateData();
                        }
                    });
            MapView.addLayerChangeListener(this);
            Main.map.mapView.addMouseListener(this);
        }
    }
    
    /**
     * Updates the MapDust plugin data. Downloads the list of
     * <code>MapdustBug</code> objects for the given area, and updates the map
     * and the Mapdust layer with the new data.
     */
    @Override
    public synchronized void updateData() {
        if (Main.map != null && Main.map.mapView != null) {
            try {
                /* downloads the MapDust bugs */
                this.mapdustBugList = getMapdustBugs();
                if (getMapdustGUI().isDialogShowing()) {
                    /* updates the views */
                    updateView();
                    /* show message if there is not bug in the given area */
                    if (this.mapdustBugList == null
                            || this.mapdustBugList.size() == 0) {
                        String waringMessage = "There is no MapDust bug in ";
                        waringMessage += "your visible area.";
                        JOptionPane.showMessageDialog(Main.parent, 
                                tr(waringMessage), tr("Warning"),
                                JOptionPane.WARNING_MESSAGE);
                    }
                }
            } catch (MapdustServiceHandlerException e) {
                /* show errprMessage, and remove the layer */
                String errorMessage = "There was a Mapdust service error.";
                errorMessage += " Please try later.";
                JOptionPane.showMessageDialog(Main.parent, tr(errorMessage),
                        tr("Error"), JOptionPane.ERROR_MESSAGE);
                MapView.removeLayerChangeListener(this);
                Main.map.mapView.removeLayer(mapdustLayer);
                Main.map.remove(mapdustGUI);
            }
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
        if (this.mapdustBugList == null) {
            this.mapdustBugList = new ArrayList<MapdustBug>();
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
    
    @Override
    public void activeLayerChange(Layer arg0, Layer arg1) {}
    
    @Override
    public void layerAdded(Layer layer) {
        if (layer instanceof MapdustLayer) {
            /* download the MapDust bugs and update the plugin */
            updateData();
        }
    }
    
    @Override
    public void layerRemoved(Layer layer) {
        if (layer instanceof MapdustLayer) {
            /* remove the layer */
            MapView.removeLayerChangeListener(this);
            Main.map.mapView.removeLayer(layer);
            Main.map.remove(mapdustGUI);
            if (mapdustGUI != null) {
                mapdustGUI.update(null, this);
            }
            mapdustLayer = null;
        }
    }
    
    @Override
    public void mouseClicked(MouseEvent event) {
        if (mapdustLayer != null && mapdustLayer.isVisible()
                && Main.map.mapView.getActiveLayer() == getMapdustLayer()) {
            /* show add bug dialog */
            if (event.getClickCount() == 2) {
                mapdustGUI.getPanel().getBtnPanel().getBtnWorkOffline()
                        .setEnabled(false);
                mapdustGUI.getPanel().getBtnPanel().getBtnRefresh()
                        .setEnabled(false);
                mapdustGUI.getPanel().getBtnPanel().getBtnAddComment()
                        .setEnabled(false);
                mapdustGUI.getPanel().getBtnPanel().getBtnFixBugReport()
                        .setEnabled(false);
                mapdustGUI.getPanel().getBtnPanel().getBtnInvalidateBugReport()
                        .setEnabled(false);
                mapdustGUI.getPanel().getBtnPanel().getBtnReOpenBugReport()
                        .setEnabled(false);
                Main.pref.put("mapdust.modify", true);
                MapdustBug selectedBug = mapdustGUI.getPanel().getSelectedBug();
                if (selectedBug != null) {
                    Main.pref.put("selectedBug.status", selectedBug.getStatus()
                            .getValue());
                } else {
                    Main.pref.put("selectedBug.status", "create");
                }
                String title = "Create bug report";
                String iconName = "dialogs/open.png";
                String messageText = "In order to create a new bug report you";
                messageText += " need to provide your nickname and a brief";
                messageText += " description for the bug.";
                Point point = event.getPoint();
                CreateIssueDialog dialog =
                        new CreateIssueDialog(tr(title), iconName,
                                tr(messageText), point, this);
                dialog.setLocationRelativeTo(null);
                dialog.getContentPane().setPreferredSize(dialog.getSize());
                dialog.pack();
                dialog.setVisible(true);
                return;
            }
            if (event.getButton() == MouseEvent.BUTTON1) {
                /* allow click on the bug icon on the map */
                Point p = event.getPoint();
                MapdustBug nearestBug = getNearestBug(p);
                if (nearestBug != null) {
                    mapdustLayer.setBugSelected(nearestBug);
                    /* set also in the list of bugs the element */
                    mapdustGUI.getPanel().setSelectedBug(nearestBug);
                    Main.map.mapView.repaint();
                }
            }
        }
    }
    
    /**
     * Updates the current view, with the given list of <code>MapdustBug</code>
     * objects.
     */
    private void updateView() {
        /* update the dialog with the new data */
        mapdustGUI.update(mapdustBugList, this);
        if (mapdustLayer == null) {
            /* create and add the layer */
            Main.map.mapView.removeAll();
            mapdustLayer =
                    new MapdustLayer("MapDust", mapdustGUI, mapdustBugList);
            Main.main.addLayer(mapdustLayer);
            Main.map.mapView.moveLayer(mapdustLayer, 0);
            MapView.addLayerChangeListener(this);
        } else {
            /* re-set the properties */
            mapdustLayer.destroy();
            mapdustLayer.setMapdustGUI(mapdustGUI);
            mapdustLayer.setMapdustBugList(mapdustBugList);
            mapdustLayer.setBugSelected(null);
        }
        /* repaint */
        Main.map.mapView.invalidate();
        Main.map.repaint();
        Main.map.mapView.repaint();
    }
    
    /**
     * Downloads the MapDust bugs from the current map view, and updates the
     * plugin data with the new downloaded data.
     */
    @Override
    public void initialUpdate() {
        if (containsOsmDataLayer()) {
            Main.worker.execute(new Runnable() {
                
                @Override
                public void run() {
                    updateData();
                    
                }
            });
        }
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
     * Updates the MapDust bugs list with the given <code>MapdustBug</code>
     * object.
     * 
     * @param mapdustBug The <code>MapdustBug</code> object
     */
    private void updateMapdustBugList(MapdustBug mapdustBug) {
        MapdustBug oldBug = null;
        for (MapdustBug bug : mapdustBugList) {
            if (bug.getId().equals(mapdustBug.getId())) {
                oldBug = bug;
            }
        }
        if (oldBug != null) {
            /* remove, add */
            mapdustBugList.remove(oldBug);
            mapdustBugList.add(0, mapdustBug);
        } else {
            /* new add */
            mapdustBugList.add(0, mapdustBug);
        }
    }
    
    /**
     * Returns the list of <code>MapdustBug</code> objects from the current
     * area.
     * 
     * @return A list of <code>MapdustBug</code> objects
     * @throws MapdustServiceHandlerException In the case of a MapDust service
     * error
     */
    private List<MapdustBug> getMapdustBugs()
            throws MapdustServiceHandlerException {
        /* get the bounding box */
        MapView mapView = Main.map.mapView;
        Bounds bounds =
                new Bounds(mapView.getLatLon(0, mapView.getHeight()),
                        mapView.getLatLon(mapView.getWidth(), 0));
        Double minLon = bounds.getMin().lon();
        Double minLat = bounds.getMin().lat();
        Double maxLon = bounds.getMax().lon();
        Double maxLat = bounds.getMax().lat();
        
        /* get the bugs from the bounding box */
        MapdustServiceHandler handler = new MapdustServiceHandler();
        List<MapdustBug> list = handler.getBugs(minLon, minLat, maxLon, maxLat);
        return list;
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
    
    @Override
    public void mouseEntered(MouseEvent arg0) {}
    
    @Override
    public void mouseExited(MouseEvent arg0) {}
    
    @Override
    public void mousePressed(MouseEvent arg0) {}
    
    @Override
    public void mouseReleased(MouseEvent arg0) {}
    
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
     * Returns the list of MapDust bugs
     * 
     * @return the mapdustBugList
     */
    public List<MapdustBug> getMapdustBugList() {
        return mapdustBugList;
    }
    
    /**
     * Sets the list of MapDust bugs
     * 
     * @param mapdustBugList the mapdustBugList to set
     */
    public void setMapdustBugList(List<MapdustBug> mapdustBugList) {
        this.mapdustBugList = mapdustBugList;
    }
}
