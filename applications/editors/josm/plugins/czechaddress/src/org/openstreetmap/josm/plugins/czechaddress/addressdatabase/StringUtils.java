/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openstreetmap.josm.plugins.czechaddress.addressdatabase;

import org.openstreetmap.josm.data.coor.LatLon;

/**
 *
 * @author radek
 */
public class StringUtils {

    public static String coordinateToString(double coor) {
        double degrees = Math.floor(coor);
        double minutes = Math.floor( 60*(coor-degrees) );
        double seconds = 60*60*(coor-degrees-minutes/60);
        
        return String.valueOf(Math.round(    degrees))     + "°" +
               String.valueOf(Math.round(    minutes))     + "'" +
               String.valueOf(Math.round(100*seconds)/100.0) + "\"";
    }

    public static String latLonToString(LatLon position) {
        if (position == null) return "";

        return "(lat: " + coordinateToString(position.lat())
             + " lon: " + coordinateToString(position.lon()) + ")";
    }

}
