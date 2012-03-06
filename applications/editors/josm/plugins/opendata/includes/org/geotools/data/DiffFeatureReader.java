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
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.geotools.feature.IllegalAttributeException;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.Id;
import org.opengis.filter.identity.Identifier;

/**
 * A  FeatureReader that considers differences.
 * <p>
 * Used to implement In-Process Transaction support. This implementation will need to peek ahead in
 * order to check for deletetions.
 * </p>
 * 
 * @author Jody Garnett, Refractions Research
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/main/src/main/java/org/geotools/data/DiffFeatureReader.java $
 */
public class DiffFeatureReader<T extends FeatureType, F extends Feature> implements  FeatureReader<T, F> {
    FeatureReader<T, F> reader;
    Diff diff;

    /** Next value as peeked by hasNext() */
    F next = null;
    private Filter filter;
    private Set encounteredFids;

	private Iterator<F> addedIterator;
	private Iterator<F> modifiedIterator;
	private Iterator<Identifier> fids;
	
	private boolean fidFilter = false;
	
    /**
     * This constructor grabs a "copy" of the current diff.
     * <p>
     * This reader is not "live" to changes over the course of the Transaction. (Iterators are not
     * always stable of the course of modifications)
     * </p>
     * 
     * @param reader
     * @param diff2 Differences of Feature by FID
     */
    public DiffFeatureReader(FeatureReader<T, F> reader, Diff diff2, Filter filter ) {
        this.reader = reader;
        this.diff = diff2;
        this.filter = filter;
        encounteredFids=new HashSet();

        if( filter instanceof Id){
        	fidFilter=true;
        }
        
        synchronized (diff) {
        	addedIterator=diff.added.values().iterator();
        	modifiedIterator=diff.modified2.values().iterator();
        }
    }

    /**
     * @see org.geotools.data.FeatureReader#getFeatureType()
     */
    public T getFeatureType() {
        return reader.getFeatureType();
    }

    /**
     * @see org.geotools.data.FeatureReader#next()
     */
    public F next() throws IOException, IllegalAttributeException, NoSuchElementException {
        if (hasNext()) {
        	F live = next;
        	next = null;

            return live;
        }

        throw new NoSuchElementException("No more Feature exists");
    }

    /**
     * @see org.geotools.data.FeatureReader#hasNext()
     */
    public boolean hasNext() throws IOException {
        if (next != null) {
            // We found it already
            return true;
        }
        F peek;

        if( filter==Filter.EXCLUDE)
            return false;
        
        while( (reader != null) && reader.hasNext() ) {

            try {
                peek = reader.next();
            } catch (NoSuchElementException e) {
                throw new DataSourceException("Could not aquire the next Feature", e);
            } catch (IllegalAttributeException e) {
                throw new DataSourceException("Could not aquire the next Feature", e);
            }

            String fid = peek.getIdentifier().getID();
            encounteredFids.add(fid);

            if (diff.modified2.containsKey(fid)) {
                F changed = (F) diff.modified2.get(fid);
                if (changed == TransactionStateDiff.NULL || !filter.evaluate(changed) ) {
                    continue;
                } else {
                    next = changed;
                    return true;
                }
            } else {

                next = peek; // found feature
                return true;
            }
        }

        queryDiff();
        return next != null;
    }

    /**
     * @see org.geotools.data.FeatureReader#close()
     */
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
            reader = null;
        }

        if (diff != null) {
            diff = null;
            addedIterator=null;
        }
    }
    
    protected void queryDiff() {
        if( fidFilter ){
            queryFidFilter();
        } else {
        	queryAdded();
        	queryModified();
         }
    }
    
	protected void queryAdded() {
		while( addedIterator.hasNext() && next == null ){
			next = addedIterator.next();
			if( encounteredFids.contains(next.getIdentifier().getID()) || !filter.evaluate(next)){
				next = null;
			}
		}
	}
	
	protected void queryModified() {
		while( modifiedIterator.hasNext() && next == null ){
			next = modifiedIterator.next();
			if( next==TransactionStateDiff.NULL || encounteredFids.contains(next.getIdentifier().getID()) || !filter.evaluate(next) ){
				next = null;
			}
		}
	}
	
	protected void queryFidFilter() {
		Id fidFilter = (Id) filter;
		if (fids == null) {
		    fids = fidFilter.getIdentifiers().iterator();
		}
        while( fids.hasNext() && next == null ) {
		    String fid = fids.next().toString();
		    if( !encounteredFids.contains(fid) ){
    			next = (F) diff.modified2.get(fid);
    		    if( next==null ){
    		    	next = (F) diff.added.get(fid);
    		    }
		    }
		}
	}
}
