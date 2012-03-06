/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.filter;

import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;


/**
 * Defines an expression, the units that make up Filters.   This filter holds
 * one or more filters together and relates them logically in an internally
 * defined manner.
 *
 * @author Rob Hranac, Vision for New York
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/api/src/main/java/org/geotools/filter/Expression.java $
 * @version $Id: Expression.java 37280 2011-05-24 07:53:02Z mbedward $
 *
 * @deprecated use {@link org.opengis.filter.expression.Expression}
 */
public interface Expression extends ExpressionType, org.opengis.filter.expression.Expression {
    /**
     * Gets the type of this expression.
     *
     * @return Expression type.
     *
     * @deprecated The enumeration based type system has been replaced by a
     * class based type system.
     */
    short getType();

    /**
     * Evaluates the expression against an instance of {@link Feature}.
     *
     * @param feature The feature being evaluated.
     *
     * @return The result.
     */
    Object evaluate(SimpleFeature feature);
}
