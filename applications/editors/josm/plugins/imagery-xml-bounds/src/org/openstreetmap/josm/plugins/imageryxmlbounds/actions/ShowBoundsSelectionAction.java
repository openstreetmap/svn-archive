// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.imageryxmlbounds.actions;

import java.util.Collection;

import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

/**
 *
 * @author Don-vip
 *
 */
public class ShowBoundsSelectionAction extends ShowBoundsAction implements SelectionChangedListener {

    /**
     * Constructs a new {@code ShowBoundsSelectionAction}.
     */
    public ShowBoundsSelectionAction() {
        putValue("toolbar", "xml-imagery-bounds");
    }

    @Override
    public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {
        updateOsmPrimitives(newSelection);
    }
}
