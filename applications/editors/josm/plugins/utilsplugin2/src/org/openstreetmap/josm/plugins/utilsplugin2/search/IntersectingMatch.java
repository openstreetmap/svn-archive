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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (all ? 1231 : 1237);
        result = prime * result + ((intersecting == null) ? 0 : intersecting.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj) || getClass() != obj.getClass())
            return false;
        IntersectingMatch other = (IntersectingMatch) obj;
        if (all != other.all)
            return false;
        if (intersecting == null) {
            if (other.intersecting != null)
                return false;
        } else if (!intersecting.equals(other.intersecting))
            return false;
        return true;
    }

}
