/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data;

import org.opengis.feature.type.AttributeDescriptor;

/**
 * Provides support for creating AttributeReaders.
 * @since 2.0
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/main/src/main/java/org/geotools/data/AbstractAttributeIO.java $
 * @version $Id: AbstractAttributeIO.java 37298 2011-05-25 05:16:15Z mbedward $
 * @author  Ian Schneider
 */
public abstract class AbstractAttributeIO implements AttributeReader {
    
    protected AttributeDescriptor[] metaData;
    
    protected AbstractAttributeIO(AttributeDescriptor[] metaData) {
        this.metaData = metaData;
    }
        
    public final int getAttributeCount() {
        return metaData.length;
    }
    
    public final AttributeDescriptor getAttributeType(int position) {
        return metaData[position];
    }
}
