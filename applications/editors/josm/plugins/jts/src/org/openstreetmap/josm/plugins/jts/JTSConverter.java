package org.openstreetmap.josm.plugins.jts;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import java.util.List;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;

/**
 * Methods to convert JOSM geometry to JTS geometry
 */
public class JTSConverter {
    private PrecisionModel precisionModel;
    private GeometryFactory geometryFactory;
    private boolean useEastNorth;
    /**
     * Conversions will use latitude/longitude for coordinates
     */
    public JTSConverter() {
        this(false);
    }

    /**
     * Conversions will use LatLon if useEastNorth is false, otherwise EastNorth
     * (the currently selected projection) will be used for coordinates.
     * @param useEastNorth
     */
    public JTSConverter(boolean useEastNorth) {
        this.useEastNorth = useEastNorth;
        if (useEastNorth)
            precisionModel = new PrecisionModel();
        else
            precisionModel = new OsmPrecisionModel();
        geometryFactory = new GeometryFactory(precisionModel);
    }

    public PrecisionModel getPrecisionModel() {
        return precisionModel;
    }

    public GeometryFactory getGeometryFactory() {
        return geometryFactory;
    }

    /**
     * Simple subclass to match precision with the OSM data model (7 decimal
     * places)
     */
    public class OsmPrecisionModel extends com.vividsolutions.jts.geom.PrecisionModel {

        public OsmPrecisionModel() {
            super(10000000);
        }
    }

    public Coordinate convertNodeToCoordinate(Node node) {
        if (useEastNorth)
            return new Coordinate(node.getEastNorth().getX(), node.getEastNorth().getY());
        else
            return new Coordinate(node.getCoor().getX(), node.getCoor().getY());
    }

    public CoordinateSequence convertNodesToCoordinateSequence(List<Node> nodes) {
        Coordinate coords[] = new Coordinate[nodes.size()];
        for (int i = 0; i < nodes.size(); i++) {
            coords[i] = convertNodeToCoordinate(nodes.get(i));
        }
        return new CoordinateArraySequence(coords);
    }

    public Point convertNode(Node node) {
        Coordinate coords[] = {convertNodeToCoordinate(node)};
        return new com.vividsolutions.jts.geom.Point(new CoordinateArraySequence(coords), getGeometryFactory());
    }

    public Geometry convertWay(Way way) {
        CoordinateSequence coordSeq = convertNodesToCoordinateSequence(way.getNodes());

        // TODO: need to check tags to determine whether area or not
        if (way.isClosed()) {
            LinearRing ring = new LinearRing(coordSeq, getGeometryFactory());
            return new Polygon(ring, null, getGeometryFactory());
        } else {
            return new LineString(coordSeq, getGeometryFactory());
        }
    }

    public Geometry convert(OsmPrimitive prim) {
        if (prim instanceof Node) {
            return convertNode((Node) prim);
        } else if (prim instanceof Way) {
            return convertWay((Way) prim);
        } else if (prim instanceof Relation) {
            throw new UnsupportedOperationException("Relations not supported yet.");
        } else {
            throw new UnsupportedOperationException("Unknown primitive.");
        }
    }
}
