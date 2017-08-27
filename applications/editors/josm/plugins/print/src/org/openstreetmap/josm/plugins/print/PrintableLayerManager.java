// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.print;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.MainLayerManager;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;

public class PrintableLayerManager extends MainLayerManager {

    private static final MainLayerManager layerManager = MainApplication.getLayerManager();

    @Override
    public synchronized void removeActiveLayerChangeListener(ActiveLayerChangeListener listener) {
        layerManager.removeActiveLayerChangeListener(listener);
    }

    @Override
    public void setActiveLayer(Layer layer) {
        layerManager.setActiveLayer(layer);
    }

    @Override
    public synchronized Layer getActiveLayer() {
        return layerManager.getActiveLayer();
    }

    @Override
    public synchronized OsmDataLayer getEditLayer() {
        return layerManager.getEditLayer();
    }

    @Override
    public synchronized DataSet getEditDataSet() {
        return layerManager.getEditDataSet();
    }

    @Override
    public synchronized List<Layer> getVisibleLayersInZOrder() {
        ArrayList<Layer> layers = new ArrayList<>();
        for (Layer l: layerManager.getLayers()) {
            if (l.isVisible()) {
                layers.add(l);
            }
        }
        Collections.sort(
            layers,
            new Comparator<Layer>() {
                @Override
                public int compare(Layer l2, Layer l1) { // l1 and l2 swapped!
                    if (l1 instanceof OsmDataLayer && l2 instanceof OsmDataLayer) {
                        if (l1 == layerManager.getActiveLayer()) return -1;
                        if (l2 == layerManager.getActiveLayer()) return 1;
                        return Integer.valueOf(layerManager.getLayers().indexOf(l1)).
                                     compareTo(layerManager.getLayers().indexOf(l2));
                    } else
                        return Integer.valueOf(layerManager.getLayers().indexOf(l1)).
                                     compareTo(layerManager.getLayers().indexOf(l2));
                }
            }
        );
        return layers;
    }

    @Override
    public void addLayer(Layer layer) {
        layerManager.addLayer(layer);
    }

    @Override
    public void removeLayer(Layer layer) {
        layerManager.removeLayer(layer);
    }

    @Override
    public void moveLayer(Layer layer, int position) {
        layerManager.moveLayer(layer, position);
    }

    @Override
    public List<Layer> getLayers() {
        return layerManager.getLayers();
    }

    @Override
    public <T extends Layer> List<T> getLayersOfType(Class<T> ofType) {
        return layerManager.getLayersOfType(ofType);
    }

    @Override
    public synchronized boolean containsLayer(Layer layer) {
        return layerManager.containsLayer(layer);
    }

    @Override
    public synchronized void addLayerChangeListener(LayerChangeListener listener) {
        layerManager.addLayerChangeListener(listener);
    }

    @Override
    public synchronized void addAndFireLayerChangeListener(LayerChangeListener listener) {
        layerManager.addAndFireLayerChangeListener(listener);
    }

    @Override
    public synchronized void removeLayerChangeListener(LayerChangeListener listener) {
        layerManager.removeLayerChangeListener(listener);
    }

    @Override
    public synchronized void removeAndFireLayerChangeListener(LayerChangeListener listener) {
        layerManager.removeAndFireLayerChangeListener(listener);
    }
}
