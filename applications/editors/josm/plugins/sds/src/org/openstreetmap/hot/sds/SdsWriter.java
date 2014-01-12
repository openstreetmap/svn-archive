// License: GPL. Copyright 2007 by Immanuel Scholz and others
package org.openstreetmap.hot.sds;

import java.io.PrintWriter;
import java.util.Map;
import java.util.Map.Entry;

import org.openstreetmap.josm.data.osm.IPrimitive;
import org.openstreetmap.josm.io.XmlWriter;

/**
 * Save the dataset into a stream as osm intern xml format. This is not using any
 * xml library for storing.
 * @author imi
 */
public class SdsWriter extends XmlWriter {

    protected SdsWriter(PrintWriter out) {
        super(out);
    }

     public void header() {
        out.println("<?xml version='1.0' encoding='UTF-8'?>");
        out.print("<osm_sds>");
    }
    public void footer() {
        out.println("</osm_sds>");
    }

    public void write(IPrimitive what, Map<String,String> tags) {
    	out.print("<osm_shadow osm_type=\"");
    	out.print(what.getType().getAPIName());
    	out.print("\" osm_id=\"");
    	out.print(what.getId());
    	out.println("\">");
        
    	if (tags != null) {
    		for(Entry<String,String> e : tags.entrySet()) {
    			out.println("    <tag k='"+ XmlWriter.encode(e.getKey()) +
    					"' v='"+XmlWriter.encode(e.getValue())+ "' />");
    		}
    	}
    	
    	out.println("</osm_shadow>");
    }

    public void close() {
        out.close();
    }

    @Override
    public void flush() {
        out.flush();
    }
}
