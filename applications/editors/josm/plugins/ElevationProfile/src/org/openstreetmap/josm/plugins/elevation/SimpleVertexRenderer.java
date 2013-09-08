/*
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
