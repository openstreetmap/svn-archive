/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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

import java.util.Set;


/**
 * This is used to represent the absense of a Transaction and the use of
 * AutoCommit.
 *
 * <p>
 * This class serves as the implementation of the constant Transaction.NONE.
 * It is a NullObject and we feel no need to make this class public.
 * </p>
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/api/src/main/java/org/geotools/data/AutoCommitTransaction.java $
 */
class AutoCommitTransaction implements Transaction {
    /**
     * Authorization IDs are not stored by AutoCommit.
     *
     * <p>
     * Authorization IDs are only stored for the duration of a Transaction.
     * </p>
     *
     * @return Set of authorizations
     *
     * @throws UnsupportedOperationException AUTO_COMMIT does not support this
     */
    public Set<String> getAuthorizations() {
        throw new UnsupportedOperationException(
            "Authorization IDs are not valid for AutoCommit Transaction");
    }

    /**
     * AutoCommit does not save State.
     *
     * <p>
     * While symetry would be good, state should be commited not stored for
     * later.
     * </p>
     *
     * @param key Key that is not used to Store State
     * @param state State we are not going to externalize
     *
     * @throws UnsupportedOperationException AutoCommit does not support State
     */
    public void putState(Object key, State state) {
        throw new UnsupportedOperationException(
            "AutoCommit does not support the putState opperations");
    }

    /**
     * I am not sure should AutoCommit be able to save sate?
     *
     * <p>
     * While symetry would be good, state should be commited not stored for
     * later.
     * </p>
     *
     * @param key Key used to retrieve State
     *
     * @return State earlier provided with putState
     *
     * @throws UnsupportedOperationException As Autocommit does not support
     *         State
     */
    public State getState(Object key) {
        throw new UnsupportedOperationException(
            "AutoCommit does not support the getState opperations");
    }
}
