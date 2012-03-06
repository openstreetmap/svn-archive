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
 */
package org.geotools.util;

import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * Throws when an operation can't use arbitrary implementation of an interface, and
 * a given instance doesn't meet the requirement. For example this exception may be
 * thrown when an operation requires a Geotools implementation of a
 * <A HREF="http://geoapi.sourceforge.net">GeoAPI</A> interface.
 *
 * @since 2.0
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/metadata/src/main/java/org/geotools/util/UnsupportedImplementationException.java $
 * @version $Id: UnsupportedImplementationException.java 37298 2011-05-25 05:16:15Z mbedward $
 * @author Martin Desruisseaux (IRD)
 */
public class UnsupportedImplementationException extends UnsupportedOperationException {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = -649050339146622730L;

    /**
     * Constructs an exception with an error message formatted for the specified class.
     *
     * @param classe The unexpected implementation class.
     */
    public UnsupportedImplementationException(final Class<?> classe) {
        super(Errors.format(ErrorKeys.UNKNOW_TYPE_$1, classe));
    }
}
