/**
 * This program is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU General Public License as published by the 
 * Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.openstreetmap.josm.plugins.elevation;

import java.awt.Color;

import org.openstreetmap.josm.plugins.elevation.grid.EleVertex;

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
