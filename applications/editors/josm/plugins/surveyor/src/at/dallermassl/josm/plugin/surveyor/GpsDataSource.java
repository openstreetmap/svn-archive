/**
 * 
 */
package at.dallermassl.josm.plugin.surveyor;

import livegps.LiveGpsData;

/**
 * @author cdaller
 *
 */
public interface GpsDataSource {
    /**
     * Returns gps data.
     * @return gps data.
     */
    public LiveGpsData getGpsData();

}
