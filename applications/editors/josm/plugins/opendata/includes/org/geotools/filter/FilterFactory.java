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

import org.geotools.factory.Factory;
import org.opengis.feature.type.FeatureTypeFactory;


/**
 * This specifies the interface to create filters.
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/api/src/main/java/org/geotools/filter/FilterFactory.java $
 * @version $Id: FilterFactory.java 37280 2011-05-24 07:53:02Z mbedward $
 *
 * @task TODO: This needs to be massively overhauled.  This should be the
 *       source of immutability of filters.  See {@link FeatureTypeFactory},
 *       as that provides a good example of what this should look like.  The
 *       mutable factory to create immutable objects is a good model for this.
 *       The creation methods should only create fully formed filters.  This
 *       in turn means that all the set functions in the filters should be
 *       eliminated.  When rewriting this class/package, keep in mind
 *       FilterSAXParser in the filter module, as the factory should fit
 *       cleanly with that, and should handle sax parsing without too much
 *       memory overhead.
 * @task REVISIT: resolve errors, should all throw errors?
 *
 * @deprecated use {@link org.opengis.filter.FilterFactory}
 */
public interface FilterFactory extends Factory, org.opengis.filter.FilterFactory2 {

    /**
     * @deprecated use {@link org.opengis.filter.FilterFactory#or(org.opengis.filter.Filter, org.opengis.filter.Filter)}
     */
    public Filter or(Filter f1, Filter f2);

    /**
     * @deprecated use {@link org.opengis.filter.FilterFactory#and(org.opengis.filter.Filter, org.opengis.filter.Filter)}
     */
    public Filter and(Filter f1, Filter f2);

    /**
     * @deprecated use {@link org.opengis.filter.FilterFactory#not(org.opengis.filter.Filter)}
     */
    public Filter not(Filter f);
}
