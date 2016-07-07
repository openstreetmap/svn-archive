// License: GPL. For details, see LICENSE file.
package indoor_sweepline;

import java.util.Vector;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;

public class BeamGeography {

    public BeamGeography(DataSet dataSet, ModelGeography target) {
        partsGeography = new Vector<>();
        nodes = new Vector<>();
        this.dataSet = dataSet;
        this.target = target;
    }

    public void appendNodes(int from, int to, String level) {
        if (from <= to) {
            for (int i = from; i < to; ++i) {
                target.appendNode(nodes.elementAt(i));
                CorridorPart part = parts.elementAt(i);
                partsGeography.elementAt(i).appendNodes(part.getType(), part.getSide(), level,
                        nodes.elementAt(i).getCoor(), nodes.elementAt(i+1).getCoor(), target);
            }
            target.appendNode(nodes.elementAt(to));
        } else {
            for (int i = from; i > to; --i) {
                target.appendNode(nodes.elementAt(i));
                CorridorPart part = parts.elementAt(i-1);
                partsGeography.elementAt(i-1).appendNodes(part.getType(), part.getSide(), level,
                        nodes.elementAt(i).getCoor(), nodes.elementAt(i-1).getCoor(), target);
            }
            target.appendNode(nodes.elementAt(to));
        }
    }

    public void adjustNodes(LatLon pivot, Vector<CorridorPart> parts, double beamOffset) {
        double offset = -beamOffset;
        this.parts = parts;

        adjustNode(0, new LatLon(addMetersToLat(pivot, offset), pivot.lon()));

        for (int i = 0; i < parts.size(); ++i) {
            adjustPartGeography(i);
            offset += parts.elementAt(i).width;
            adjustNode(i+1, new LatLon(addMetersToLat(pivot, offset), pivot.lon()));
        }

        // Size reduction not implemented
    }

    private void adjustNode(int i, LatLon coor) {
        if (nodes.size() <= i)
            nodes.setSize(i+1);
        Node node = nodes.elementAt(i);
        if (node == null) {
            node = new Node(coor);
            dataSet.addPrimitive(node);
            nodes.setElementAt(node, i);
        } else
            node.setCoor(coor);
    }

    private void adjustPartGeography(int i) {
        if (partsGeography.size() <= i)
            partsGeography.setSize(i+1);
        CorridorGeography partGeography = partsGeography.elementAt(i);
        if (partGeography == null) {
            partGeography = new CorridorGeography(dataSet);
            partsGeography.setElementAt(partGeography, i);
        }
    }

    public LatLon coorAt(int i) {
        return nodes.elementAt(i).getCoor();
    }

    private Vector<CorridorPart> parts;
    private Vector<CorridorGeography> partsGeography;
    private ModelGeography target;
    private DataSet dataSet;
    private Vector<Node> nodes;

    private static double addMetersToLat(LatLon latLon, double south) {
        return latLon.lat() - south *(360./4e7);
    }
}
