/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2011, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2003-2005, Open Geospatial Consortium Inc.
 *    
 *    All Rights Reserved. http://www.opengis.org/legal/
 */
package org.opengis.referencing;


/**
 * Thrown when an {@linkplain AuthorityFactory authority factory} can't find
 * the requested authority code.
 *
 * @author  Martin Desruisseaux (IRD)
 * @since   GeoAPI 1.0
 *
 * @see org.opengis.referencing.datum.DatumAuthorityFactory
 * @see org.opengis.referencing.crs.CRSAuthorityFactory
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/opengis/src/main/java/org/opengis/referencing/NoSuchAuthorityCodeException.java $
 */
public class NoSuchAuthorityCodeException extends FactoryException {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -1573748311981746573L;

    /**
     * Constructs an exception with the specified detail message and authority code.
     *
     * @param  message The detail message. The detail message is saved
     *         for later retrieval by the {@link #getMessage()} method.
     * @param  authority The authority.
     * @param  code The invalid authority code.
     */
    public NoSuchAuthorityCodeException(String message, String authority, String code) {
        super(message);
    }
}
