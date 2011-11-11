/*
 * Copyright 2008, Friedrich Maier
 * 
 * This file is part of JTileDownloader.
 * (see http://wiki.openstreetmap.org/index.php/JTileDownloader)
 *
 * JTileDownloader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JTileDownloader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy (see file COPYING.txt) of the GNU 
 * General Public License along with JTileDownloader.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.openstreetmap.fma.jtiledownloader.tilelist;

import java.util.logging.Logger;

public class TileListBBoxLatLon
    extends TileListCommonBBox
{
    private static final Logger log = Logger.getLogger(TileListBBoxLatLon.class.getName());
    private double _minLat;
    private double _minLon;
    private double _maxLat;
    private double _maxLon;

    public void calculateTileValuesXY()
    {

        log.fine("calculate tile values for (BBoxLatLon):");

        log.fine("minLat=" + _minLat);
        log.fine("minLon=" + _minLon);
        log.fine("maxLat=" + _maxLat);
        log.fine("maxLon=" + _maxLon);

        calculateTileValuesXY(_minLat, _minLon, _maxLat, _maxLon);
    }

    /**
     * Getter for minLat
     * @return the minLat
     */
    public final double getMinLat()
    {
        return _minLat;
    }

    /**
     * Setter for minLat
     * @param minLat the minLat to set
     */
    public final void setMinLat(double minLat)
    {
        _minLat = minLat;
    }

    /**
     * Getter for minLon
     * @return the minLon
     */
    public final double getMinLon()
    {
        return _minLon;
    }

    /**
     * Setter for minLon
     * @param minLon the minLon to set
     */
    public final void setMinLon(double minLon)
    {
        _minLon = minLon;
    }

    /**
     * Getter for maxLat
     * @return the maxLat
     */
    public final double getMaxLat()
    {
        return _maxLat;
    }

    /**
     * Setter for maxLat
     * @param maxLat the maxLat to set
     */
    public final void setMaxLat(double maxLat)
    {
        _maxLat = maxLat;
    }

    /**
     * Getter for maxLon
     * @return the maxLon
     */
    public final double getMaxLon()
    {
        return _maxLon;
    }

    /**
     * Setter for maxLon
     * @param maxLon the maxLon to set
     */
    public final void setMaxLon(double maxLon)
    {
        _maxLon = maxLon;
    }

}
