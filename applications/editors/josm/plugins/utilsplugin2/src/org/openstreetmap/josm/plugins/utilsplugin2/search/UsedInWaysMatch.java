// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.utilsplugin2.search;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.search.PushbackTokenizer;
import org.openstreetmap.josm.data.osm.search.SearchParseError;
import org.openstreetmap.josm.data.osm.visitor.OsmPrimitiveVisitor;

/**
 * Matches nodes that are attached to given range of ways
 */
public class UsedInWaysMatch extends RangeMatch {
    public UsedInWaysMatch(PushbackTokenizer.Range range) {
        super(range);
    }

    public UsedInWaysMatch(PushbackTokenizer tokenizer) throws SearchParseError {
        this(tokenizer.readRange(tr("Range of attached ways count")));
    }

    private static class WayCounter implements OsmPrimitiveVisitor {
        int count;
        @Override
        public void visit(Way w) {
            count++;
        }

        @Override
        public void visit(Node n) {
            // Do nothing
        }

        @Override
        public void visit(Relation r) {
            // Do nothing
        }
    }

    WayCounter counter = new WayCounter();

    @Override protected Long getNumber(OsmPrimitive osm) {
        if (osm instanceof Node) {
            counter.count = 0;
            osm.visitReferrers(counter);
            return Long.valueOf(counter.count);
        } else return null;
    }

    @Override protected String getString() {
        return "wayrefs";
    }
}
