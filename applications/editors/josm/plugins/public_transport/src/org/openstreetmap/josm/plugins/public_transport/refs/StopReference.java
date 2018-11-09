// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.public_transport.refs;

import org.openstreetmap.josm.data.osm.Node;

public class StopReference implements Comparable<StopReference> {
    public int index = 0;

    public double pos = 0;

    public double distance = 0;

    public String name = "";

    public String role = "";

    public Node node;

    public StopReference(int inIndex, double inPos, double inDistance, String inName,
            String inRole, Node inNode) {
        index = inIndex;
        pos = inPos;
        distance = inDistance;
        name = inName;
        role = inRole;
        node = inNode;
    }

    @Override
    public int compareTo(StopReference sr) {
        if (this.index < sr.index)
            return -1;
        if (this.index > sr.index)
            return 1;
        if (this.pos < sr.pos)
            return -1;
        if (this.pos > sr.pos)
            return 1;
        return 0;
    }
}
