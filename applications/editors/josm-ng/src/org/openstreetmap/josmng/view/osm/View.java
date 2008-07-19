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

package org.openstreetmap.josmng.view.osm;

import org.openstreetmap.josmng.osm.OsmPrimitive;
import org.openstreetmap.josmng.view.BBox;
import org.openstreetmap.josmng.view.MapView;

/**
 * Visual representation of an OSM primitive. The visualization is actually
 * performed by passing a number of {@link Part}s to the {@link Drawer} during
 * the {@link #collect(org.openstreetmap.josmng.view.osm.Drawer, org.openstreetmap.josmng.view.MapView, boolean)}
 * callback.
 * 
 * @author nenik
 */
abstract class View<T extends OsmPrimitive> {
    private final T prim;
    private Style style;

    public View(T prim) {
        this.prim = prim;
    }
    
    public final T getPrimitive() {
        return prim;
    }

    public void resetStyle() {
        style = Style.get(this);
    }
    
    public void collect(Drawer drawer, MapView parent, boolean selected) {
        style.collect(drawer, parent, this, selected);
    }

    public int getMaxScale() {
        return style.getMaxScale();
    }
    
    public abstract BBox getBounds();
    
    public boolean intersects(BBox box) {
        return getBounds().intersects(box);
    }

    /**
     * Callback to tell the View to recalculate its bounds and,
     * if {@code members=true} also check its members (or {@link Way}'s
     * {@link Node}s) for a change.
     * 
     * @param parent the container used as a memeber and coordinate translator.
     * @param members
     */
    abstract void update(ViewData parent, boolean members);
}
