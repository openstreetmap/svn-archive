// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.paris.datasets;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.datasets.fr.FrenchShpHandler;

public class ParisShpHandler extends FrenchShpHandler {
    
    public final Map<String, Node> nodeMap = new HashMap<>();
    
    protected OsmPrimitive dataPrimitive;
    protected Way closedWay;
    protected List<Way> ways;
    public List<Node> nodes;
    
    public ParisShpHandler() {
        setDbfCharset(Charset.forName(OdConstants.CP850));
    }
    
    private Node getNode(EastNorth en, String key) {
        Node n = nodeMap.get(key);
        /*if (n == null) {
            for (Node node : nodes.values()) {
                if (node.getEastNorth().equalsEpsilon(en, 0.0000001)) {
                    return node;
                }
            }
        }*/
        return n;
    }
    
    protected Node createOrGetNode(DataSet ds, EastNorth en) {
        String key = en.getX()+"/"+en.getY();
        Node n = getNode(en, key);
        if (n == null) {
            n = new Node(en);
            nodeMap.put(key, n);
            ds.addPrimitive(n);
        }
        return n;
    }

    protected final void initFeaturesPrimitives(Set<OsmPrimitive> featurePrimitives) {
        dataPrimitive = null;
        closedWay = null;
        ways = new ArrayList<>();
        nodes = new ArrayList<>();
        for (OsmPrimitive p : featurePrimitives) {
            if (p.hasKeys()) {
                dataPrimitive = p;
            }
            if (p instanceof Way) {
                Way w = (Way) p;
                ways.add(w);
                if (w.isClosed()) {
                    closedWay = w;
                }
            } else if (p instanceof Node) {
                nodes.add((Node) p);
            }
        }
    }
    
    protected final void removePrimitives(DataSet result) {
        for (Way w : ways) {
            w.setNodes(null);
            result.removePrimitive(w);
        }
        for (Node n : nodes) {
            result.removePrimitive(n);
        }
    }
}
