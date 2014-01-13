// License: GPL v2 or later. See LICENSE file for details.
package org.openstreetmap.josm.plugins.utilsplugin2.search;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.search.SearchCompiler;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.utilsplugin2.selection.NodeWayUtils;


/**
 * Matches all ways connected to [nodes and ways which match the expression]..
 */
public class ConnectedMatch extends SearchCompiler.UnaryMatch {
    private Collection<Way> connected = null;
    boolean all;

    public ConnectedMatch(SearchCompiler.Match match, boolean all) {
        super(match);
        this.all = all;
    }

    /**
     * Find (all) ways connected to ways or nodes which match the expression.
     */
    private void init(boolean all) {
        Collection<Way> matchedWays = new HashSet<Way>();
        Set<Node> matchedNodes = new HashSet<Node>();
        // find all ways that match the expression
        Collection<Way> allWays = Main.main.getCurrentDataSet().getWays();
        for (Way way : allWays) {
            if (match.match(way)) {
                matchedWays.add(way);
            }
        }
        // find all nodes that match the expression
        Collection<Node> allNodes = Main.main.getCurrentDataSet().getNodes();
        for (Node node : allNodes) {
            if (match.match(node)) {
                matchedNodes.add(node);
            }
        }
        Set<Way> newWays = new HashSet<Way>();
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
            return connected.contains((Way) osm);
        }
        return false;
    }
    
}
