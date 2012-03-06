/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
 *    Created on 23 October 2002, 17:19
 */
package org.geotools.filter;

import org.opengis.filter.FilterFactory;


/**
 * Abstract filter implementation provides or and and methods for child filters
 * to use.
 *
 * @author Ian Turton, CCG
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/main/src/main/java/org/geotools/filter/AbstractFilterImpl.java $
 * @version $Id: AbstractFilterImpl.java 37298 2011-05-25 05:16:15Z mbedward $
 */
public abstract class AbstractFilterImpl
    extends org.geotools.filter.AbstractFilter {
   
	protected AbstractFilterImpl(FilterFactory factory) {
		super(factory);
	}
}
