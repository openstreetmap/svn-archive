// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.imageryxmlbounds.actions;

import org.openstreetmap.josm.data.osm.DataSelectionListener;

/**
 *
 * @author Don-vip
 *
 */
public class ShowBoundsSelectionAction extends ShowBoundsAction implements DataSelectionListener {

    /**
     * Constructs a new {@code ShowBoundsSelectionAction}.
     */
    public ShowBoundsSelectionAction() {
        putValue("toolbar", "xml-imagery-bounds");
    }

    @Override
    public void selectionChanged(SelectionChangeEvent event) {
        updateOsmPrimitives(event.getSelection());
    }
}
