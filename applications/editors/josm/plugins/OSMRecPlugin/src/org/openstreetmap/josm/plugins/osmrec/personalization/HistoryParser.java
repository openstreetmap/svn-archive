// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.osmrec.personalization;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeocentricCRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.openstreetmap.josm.io.OsmApi;
import org.openstreetmap.josm.plugins.osmrec.container.OSMNode;
import org.openstreetmap.josm.plugins.osmrec.container.OSMWay;
import org.openstreetmap.josm.tools.HttpClient;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.XmlUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Parses the history of an OSM user's changesets using OSM API.
 *
 * @author imis-nkarag
 */
public class HistoryParser {
    private static final String OSM_API = OsmApi.getOsmApi().getBaseUrl();
    private static final CoordinateReferenceSystem sourceCRS = DefaultGeographicCRS.WGS84;
    private static final CoordinateReferenceSystem targetCRS = DefaultGeocentricCRS.CARTESIAN;
    private static final GeometryFactory geometryFactory = new GeometryFactory();
    private MathTransform transform;
    private OSMNode nodeTmp;

    private final List<OSMNode> nodeList;
    private final Map<String, OSMNode> nodesWithIDs;
    private final List<OSMWay> wayList;
    private OSMWay wayTmp;
    private final String username;

    /**
     * Constructs a new {@code HistoryParser}.
     * @param username user name
     */
    public HistoryParser(String username) {
        this.username = username;
        transform = null;
        try {
            transform = CRS.findMathTransform(sourceCRS, targetCRS, true);
        } catch (FactoryException ex) {
            Logger.getLogger(HistoryParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        nodeList = new ArrayList<>();
        nodesWithIDs = new HashMap<>();
        wayList = new ArrayList<>();
    }

    public void historyParse(String timeInterval) {

        HashSet<String> changesetIDsList = new HashSet<>();

        try {
            String osmUrl = OSM_API + "changesets?display_name=" + username + "&time=" + timeInterval;
            InputStream xml = HttpClient.create(new URL(osmUrl)).connect().getContent();
            NodeList nodes = XmlUtils.parseSafeDOM(xml).getElementsByTagName("changeset");

            Logging.debug("changeset size "+ nodes.getLength());
            for (int i = 0; i < nodes.getLength(); i++) {
                Logging.debug("attributes of " + i + "th changeset");
                String id = nodes.item(i).getAttributes().item(3).toString();
                Logging.debug("id:" + nodes.item(i).getAttributes().item(3));
                id = stripQuotes(id);
                changesetIDsList.add(id);
            }

            for (String id : changesetIDsList) {
                getChangesetByID(id);
            }
        } catch (IOException | ParserConfigurationException | SAXException ex) {
            Logger.getLogger(HistoryParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void getChangesetByID(String id) {
        try {
            String changesetByIDURL = OSM_API+ "changeset/" + id + "/download";
            InputStream xml = HttpClient.create(new URL(changesetByIDURL)).connect().getContent();
            Node osmChange = XmlUtils.parseSafeDOM(xml).getFirstChild();

            //get all nodes first, in order to be able to call all nodes references and create the geometries
            for (int i = 0; i < osmChange.getChildNodes().getLength(); i++) {
                String changeType = osmChange.getChildNodes().item(i).getNodeName();
                if (!(changeType.equals("#text") || changeType.equals("delete"))) {

                    NodeList changeChilds = osmChange.getChildNodes().item(i).getChildNodes();

                    Node osmObject = changeChilds.item(1);

                    if (osmObject.getNodeName().equals("node")) {
                        //node data
                        nodeTmp = new OSMNode();
                        nodeTmp.setID(osmObject.getAttributes().getNamedItem("id").getNodeValue());

                        //parse geometry
                        double longitude = Double.parseDouble(osmObject.getAttributes().getNamedItem("lon").getNodeValue());
                        double latitude = Double.parseDouble(osmObject.getAttributes().getNamedItem("lat").getNodeValue());
                        Coordinate targetGeometry = null;
                        Coordinate sourceCoordinate = new Coordinate(longitude, latitude);
                        try {
                            targetGeometry = JTS.transform(sourceCoordinate, null, transform);
                        } catch (MismatchedDimensionException | TransformException ex) {
                            Logger.getLogger(HistoryParser.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        //create geometry object
                        Geometry geom = geometryFactory.createPoint(new Coordinate(targetGeometry));
                        nodeTmp.setGeometry(geom);

                        nodeList.add(nodeTmp);
                        nodesWithIDs.put(nodeTmp.getID(), nodeTmp);
                    }
                }
            }

            for (int i = 0; i < osmChange.getChildNodes().getLength(); i++) {
                String changeType = osmChange.getChildNodes().item(i).getNodeName();
                if (!(changeType.equals("#text") || changeType.equals("delete"))) {
                    NodeList changeChilds = osmChange.getChildNodes().item(i).getChildNodes();

                    Node osmObject = changeChilds.item(1);
                    if (osmObject.getNodeName().equals("way")) {

                        //get way data
                        wayTmp = new OSMWay();
                        wayTmp.setID(osmObject.getAttributes().getNamedItem("id").getNodeValue());
                        // extract tags, then set tags to osm object
                        Logging.debug("\n\nWAY: " + wayTmp.getID());
                        for (int l = 0; l < osmObject.getChildNodes().getLength(); l++) {
                            String wayChild = osmObject.getChildNodes().item(l).getNodeName();

                            if (wayChild.equals("tag")) {
                                String key = osmObject.getChildNodes().item(l).getAttributes().getNamedItem("k").getNodeValue();
                                String value = osmObject.getChildNodes().item(l).getAttributes().getNamedItem("v").getNodeValue();
                                System.out.println("key: " + key + " value: " + value);
                                wayTmp.setTagKeyValue(key, value);
                            } else if (wayChild.equals("nd")) {
                                wayTmp.addNodeReference(osmObject.getChildNodes().item(l).getAttributes().getNamedItem("ref").getNodeValue());
                            }
                        }

                        //construct the Way geometry from each node of the node references
                        List<String> references = wayTmp.getNodeReferences();

                        for (String entry: references) {
                            if (nodesWithIDs.containsKey(entry)) {
                                Geometry geometry = nodesWithIDs.get(entry).getGeometry(); //get the geometry of the node with ID=entry
                                wayTmp.addNodeGeometry(geometry); //add the node geometry in this way
                            } else {
                                Logging.debug("nodes with ids, no entry " + entry);
                                getNodeFromAPI(entry);
                            }
                        }

                        Geometry geom = geometryFactory.buildGeometry(wayTmp.getNodeGeometries());
                        if ((wayTmp.getNodeGeometries().size() > 3) &&
                                wayTmp.getNodeGeometries().get(0).equals(wayTmp.getNodeGeometries()
                                        .get(wayTmp.getNodeGeometries().size()-1))) {
                            //checks if the beginning and ending node are the same and the number of nodes are more than 3.
                            //the nodes must be more than 3, because jts does not allow a construction of a linear ring with less points.

                            if (!(wayTmp.getTagKeyValue().containsKey("barrier") || wayTmp.getTagKeyValue().containsKey("highway"))) {
                                //this is not a barrier nor a road, so construct a polygon geometry

                                LinearRing linear = geometryFactory.createLinearRing(geom.getCoordinates());
                                Polygon poly = new Polygon(linear, null, geometryFactory);
                                wayTmp.setGeometry(poly);
                            } else {
                                //it is either a barrier or a road, so construct a linear ring geometry
                                LinearRing linear = geometryFactory.createLinearRing(geom.getCoordinates());
                                wayTmp.setGeometry(linear);
                            }
                        } else if (wayTmp.getNodeGeometries().size() > 1) {
                            //it is an open geometry with more than one nodes, make it linestring

                            LineString lineString = geometryFactory.createLineString(geom.getCoordinates());
                            wayTmp.setGeometry(lineString);
                        } else { //we assume all the rest geometries are points
                            //some ways happen to have only one point. Construct a  Point.
                            Point point = geometryFactory.createPoint(geom.getCoordinate());
                            wayTmp.setGeometry(point);
                        }
                        wayList.add(wayTmp);
                    }
                }
            }
        } catch (IOException | ParserConfigurationException | SAXException ex) {
            Logger.getLogger(HistoryParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String stripQuotes(String id) {
        return id.substring(4, id.length()-1);
    }

    private void getNodeFromAPI(String nodeID) {
        try {
            String osmUrl = OSM_API + "node/" + nodeID;
            InputStream xml = HttpClient.create(new URL(osmUrl)).connect().getContent();
            NodeList nodes = XmlUtils.parseSafeDOM(xml).getElementsByTagName("node");
            String lat = nodes.item(0).getAttributes().getNamedItem("lat").getNodeValue();
            String lon = nodes.item(0).getAttributes().getNamedItem("lon").getNodeValue();

            nodeTmp = new OSMNode();
            nodeTmp.setID(nodeID);

            //parse geometry
            double longitude = Double.parseDouble(lon);
            double latitude = Double.parseDouble(lat);
            Coordinate targetGeometry = null;
            Coordinate sourceCoordinate = new Coordinate(longitude, latitude);
            try {
                targetGeometry = JTS.transform(sourceCoordinate, null, transform);
            } catch (MismatchedDimensionException | TransformException ex) {
                Logger.getLogger(HistoryParser.class.getName()).log(Level.SEVERE, null, ex);
            }

            //create geometry object
            Geometry geom = geometryFactory.createPoint(new Coordinate(targetGeometry));
            nodeTmp.setGeometry(geom);

            nodeList.add(nodeTmp);
            nodesWithIDs.put(nodeTmp.getID(), nodeTmp);

        } catch (IOException | ParserConfigurationException | SAXException ex) {
            Logger.getLogger(HistoryParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public List<OSMWay> getWayList() {
        return wayList;
    }
}
