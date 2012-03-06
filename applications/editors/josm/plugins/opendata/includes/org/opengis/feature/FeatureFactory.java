/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2011, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2004-2007 Open Geospatial Consortium Inc.
 *    
 *    All Rights Reserved. http://www.opengis.org/legal/
 */
package org.opengis.feature;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * Factory for creation of attributes, associations, and features.
 * <p>
 * Implementations of this interface should not contain any "special logic" for
 * creating attributes and features. Method implementations should be straight
 * through calls to a constructor.
 * </p>
 *
 * @author Gabriel Roldan (Axios Engineering)
 * @author Justin Deoliveira (The Open Planning Project)
 * @since 2.2
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/opengis/src/main/java/org/opengis/feature/FeatureFactory.java $
 */
public interface FeatureFactory {

    /**
     * Create a SimpleFeature from an array of objects.
     * <p>
     * Please note that the provided array may be used directly by an implementation.
     * 
     * @param array Object array of values; this array may beused directly.
     * @param type The type of the simple feature.
     * @param id The id of the feature.
     */   
    SimpleFeature createSimpleFeature( Object[] array, SimpleFeatureType type, String id );   
}
