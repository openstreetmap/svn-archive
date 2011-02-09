package org.openstreetmap.gui.jmapviewer;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class ScanexIRSTileSource extends OsmTileSource.AbstractOsmTileSource {
    private static String API_KEY = "4018C5A9AECAD8868ED5DEB2E41D09F7";
    private static String BASE_URI = "/TileSender.ashx?ModeKey=tile&MapName=F7B8CF651682420FA1749D894C8AD0F6&LayerName=BAC78D764F0443BD9AF93E7A998C9F5B";

    public ScanexIRSTileSource() {
        super("IRS satellite imagery", "http://maps.kosmosnimki.ru");
    }

    @Override
    public int getMaxZoom() {
        return 14;
    }

    @Override
    public String getExtension() {
        return("jpeg");
    }

    @Override
    public String getTilePath(int zoom, int tilex, int tiley) {
	int tmp = (int)Math.pow(2.0, zoom - 1);

	tilex = tilex - tmp;
	tiley = tmp - tiley - 1;

	return BASE_URI + "&apikey=" + API_KEY + "&x=" + tilex + "&y=" + tiley + "&z=" + zoom;
    }

    public TileUpdate getTileUpdate() {
        return TileUpdate.IfNoneMatch;
    }

    private static double RADIUS_E = 6378137;	/* radius of Earth at equator, m */
    private static double EQUATOR = 40075016.68557849; /* equator length, m */
    private static double E = 0.0818191908426;	/* eccentricity of Earth's ellipsoid */

    @Override
    public double latToTileY(double lat, int zoom) {
        double tmp = Math.tan(Math.PI/4 * (1 + lat/90));
        double pow = Math.pow(Math.tan(Math.PI/4 + Math.asin(E * Math.sin(Math.toRadians(lat)))/2), E);

	return (EQUATOR/2 - (RADIUS_E * Math.log(tmp/pow))) * Math.pow(2.0, zoom) / EQUATOR;
    }

    @Override
    public double lonToTileX(double lon, int zoom) {
	return (RADIUS_E * lon * Math.PI / (90*EQUATOR) + 1) * Math.pow(2.0, zoom - 1);
    }

    /*
     * DIRTY HACK ALERT!
     *
     * Until I can't solve the equation, use dihotomy :(
     */
    @Override
    public double tileYToLat(int y, int zoom) {
	double lat = 0;
	double minl = OsmMercator.MIN_LAT;
	double maxl = OsmMercator.MAX_LAT;
	double c;

	for (int i=0; i < 60; i++) {
		c = latToTileY(lat, zoom);
		if (c < y) {
			maxl = lat;
			lat -= (lat - minl)/2;
		} else {
			minl = lat;
			lat += (maxl - lat)/2;
		}
	}

	return lat;
    }

    @Override
    public double tileXToLon(int x, int zoom) {
        return (x / Math.pow(2.0, zoom - 1) - 1) * (90*EQUATOR) / RADIUS_E / Math.PI;
    }
}
