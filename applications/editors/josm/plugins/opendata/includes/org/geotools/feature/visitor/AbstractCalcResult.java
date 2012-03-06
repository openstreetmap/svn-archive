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
package org.geotools.feature.visitor;



/**
 * An abstract implementation for CalcResults. Each subclass should implement
 * its own getValue(), merge(), and constructor methods.
 * 
 * @author Cory Horner, Refractions
 * 
 * @since 2.2.M2
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/main/src/main/java/org/geotools/feature/visitor/AbstractCalcResult.java $
 */
public class AbstractCalcResult implements CalcResult {
    public boolean isCompatible(CalcResult targetResults) {
        return targetResults == CalcResult.NULL_RESULT;
    }

    public CalcResult merge(CalcResult resultsToAdd) {
    	if(resultsToAdd == CalcResult.NULL_RESULT) {
    		return this;
    	} else {
    		if (!isCompatible(resultsToAdd)) {
                throw new IllegalArgumentException(
                    "Parameter is not a compatible type");
            } else {
            	throw new IllegalArgumentException(
				"The CalcResults claim to be compatible, but the appropriate merge " +
				"method has not been implemented.");
            }
    	}
    }

    public Object getValue() {
        return null;
    }

    public String toString() {
        return getValue().toString();
    }
}
