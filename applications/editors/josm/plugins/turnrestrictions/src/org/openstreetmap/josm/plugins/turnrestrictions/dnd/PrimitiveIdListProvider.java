package org.openstreetmap.josm.plugins.turnrestrictions.dnd;

import java.util.List;

import org.openstreetmap.josm.data.osm.PrimitiveId;
public interface PrimitiveIdListProvider {
    /**
     * Replies the list of currently selected primitive IDs. Replies an empty list if no primitive IDs
     * are selected.
     * 
     * @return the list of currently selected primitive IDs
     */
    List<PrimitiveId> getSelectedPrimitiveIds();
}
