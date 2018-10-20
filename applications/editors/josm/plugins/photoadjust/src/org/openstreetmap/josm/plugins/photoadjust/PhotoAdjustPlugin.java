// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.photoadjust;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeEvent;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeListener;
import org.openstreetmap.josm.gui.layer.geoimage.GeoImageLayer;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * This is the main class for the photo adjust plugin.  The plugin
 * allows to move photos on the map and to place photos without
 * coordinates (untagged photos) on the map.
 */
public class PhotoAdjustPlugin extends Plugin implements ActiveLayerChangeListener {

    private GeoImageLayer imageLayer;
    private MouseAdapter mouseAdapter;
    private MouseMotionAdapter mouseMotionAdapter;
    private final PhotoAdjustWorker worker = new PhotoAdjustWorker();

    /**
     * Will be invoked by JOSM to bootstrap the plugin.
     *
     * @param info  information about the plugin and its local installation
     */
    public PhotoAdjustPlugin(PluginInformation info) {
        super(info);
        GeoImageLayer.registerMenuAddition(new UntaggedGeoImageLayerAction());
        PhotoPropertyEditor.init();
        initAdapters();
    }

    /**
     * Create mouse adapters that wait for activity on the GeoImageLayer.
     */
    private void initAdapters() {
        mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent evt) {
                if (imageLayer != null) {
                    List<GeoImageLayer> layers = new ArrayList<>(1);
                    layers.add(imageLayer);
                    worker.doMousePressed(evt, layers);
                }
            }

            @Override
            public void mouseReleased(MouseEvent evt) {
                if (imageLayer != null) {
                    worker.doMouseReleased(evt);
                }
            }
        };

        mouseMotionAdapter = new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent evt) {
                worker.doMouseDragged(evt);
            }
        };
    }

    /**
     * Called when the JOSM map frame is created or destroyed.
     */
    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (oldFrame == null && newFrame != null) {
            MainApplication.getLayerManager().addAndFireActiveLayerChangeListener(this);
            PhotoAdjustMapMode adjustMode = new PhotoAdjustMapMode(worker);
            adjustMode.installMapMode(newFrame);
        } else {
            MainApplication.getLayerManager().removeActiveLayerChangeListener(this);
        }
    }

    @Override
    public void activeOrEditLayerChanged(ActiveLayerChangeEvent e) {
        worker.reset();
        Layer oldLayer = e.getPreviousActiveLayer();
        Layer newLayer = MainApplication.getLayerManager().getActiveLayer();
        if (oldLayer instanceof GeoImageLayer
            && newLayer instanceof GeoImageLayer) {
            imageLayer = (GeoImageLayer)newLayer;
        } else {
            if (oldLayer instanceof GeoImageLayer) {
                MainApplication.getMap().mapView.removeMouseListener(mouseAdapter);
                MainApplication.getMap().mapView.removeMouseMotionListener(mouseMotionAdapter);
                imageLayer = null;
            }
            if (newLayer instanceof GeoImageLayer) {
                imageLayer = (GeoImageLayer)newLayer;
                MainApplication.getMap().mapView.addMouseListener(mouseAdapter);
                MainApplication.getMap().mapView.addMouseMotionListener(mouseMotionAdapter);
            }
        }
    }
}
