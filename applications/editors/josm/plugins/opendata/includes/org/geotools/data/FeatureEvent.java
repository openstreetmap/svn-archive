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

import java.util.EventObject;

import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;

import com.vividsolutions.jts.geom.Envelope;


/**
 * Represents all events triggered by DataStore instances (typically change events).
 *
 * <p>
 * The "Source" for FeatureEvents is taken to be a <code>FeatureSource</code>,
 * rather than <code>DataStore</code>. The is due to SimpleFeatureSource having a
 * hold of Transaction information.
 * </p>
 *
 * <p>
 * DataStore implementations will actually keep the list listeners sorted
 * by TypeName, and can report FeatureWriter modifications as required
 * (by filtering the Listener list by typeName and Transaction).
 * </p>
 *
 * <p>
 * The Transaction.commit() operation will also need to provide notification, this
 * shows up as a CHANGE event; with a bit more detail being available in the subclass
 * BatchFeatureEvent.
 * </p>
 * 
 * @since GeoTools 2.0
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/api/src/main/java/org/geotools/data/FeatureEvent.java $
 */
public class FeatureEvent extends EventObject {
    private static final long serialVersionUID = 3154238322369916485L;

    /**
     * FeatureWriter event type denoting the adding features.
     *
     * <p>
     * This EventType is used when FeatureWriter.write() is called when
     * <code>FeatureWriter.hasNext()</code> has previously returned
     * <code>false</code>. This action represents a newly create Feature being
     * passed to the DataStore.
     * </p>
     *
     * <p>
     * The FeatureWriter making the modification will need to check that
     * <code>typeName</code> it is modifing matches the
     * <code>FeatureSource.getSchema().getTypeName()</code> before sending
     * notification to any listeners on the FeatureSource.
     * </p>
     *
     * <p>
     * If the FeatureWriter is opperating against a Transaction it will need
     * ensure that to check the FeatureSource.getTransaction() for a match
     * before sending notification to any listeners on the FeatureSource.
     * </p>
     *
     * <p>
     * FeatureEvent.getBounds() should reflect the the Bounding Box of the
     * newly created Features.
     * </p>
     * @deprecated Please use FeatureEvent.getType() == Type.ADDED
     */
    public static final int FEATURES_ADDED = 1;

    /**
     * Event type constant denoting that features in the collection has been
     * modified.
     *
     * <p>
     * This EventType is used when a FeatureWriter.write() is called when
     * <code>FeatureWriter.hasNext()</code> returns <code>true</code> and the
     * current Feature has been changed. This EventType is also used when a
     * Transaction <code>commit()</code> or <code>rolledback</code> is called.
     * </p>
     *
     * <p>
     * The FeatureWriter making the modification will need to check that
     * <code>typeName</code> it is modifing matches the
     * <code>FeatureSource.getSchema().getTypeName()</code> before sending
     * notification to any listeners on the FeatureSource.
     * </p>
     *
     * <p>
     * If the FeatureWriter is opperating against a Transaction it will need
     * ensure that to check the FeatureSource.getTransaction() for a match
     * before sending notification to any listeners on the FeatureSource. All
     * FeatureSources of the same typename will need to be informed of a
     * <code>commit</code>, except ones in the same Transaction,  and only
     * FeatureSources in the same Transaction will need to be informed of a
     * rollback.
     * </p>
     *
     * <p>
     * FeatureEvent.getBounds() should reflect the the BoundingBox of the
     * FeatureWriter modified Features. This may not be possible during a
     * <code>commit()</code> or <code>rollback()</code> opperation.
     * </p>
     * @deprecated Please use FeatureEvent.getType() == Type.CHANGED 
     */
    public static final int FEATURES_CHANGED = 0;

    /**
     * Event type constant denoting the removal of a feature.
     *
     * <p>
     * This EventType is used when FeatureWriter.remove() is called. This
     * action represents a Feature being removed from the DataStore.
     * </p>
     *
     * <p>
     * The FeatureWriter making the modification will need to check that
     * <code>typeName</code> it is modifing matches the
     * <code>FeatureSource.getSchema().getTypeName()</code> before sending
     * notification to any listeners on the FeatureSource.
     * </p>
     *
     * <p>
     * If the FeatureWriter is opperating against a Transaction it will need
     * ensure that to check the FeatureSource.getTransaction() for a match
     * before sending notification to any listeners on the FeatureSource.
     * </p>
     *
     * <p>
     * FeatureEvent.getBounds() should reflect the the Bounding Box of the
     * removed Features.
     * </p>
     * @deprecated Please use FeatureEvent.getType() == Type.REMOVED 
     */
    public static final int FEATURES_REMOVED = -1;
        
    /**
     * Constructs a new FeatureEvent.
     *
     * @param SimpleFeatureSource The DataStore that fired the event
     * @param eventType One of FEATURE_CHANGED, FEATURE_REMOVED or
     *        FEATURE_ADDED
     * @param bounds The area modified by this change
     * @deprecated Please use FeatureEvent( FeatureSource, Type, Envelope )
     */
    public FeatureEvent(FeatureSource<? extends FeatureType, ? extends Feature> featureSource,
            int eventType, Envelope bounds) {
        super(featureSource);        
    }
}
