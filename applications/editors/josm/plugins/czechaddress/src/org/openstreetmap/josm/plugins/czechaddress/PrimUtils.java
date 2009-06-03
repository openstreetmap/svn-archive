/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openstreetmap.josm.plugins.czechaddress;

import java.util.Comparator;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

/**
 *
 * @author Radomír Černoch, radomir.cernoch@gmail.com
 */
public class PrimUtils {

    public static final String KEY_HIGHWAY      = "highway";
    public static final String KEY_PLACE        = "place";

    public static final String KEY_ADDR_CP      = "addr:alternatenumber";
    public static final String KEY_ADDR_CO      = "addr:housenumber";
    public static final String KEY_ADDR_STREET  = "addr:street";
    public static final String KEY_ADDR_CITY    = "addr:city";
    public static final String KEY_ADDR_COUNTRY = "addr:country";
    public static final String KEY_IS_IN        = "is_in";
    public static final String KEY_NAME         = "name";


    private static final String[] keysToCompare = new String[]
        {KEY_PLACE, KEY_NAME, KEY_ADDR_COUNTRY, KEY_ADDR_CITY, KEY_IS_IN,
         KEY_ADDR_STREET, KEY_ADDR_CO, KEY_ADDR_CP };

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
                if (val != 0) {
                    return val;
                }
            }

            return o1.toString().compareTo(o2.toString());

            /*LatLon pos1 = null;
            LatLon pos2 = null;
            if (o1 instanceof Node) pos1 = ((Node) o1).coor;
            if (o1 instanceof Way)  pos1 = ((Way)  o1).lastNode().coor;
            if (o2 instanceof Node) pos1 = ((Node) o2).coor;
            if (o2 instanceof Way)  pos1 = ((Way)  o2).lastNode().coor;

            if (pos1 != null && pos2 != null) {
            if (pos1.lat() < pos2.lat()) return -1;
            if (pos1.lat() > pos2.lat()) return  1;
            if (pos1.lon() < pos2.lon()) return -1;
            if (pos1.lon() > pos2.lon()) return  1;
            }*/
        }
    };

}
