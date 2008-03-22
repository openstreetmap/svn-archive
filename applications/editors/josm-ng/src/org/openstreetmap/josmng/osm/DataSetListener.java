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

package org.openstreetmap.josmng.osm;

import java.util.Collection;
import java.util.EventListener;

/**
 * A listener listening for all DataSet changes.
 * INCOMPLETE (missing relation-related events)!
 * 
 * @author nenik
 */
public interface DataSetListener extends EventListener {
    /**
     * A bunch of primitives were added into the DataSet, or existing
     * deleted/invisible primitives were resurrected.
     * 
     * @param added A collection of newly-visible primitives
     */
    public void primtivesAdded(Collection<? extends OsmPrimitive> added);

    /**
     * A bunch of primitives were removed from the DataSet, or preexisting
     * primitives were marked as deleted.
     * 
     * @param removed A collection of newly-invisible primitives
     */
    public void primtivesRemoved(Collection<? extends OsmPrimitive> removed);
    
    /**
     * There was some change in the tag set of a primitive. It can have been
     * a tag addition, tag removal or change in tag value.
     * 
     * @param prim the primitive, whose tags were affected.
     */
    public void tagsChanged(OsmPrimitive prim);
    
    /**
     * A node's coordinates were modified.
     * @param node The node that was moved.
     */
    public void nodeMoved(Node node);
    
    /**
     * A way's node list was changed.
     * @param way The way that was modified.
     */
    public void wayNodesChanged(Way way);
}
