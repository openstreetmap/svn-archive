// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io;

import java.util.regex.Pattern;

import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;

public class ProjectionPatterns {

    private final Pattern xPattern;
    private final Pattern yPattern;
    private final Pattern xyPattern;
    private final Projection projection;

    public ProjectionPatterns(Pattern xPattern, Pattern yPattern, Pattern xyPattern, Projection projection) {
        this.xPattern = xPattern;
        this.yPattern = yPattern;
        this.xyPattern = xyPattern;
        this.projection = projection;
        OdConstants.PROJECTIONS.add(this);
    }

    public ProjectionPatterns(String proj, Projection projection) {
        this(getCoordinatePattern(OdConstants.X_STRING, proj),
             getCoordinatePattern(OdConstants.Y_STRING, proj),
             getCoordinatePattern(OdConstants.XY_STRING, proj), projection);
    }

    public ProjectionPatterns(String proj) {
        this(proj, null);
    }

    public final Pattern getXPattern() {
        return xPattern;
    }

    public final Pattern getYPattern() {
        return yPattern;
    }

    public final Pattern getXYPattern() {
        return xyPattern;
    }

    public Projection getProjection(String xFieldName, String yFieldName) {
        return getProjection();
    }

    public final Projection getProjection() {
        return projection;
    }

    public static final Pattern getCoordinatePattern(String coor, String proj) {
        if (proj != null && !proj.isEmpty()) {
            return Pattern.compile("(?:.*(?:"+coor+").*(?:"+proj+").*)|(?:.*("+proj+").*(?:"+coor+").*)", Pattern.CASE_INSENSITIVE);
        } else {
            return Pattern.compile(coor, Pattern.CASE_INSENSITIVE);
        }
    }

    @Override
    public String toString() {
        return "[xPattern=" + xPattern + ", yPattern=" + yPattern + ", xyPattern=" + xyPattern + ", projection=" + projection + ']';
    }
}
