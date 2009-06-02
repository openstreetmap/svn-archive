package org.openstreetmap.josm.plugins.czechaddress.intelligence;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.AddressElement;

/**
 *
 * @author Radomír Černoch, radomir.cernoch@gmail.com
 */
public interface ReasonerListener {

    public void elementChanged(AddressElement elem);
    public void primitiveChanged(OsmPrimitive prim);
    public void resonerReseted();

}
