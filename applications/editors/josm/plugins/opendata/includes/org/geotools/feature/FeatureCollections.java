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

import java.util.Collections;
import java.util.Map;

import org.geotools.factory.Factory;

/**
 * A utility class for working with FeatureCollections.
 * Provides a mechanism for obtaining a SimpleFeatureCollection instance.
 * @author  Ian Schneider
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/api/src/main/java/org/geotools/feature/FeatureCollections.java $
 */
public abstract class FeatureCollections implements Factory {
  
  /**
   * Returns the implementation hints. The default implementation returns en empty map.
   */
  public Map getImplementationHints() {
    return Collections.EMPTY_MAP;
  }  
}
