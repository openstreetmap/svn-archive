/**
 * 
 */
package at.dallermassl.josm.plugin.surveyor.util;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.layer.Layer;

/**
 * @author cdaller
 *
 */
public class LayerUtil {

    /**
     * Returns the layer with the given name and type from the map view or <code>null</code>.
     * @param <LayerType> the type of the layer.
     * @param layerName the name of the layer.
     * @param layerType the type of the layer.
     * @return the layer or <code>null</code>.
     */
    public static <LayerType extends Layer> LayerType findGpsLayer(String layerName, Class<LayerType> layerType) {
        LayerType result = null;
        if(Main.map != null && Main.map.mapView != null) {
            for(Layer layer : Main.map.mapView.getAllLayers()) {
                if(layerName.equals(layer.name) && layerType.isAssignableFrom(layer.getClass())) {
                    result = (LayerType) layer;
                    break;
                }
            }
        }
        return result;
    }

}
