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
 * Matches primitives used in specied number of relations
 */
public class UsedInRelationsMatch extends RangeMatch {
    public UsedInRelationsMatch(PushbackTokenizer.Range range) {
        super(range);
    }

    public UsedInRelationsMatch(PushbackTokenizer tokenizer) throws SearchParseError {
        this(tokenizer.readRange(tr("Range of referencing relation count")));
    }

    private static class RelationCounter implements OsmPrimitiveVisitor {
        int count;
        @Override
        public void visit(Way w) {
            // Do nothing
        }

        @Override
        public void visit(Node n) {
            // Do nothing
        }

        @Override
        public void visit(Relation r) {
            count++;
        }
    }

    RelationCounter counter = new RelationCounter();

    @Override
    protected Long getNumber(OsmPrimitive osm) {
        counter.count = 0;
        osm.visitReferrers(counter);
        return Long.valueOf(counter.count);
    }

    @Override
    protected String getString() {
        return "usedinrelations";
    }
}
