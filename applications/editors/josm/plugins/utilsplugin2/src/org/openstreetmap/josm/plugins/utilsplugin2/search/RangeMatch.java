// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.utilsplugin2.search;

import org.openstreetmap.josm.actions.search.PushbackTokenizer;
import org.openstreetmap.josm.actions.search.SearchCompiler;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

/**
 * TODO: remove this copied class and make it public in JOSM core
 */
public abstract class RangeMatch extends SearchCompiler.Match {

    private final long min;
    private final long max;

    public RangeMatch(long min, long max) {
        this.min = Math.min(min, max);
        this.max = Math.max(min, max);
    }

    public RangeMatch(PushbackTokenizer.Range range) {
        this(range.getStart(), range.getEnd());
    }

    protected abstract Long getNumber(OsmPrimitive osm);

    protected abstract String getString();

    @Override
    public boolean match(OsmPrimitive osm) {
        Long num = getNumber(osm);
        if (num == null)
            return false;
        else
            return (num >= min) && (num <= max);
    }

    @Override
    public String toString() {
        return getString() + "=" + min + "-" + max;
    }
}

