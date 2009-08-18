/**
 * Copyright 2009, Sven Strickroth <email@cs-ware.de>
 * 
 * This file is part of jTileDownloader.
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

package org.openstreetmap.fma.jtiledownloader.tilelist;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class TileListCommonGPX
    extends TileListCommon
{
    private Vector<String> tilesToDownload = new Vector<String>();

    public void updateList(String fileName)
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
                            for (int j = 0; j < trkSegs.getLength(); j++)
                            {
                                if (trkSegs.item(j).getLocalName() != null && trkSegs.item(j).getLocalName().equalsIgnoreCase("trkseg"))
                                {
                                    // handle all trkpts
                                    NodeList trkPts = trkSegs.item(j).getChildNodes();
                                    for (int k = 0; k < trkPts.getLength(); k++)
                                    {
                                        if (trkPts.item(k).getLocalName() != null && trkPts.item(k).getLocalName().equalsIgnoreCase("trkpt"))
                                        {
                                            handleTrkPt(trkPts.item(k), zoomLevel);
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
                System.out.println("\n** Parsing error, line " + spe.getLineNumber() + ", uri " + spe.getSystemId());
                System.out.println("   " + spe.getMessage());
                Exception e = (spe.getException() != null) ? spe.getException() : spe;
                e.printStackTrace();
            }
            catch (SAXException sxe)
            {
                Exception e = (sxe.getException() != null) ? sxe.getException() : sxe;
                e.printStackTrace();
            }
            catch (ParserConfigurationException pce)
            {
                pce.printStackTrace();
            }
            catch (IOException ioe)
            {
                ioe.printStackTrace();
            }
        }
        if (tilesToDownload.isEmpty())
        {
            tilesToDownload.add(getTileServerBaseUrl() + "0/0/0.png");
        }
    }

    private void handleTrkPt(Node item, int zoomLevel)
    {
        NamedNodeMap attrs = item.getAttributes();
        if (attrs.getNamedItem("lat") != null && attrs.getNamedItem("lon") != null)
        {
            try
            {
                Double lat = Double.parseDouble(attrs.getNamedItem("lat").getTextContent());
                Double lon = Double.parseDouble(attrs.getNamedItem("lon").getTextContent());
                int downloadTileXIndex = calculateTileX(lon, zoomLevel);
                int downloadTileYIndex = calculateTileY(lat, zoomLevel);
                String urlPathToFile = getTileServerBaseUrl() + zoomLevel + "/" + downloadTileXIndex + "/" + downloadTileYIndex + ".png";
                if (!tilesToDownload.contains(urlPathToFile))
                {
                    log("add " + urlPathToFile + " to download list.");
                    tilesToDownload.add(urlPathToFile);
                }
            }
            catch (NumberFormatException e)
            {
                // ignore ;)
            }
        }
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.tilelist.TileList#getFileListToDownload()
     * {@inheritDoc}
     */
    public Vector<String> getFileListToDownload()
    {
        return tilesToDownload;
    }
}
