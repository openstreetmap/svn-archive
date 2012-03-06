/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2001-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.resources;

import java.text.ChoiceFormat;


/**
 * Simple mathematical functions.
 *
 * @since 2.0
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/metadata/src/main/java/org/geotools/resources/XMath.java $
 * @version $Id: XMath.java 37298 2011-05-25 05:16:15Z mbedward $
 * @author Martin Desruisseaux (IRD)
 */
public final class XMath {

    /**
     * Do not allow instantiation of this class.
     */
    private XMath() {
    }

    /**
     * Finds the least double greater than <var>f</var>.
     * If {@code NaN}, returns same value.
     *
     * @see java.text.ChoiceFormat#nextDouble
     *
     * @todo Remove this method when we will be allowed to use Java 6.
     */
    public static double next(final double f) {
        return ChoiceFormat.nextDouble(f);
    }

    /**
     * Finds the greatest double less than <var>f</var>.
     * If {@code NaN}, returns same value.
     *
     * @see java.text.ChoiceFormat#previousDouble
     *
     * @todo Remove this method when we will be allowed to use Java 6.
     */
    public static double previous(final double f) {
        return ChoiceFormat.previousDouble(f);
    }
}
