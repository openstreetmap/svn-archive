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
package org.geotools.resources;



/**
 * Interface for object that can be formatted as
 * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
 * Known Text</cite> (WKT), but can't extends {@link org.geotools.referencing.wkt.Formattable}. This
 * interface is especially used for {@code AffineTransform2D} implementation. This interface is not
 * public because the {@code formatWKT(Formatter)} method usually has a protected access.
 *
 * @since 2.0
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/referencing/src/main/java/org/geotools/resources/Formattable.java $
 * @version $Id: Formattable.java 37299 2011-05-25 05:21:24Z mbedward $
 * @author Martin Desruisseaux (IRD)
 *
 * @todo Consider renaming {@link org.geotools.referencing.wkt.Formattable} as
 *       {@code AbstractFormattable} and move this interface in the wkt package.
 */
public interface Formattable {

}
