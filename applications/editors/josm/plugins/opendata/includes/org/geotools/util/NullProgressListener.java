/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.util;



/**
 * A default progress listener implementation suitable for
 * subclassing.
 * <p>
 * This implementation supports cancelation and getting/setting the description.
 * The default implementations of the other methods do nothing.
 * </p>
 *
 * @since 2.2
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/main/src/main/java/org/geotools/util/NullProgressListener.java $
 * @version $Id: NullProgressListener.java 37298 2011-05-25 05:16:15Z mbedward $
 */
public class NullProgressListener implements org.opengis.util.ProgressListener {

    /**
     * {@code true} if the action is canceled.
     */
    private boolean canceled = false;

    /**
     * Creates a null progress listener with no description.
     */
    public NullProgressListener() {
    }

    public void started() {
        //do nothing
    }

    public void progress(float percent) {
        //do nothing
    }
    
    public void complete() {
        //do nothing
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void exceptionOccurred(Throwable exception) {
        //do nothing
    }
}
