// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.utilsplugin2.search;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.actions.search.PushbackTokenizer;
import org.openstreetmap.josm.actions.search.SearchCompiler;
import org.openstreetmap.josm.data.osm.Changeset;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.visitor.Visitor;

/**
 * Matches primitives used in specied number of relations
 */
public class UsedInRelationsMatch extends RangeMatch {
    public UsedInRelationsMatch(PushbackTokenizer.Range range) {
        super(range);
    }

    public UsedInRelationsMatch(PushbackTokenizer tokenizer) throws SearchCompiler.ParseError {
        this(tokenizer.readRange(tr("Range of referencing relation count")));
    }

    private class RelationCounter implements Visitor {
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

        @Override
        public void visit(Changeset cs) {
            // Do nothing
        }
    }

    RelationCounter counter = new RelationCounter();

    @Override
    protected Long getNumber(OsmPrimitive osm) {
        counter.count = 0;
        osm.visitReferrers(counter);
        return new Long(counter.count);
    }

    @Override
    protected String getString() {
        return "usedinrelations";
    }
}
