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
package org.geotools.data;

import java.io.IOException;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * DataStore represents a single file of content.
 * <p>
 * Allows developer to skip refering to the typeName when a file contains
 * only a single set of content.
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/api/src/main/java/org/geotools/data/FileDataStore.java $
 */
public interface FileDataStore extends DataStore {
    
    /**
     * FeatureType for the file being read.
     * <p>
     * This is the same as getSchema( getTypeName[0] )
     * </p>
     * 
     * @return FeatureType of the file being read
     * @see org.geotools.data.DataStore#getSchema(java.lang.String)
     */
    SimpleFeatureType getSchema() throws IOException;

    /**
     * @see org.geotools.data.DataStore#getFeatureReader(java.lang.String)
     */
    FeatureReader<SimpleFeatureType, SimpleFeature> getFeatureReader() throws IOException;
}
