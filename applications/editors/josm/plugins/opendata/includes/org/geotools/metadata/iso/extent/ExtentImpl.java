/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.metadata.iso.extent;

import java.util.Collection;

import org.geotools.metadata.iso.MetadataEntity;
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.extent.GeographicExtent;


/**
 * Information about spatial, vertical, and temporal extent.
 * This interface has four optional attributes
 * ({@linkplain #getGeographicElements geographic elements},
 *  {@linkplain #getTemporalElements temporal elements}, and
 *  {@linkplain #getVerticalElements vertical elements}) and an element called
 *  {@linkplain #getDescription description}.
 *  At least one of the four shall be used.
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/metadata/src/main/java/org/geotools/metadata/iso/extent/ExtentImpl.java $
 * @version $Id: ExtentImpl.java 37298 2011-05-25 05:16:15Z mbedward $
 * @author Martin Desruisseaux (IRD)
 * @author Touraïvane
 *
 * @since 2.1
 */
public class ExtentImpl extends MetadataEntity implements Extent {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 7812213837337326257L;

    /**
     * A geographic extent ranging from 180°W to 180°E and 90°S to 90°N.
     *
     * @since 2.2
     */
    public static final Extent WORLD;
    static {
        final ExtentImpl world = new ExtentImpl();
        world.getGeographicElements().add(GeographicBoundingBoxImpl.WORLD);
        world.freeze();
        WORLD = world;
    }

    /**
     * Provides geographic component of the extent of the referring object
     */
    private Collection<GeographicExtent> geographicElements;

    /**
     * Constructs an initially empty extent.
     */
    public ExtentImpl() {
    }

    /**
     * Provides geographic component of the extent of the referring object
     */
    public synchronized Collection<GeographicExtent> getGeographicElements() {
        return (geographicElements = nonNullCollection(geographicElements, GeographicExtent.class));
    }
}
