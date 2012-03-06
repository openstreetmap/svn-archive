/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2011, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2005 Open Geospatial Consortium Inc.
 *    
 *    All Rights Reserved. http://www.opengis.org/legal/
 */
package org.opengis.filter;

import org.opengis.annotation.Extension;


/**
 * Visitor with {@code visit} methods to be called by {@link Filter#accept Filter.accept(...)}.
 * <p>
 * Consider: It is unclear if this visitor should be applied directly to Filter, or should be walked accross
 * the data structure by hand.  The standard complient structure is well defined, and this should negate
 * the need for a formal visitor (we don't have internal structure we are hiding).
 * </p>
 * <p>
 * There is still a very valid use for FilterVisitor, a instance may implement both FilterVisitor and ExpressionVisitor
 * and ExpressionVisitory in one direction, and a FilterVisitor and a StyleVisitor in the other. The ability
 * to directly focus on transforming data within a larger structure is something a normal data walk
 * can not accomplish in a scalable manner.
 * </p>
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/opengis/src/main/java/org/opengis/filter/FilterVisitor.java $
 * @version <A HREF="http://www.opengis.org/docs/02-059.pdf">Implementation specification 1.0</A>
 * @author Chris Dillard (SYS Technologies)
 * @since GeoAPI 2.0
 */
@Extension
public interface FilterVisitor {

    /**
     * Visit {@link Filter#EXCLUDE} (often used during data structure transformations).
     *
     * @param filter {@link Filter#EXCLUDE}.
     * @param extraData Value object provided to visitor
     * @return subclass supplied
     */
    Object visit(ExcludeFilter filter, Object extraData);

    /**
     * Visit {@link Filter#INCLUDE} (often used during data structure transformations).
     *
     * @param filter {@link Filter#INCLUDE}.
     * @param extraData Value object provided to visitor
     * @return subclass supplied
     */
    Object visit(IncludeFilter filter, Object extraData);

    Object visit(And filter,                            Object extraData);
    Object visit(Id filter,                             Object extraData);
    Object visit(Not filter,                            Object extraData);
    Object visit(Or filter,                             Object extraData);
}
