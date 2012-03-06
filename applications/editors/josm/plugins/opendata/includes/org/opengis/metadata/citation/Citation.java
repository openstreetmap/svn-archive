/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2011, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2004-2005, Open Geospatial Consortium Inc.
 *    
 *    All Rights Reserved. http://www.opengis.org/legal/
 */
package org.opengis.metadata.citation;

import static org.opengis.annotation.Obligation.MANDATORY;
import static org.opengis.annotation.Obligation.OPTIONAL;
import static org.opengis.annotation.Specification.ISO_19115;

import java.util.Collection;

import org.opengis.annotation.UML;
import org.opengis.metadata.Identifier;
import org.opengis.util.InternationalString;


/**
 * Standardized resource reference.
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/opengis/src/main/java/org/opengis/metadata/citation/Citation.java $
 * @version <A HREF="http://www.opengeospatial.org/standards/as#01-111">ISO 19115</A>
 * @author  Martin Desruisseaux (IRD)
 * @author  Cory Horner (Refractions Research)
 * @since   GeoAPI 1.0
 */
@UML(identifier="CI_Citation", specification=ISO_19115)
public interface Citation {
    /**
     * Name by which the cited resource is known.
     *
     * @return The cited resource name.
     */
    @UML(identifier="title", obligation=MANDATORY, specification=ISO_19115)
    InternationalString getTitle();

    /**
     * Short name or other language name by which the cited information is known.
     * Example: "DCW" as an alternative title for "Digital Chart of the World".
     *
     * @return Other names for the resource, or an empty collection if none.
     */
    @UML(identifier="alternateTitle", obligation=OPTIONAL, specification=ISO_19115)
    Collection<? extends InternationalString> getAlternateTitles();

    /**
     * Unique identifier for the resource. Example: Universal Product Code (UPC),
     * National Stock Number (NSN).
     *
     * @return The identifiers, or an empty collection if none.
     */
    @UML(identifier="identifier", obligation=OPTIONAL, specification=ISO_19115)
    Collection<? extends Identifier> getIdentifiers();

    /**
     * Name and position information for an individual or organization that is responsible
     * for the resource. Returns an empty string if there is none.
     *
     * @return The individual or organization that is responsible, or an empty collection if none.
     */
    @UML(identifier="citedResponsibleParty", obligation=OPTIONAL, specification=ISO_19115)
    Collection<? extends ResponsibleParty> getCitedResponsibleParties();

    /**
     * Mode in which the resource is represented, or an empty string if none.
     *
     * @return The presentation mode, or an empty collection if none.
     */
    @UML(identifier="presentationForm", obligation=OPTIONAL, specification=ISO_19115)
    Collection<PresentationForm> getPresentationForm();
}
