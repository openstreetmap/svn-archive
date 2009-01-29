package org.openstreetmap.fma.jtiledownloader;

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
public interface Constants
{
    public static final String VERSION = "0-4-0";

    public static final double EARTH_CIRC_POLE = 40.007863 * Math.pow(10, 6);
    public static final double EARTH_CIRC_EQUATOR = 40.075016 * Math.pow(10, 6);
    public static final double MIN_LAT = -180;
    public static final double MAX_LAT = 180;
    public static final double MIN_LON = -85.0511;
    public static final double MAX_LON = 85.0511;

    public static final String[] INPUT_TAB_TYPE = new String[] {"Paste URL (Square)", "Bounding Box (Lat/Lon)", "Bounding Box (X/Y)" };
    public static final String[] CONFIG_TYPE = new String[] {"UrlSquare", "BBoxLatLon", "BBoxXY", "GPX" };
    public static final int TYPE_URLSQUARE = 0;
    public static final int TYPE_BOUNDINGBOX_LATLON = 1;
    public static final int TYPE_BOUNDINGBOX_XY = 2;
    public static final int TYPE_GPX = 3;

}
