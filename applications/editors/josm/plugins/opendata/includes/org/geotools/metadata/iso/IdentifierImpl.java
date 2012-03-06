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
package org.geotools.metadata.iso;

import org.opengis.metadata.Identifier;
import org.opengis.metadata.citation.Citation;


/**
 * Value uniquely identifying an object within a namespace.
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/metadata/src/main/java/org/geotools/metadata/iso/IdentifierImpl.java $
 * @version $Id: IdentifierImpl.java 37298 2011-05-25 05:16:15Z mbedward $
 * @author Martin Desruisseaux (IRD)
 * @author Touraïvane
 *
 * @since 2.1
 */
public class IdentifierImpl extends MetadataEntity implements Identifier {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 7459062382170865919L;

    /**
     * Alphanumeric value identifying an instance in the namespace.
     */
    private String code;

    /**
     * Organization or party responsible for definition and maintenance of the
     * {@linkplain #getCode code}.
     */
    private Citation authority;

    /**
     * Construct an initially empty identifier.
     */
    public IdentifierImpl() {
    }

    /**
     * Creates an identifier initialized to the given code.
     */
    public IdentifierImpl(final String code) {
        setCode(code);
    }

    /**
     * Alphanumeric value identifying an instance in the namespace.
     *
     * @return The code.
     */
    public String getCode() {
        return code;
    }

    /**
     * Set the alphanumeric value identifying an instance in the namespace.
     */
    public synchronized void setCode(final String newValue) {
        checkWritePermission();
        code = newValue;
    }

    /**
     * Organization or party responsible for definition and maintenance of the
     * {@linkplain #getCode code}.
     *
     * @return The authority, or {@code null} if not available.
     */
    public Citation getAuthority() {
        return authority;
    }

    /**
     * Set the organization or party responsible for definition and maintenance of the
     * {@linkplain #getCode code}.
     */
    public synchronized void setAuthority(final Citation newValue) {
        checkWritePermission();
        authority = newValue;
    }
}
