package org.openstreetmap.josm.plugins.piclayer.layer.kml;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class KMLHandler extends DefaultHandler {

    private boolean inGroundOverlay = false;
    private boolean inIcon = false;
    private boolean inLatLonBox = false;
    private String value;
    private List<KMLGroundOverlay> result;
    private KMLGroundOverlay overlay;

    KMLHandler() {
        result = new ArrayList<>();
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        if ("GroundOverlay".equals(localName)) {
            inGroundOverlay = true;
            overlay = new KMLGroundOverlay();
        } else if (inGroundOverlay && "Icon".equals(localName)) {
            inIcon = true;
        } else if (inGroundOverlay && "LatLonBox".equals(localName)) {
            inLatLonBox = true;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (inGroundOverlay && "name".equals(localName)) {
            overlay.setName(value.trim());
        } else if (inIcon && "href".equals(localName)) {
            overlay.setFileName(value.trim());
        } else if (inLatLonBox && "north".equals(localName)) {
            overlay.setNorth(Double.valueOf(value.trim()));
        } else if (inLatLonBox && "east".equals(localName)) {
            overlay.setEast(Double.valueOf(value.trim()));
        } else if (inLatLonBox && "south".equals(localName)) {
            overlay.setSouth(Double.valueOf(value.trim()));
        } else if (inLatLonBox && "west".equals(localName)) {
            overlay.setWest(Double.valueOf(value.trim()));
        } else if (inLatLonBox && "rotation".equals(localName)) {
            overlay.setRotate(Double.valueOf(value.trim()));
        } else if (inLatLonBox && "LatLonBox".equals(localName)) {
            inLatLonBox = false;
        } else if (inIcon && "Icon".equals(localName)) {
            inIcon = false;
        } else if (inGroundOverlay && "GroundOverlay".equals(localName)) {
            inGroundOverlay = false;
            result.add(overlay);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        value = new String(ch, start, length);
    }

    public List<KMLGroundOverlay> getResult() {
        return result;
    }
}