package org.openstreetmap.josm.plugins.elevation;

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
