// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.utilsplugin2.search;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.search.PushbackTokenizer;
import org.openstreetmap.josm.data.osm.search.SearchParseError;

/**
 * Matches objects with a number of child primitives in the given range.
 */
public class ChildrenMatch extends RangeMatch {
    public ChildrenMatch(PushbackTokenizer.Range range) {
        super(range);
    }

    public ChildrenMatch(PushbackTokenizer tokenizer) throws SearchParseError {
        this(tokenizer.readRange(tr("Range of child primitives count")));
    }

    @Override
    protected Long getNumber(OsmPrimitive osm) {
        if (osm instanceof Way) {
            return (long) ((Way) osm).getNodesCount();
        } else if (osm instanceof Relation) {
            return (long) ((Relation) osm).getMembersCount();
        } else {
            return null;
        }
    }

    @Override
    protected String getString() {
        return "children";
    }
}
