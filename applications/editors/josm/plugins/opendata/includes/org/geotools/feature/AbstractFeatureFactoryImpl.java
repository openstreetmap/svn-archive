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
package org.geotools.feature;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.opengis.feature.FeatureFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory2;

/**
 * Factory for creating instances of the Attribute family of classes.
 * 
 * @author Ian Schneider
 * @author Gabriel Roldan
 * @author Justin Deoliveira
 * 
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/main/src/main/java/org/geotools/feature/AbstractFeatureFactoryImpl.java $
 * @version $Id: AbstractFeatureFactoryImpl.java 37680 2011-07-19 15:18:56Z groldan $
 */
public abstract class AbstractFeatureFactoryImpl implements FeatureFactory {
 
    
    FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
    
    /**
     * Whether the features to be built should be self validating on construction and value setting, or not.
     * But default, not, subclasses do override this value
     */
    boolean validating = false;
	
    public SimpleFeature createSimpleFeature(Object[] array,
            SimpleFeatureType type, String id) {
        if( type.isAbstract() ){
            throw new IllegalArgumentException("Cannot create an feature of an abstract FeatureType "+type.getTypeName());
        }
        return new SimpleFeatureImpl(array, type, ff.featureId(id), validating);
    }
}
