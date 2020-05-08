// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.utilsplugin2.search;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.search.SearchCompiler;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.utilsplugin2.selection.NodeWayUtils;

/**
 * Matches all ways connected to [nodes and ways which match the expression]..
 */
public class ConnectedMatch extends SearchCompiler.UnaryMatch {
    private Set<Way> connected = null;
    boolean all;

    public ConnectedMatch(SearchCompiler.Match match, boolean all) {
        super(match);
        this.all = all;
    }

    /**
     * Find (all) ways connected to ways or nodes which match the expression.
     */
    private void init(boolean all) {
        Collection<Way> matchedWays = new HashSet<>();
        Set<Node> matchedNodes = new HashSet<>();
        // find all ways that match the expression
        Collection<Way> allWays = MainApplication.getLayerManager().getEditDataSet().getWays();
        for (Way way : allWays) {
            if (match.match(way)) {
                matchedWays.add(way);
            }
        }
        // find all nodes that match the expression
        Collection<Node> allNodes = MainApplication.getLayerManager().getEditDataSet().getNodes();
        for (Node node : allNodes) {
            if (match.match(node)) {
                matchedNodes.add(node);
            }
        }
        Set<Way> newWays = new HashSet<>();
        if (all) {
            NodeWayUtils.addWaysConnectedToNodes(matchedNodes, newWays);
            NodeWayUtils.addWaysConnectedToWaysRecursively(matchedWays, newWays);
        } else {
            NodeWayUtils.addWaysConnectedToNodes(matchedNodes, newWays);
            NodeWayUtils.addWaysConnectedToWays(matchedWays, newWays);
        }
        connected = newWays;
    }

    @Override
    public boolean match(OsmPrimitive osm) {
        if (connected == null) {
            init(all); // lazy initialization
        }
        if (osm instanceof Way) {
            return connected.contains(osm);
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (all ? 1231 : 1237);
        result = prime * result + ((connected == null) ? 0 : connected.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj) || getClass() != obj.getClass())
            return false;
        ConnectedMatch other = (ConnectedMatch) obj;
        if (all != other.all)
            return false;
        if (connected == null) {
            if (other.connected != null)
                return false;
        } else if (!connected.equals(other.connected))
            return false;
        return true;
    }

}
