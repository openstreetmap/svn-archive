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
package org.geotools.data;

import java.util.Set;


/**
 * The controller for Transaction with FeatureStore.
 *
 * <p>
 * Shapefiles, databases, etc. are safely modified with the assistance of this
 * interface. Transactions are also to provide authorization when working with
 * locked features.
 * </p>
 *
 * <p>
 * All operations are considered to be working against a Transaction.
 * Transaction.AUTO_COMMIT is used to represent an immidiate mode where
 * requests are immidately commited.
 * </p>
 *
 * <p>
 * For more information please see DataStore and FeatureStore.
 * </p>
 *
 * <p>
 * Example Use:
 * </p>
 * <pre><code>
 * Transaction t = new DefaultTransaction("handle");
 * t.putProperty( "hint", new Integer(7) );
 * try {
 *     SimpleFeatureStore road = (SimpleFeatureStore) store.getFeatureSource("road");
 *     FeatureStore river = (SimpleFeatureStore) store.getFeatureSource("river");
 *
 *     road.setTransaction( t );
 *     river.setTransaction( t );
 *
 *     t.addAuthorization( lockID );  // provide authoriztion
 *     road.removeFeatures( filter ); // operate against transaction
 *     river.removeFeature( filter ); // operate against transaction
 *
 *     t.commit(); // commit operations
 * }
 * catch (IOException io){
 *     t.rollback(); // cancel operations
 * }
 * finally {
 *     t.close(); // free resources
 * }
 * </code></pre>
 * <p>
 * Example code walkthrough (from the perspective of Transaction):
 * </p>
 * <ol>
 * <li>A new transaction is created (an instanceof DefaultTransaction with a handle)</li>
 * <li>A hint is provided using Transaction.putProperty( key, value )</li>
 * <li>Transaction is provided to two FeatureStores, this may result
 *     in Transaction.State instances being registered</li>
 *     <ul>
 *     <li>TransactionStateDiff (stored by DataStore):
 *         Used for in memory locking is used by many DataStore's
 *         (like ShapefileDataStore).
 *         Lazy creation by AbstractDataStore.state(transaction).
 *         </li>
 *     <li>JDBCTransactionState (stored by ConnectionPool):
 *         Used to manage connection rollback/commit.
 *         Lazy creation as part of JDBCDataStore.getConnection(transaction).
 *         </li>
 *     <li>InProcessLockingManager.FeatureLock (stored by LockingManger):
 *         Used for per transaction FeatureLocks, used to free locked features
 *         on Transaction commit/rollback.
 *         </li>
 *     </ul>
 *     These instances of Transaction state may make use of any hint provided
 *     to Transaction.putProperty( key, value ) when they are connected with
 *     Transaction.State.setTransaction( transaction ).
 * <li>t.addAuthorization(lockID) is called, each Transaction.State has its
 *     addAuthroization(String) callback invoked with the value of lockID</li>
 * <li>FeatureStore.removeFeatures methods are called on the two DataStores.
 *     <ul>
 *     <li>PostgisFeatureStore.removeFeatures(fitler) handles operation
 *         without delegation.
 *         </li>
 *     <li>Most removeFeature(filter) implementations use the implementation
 *         provided by AbstractFeatureStore which delegates to FeatureWriter.
 *         </li>
 *     </ul>
 *     Any of these operations may make use of the
 *     Transaction.putProperty( key, value ).
 * <li>The transaction is commited, all of the Transaction.State methods have
 *     there Transaction.State.commit() methods called gicing them a chance
 *     to applyDiff maps, or commit various connections.
 *     </li>
 * <li>The transaction is closed, all of the Transaction.State methods have
 *     there Transaction.State.setTransaction( null ) called, giving them a
 *     chance to clean up diffMaps, or return connections to the pool.
 *     </li>
 * </ol>
 * @author Jody Garnett
 * @author Chris Holmes, TOPP
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/api/src/main/java/org/geotools/data/Transaction.java $
 * @version $Id: Transaction.java 37280 2011-05-24 07:53:02Z mbedward $
 */
public interface Transaction {
    /** 
     * Represents AUTO_COMMIT mode as opposed to operations with commit/rollback
     * control under a user-supplied transaction.
     */
    static final Transaction AUTO_COMMIT = new AutoCommitTransaction();

    /**
     * List of Authorizations IDs held by this transaction.
     *
     * <p>
     * This list is reset by the next call to commit() or rollback().
     * </p>
     *
     * <p>
     * Authorization IDs are used to provide FeatureLock support.
     * </p>
     *
     * @return List of Authorization IDs
     */
    Set<String> getAuthorizations();

    /**
     * Allows SimpleFeatureSource to squirel away information( and callbacks ) for
     * later.
     *
     * <p>
     * The most common example is a JDBC DataStore saving the required
     * connection for later operations.
     * </p>
     * <pre><code>
     * ConnectionState implements State {
     *     public Connection conn;
     *     public addAuthorization() {}
     *     public commit(){ conn.commit(); }
     *     public rollback(){ conn.rollback(); }
     * }
     * </code></pre>
     *
     * <p>
     * putState will call State.setTransaction( transaction ) to allow State a
     * chance to configure itself.
     * </p>
     *
     * @param key Key used to externalize State
     * @param state Externalized State
     */
    void putState(Object key, State state);

    /**
     * Allows DataStores to squirel away information( and callbacks ) for
     * later.
     *
     * <p>
     * The most common example is a JDBC DataStore saving the required
     * connection for later operations.
     * </p>
     *
     * @return Current State externalized by key, or <code>null</code> if not
     *         found
     */
    State getState(Object key);

    /**
     * DataStore implementations can use this interface to externalize the
     * state they require to implement Transaction Support.
     *
     * <p>
     * The commit and rollback methods will be called as required. The
     * intension is that several DataStores can share common transaction state
     * (example: Postgis DataStores sharing a connection to the same
     * database).
     * </p>
     *
     * @author jgarnett, Refractions Reasearch Inc.
     * @version CVS Version
     *
     * @see org.geotools.data
     */
    static public interface State {
    }
}
