/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openstreetmap.josm.plugins.utilsplugin2.search;

import java.util.Collection;
import java.util.HashSet;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.search.SearchCompiler;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.utilsplugin2.selection.NodeWayUtils;

/**
 * Matches all objects contained within the match expression.
 */
public class InsideMatch extends SearchCompiler.UnaryMatch {
    private Collection<OsmPrimitive> inside = null;

    public InsideMatch(SearchCompiler.Match match) {
        super(match);
    }

    /**
     * Find all objects inside areas which match the expression
     */
    private void init() {
        Collection<OsmPrimitive> matchedAreas = new HashSet<>();
        // find all ways that match the expression
        Collection<Way> ways = Main.getLayerManager().getEditDataSet().getWays();
        for (Way way : ways) {
            if (match.match(way)) {
                matchedAreas.add(way);
            }
        }
        // find all relations that match the expression
        Collection<Relation> rels = Main.getLayerManager().getEditDataSet().getRelations();
        for (Relation rel : rels) {
            if (match.match(rel)) {
                matchedAreas.add(rel);
            }
        }
        inside = NodeWayUtils.selectAllInside(matchedAreas, Main.getLayerManager().getEditDataSet(), false);
    }

    @Override
    public boolean match(OsmPrimitive osm) {
        if (inside == null) {
            init(); // lazy initialization
        }
        return inside.contains(osm);
    }
}
