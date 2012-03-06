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
package org.geotools.measure;

import javax.measure.quantity.Dimensionless;
import javax.measure.unit.Unit;


/**
 * A set of units to use in addition of {@link SI} and {@link NonSI}.
 *
 * @since 2.1
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/referencing/src/main/java/org/geotools/measure/Units.java $
 * @version $Id: Units.java 37299 2011-05-25 05:21:24Z mbedward $
 * @author Martin Desruisseaux (IRD)
 */
public final class Units {
    /**
     * Do not allows instantiation of this class.
     */
    private Units() {
    }

    /**
     * Parts per million.
     */
    public static final Unit<Dimensionless> PPM = Unit.ONE.times(1E-6);
}
