// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.czechaddress.intelligence;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.AddressElement;

/**
 * Interface capable of sensing changes in the {@link Reasoner}.
 *
 * @author Radomír Černoch, radomir.cernoch@gmail.com
 */
public interface ReasonerListener {

    void elementChanged(AddressElement elem);

    void primitiveChanged(OsmPrimitive prim);

    void resonerReseted();
}
