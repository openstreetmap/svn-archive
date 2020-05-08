// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.utilsplugin2.search;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.search.PushbackTokenizer;
import org.openstreetmap.josm.data.osm.search.SearchCompiler;

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (max ^ (max >>> 32));
        result = prime * result + (int) (min ^ (min >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        RangeMatch other = (RangeMatch) obj;
        if (max != other.max)
            return false;
        if (min != other.min)
            return false;
        return true;
    }
}

