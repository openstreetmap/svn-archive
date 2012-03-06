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

import java.io.IOException;


/**
 * Indicates a lock contention, and attempt was made to modify or aquire with
 * out Authroization.
 *
 * @author Jody Garnett, Refractions Research
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/main/src/main/java/org/geotools/data/FeatureLockException.java $
 */
public class FeatureLockException extends IOException {
    private static final long serialVersionUID = 1L;

    public FeatureLockException() {
        super();
    }

    public FeatureLockException(String message) {
        super(message);
    }

    public FeatureLockException(String message, String featureID) {
        super(message);
    }

    public FeatureLockException(String message, String featureID, Throwable t) {
        super(message);
        initCause(t);
    }
}
