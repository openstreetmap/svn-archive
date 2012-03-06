/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2011, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2003-2005, Open Geospatial Consortium Inc.
 *    
 *    All Rights Reserved. http://www.opengis.org/legal/
 */
package org.opengis.referencing.datum;

import static org.opengis.annotation.Obligation.OPTIONAL;
import static org.opengis.annotation.Specification.ISO_19111;

import org.opengis.annotation.UML;
import org.opengis.metadata.extent.Extent;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.util.InternationalString;


/**
 * Specifies the relationship of a coordinate system to the earth, thus creating a {@linkplain
 * org.opengis.referencing.crs.CoordinateReferenceSystem coordinate reference system}. A datum uses a
 * parameter or set of parameters that determine the location of the origin of the coordinate
 * reference system. Each datum subtype can be associated with only specific types of
 * {@linkplain org.opengis.referencing.cs.CoordinateSystem coordinate systems}.
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/opengis/src/main/java/org/opengis/referencing/datum/Datum.java $
 * @version <A HREF="http://portal.opengeospatial.org/files/?artifact_id=6716">Abstract specification 2.0</A>
 * @author  Martin Desruisseaux (IRD)
 * @since   GeoAPI 1.0
 *
 * @see org.opengis.referencing.cs.CoordinateSystem
 * @see org.opengis.referencing.crs.CoordinateReferenceSystem
 */
@UML(identifier="CD_Datum", specification=ISO_19111)
public interface Datum extends IdentifiedObject {
    /**
     * Key for the <code>{@value}</code> property to be given to the
     * {@linkplain DatumFactory datum factory} <code>createFoo(&hellip;)</code> methods.
     * This is used for setting the value to be returned by {@link #getAnchorPoint}.
     *
     * @see #getAnchorPoint
     */
    String ANCHOR_POINT_KEY = "anchorPoint";

    /**
     * Key for the <code>{@value}</code> property to be given to the
     * {@linkplain DatumFactory datum factory} <code>createFoo(&hellip;)</code> methods.
     * This is used for setting the value to be returned by {@link #getRealizationEpoch}.
     *
     * @see #getRealizationEpoch
     */
    String REALIZATION_EPOCH_KEY = "realizationEpoch";

    /**
     * Key for the <code>{@value}</code> property to be given to the
     * {@linkplain DatumFactory datum factory} <code>createFoo(&hellip;)</code> methods.
     * This is used for setting the value to be returned by {@link #getDomainOfValidity}.
     *
     * @see #getDomainOfValidity
     *
     * @since GeoAPI 2.1
     */
    String DOMAIN_OF_VALIDITY_KEY = "domainOfValidity";

    /**
     * Key for the <code>{@value}</code> property to be given to the
     * {@linkplain DatumFactory datum factory} <code>createFoo(&hellip;)</code> methods.
     * This is used for setting the value to be returned by {@link #getScope}.
     *
     * @see #getScope
     */
    String SCOPE_KEY = "scope";

    /**
     * Area or region or timeframe in which this datum is valid.
     *
     * @return The datum valid domain, or {@code null} if not available.
     *
     * @since GeoAPI 2.1
     */
    @UML(identifier="domainOfValidity", obligation=OPTIONAL, specification=ISO_19111)
    Extent getDomainOfValidity();

    /**
     * Description of domain of usage, or limitations of usage, for which this
     * datum object is valid.
     *
     * @return A description of domain of usage, or {@code null} if none.
     */
    @UML(identifier="scope", obligation=OPTIONAL, specification=ISO_19111)
    InternationalString getScope();
}
