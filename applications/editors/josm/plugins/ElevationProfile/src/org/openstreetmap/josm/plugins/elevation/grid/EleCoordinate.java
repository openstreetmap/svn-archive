/**
 * This program is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU General Public License as published by the 
 * Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.openstreetmap.josm.plugins.elevation.grid;

import org.openstreetmap.josm.data.coor.LatLon;

public class EleCoordinate extends LatLon {    
    /**
     * 
     */
    private static final long serialVersionUID = 9121465585197496570L;
    
    private double ele = Double.NaN; 

    public EleCoordinate() {
	this(Double.NaN, Double.NaN, Double.NaN);
    }
    
    public EleCoordinate(double lat, double lon, double ele) {
	super(lat, lon);
	this.ele = ele;
    }
    
    public EleCoordinate(LatLon latLon, double ele) {
	this(latLon.lat(), latLon.lon(), ele);	
    }

    /**
     * Gets the elevation in meter.
     *
     * @return the ele
     */
    public double getEle() {
        return ele;
    }
  
    public String toString() {
        return "EleCoordinate[" + lat() + ", " + lon() + ", " + getEle() + "]";
    }
}
