package org.openstreetmap.josm.plugins.czechaddress.intelligence;

import java.util.Collection;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.czechaddress.NotNullList;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.House;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.Street;

/**
 * Listenes to the current selection and updates all selected elements.
 *
 * <p>Currently JOSM has no way of giving notice about a changed or deleted
 * node. This class tries to overcome this at the cost of computational
 * inefficiency. It monitors every de-selected primitive.</p>
 *
 * @author Radomír Černoch, radomir.cernoch@gmail.com
 */
public class SelectionMonitor implements SelectionChangedListener {

    Collection<OsmPrimitive> lastSelection = new NotNullList<OsmPrimitive>();

    private SelectionMonitor() {}
    private static SelectionMonitor singleton = null;
    public  static SelectionMonitor getInstance() {
        if (singleton == null) {
            singleton = new SelectionMonitor();
            DataSet.addSelectionListener(singleton);
        }
        return singleton;
    }

    public void selectionChanged(Collection<? extends OsmPrimitive>
                                                                 newSelection) {
        Reasoner r = Reasoner.getInstance();

        synchronized(r) {
            r.openTransaction();
            for (OsmPrimitive selectedPrim :newSelection)
                if (House.isMatchable(selectedPrim) || Street.isMatchable(selectedPrim))
                    r.update(selectedPrim);
            for (OsmPrimitive selectedPrim :lastSelection)
                if (House.isMatchable(selectedPrim) || Street.isMatchable(selectedPrim))
                    r.update(selectedPrim);
            r.closeTransaction();
        }

        lastSelection.clear();
        for (OsmPrimitive prim : newSelection)
            lastSelection.add(prim);
    }
}
