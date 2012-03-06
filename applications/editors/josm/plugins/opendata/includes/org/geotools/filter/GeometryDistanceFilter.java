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

import org.opengis.filter.spatial.DistanceBufferOperator;


/**
 * Defines geometry filters with a distance element.
 *
 * <p>
 * These filters are defined in the filter spec by the DistanceBufferType,
 * which contains an additioinal field for a distance.  The two filters that
 * use the distance buffer type are Beyond and DWithin.
 * </p>
 *
 * <p>
 * From the spec: The spatial operators DWithin and Beyond test whether the
 * value of a geometric property is within or beyond a specified distance of
 * the specified literal geometric value.  Distance values are expressed using
 * the Distance element.
 * </p>
 *
 * <p>
 * For now this code does not take into account the units of distance,  we will
 * assume that the filter units are the same as the geometry being filtered.
 * </p>
 *
 * <p></p>
 *
 * @author Chris Holmes, TOPP
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/api/src/main/java/org/geotools/filter/GeometryDistanceFilter.java $
 * @version $Id: GeometryDistanceFilter.java 37280 2011-05-24 07:53:02Z mbedward $
 *
 * @task REVISIT: add units for distance.  Should it just be a string?  Or
 *       should it actually resolve the definition?
 *
 * @deprecated use {@link org.opengis.filter.spatial.DistanceBufferOperator}
 *
 */
public interface GeometryDistanceFilter extends GeometryFilter, DistanceBufferOperator {
    /**
     * Returns true if the passed in object is the same as this filter.  Checks
     * to make sure the filter types are the same as well as all three of the
     * values.
     *
     * @param obj The filter to test equality against.
     *
     * @return True if the objects are equal.
     */
    boolean equals(Object obj);
}
