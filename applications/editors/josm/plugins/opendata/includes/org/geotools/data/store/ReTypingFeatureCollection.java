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
package org.geotools.data.store;

import java.util.Iterator;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.collection.DecoratingSimpleFeatureCollection;
import org.geotools.feature.collection.DelegateSimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * SimpleFeatureCollection decorator which decorates a feature collection "re-typing" 
 * its schema based on attributes specified in a query.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/main/src/main/java/org/geotools/data/store/ReTypingFeatureCollection.java $
 */
public class ReTypingFeatureCollection extends DecoratingSimpleFeatureCollection {

	SimpleFeatureType featureType;
    
	public ReTypingFeatureCollection ( SimpleFeatureCollection delegate, SimpleFeatureType featureType ) {
		super(delegate);
		this.featureType = featureType;
	}
	
	public SimpleFeatureType getSchema() {
	    return featureType;
	}
		
	public SimpleFeatureIterator features() {
		return new DelegateSimpleFeatureIterator( this, iterator() );
	}

	public Iterator<SimpleFeature> iterator() {
		return new ReTypingIterator( delegate.iterator(), delegate.getSchema(), featureType );
	}
	
	public void close(Iterator close) {
		ReTypingIterator reType = (ReTypingIterator) close;
		delegate.close( reType.getDelegate() );
	}
}
