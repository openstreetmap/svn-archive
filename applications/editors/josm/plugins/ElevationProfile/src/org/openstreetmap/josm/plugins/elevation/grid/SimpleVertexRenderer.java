// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.elevation.grid;

import java.awt.Color;

import org.openstreetmap.josm.plugins.elevation.ColorMap;
import org.openstreetmap.josm.plugins.elevation.IVertexRenderer;

/**
 * @author Olli
 *
 */
public class SimpleVertexRenderer implements IVertexRenderer {
    private ColorMap cMap = null;

    /**
     * 
     */
    public SimpleVertexRenderer() {
        cMap = ColorMap.getMap(ColorMap.getNames()[0]);
    }

    @Override
    public Color getElevationColor(EleVertex vertex) {
        return cMap.getColor((int) vertex.getEle());
    }


    @Override
    public void selectColorMap(String name) {
        // TODO Auto-generated method stub

    }

}
