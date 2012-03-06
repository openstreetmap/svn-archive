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

import org.opengis.filter.expression.PropertyName;


/**
 * The geotools representation of the PropertyName tag in an xml encoded
 * filter.
 * <p>
 * It should handle xpath attributePaths of features, and should
 * report the attribute found at the attributePath of a feature.
 * </p>
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/api/src/main/java/org/geotools/filter/AttributeExpression.java $
 * @version $Id: AttributeExpression.java 37280 2011-05-24 07:53:02Z mbedward $
 *
 * @deprecated use {@link org.opengis.filter.expression.PropertyName}
 */
public interface AttributeExpression extends Expression, PropertyName {

    /**
     * Gets the attribute path of this expression.
     *
     * @return the attribute to be queried.
     *
     * @deprecated use {@link PropertyName#getPropertyName()}
     */
    String getAttributePath();
}
