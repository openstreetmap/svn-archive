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

import java.awt.Point;

import java.awt.Rectangle;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.openstreetmap.josmng.osm.Node;
import org.openstreetmap.josmng.osm.OsmPrimitive;
import org.openstreetmap.josmng.osm.Visitor;
import org.openstreetmap.josmng.utils.UndoHelper;

/**
 * A layer representing editable OSM data without any specific visualization.
 * INCOMPLETE!
 * TODO:
 *   - selection
 *   - dataset access
 * 
 * @author nenik
 */
public abstract class EditableLayer extends Layer {
    
    private Collection<OsmPrimitive> selection = new LinkedHashSet<OsmPrimitive>();

    protected EditableLayer(MapView parent) {
        super(parent);
    }

    public abstract boolean isEmpty();
    
    public abstract UndoHelper getUndoManager();

    public abstract Collection<OsmPrimitive> getPrimitivesInRect(Rectangle screenRect, boolean contained);
    
    /**
     * Get current selection. The returned set is a copy, so it can be freely
     * modified without affecting the actual selection. It can also be safely
     * iterated while changing the real selection.
     * The returned set keeps the order of adding primitives into the selection.
     * 
     * @return A snapshot of the current selection. 
     */
    public Set<OsmPrimitive> getSelection() {
        return new LinkedHashSet<OsmPrimitive>(selection);
    }
    
    public <V extends Visitor> V visitSelection(V visitor) {
        visitor.visitCollection(selection);
        return visitor;
    }
    
    public void setSelection(Collection<OsmPrimitive> newSelection) {
        selection.clear();
        selection.addAll(newSelection);
        // XXX: notify the change
        parent.repaint();
    }
    
    public void toggleSelected(OsmPrimitive prim) {
        if (selection.contains(prim)) {
            selection.remove(prim);
        } else {
            selection.add(prim);
        }
    }
    
}
