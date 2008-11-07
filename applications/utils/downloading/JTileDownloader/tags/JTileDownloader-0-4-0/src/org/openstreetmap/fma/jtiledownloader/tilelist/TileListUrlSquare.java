package org.openstreetmap.fma.jtiledownloader.tilelist;

/**
 * Copyright 2008, Friedrich Maier 
 * 
 * This file is part of JTileDownloader. 
 * (see http://wiki.openstreetmap.org/index.php/JTileDownloader)
 *
 *    JTileDownloader is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    JTileDownloader is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy (see file COPYING.txt) of the GNU 
 *    General Public License along with JTileDownloader.  
 *    If not, see <http://www.gnu.org/licenses/>.
 */
public class TileListUrlSquare
    extends TileListCommonBBox
{

    private int _radius; // radius in m
    private double _latitude;
    private double _longitude;

    public void calculateTileValuesXY()
    {

        log("calculate tile values for (UrlSquare:) lat " + _latitude + ", lon " + _longitude + ", radius " + _radius);

        if (_radius > 6370000 * 2 * 4)
        {
            _radius = 6370000 * 2 * 4;
        }

        double minLat = _latitude - 360 * (_radius / EARTH_CIRC_POLE);
        double minLon = _longitude - 360 * (_radius / (EARTH_CIRC_EQUATOR * Math.cos(_longitude * Math.PI / 180)));
        double maxLat = _latitude + 360 * (_radius / EARTH_CIRC_POLE);
        double maxLon = _longitude + 360 * (_radius / (EARTH_CIRC_EQUATOR * Math.cos(_longitude * Math.PI / 180)));

        log("minLat=" + minLat);
        log("minLon=" + minLon);
        log("maxLat=" + maxLat);
        log("maxLon=" + maxLon);

        calculateTileValuesXY(minLat, minLon, maxLat, maxLon);

    }

    /**
     * Getter for radius
     * @return the radius
     */
    protected final int getRadius()
    {
        return _radius;
    }

    /**
     * Setter for radius in meter
     * @param radius the radius to set
     */
    public final void setRadius(int radius)
    {
        _radius = radius;
    }

    /**
     * Getter for latitude
     * @return the latitude
     */
    protected final double getLatitude()
    {
        return _latitude;
    }

    /**
     * Setter for latitude
     * @param latitude the latitude to set
     */
    public final void setLatitude(double latitude)
    {
        _latitude = latitude;
    }

    /**
     * Getter for longitude
     * @return the longitude
     */
    protected final double getLongitude()
    {
        return _longitude;
    }

    /**
     * Setter for longitude
     * @param longitude the longitude to set
     */
    public final void setLongitude(double longitude)
    {
        _longitude = longitude;
    }

}
