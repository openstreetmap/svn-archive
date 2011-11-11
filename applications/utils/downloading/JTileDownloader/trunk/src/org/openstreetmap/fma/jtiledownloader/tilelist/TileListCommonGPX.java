/*
 * Copyright 2009, Sven Strickroth <email@cs-ware.de>
 * 
 * This file is part of jTileDownloader.
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openstreetmap.fma.jtiledownloader.Constants;
import org.openstreetmap.fma.jtiledownloader.datatypes.Tile;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class TileListCommonGPX
    extends TileListCommon
{
    private final static Logger log = Logger.getLogger(TileListCommonGPX.class.getName());
    private ArrayList<Tile> tilesToDownload = new ArrayList<Tile>();

    public void updateList(String fileName, int corridorSize)
    {
        tilesToDownload.clear();
        File file = new File(fileName);
        if (file.exists() && file.isFile())
        {
            try
            {
                DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
                domFactory.setNamespaceAware(true); // never forget this!
                DocumentBuilder builder = domFactory.newDocumentBuilder();
                Document document = builder.parse(file);

                Node gpxNode = document.getFirstChild();
                if (!gpxNode.getNodeName().equalsIgnoreCase("gpx"))
                {
                    throw new RuntimeException("invalid file!");
                }

                //Object result = expr.evaluate(document, XPathConstants.NODESET);
                //NodeList nodes = (NodeList) result;
                NodeList nodes = gpxNode.getChildNodes();
                int detectedTrackNumber = 0;
                for (int i = 0; i < nodes.getLength(); i++)
                {
                    if (nodes.item(i).getLocalName() != null && nodes.item(i).getLocalName().equalsIgnoreCase("trk"))
                    {
                        detectedTrackNumber++;
                        //if (detectedTrackNumber == 1) {
                        // Download all zoomlevels
                        for (int zoomLevel : getDownloadZoomLevels())
                        {
                            // handle all trgSegments
                            NodeList trkSegs = nodes.item(i).getChildNodes();
                            for (int indexTrkSeg = 0; indexTrkSeg < trkSegs.getLength(); indexTrkSeg++)
                            {
                                if (trkSegs.item(indexTrkSeg).getLocalName() != null && trkSegs.item(indexTrkSeg).getLocalName().equalsIgnoreCase("trkseg"))
                                {
                                    // handle all trkpts
                                    NodeList trkPts = trkSegs.item(indexTrkSeg).getChildNodes();
                                    for (int indexTrkPt = 0; indexTrkPt < trkPts.getLength(); indexTrkPt++)
                                    {
                                        if (trkPts.item(indexTrkPt).getLocalName() != null && trkPts.item(indexTrkPt).getLocalName().equalsIgnoreCase("trkpt"))
                                        {
                                            handleTrkPt(trkPts.item(indexTrkPt), zoomLevel, corridorSize);
                                        }
                                    }
                                }
                            }
                        }
                        //}
                    }
                }
            }
            catch (SAXParseException spe)
            {
                Exception e = (spe.getException() != null) ? spe.getException() : spe;
                log.log(Level.SEVERE, "Error parsing " + spe.getSystemId() + " line " + spe.getLineNumber(), e);
            }
            catch (SAXException sxe)
            {
                Exception e = (sxe.getException() != null) ? sxe.getException() : sxe;
                log.log(Level.SEVERE, "Error parsing GPX", e);
            }
            catch (ParserConfigurationException pce)
            {
                log.log(Level.SEVERE, "Error in parser configuration", pce);
            }
            catch (IOException ioe)
            {
                log.log(Level.SEVERE, "Error parsing GPX", ioe);
            }
        }
    }

    private void handleTrkPt(Node item, int zoomLevel, int corridorSize)
    {
        NamedNodeMap attrs = item.getAttributes();
        if (attrs.getNamedItem("lat") != null && attrs.getNamedItem("lon") != null)
        {
            try
            {
                Double lat = Double.parseDouble(attrs.getNamedItem("lat").getTextContent());
                Double lon = Double.parseDouble(attrs.getNamedItem("lon").getTextContent());
                int minDownloadTileXIndex = 0;
                int maxDownloadTileXIndex = 0;
                int minDownloadTileYIndex = 0;
                int maxDownloadTileYIndex = 0;
                if (corridorSize > 0)
                {
                    double minLat = lat - 360 * (corridorSize * 1000 / Constants.EARTH_CIRC_POLE);
                    double minLon = lon - 360 * (corridorSize * 1000 / (Constants.EARTH_CIRC_EQUATOR * Math.cos(lon * Math.PI / 180)));
                    double maxLat = lat + 360 * (corridorSize * 1000 / Constants.EARTH_CIRC_POLE);
                    double maxLon = lon + 360 * (corridorSize * 1000 / (Constants.EARTH_CIRC_EQUATOR * Math.cos(lon * Math.PI / 180)));
                    minDownloadTileXIndex = calculateTileX(minLon, zoomLevel);
                    maxDownloadTileXIndex = calculateTileX(maxLon, zoomLevel);
                    minDownloadTileYIndex = calculateTileY(minLat, zoomLevel);
                    maxDownloadTileYIndex = calculateTileY(maxLat, zoomLevel);
                }
                else
                {
                    minDownloadTileXIndex = calculateTileX(lon, zoomLevel);
                    maxDownloadTileXIndex = minDownloadTileXIndex;
                    minDownloadTileYIndex = calculateTileY(lat, zoomLevel);
                    maxDownloadTileYIndex = minDownloadTileYIndex;
                }

                for (int tileXIndex = Math.min(minDownloadTileXIndex, maxDownloadTileXIndex); tileXIndex <= Math.max(minDownloadTileXIndex, maxDownloadTileXIndex); tileXIndex++)
                {
                    for (int tileYIndex = Math.min(minDownloadTileYIndex, maxDownloadTileYIndex); tileYIndex <= Math.max(minDownloadTileYIndex, maxDownloadTileYIndex); tileYIndex++)
                    {
                        Tile tile = new Tile(tileXIndex, tileYIndex, zoomLevel);
                        if (!tilesToDownload.contains(tile))
                        {
                            log.fine("add " + tile + " to download list.");
                            tilesToDownload.add(tile);
                        }
                    }

                }
            }
            catch (NumberFormatException e)
            {
                // ignore ;)
            }
        }
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.tilelist.TileList#getTileListToDownload()
     */
    public ArrayList<Tile> getTileListToDownload()
    {
        return tilesToDownload;
    }
}
