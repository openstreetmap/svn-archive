// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.utilsplugin2.search;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.search.SearchCompiler;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.utilsplugin2.selection.NodeWayUtils;

/**
 * Find (all) ways intersecting ways or nodes which match the expression.
 */
public class IntersectingMatch extends SearchCompiler.UnaryMatch {
    private Collection<Way> intersecting = null;
    boolean all;

    public IntersectingMatch(SearchCompiler.Match match, boolean all) {
        super(match);
        this.all = all;
        //init(all);
    }

    /**
     * Find (all) ways intersecting ways which match the expression.
     */
    private void init(boolean all) {
        Collection<Way> matchedWays = new HashSet<>();
        // find all ways that match the expression
        Collection<Way> allWays = MainApplication.getLayerManager().getEditDataSet().getWays();
        for (Way way : allWays) {
            if (match.match(way)) {
                matchedWays.add(way);
            }
        }
        Set<Way> newWays = new HashSet<>();
        if (all) {
            NodeWayUtils.addWaysIntersectingWaysRecursively(allWays, matchedWays, newWays);
        } else {
            NodeWayUtils.addWaysIntersectingWays(allWays, matchedWays, newWays);
        }
        intersecting = newWays;
    }

    @Override
    public boolean match(OsmPrimitive osm) {
        if (intersecting == null) {
            init(all); // lazy initialization
        }
        if (osm instanceof Way) {
            return intersecting.contains(osm);
        }
        return false;
    }

}
