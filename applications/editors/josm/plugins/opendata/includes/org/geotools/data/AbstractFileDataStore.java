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
 * <p>
 * This class assumes the DataStore represents a single source,  represented by
 * a URL. In many cases the default functionality  is chained off to the
 * parent class (AbstractDataStore).
 * </p>
 *
 * @author dzwiers
 *
 * @see AbstractDataStore
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/main/src/main/java/org/geotools/data/AbstractFileDataStore.java $
 */
public abstract class AbstractFileDataStore extends AbstractDataStore implements FileDataStore {
    /**
     * Singular version, returns the FeatureType for the url being read.
     *
     * @see org.geotools.data.DataStore#getSchema(java.lang.String)
     */
    public SimpleFeatureType getSchema() throws IOException {
        return this.getSchema( getTypeNames()[0] );
    }

    /**
     * Singular version, which must be implemented to represent a Reader  for
     * the url being read.
     *
     * @see org.geotools.data.DataStore#getFeatureReader(java.lang.String)
     */
    public FeatureReader<SimpleFeatureType, SimpleFeature> getFeatureReader()
        throws IOException{
        return getFeatureReader( getTypeNames()[0] );
    }
}
