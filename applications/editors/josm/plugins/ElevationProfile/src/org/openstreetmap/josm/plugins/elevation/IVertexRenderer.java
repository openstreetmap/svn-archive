package org.openstreetmap.josm.plugins.elevation;

import java.awt.Color;

/**
 * The interface IVertexRenderer.
 * 
 * Implementors should provide a default color map which cannot be unregistered
 */
public interface IVertexRenderer {
    
    /**
     * Gets the color according to the given elevation.
     *
     * @param vertex the elevation vertex
     * @return the elevation color
     */
    public Color getElevationColor(EleVertex vertex);
    
    /**
     * Selects color map with the given name. If no
     * such color map exists, the old color map is kept.
     *
     * @param mapToUse the map to use
     */
    public void selectColorMap(String name);
   
}
