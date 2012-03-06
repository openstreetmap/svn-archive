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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.Transaction.State;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.identity.FeatureId;

import com.vividsolutions.jts.geom.Geometry;


/**
 * A Transaction.State that keeps a difference table for use with
 * AbstractDataStore.
 *
 * @author Jody Garnett, Refractions Research
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/main/src/main/java/org/geotools/data/TransactionStateDiff.java $
 */
public class TransactionStateDiff implements State {
    /**
     * DataStore used to commit() results of this transaction.
     *
     * @see TransactionStateDiff.commit();
     */
    AbstractDataStore store;

    /** Tranasction this State is opperating against. */
    Transaction transaction;

    /**
     * Map of differences by typeName.
     * 
     * <p>
     * Differences are stored as a Map of Feature by fid, and are reset during
     * a commit() or rollback().
     * </p>
     */
    Map typeNameDiff = new HashMap();

    public TransactionStateDiff(AbstractDataStore dataStore) {
        store = dataStore;
    }

    public synchronized Diff diff(String typeName) throws IOException {
        if (!exists(typeName)) {
            throw new IOException(typeName + " not defined");
        }

        if (typeNameDiff.containsKey(typeName)) {
            return (Diff) typeNameDiff.get(typeName);
        } else {
            Diff diff = new Diff();
            typeNameDiff.put(typeName, diff);

            return diff;
        }
    }
    
    boolean exists(String typeName) {
        String[] types;
        try {
            types = store.getTypeNames();
        } catch (IOException e) {
            return false;
        }
        Arrays.sort(types);

        return Arrays.binarySearch(types, typeName) != -1;
    }

    /**
     * Convience Method for a Transaction based FeatureWriter
     * 
     * <p>
     * Constructs a DiffFeatureWriter that works against this Transaction.
     * </p>
     *
     * @param typeName Type Name to record differences against
     * @param filter 
     *
     * @return A FeatureWriter that records Differences against a FeatureReader
     *
     * @throws IOException If a FeatureRader could not be constucted to record
     *         differences against
     */
    public synchronized FeatureWriter<SimpleFeatureType, SimpleFeature> writer(final String typeName, Filter filter)
        throws IOException {
        Diff diff = diff(typeName);
         FeatureReader<SimpleFeatureType, SimpleFeature> reader = new FilteringFeatureReader<SimpleFeatureType, SimpleFeature>(store.getFeatureReader(typeName, new DefaultQuery(typeName, filter)), filter);

        return new DiffFeatureWriter(reader, diff, filter) {
                public void fireNotification(int eventType, ReferencedEnvelope bounds) {
                    switch (eventType) {
                    case FeatureEvent.FEATURES_ADDED:
                        store.listenerManager.fireFeaturesAdded(typeName,
                            transaction, bounds, false);

                        break;

                    case FeatureEvent.FEATURES_CHANGED:
                        store.listenerManager.fireFeaturesChanged(typeName,
                            transaction, bounds, false);

                        break;

                    case FeatureEvent.FEATURES_REMOVED:
                        store.listenerManager.fireFeaturesRemoved(typeName,
                            transaction, bounds, false);

                        break;
                    }
                }
                public String toString() {
                    return "<DiffFeatureWriter>("+reader.toString()+")";
                }
            };
    }
    /**
     * A NullObject used to represent the absence of a SimpleFeature.
     * <p>
     * This class is used by TransactionStateDiff as a placeholder
     * to represent features that have been removed. The concept
     * is generally useful and may wish to be taken out as a separate
     * class (used for example to represent deleted rows in a shapefile).
     */
    public static final SimpleFeature NULL=new SimpleFeature( ){
        public Object getAttribute(String path) {
            return null;
        }

        public Object getAttribute(int index) {
            return null;
        }

        public ReferencedEnvelope getBounds() {
            return null;
        }

        public Geometry getDefaultGeometry() {
            return null;
        }

        public SimpleFeatureType getFeatureType() {
            return null;
        }

        public String getID() {
            return null;
        }
        public FeatureId getIdentifier() {
        	return null;
        }

        public void setAttribute(int position, Object val) {
        }

        public void setAttribute(String path, Object attribute)
                throws IllegalAttributeException {
        }

        public Object getAttribute(Name name) {
            return null;
        }

        public List<Object> getAttributes() {
            return null;
        }

        public SimpleFeatureType getType() {
            return null;
        }

        public void setAttribute(Name name, Object value) {
        }

        public void setAttributes(List<Object> values) {
        }

        public GeometryAttribute getDefaultGeometryProperty() {
            return null;
        }

        public Collection<Property> getProperties() {
            return null;
        }
        
        public Property getProperty(Name name) {
            return null;
        }

        public Property getProperty(String name) {
            return null;
        }

        public Collection<? extends Property> getValue() {
            return null;
        }

        public AttributeDescriptor getDescriptor() {
            return null;
        }

        public Name getName() {
            return null;
        }

        public Map<Object, Object> getUserData() {
            return null;
        }

        public boolean isNillable() {
            return false;
        }

        public String toString() {
            return "<NullFeature>";
        }
        public int hashCode() {
            return 0;
        }
		public boolean equals( Object arg0 ) {
		    return arg0 == this;
		}
		public void validate() {
		}
    };
}
