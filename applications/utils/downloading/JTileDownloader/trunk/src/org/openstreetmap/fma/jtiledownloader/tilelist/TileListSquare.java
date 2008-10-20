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

    private static final double MIN_LAT = -180;
    private static final double MAX_LAT = 180;
    private static final double MIN_LON = -85.0511;
    private static final double MAX_LON = 85.0511;

    private int _xTopLeft = 0;
    private int _yTopLeft = 0;
    private int _xBottomRight = 0;
    private int _yBottomRight = 0;

    private int _downloadZoomLevel;
    private int _radius; // radius in m
    private String _tileServerBaseUrl;
    private double _latitude;
    private double _longitude;

    public void calculateTileValuesXY()
    {

        log("calculate tile values for lat " + _latitude + ", lon " + _longitude + ", radius " + _radius);

        if (_radius > 6370000 * 2 * 4)
        {
            _radius = 6370000 * 2 * 4;
        }

        double minLat = _latitude - 360 * (_radius / EARTH_CIRC_POLE);
        double minLon = _longitude - 360 * (_radius / (EARTH_CIRC_EQUATOR * Math.cos(_longitude * Math.PI / 180)));
        double maxLat = _latitude + 360 * (_radius / EARTH_CIRC_POLE);
        double maxLon = _longitude + 360 * (_radius / (EARTH_CIRC_EQUATOR * Math.cos(_longitude * Math.PI / 180)));

        if (minLat < MIN_LAT)
        {
            minLat = MIN_LAT;
        }
        if (maxLat > MAX_LAT)
        {
            maxLat = MAX_LAT;
        }
        if (minLat > MAX_LAT)
        {
            minLat = MAX_LAT;
        }
        if (maxLat < MIN_LAT)
        {
            maxLat = MIN_LAT;
        }
        if (minLon < MIN_LON)
        {
            minLon = MIN_LON;
        }
        if (maxLon > MAX_LON)
        {
            maxLon = MAX_LON;
        }
        if (minLon > MAX_LON)
        {
            minLon = MAX_LON;
        }
        if (maxLon < MIN_LON)
        {
            maxLon = MIN_LON;
        }

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
    private static int calculateTileY(double lat, int zoomLevel)
    {
        int y = (int) Math.floor((1 - Math.log(Math.tan(lat * Math.PI / 180) + 1 / Math.cos(lat * Math.PI / 180)) / Math.PI) / 2 * (1 << zoomLevel));
        return y;
    }

    /**
     * @param lon
     * @param zoomLevel
     * @return
     */
    private static int calculateTileX(double lon, int zoomLevel)
    {
        int x = (int) Math.floor((lon + 180) / 360 * (1 << zoomLevel));
        return x;
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
