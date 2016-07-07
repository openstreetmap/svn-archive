// License: GPL. For details, see LICENSE file.
package indoor_sweepline;

import java.util.Vector;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;

public class CorridorGeography {

    public CorridorGeography(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    private static final double MIN_LENGTH = 10.;

    private void setExtraElements(CorridorPart.ReachableSide side, LatLon from, LatLon to,
            boolean extraWayUp, double minLength) {
        LatLon middleCoor = new LatLon((from.lat() + to.lat())/2.,
                (from.lon() + to.lon())/2.);
        if (middleNode == null) {
            middleNode = new Node(middleCoor);
            dataSet.addPrimitive(middleNode);
        } else
            middleNode.setCoor(middleCoor);

        LatLon start = from;
        if (side == CorridorPart.ReachableSide.LEFT) {
            if (middleCoor.lat() < start.lat())
                start = to;
        } else if (side == CorridorPart.ReachableSide.RIGHT) {
            if (start.lat() < middleCoor.lat())
                start = to;
        } else if (side == CorridorPart.ReachableSide.FRONT) {
            if (start.lon() < middleCoor.lon())
                start = to;
        } else if (side == CorridorPart.ReachableSide.BACK) {
            if (middleCoor.lon() < start.lon())
                start = to;
        }

        double scale = Math.cos(middleCoor.lat() * (Math.PI/180.));
        double length = Math.sqrt((start.lat() - middleCoor.lat()) * (start.lat() - middleCoor.lat()) +
                (start.lon() - middleCoor.lon()) * (start.lon() - middleCoor.lon()) * scale * scale) / 180. * 20000000.;
        double lengthFactor = length < minLength ? minLength / length : 1.;
        LatLon detachedCoor = new LatLon(middleCoor.lat() + (start.lon() - middleCoor.lon()) * scale * lengthFactor,
                middleCoor.lon() - (start.lat() - middleCoor.lat()) / scale * lengthFactor);
        if (detachedNode == null) {
            detachedNode = new Node(detachedCoor);
            dataSet.addPrimitive(detachedNode);
        } else
            detachedNode.setCoor(detachedCoor);

        Vector<Node> extraWayNodes = new Vector<>();
        if (extraWayUp) {
            extraWayNodes.add(middleNode);
            extraWayNodes.add(detachedNode);
        } else {
            extraWayNodes.add(detachedNode);
            extraWayNodes.add(middleNode);
        }
        if (extraWay == null) {
            extraWay = new Way();
            extraWay.setNodes(extraWayNodes);
            dataSet.addPrimitive(extraWay);
        } else
            extraWay.setNodes(extraWayNodes);
    }

    public void appendNodes(CorridorPart.Type type, CorridorPart.ReachableSide side, String level,
            LatLon from, LatLon to, ModelGeography target) {
        if (type == CorridorPart.Type.STAIRS_UP || type == CorridorPart.Type.STAIRS_DOWN) {
            setExtraElements(side, from, to, type == CorridorPart.Type.STAIRS_UP, MIN_LENGTH);
            target.appendNode(middleNode);

            detachedNode.removeAll();

            extraWay.removeAll();
            extraWay.put("highway", "steps");
            extraWay.put("incline", "up");
            extraWay.put("level", level);
        } else if (type == CorridorPart.Type.ESCALATOR_UP_LEAVING
                || type == CorridorPart.Type.ESCALATOR_UP_ARRIVING
                || type == CorridorPart.Type.ESCALATOR_UP_BIDIRECTIONAL
                || type == CorridorPart.Type.ESCALATOR_DOWN_LEAVING
                || type == CorridorPart.Type.ESCALATOR_DOWN_ARRIVING
                || type == CorridorPart.Type.ESCALATOR_DOWN_BIDIRECTIONAL) {
            setExtraElements(side, from, to,
                    type == CorridorPart.Type.ESCALATOR_UP_LEAVING
                    || type == CorridorPart.Type.ESCALATOR_UP_ARRIVING
                    || type == CorridorPart.Type.ESCALATOR_UP_BIDIRECTIONAL, MIN_LENGTH);
            target.appendNode(middleNode);

            detachedNode.removeAll();

            extraWay.removeAll();
            extraWay.put("highway", "steps");
            extraWay.put("incline", "up");
            if (type == CorridorPart.Type.ESCALATOR_UP_LEAVING
                    || type == CorridorPart.Type.ESCALATOR_DOWN_ARRIVING)
                extraWay.put("conveying", "forward");
            else if (type == CorridorPart.Type.ESCALATOR_UP_ARRIVING
                    || type == CorridorPart.Type.ESCALATOR_DOWN_LEAVING)
                extraWay.put("conveying", "backward");
            else
                extraWay.put("conveying", "reversible");
            extraWay.put("level", level);
        } else if (type == CorridorPart.Type.ELEVATOR) {
            setExtraElements(side, from, to, true, 0.);
            target.appendNode(middleNode);

            detachedNode.removeAll();
            detachedNode.put("highway", "elevator");

            extraWay.removeAll();
            extraWay.put("highway", "footway");
            extraWay.put("level", level);
        } else {
            if (extraWay != null) {
                extraWay.setDeleted(true);
                extraWay = null;
            }
            if (middleNode != null) {
                middleNode.setDeleted(true);
                middleNode = null;
            }
            if (detachedNode != null) {
                detachedNode.setDeleted(true);
                detachedNode = null;
            }
        }
    }

    private DataSet dataSet;
    private Node middleNode;
    private Node detachedNode;
    private Way extraWay;
}
