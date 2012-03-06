/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.referencing.factory.epsg;

import java.net.URL;

import org.geotools.factory.Hints;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


/**
 * Provides common {@linkplain CoordinateReferenceSystem Coordinate Reference Systems}
 * not found in the standard EPSG database. Those CRS will be registered in
 * {@code "EPSG"} name space.
 *
 * @since 2.4
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/plugin/epsg-extension/src/main/java/org/geotools/referencing/factory/epsg/UnnamedExtension.java $
 * @version $Id: UnnamedExtension.java 37305 2011-05-25 05:57:57Z mbedward $
 * @author Andrea Aime
 */
public class UnnamedExtension extends FactoryUsingWKT { // NO_UCD
    /**
     * The default filename to read. This file will be searched in the
     * {@code org/geotools/referencing/factory/espg} directory in the
     * classpath or in a JAR file.
     *
     * @see #getDefinitionsURL
     */
    public static final String FILENAME = "unnamed.properties";

    /**
     * Constructs an authority factory using the default set of factories.
     */
    public UnnamedExtension() {
        this(null);
    }

    /**
     * Constructs an authority factory using a set of factories created from the specified hints.
     * This constructor recognizes the {@link Hints#CRS_FACTORY CRS}, {@link Hints#CS_FACTORY CS},
     * {@link Hints#DATUM_FACTORY DATUM} and {@link Hints#MATH_TRANSFORM_FACTORY MATH_TRANSFORM}
     * {@code FACTORY} hints.
     */
    public UnnamedExtension(final Hints hints) {
        super(hints, DEFAULT_PRIORITY - 2);
    }

    /**
     * Returns the URL to the property file that contains CRS definitions.
     * The default implementation returns the URL to the {@value #FILENAME} file.
     *
     * @return The URL, or {@code null} if none.
     */
    @Override
    protected URL getDefinitionsURL() {
        return UnnamedExtension.class.getResource(FILENAME);
    }
}
