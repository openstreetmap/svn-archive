/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi;

import java.io.IOException;

import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * This holds the common functionality for all POI
 *  Document classes.
 * Currently, this relates to Document Information Properties 
 * 
 * @author Nick Burch
 */
public abstract class POIDocument {
	/** Holds metadata on our document */
	private SummaryInformation sInf;
	/** Holds further metadata on our document */
	private DocumentSummaryInformation dsInf;
	/**	The directory that our document lives in */
	protected DirectoryNode directory;
	
	/** For our own logging use */
	private final static POILogger logger = POILogFactory.getLogger(POIDocument.class);

    /* Have the property streams been read yet? (Only done on-demand) */
    private boolean initialized = false;
    

    protected POIDocument(DirectoryNode dir) {
    	this.directory = dir;
    }


	/**
	 * Fetch the Document Summary Information of the document
	 */
	public DocumentSummaryInformation getDocumentSummaryInformation() { // NO_UCD
        if(!initialized) readProperties();
        return dsInf;
    }

	/** 
	 * Fetch the Summary Information of the document
	 */
	public SummaryInformation getSummaryInformation() { // NO_UCD
        if(!initialized) readProperties();
        return sInf;
    }
	

	/**
	 * Find, and create objects for, the standard
	 *  Documment Information Properties (HPSF).
	 * If a given property set is missing or corrupt,
	 *  it will remain null;
	 */
	protected void readProperties() {
		PropertySet ps;
		
		// DocumentSummaryInformation
		ps = getPropertySet(DocumentSummaryInformation.DEFAULT_STREAM_NAME);
		if(ps != null && ps instanceof DocumentSummaryInformation) {
			dsInf = (DocumentSummaryInformation)ps;
		} else if(ps != null) {
			logger.log(POILogger.WARN, "DocumentSummaryInformation property set came back with wrong class - ", ps.getClass());
		}

		// SummaryInformation
		ps = getPropertySet(SummaryInformation.DEFAULT_STREAM_NAME);
		if(ps instanceof SummaryInformation) {
			sInf = (SummaryInformation)ps;
		} else if(ps != null) {
			logger.log(POILogger.WARN, "SummaryInformation property set came back with wrong class - ", ps.getClass());
		}

		// Mark the fact that we've now loaded up the properties
        initialized = true;
	}

	/** 
	 * For a given named property entry, either return it or null if
	 *  if it wasn't found
	 */
	protected PropertySet getPropertySet(String setName) {
        //directory can be null when creating new documents
        if(directory == null) return null;
        
        DocumentInputStream dis;
		try {
			// Find the entry, and get an input stream for it
			dis = directory.createDocumentInputStream(setName);
		} catch(IOException ie) {
			// Oh well, doesn't exist
			logger.log(POILogger.WARN, "Error getting property set with name " + setName + "\n" + ie);
			return null;
		}

		try {
			// Create the Property Set
			PropertySet set = PropertySetFactory.create(dis);
			return set;
		} catch(IOException ie) {
			// Must be corrupt or something like that
			logger.log(POILogger.WARN, "Error creating property set with name " + setName + "\n" + ie);
		} catch(org.apache.poi.hpsf.HPSFException he) {
			// Oh well, doesn't exist
			logger.log(POILogger.WARN, "Error creating property set with name " + setName + "\n" + he);
		}
		return null;
	}
}
