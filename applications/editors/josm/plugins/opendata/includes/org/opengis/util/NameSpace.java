/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2011, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2003-2005, Open Geospatial Consortium Inc.
 *    
 *    All Rights Reserved. http://www.opengis.org/legal/
 */
package org.opengis.util;

import static org.opengis.annotation.Obligation.MANDATORY;
import static org.opengis.annotation.Specification.ISO_19103;

import org.opengis.annotation.UML;


/**
 * A domain in which {@linkplain GenericName names} given by character strings are defined.
 *
 * @author Bryce Nordgren (USDA)
 * @author Martin Desruisseaux (IRD)
 * @since GeoAPI 2.1
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/opengis/src/main/java/org/opengis/util/NameSpace.java $
 */
@UML(identifier="NameSpace", specification=ISO_19103)
public interface NameSpace {

    /**
     * Represents the identifier of this namespace. If the {@linkplain #isGlobal global} attribute is
     * {@code true}, indicating that this is a top level {@code NameSpace}, then the name should be a
     * {@linkplain LocalName local name}. If {@code false}, name should be a fully-qualified name where
     * <code>name.{@linkplain GenericName#scope() scope()}.{@linkplain #isGlobal} == true</code>.
     *
     * @return The identifier of this namespace.
     */
    @UML(identifier="name", obligation=MANDATORY, specification=ISO_19103)
    GenericName name();
}
