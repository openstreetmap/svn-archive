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



/**
 * Defines an OpenGIS Filter object, with default behaviors for all methods.
 *
 * @author Rob Hranac, Vision for New York
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/api/src/main/java/org/geotools/filter/Filter.java $
 * @version $Id: Filter.java 37280 2011-05-24 07:53:02Z mbedward $
 *
 * @deprecated use {@link org.opengis.filter.Filter}
 */
public interface Filter extends FilterType, org.opengis.filter.Filter {

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @task Gets a short representation of the type of this filter.
     *
     * @deprecated The enumeration base type system is replaced with a class
     * based type system. An 'instanceof' check should be made instead of
     * calling this method.
     */
    short getFilterType();
}
