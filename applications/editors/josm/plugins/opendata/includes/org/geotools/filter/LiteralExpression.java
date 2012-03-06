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

import org.opengis.filter.expression.Literal;


/**
 * Defines an expression that holds a literal for return.
 *
 * @author Rob Hranac, Vision for New York
 * @author Chris Holmes, TOPP
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/api/src/main/java/org/geotools/filter/LiteralExpression.java $
 * @version $Id: LiteralExpression.java 37280 2011-05-24 07:53:02Z mbedward $
 *
 * @deprecated use {@link org.opengis.filter.expression.Literal}
 */
public interface LiteralExpression extends Expression, Literal {
    /**
     * Sets the literal.
     *
     * @param literal The literal to store inside this expression.
     *
     * @throws IllegalFilterException This literal type is not in scope.
     *
     * @deprecated use {@link Literal#setValue(Object)}
     */
    void setLiteral(Object literal) throws IllegalFilterException;

    /**
     * Returns the literal type.
     *
     * @return the short representation of the literal expression type.
     */
    short getType();
}
