
package org.openstreetmap.josm.plugins.utilsplugin2.search;

import org.openstreetmap.josm.actions.search.PushbackTokenizer;
import org.openstreetmap.josm.actions.search.SearchCompiler;
import org.openstreetmap.josm.data.osm.Changeset;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.visitor.Visitor;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Matches objects with a version number in the given range.
 */
public class UsedInWaysMatch extends RangeMatch {
    public UsedInWaysMatch(PushbackTokenizer.Range range) {super(range);}
    public UsedInWaysMatch(PushbackTokenizer tokenizer) throws SearchCompiler.ParseError {
        this(tokenizer.readRange(tr("Range of attached way cuunt")));
    }
    private class WayCounter implements Visitor {
        int count;
        @Override
        public void visit(Way w) { count++; }
        @Override   public void visit(Node n) { }
        @Override   public void visit(Relation r) {   }
        @Override   public void visit(Changeset cs) {   }
    }
    WayCounter counter = new WayCounter();

@Override protected Long getNumber(OsmPrimitive osm) {
        if (osm instanceof Node) {
            counter.count=0;
            osm.visitReferrers(counter);
            return new Long(counter.count);
        } else return null;
    }
    @Override protected String getString() {
        return "wayrefs";
    }
}
