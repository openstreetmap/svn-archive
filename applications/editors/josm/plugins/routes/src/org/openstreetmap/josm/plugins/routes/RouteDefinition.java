// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.routes;

import java.awt.Color;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.search.SearchCompiler;
import org.openstreetmap.josm.data.osm.search.SearchCompiler.Match;
import org.openstreetmap.josm.data.osm.search.SearchParseError;
import org.openstreetmap.josm.tools.Logging;

public class RouteDefinition {

    private final Color color;
    private final String matchString;
    private Match match;
    private final int index;

    public RouteDefinition(int index, Color color, String expression) {
        this.color = color;
        this.matchString = expression;
        this.index = index;
        try {
            match = SearchCompiler.compile(expression);
        } catch (SearchParseError e) {
            match = new SearchCompiler.Never();
            Logging.error(e);
        }
    }

    public boolean matches(OsmPrimitive primitive) {
        return match.match(primitive);
    }

    public Color getColor() {
        return color;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return color.toString() + " " + matchString;
    }

}
