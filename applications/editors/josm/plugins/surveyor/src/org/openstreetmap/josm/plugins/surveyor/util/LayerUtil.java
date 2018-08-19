// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.surveyor.util;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.Layer;

/**
 * @author cdaller
 *
 */
public final class LayerUtil {

    private LayerUtil() {
        // Hide default contructir for utilities classes
    }
    
    /**
     * Returns the layer with the given name and type from the map view or <code>null</code>.
     * @param <LayerType> the type of the layer.
     * @param layerName the name of the layer.
     * @param layerType the type of the layer.
     * @return the layer or <code>null</code>.
     */
    @SuppressWarnings("unchecked")
    public static <LayerType extends Layer> LayerType findGpsLayer(String layerName, Class<LayerType> layerType) {
        Layer result = null;
        if (MainApplication.getMap() != null && MainApplication.getMap().mapView != null) {
            for (Layer layer : MainApplication.getLayerManager().getLayers()) {
                if (layerName.equals(layer.getName()) && layerType.isAssignableFrom(layer.getClass())) {
                    result = layer;
                    break;
                }
            }
        }
        return (LayerType) result;
    }
}
