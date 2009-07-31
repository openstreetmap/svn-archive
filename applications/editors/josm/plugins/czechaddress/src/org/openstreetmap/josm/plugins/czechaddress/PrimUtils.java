package org.openstreetmap.josm.plugins.czechaddress;

import java.util.Comparator;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

/**
 * Utilities for handling {@link OsmPrimitive}s.
 * 
 * @author Radomír Černoch, radomir.cernoch@gmail.com
 */
public class PrimUtils {

    public static final String KEY_HIGHWAY      = "highway";
    public static final String KEY_PLACE        = "place";

    public static final String KEY_ADDR_CP      = "addr:conscriptionnumber";
    public static final String KEY_ADDR_CO      = "addr:streetnumber";
    public static final String KEY_ADDR_HOUSE_N = "addr:housenumber";
    public static final String KEY_ADDR_STREET  = "addr:street";
    public static final String KEY_ADDR_CITY    = "addr:city";
    public static final String KEY_ADDR_COUNTRY = "addr:country";
    public static final String KEY_IS_IN        = "is_in";
    public static final String KEY_NAME         = "name";


    private static final String[] keysToCompare = new String[]
        {KEY_PLACE, KEY_NAME, KEY_ADDR_COUNTRY, KEY_ADDR_CITY, KEY_IS_IN,
         KEY_ADDR_STREET, KEY_ADDR_CO, KEY_ADDR_CP };

    /**
     * Comparator for {@link OsmPrimitive}, which looks at 'addr:*' tags.
     */
    public static final Comparator<OsmPrimitive> comparator =
        new Comparator<OsmPrimitive>() {
        public int compare(OsmPrimitive o1, OsmPrimitive o2) {

            for (String key : keysToCompare) {
                if (o1.get(key) == null) {
                    continue;
                }
                if (o2.get(key) == null) {
                    continue;
                }

                int val = o1.get(key).compareTo(o2.get(key));
                if (val != 0) return val;
            }

            /*int val = o1.toString().compareTo(o2.toString());
            if (val != 0) return val;*/

            return o1.compareTo(o2);
        }
    };

}
