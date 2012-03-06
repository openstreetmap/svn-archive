/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2011, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2003-2005 Open Geospatial Consortium Inc.
 *    
 *    All Rights Reserved. http://www.opengis.org/legal/
 */
package org.opengis.annotation;


/**
 * The specifications from which an interface, method or code list was derived.
 *
 * @author  Martin Desruisseaux (IRD)
 * @since   GeoAPI 2.0
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/opengis/src/main/java/org/opengis/annotation/Specification.java $
 */
public enum Specification {
    /**
     * ISO 19103, Geographic information - Conceptual schema language.
     * This is the specification for some interfaces in package {@link org.opengis.util}.
     */
    ISO_19103,

    /**
     * ISO 19107, Feature Geometry (Topic 1).
     * This is the specification for package {@link org.opengis.geometry} and sub-packages.
     *
     * @see <A HREF="http://www.opengeospatial.org/standards/as">Buy from ISO</A>
     */
    ISO_19107,

    /**
     * ISO 19111, Spatial Referencing by Coordinates (Topic 2).
     * This is the specification for package {@link org.opengis.referencing} and sub-packages.
     *
     * @see #OGC_01009
     * @see <A HREF="http://www.opengeospatial.org/standards/as#04-046r3">Download from OGC</A>
     */
    ISO_19111,

    /**
     * ISO 19115, Metadata (Topic 11).
     * This is the specification for package {@link org.opengis.metadata} and sub-packages.
     *
     * @see <A HREF="http://www.opengeospatial.org/standards/as#01-111">Buy from ISO</A>
     */
    ISO_19115,

    /**
     * Coordinate Transformation Services implementation specification.
     * This is the specification used as a complement of {@linkplain #ISO_19111 ISO 19111}
     * when an aspect was not defined in the ISO specification.
     *
     * @see #ISO_19111
     * @see <A HREF="http://www.opengeospatial.org/standards/ct">Download from OGC</A>
     */
    OGC_01009,

    /**
     * Filter encoding implementation specification.
     * This is the specification for package {@link org.opengis.filter} and sub-packages.
     *
     * @see <A HREF="http://www.opengeospatial.org/standards/filter">Download from OGC</A>
     *
     * @todo Need to be updated to {@code OGC 04-095}.
     */
    OGC_02059,

    /**
     * Specification not yet determined. This is a temporary enumeration
     * for the processing of API submitted by some contributors.
     */
    UNSPECIFIED
}
