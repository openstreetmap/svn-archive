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

import java.util.List;

import org.geotools.factory.Factory;
import org.opengis.filter.expression.Function;


/**
 * Interface for a function expression implementation
 *
 * @author James Macgill, PSU
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/api/src/main/java/org/geotools/filter/FunctionExpression.java $
 *
 * @deprecated use {@link org.opengis.filter.expression.Function}
 */
public interface FunctionExpression extends Expression, Factory, Function {
    /**
     *   Returns the number of arguments this <Function> requires.
     *
     *   For example <Function name="strCat"> [arg1][arg2]</Function>.
     *   This function must have EXACTLY 2 arguments, so this function
     *   would return 2.
     *
     *   The parser might use this information to ensure validity,
     *   and its also for reporting <Function> capabilities.
     *
     *  NOTE: this was previously javadoc-ed incorrectly, please note
     *        the new definition.
     *  NOTE: you cannot have a function with a variable number of
     *        arguments.
     *
     * @return the number of args required by this function.
     */
    int getArgCount();

    /**
     * Gets the type of this expression.
     *
     * @return the short representation of a function expression.
     */
    short getType();

    /**
     * Gets the name of this function.
     *
     * @return the name of the function.
     */
    String getName();

    /**
     * Sets the paramters for the function.
     */
    void setParameters(List parameters);
}
