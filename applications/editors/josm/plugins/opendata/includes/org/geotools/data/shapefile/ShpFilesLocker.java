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
package org.geotools.data.shapefile;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

class ShpFilesLocker {
    
    final URI uri;
    final URL url;
    final FileReader reader;
    final FileWriter writer;
    boolean upgraded;

    public ShpFilesLocker( URL url, FileReader reader ) {
        this.url = url;
        this.reader = reader;
        this.writer = null;
        
    	// extracts the URI from the URL, if possible
        this.uri = getURI(url);
    }

    URI getURI(URL url) {
        try {
    		return url.toURI();
    	} catch (URISyntaxException e) {
    		// may fail if URL does not conform to RFC 2396
 		}
        return null;
    }

    public ShpFilesLocker( URL url, FileWriter writer ) {
        this.url = url;
        this.reader = null;
        this.writer = writer;
        
    	// extracts the URI from the URL, if possible
        this.uri = getURI(url);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((reader == null) ? 0 : reader.hashCode());
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        result = prime * result + ((writer == null) ? 0 : writer.hashCode());
        return result;
    }

    @Override
    public boolean equals( Object obj ) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ShpFilesLocker other = (ShpFilesLocker) obj;
        if (reader == null) {
            if (other.reader != null)
                return false;
        } else if (!reader.equals(other.reader))
            return false;
        if (url == null) {
            if (other.url != null)
                return false;
        } else {
        	// calls URI.equals if a valid URI is available  
        	if (uri != null) {
        		if (!uri.equals(other.uri))
            		return false;
        	}
        	// if URI is not available, calls URL.equals (which may take a long time)  
        	else if (!url.equals(other.url))
                return false;
        }
        if (writer == null) {
            if (other.writer != null)
                return false;
        } else if (!writer.equals(other.writer))
            return false;
        return true;
    }
}
