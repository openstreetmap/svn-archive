// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.utilsplugin2.search;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.search.SearchCompiler;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.utilsplugin2.selection.NodeWayUtils;

/**
 * Matches all objects contained within the match expression.
 */
public class InsideMatch extends SearchCompiler.UnaryMatch {
    private Set<OsmPrimitive> inside = null;

    public InsideMatch(SearchCompiler.Match match) {
        super(match);
    }

    /**
     * Find all objects inside areas which match the expression
     */
    private void init() {
        Collection<OsmPrimitive> matchedAreas = new HashSet<>();
        // find all ways that match the expression
        Collection<Way> ways = MainApplication.getLayerManager().getEditDataSet().getWays();
        for (Way way : ways) {
            if (match.match(way)) {
                matchedAreas.add(way);
            }
        }
        // find all relations that match the expression
        Collection<Relation> rels = MainApplication.getLayerManager().getEditDataSet().getRelations();
        for (Relation rel : rels) {
            if (match.match(rel)) {
                matchedAreas.add(rel);
            }
        }
        inside = NodeWayUtils.selectAllInside(matchedAreas, MainApplication.getLayerManager().getEditDataSet(), false);
    }

    @Override
    public boolean match(OsmPrimitive osm) {
        if (inside == null) {
            init(); // lazy initialization
        }
        return inside.contains(osm);
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + ((inside == null) ? 0 : inside.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj) || getClass() != obj.getClass())
            return false;
        InsideMatch other = (InsideMatch) obj;
        if (inside == null) {
            if (other.inside != null)
                return false;
        } else if (!inside.equals(other.inside))
            return false;
        return true;
    }
}
