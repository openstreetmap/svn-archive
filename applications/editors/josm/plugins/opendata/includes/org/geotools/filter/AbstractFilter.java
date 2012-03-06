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
 */
package org.geotools.filter;


/**
 * Implements Filter interface, with constants and default behaviors for
 * methods.
 *
 * @author Rob Hranac, Vision for New York
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/main/src/main/java/org/geotools/filter/AbstractFilter.java $
 * @version $Id: AbstractFilter.java 37298 2011-05-25 05:16:15Z mbedward $
 */
public abstract class AbstractFilter extends FilterAbstract implements Filter {

    /** Defines filter type (all valid types defined below). */
    protected short filterType;

    /** Sets the permissiveness of the filter construction handling. */
    protected boolean permissiveConstruction = true;

    /**
     * 
     * @param factory
     */
    protected AbstractFilter(org.opengis.filter.FilterFactory factory) {
		super(factory);
	}
        
    /**
     * This method checks if the object is an instance of {@link Feature} and 
     * if so, calls through to {@link Filter#evaluate(Feature)}. This is done 
     * to maintain backwards compatability with previous version of Filter api 
     * which depended on Feature. If the object is not an instance of feature 
     * the super implementation is called.
     */
    /*
    public boolean evaluate(Object object) {
    	if (object instanceof Feature  || object == null ) {
    		return evaluate((Feature)object);
    	}
    	
    	return false;
    }*/
    
    /**
     * Checks to see if passed type is math.
     *
     * @param filterType Type of filter for check.
     *
     * @return Whether or not this is a math filter type.
     */
    protected static boolean isMathFilter(short filterType) {
        return ((filterType == COMPARE_LESS_THAN)
        || (filterType == COMPARE_GREATER_THAN)
        || (filterType == COMPARE_LESS_THAN_EQUAL)
        || (filterType == COMPARE_GREATER_THAN_EQUAL));
    }

    /**
     * Retrieves the type of filter.
     *
     * @return a short representation of the filter type.
     * 
     * @deprecated The enumeration base type system is replaced with a class 
     * 	based type system. An 'instanceof' check should be made instead of 
     * 	calling this method.
     */
    public short getFilterType() {
        return filterType;
    }
}
