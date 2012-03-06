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
package org.geotools.filter.capability;

import java.util.List;

import org.opengis.filter.capability.FunctionName;

/**
 * Implementation of the FunctionName interface.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/main/src/main/java/org/geotools/filter/capability/FunctionNameImpl.java $
 */
public class FunctionNameImpl extends OperatorImpl implements FunctionName {
    /** Number of required arguments */
    int argumentCount;
    
    List<String> argumentNames;
    
    public FunctionNameImpl( String name, int argumentCount ) {
        super( name );
        this.argumentCount = argumentCount;
        this.argumentNames = null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + argumentCount;
        return result;
    }

    @Override
    public boolean equals( Object obj ) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        final FunctionNameImpl other = (FunctionNameImpl) obj;
        if (argumentCount != other.argumentCount)
            return false;
        return true;
    }
}
