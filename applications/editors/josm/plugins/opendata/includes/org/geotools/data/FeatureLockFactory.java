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

import java.util.Collections;
import java.util.Map;

import org.geotools.factory.Factory;

/**
 * This specifies the interface to create FeatureLocks.
 * <p>
 * Sample use:
 * <code><pre>
 * FeatureLock lock = FeatureLockFactory.generate( "MyLock", 3600 );
 * </pre></code>
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/api/src/main/java/org/geotools/data/FeatureLockFactory.java $
 * @version $Id: FeatureLockFactory.java 37280 2011-05-24 07:53:02Z mbedward $
 * @task REVISIT: Combine this with a factory to also make Query objects?
 * @author Chris Holmes, TOPP
 * 
 * @deprecated Please use {@link FeatureLock} directly
 */

public abstract class FeatureLockFactory implements Factory {
    /**
     * Returns the implementation hints. The default implementation returns en empty map.
     */
    public Map getImplementationHints() {
        return Collections.EMPTY_MAP;
    }
}
