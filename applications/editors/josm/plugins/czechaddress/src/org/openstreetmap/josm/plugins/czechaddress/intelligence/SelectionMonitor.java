package org.openstreetmap.josm.plugins.czechaddress.intelligence;

import java.util.Collection;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.czechaddress.CzechAddressPlugin;
import org.openstreetmap.josm.plugins.czechaddress.NotNullList;

/**
 * Listenes to the current selection for reasoning
 * 
 * <p>Currently JOSM has no way of giving notice about a changed or deleted
 * node. This class tries to overcome this at the cost of computational
 * inefficiency. It monitors every de-selected primitive.</p>
 *
 * @author Radomír Černoch, radomir.cernoch@gmail.com
 */
public class SelectionMonitor implements SelectionChangedListener {

    NotNullList<OsmPrimitive> lastSelection = new NotNullList<OsmPrimitive>();

    public SelectionMonitor() {
        DataSet.selListeners.add(this);
    }

    public void selectionChanged(
                            Collection<? extends OsmPrimitive> newSelection) {
        Reasoner r = CzechAddressPlugin.getReasoner();

        r.addPrimitives(lastSelection);

        lastSelection.clear();
        for (OsmPrimitive prim : newSelection)
            lastSelection.add(prim);
    }
}
