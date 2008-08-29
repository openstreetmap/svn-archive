/*
 *  JOSMng - a Java Open Street Map editor, the next generation.
 * 
 *  Copyright (C) 2008 Petr Nejedly <P.Nejedly@sh.cvut.cz>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */

package org.openstreetmap.josmng.view;

import java.awt.Graphics;
import org.openstreetmap.josmng.osm.Bounds;

/**
 * A Layer is generic visualization of a single data source of some kind.
 * A MapView instance displays a stack of layers, so single layer shouldn't
 * try to clear the background during painting.
 * 
 * @author nenik
 */
public abstract class Layer {
    public final MapView parent;

    protected Layer(MapView parent) {
        this.parent = parent;
    }

    // painting needs projection and transformation.
    // both is available from MapView at the time of the paint
    // the layer instance can cache the transformed data
    public abstract void paint(Graphics g);
    
    public abstract String getName();
    
    /**
     * Compute the minimal bounds covering all the entities featured by this
     * layer. The call may take long time to return.
     * 
     * @return the computed Bounds of the layer.
     */
    public abstract Bounds getBounds();

}
