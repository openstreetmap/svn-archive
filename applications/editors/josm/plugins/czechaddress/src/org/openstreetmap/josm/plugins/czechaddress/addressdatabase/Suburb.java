package org.openstreetmap.josm.plugins.czechaddress.addressdatabase;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.czechaddress.PrimUtils;

/**
 * Suburb is a part of a {@link Municipality}.
 * 
 * @author Radomír Černoch radomir.cernoch@gmail.com
 */
public class Suburb extends ElementWithStreets {

    public Suburb(String name) {
        super(name);
    }

    public static boolean isMatchable(OsmPrimitive prim) {
        for (String key : prim.keySet())
            if (key.equals(PrimUtils.KEY_PLACE))
                return true;
        return false;
    }
}
