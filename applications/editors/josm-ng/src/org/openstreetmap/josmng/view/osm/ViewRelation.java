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


import java.util.HashMap;
import java.util.Map;
import org.openstreetmap.josmng.osm.OsmPrimitive;
import org.openstreetmap.josmng.osm.Relation;
import org.openstreetmap.josmng.view.*;

/**
 * View for a Relation.
 * Relation might have specific rendering and suppress rendering some of its
 * members.
 * Caches a bbox that covers all the members.
 * 
 * @author nenik
 */
class ViewRelation extends View<Relation> {
    BBox bbox;
    Map<View, String> translatedMembers = new HashMap<View, String>();

    public ViewRelation(Relation relation, ViewData parent) {
        super(relation);
        update(parent, true);
        resetStyle();
    }

    void update(ViewData parent, boolean members) {
        if (members) {
            translatedMembers.clear();
            for (Map.Entry<OsmPrimitive, String> e : getPrimitive().getMembers().entrySet()) {
                View v = parent.add(e.getKey());
                if (v == null) continue;
                translatedMembers.put(v, e.getValue());
            }
        }
        
        bbox = new BBox();
        for (View view : translatedMembers.keySet()) bbox.add(view.getBounds());
    }        

    public int getSize() {
        return (int)Math.min(bbox.getWidth() + bbox.getHeight(), Integer.MAX_VALUE);
    }

    public @Override int getMaxScale() {
        return Math.min(super.getMaxScale(), getSize() / 3);
    }

    public BBox getBounds() {
        return bbox;
    }
}
