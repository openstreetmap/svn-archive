// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io;

import java.util.regex.Pattern;

import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;

public class ProjectionPatterns {

    private final Pattern xPattern;
    private final Pattern yPattern;
    private final Projection projection;
    
    public ProjectionPatterns(Pattern xPattern, Pattern yPattern, Projection projection) {
        this.xPattern = xPattern;
        this.yPattern = yPattern;
        this.projection = projection;
        OdConstants.PROJECTIONS.add(this);
    }

    public ProjectionPatterns(Pattern xPattern, Pattern yPattern) {
        this(xPattern, yPattern, null);
    }

    public ProjectionPatterns(String proj, Projection projection) {
        this(getCoordinatePattern(OdConstants.X_STRING, proj), getCoordinatePattern(OdConstants.Y_STRING, proj), projection);
    }

    public ProjectionPatterns(String proj) {
        this(getCoordinatePattern(OdConstants.X_STRING, proj), getCoordinatePattern(OdConstants.Y_STRING, proj), null);
    }
    
    public final Pattern getXPattern() {
        return xPattern;
    }
    
    public final Pattern getYPattern() {
        return yPattern;
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
        return "[xPattern=" + xPattern + ", yPattern=" + yPattern + ", projection=" + projection + "]";
    }
}
