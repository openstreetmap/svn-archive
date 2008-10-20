package org.openstreetmap.fma.jtiledownloader.tilelist;

import java.util.Vector;

/**
 * developed by fma (http://wiki.openstreetmap.org/index.php/User:Fma)
 * license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
public class TileListSquare
    implements TileList
{
    private static final double EARTH_CIRC_POLE = 40.007863 * Math.pow(10, 6);
    private static final double EARTH_CIRC_EQUATOR = 40.075016 * Math.pow(10, 6);

    private long _xTopLeft = 0;
    private long _yTopLeft = 0;
    private long _xBottomRight = 0;
    private long _yBottomRight = 0;

    private int _downloadZoomLevel;
    private int _radius; // radius in m
    private String _tileServerBaseUrl;
    private double _latitude;
    private double _longitude;

    public void calculateTileValuesXY()
    {

        log("calculate tile values for lat " + _latitude + ", lon " + _longitude + ", radius " + _radius);

        if (_radius > 6370000)
        {
            _radius = 6370000;
        }

        double minLat = _latitude - 360 * (_radius / EARTH_CIRC_POLE);
        double minLon = _longitude - 360 * (_radius / (EARTH_CIRC_EQUATOR * Math.cos(_longitude * Math.PI / 180)));
        double maxLat = _latitude + 360 * (_radius / EARTH_CIRC_POLE);
        double maxLon = _longitude + 360 * (_radius / (EARTH_CIRC_EQUATOR * Math.cos(_longitude * Math.PI / 180)));

        //        if (minLat < -90)
        //        {
        //            minLat = -90;
        //        }
        //        if (maxLat > 90)
        //        {
        //            maxLat = 90;
        //        }
        //        if (minLat > 90)
        //        {
        //            minLat = 90;
        //        }
        //        if (maxLat < -90)
        //        {
        //            maxLat = -90;
        //        }
        //        if (minLon < -180)
        //        {
        //            minLon = -180;
        //        }
        //        if (maxLon > 180)
        //        {
        //            maxLon = 180;
        //        }
        //        if (minLon > 180)
        //        {
        //            minLon = 180;
        //        }
        //        if (maxLon < -180)
        //        {
        //            maxLon = -180;
        //        }

        log("minLat=" + minLat);
        log("minLon=" + minLon);
        log("maxLat=" + maxLat);
        log("maxLon=" + maxLon);

        _xTopLeft = calculateTileX(minLon, _downloadZoomLevel);
        _yTopLeft = calculateTileY(maxLat, _downloadZoomLevel);
        _xBottomRight = calculateTileX(maxLon, _downloadZoomLevel);
        _yBottomRight = calculateTileY(minLat, _downloadZoomLevel);

        log("_xTopLeft=" + _xTopLeft);
        log("_yTopLeft=" + _yTopLeft);
        log("_xBottomRight=" + _xBottomRight);
        log("_yBottomRight=" + _yBottomRight);
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.tilelist.TileList#getFileListToDownload()
     * {@inheritDoc}
     */
    public Vector getFileListToDownload()
    {
        Vector tilesToDownload = new Vector();

        long xStart = getMin(_xTopLeft, _xBottomRight);
        long xEnd = getMax(_xTopLeft, _xBottomRight);

        long yStart = getMin(_yTopLeft, _yBottomRight);
        long yEnd = getMax(_yTopLeft, _yBottomRight);

        for (long downloadTileXIndex = xStart; downloadTileXIndex <= xEnd; downloadTileXIndex++)
        {
            for (long downloadTileYIndex = yStart; downloadTileYIndex <= yEnd; downloadTileYIndex++)
            {
                String urlPathToFile = getTileServerBaseUrl() + _downloadZoomLevel + "/" + downloadTileXIndex + "/" + downloadTileYIndex + ".png";

                log("add " + urlPathToFile + " to download list.");
                tilesToDownload.addElement(urlPathToFile);
            }
        }
        log("finished");

        return tilesToDownload;

    }

    /**
     * @param topLeft
     * @param bottomRight
     * @return
     */
    private long getMax(long topLeft, long bottomRight)
    {
        if (topLeft > bottomRight)
        {
            return topLeft;
        }
        return bottomRight;
    }

    /**
     * @param topLeft
     * @param bottomRight
     * @return
     */
    private long getMin(long topLeft, long bottomRight)
    {
        if (topLeft > bottomRight)
        {
            return bottomRight;
        }
        return topLeft;
    }

    /**
     * @param lat
     * @param zoomLevel
     * @return
     */
    private static long calculateTileY(double lat, double zoomLevel)
    {
        double y = ((1 - Math.log(Math.tan(lat * Math.PI / 180) + 1 / Math.cos(lat * Math.PI / 180)) / Math.PI) / 2) * Math.pow(2, zoomLevel);
        long value = new Double(Math.floor(y)).longValue();
        return value;
    }

    /**
     * @param lon
     * @param zoomLevel
     * @return
     */
    private static long calculateTileX(double lon, double zoomLevel)
    {
        double x = ((lon + 180) / 360) * Math.pow(2, zoomLevel);
        long value = new Double(Math.floor(x)).longValue();
        return value;
    }

    /**
     * method to write to System.out
     * 
     * @param msg message to log
     */
    private static void log(String msg)
    {
        System.out.println(msg);
    }

    /**
     * Getter for downloadZoomLevel
     * @return the downloadZoomLevel
     */
    protected final int getDownloadZoomLevel()
    {
        return _downloadZoomLevel;
    }

    /**
     * Setter for downloadZoomLevel
     * @param downloadZoomLevel the downloadZoomLevel to set
     */
    public final void setDownloadZoomLevel(int downloadZoomLevel)
    {
        _downloadZoomLevel = downloadZoomLevel;
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
     * Getter for tileServerBaseUrl
     * @return the tileServerBaseUrl
     */
    protected final String getTileServerBaseUrl()
    {
        return _tileServerBaseUrl;
    }

    /**
     * Setter for tileServerBaseUrl
     * @param tileServerBaseUrl the tileServerBaseUrl to set
     */
    public final void setTileServerBaseUrl(String tileServerBaseUrl)
    {
        _tileServerBaseUrl = tileServerBaseUrl;
    }

    /**
     * Getter for xTopLeft
     * @return the xTopLeft
     */
    public final long getXTopLeft()
    {
        return _xTopLeft;
    }

    /**
     * Getter for yTopLeft
     * @return the yTopLeft
     */
    public final long getYTopLeft()
    {
        return _yTopLeft;
    }

    /**
     * Getter for xBottomRight
     * @return the xBottomRight
     */
    public final long getXBottomRight()
    {
        return _xBottomRight;
    }

    /**
     * Getter for yBottomRight
     * @return the yBottomRight
     */
    public final long getYBottomRight()
    {
        return _yBottomRight;
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
