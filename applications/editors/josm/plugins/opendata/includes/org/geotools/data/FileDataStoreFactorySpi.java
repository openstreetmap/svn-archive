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
import java.net.URL;


/**
 * DataAccessFactory for working with formats based on a single URL.
 * <p>
 * This interface provides a mechanism of discovery for DataAccessFactories
 * which support singular files.
 * </p>
 *
 * @author dzwiers
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/api/src/main/java/org/geotools/data/FileDataStoreFactorySpi.java $
 */
public interface FileDataStoreFactorySpi extends DataStoreFactorySpi {

    /**
     * Tests if the provided url can be handled by this factory.
     *
     * @param url URL to a real file (may not be local)
     *
     * @return <code>true</code> if this url can when this dataStore can resolve and read the data specified
     */
    public boolean canProcess(URL url);

    /**
     * A DataStore attached to the provided url, may be created if needed.
     * <p>
     * Please note that additional configuration options may be available
     * via the traditional createDataStore( Map ) method provided by the
     * superclass.
     * <p>
     * @param url The data location for the
     *
     * @return Returns an AbstractFileDataStore created from the data source
     *         provided.
     *
     * @throws IOException
     *
     * @see AbstractFileDataStore
     */
    public FileDataStore createDataStore(URL url) throws IOException;
}
