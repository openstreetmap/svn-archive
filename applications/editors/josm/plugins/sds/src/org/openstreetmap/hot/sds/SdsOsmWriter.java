// License: GPL. Copyright 2007 by Immanuel Scholz and others
package org.openstreetmap.hot.sds;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.openstreetmap.josm.data.osm.Changeset;
import org.openstreetmap.josm.data.osm.Tagged;
import org.openstreetmap.josm.io.OsmWriter;
import org.openstreetmap.josm.io.XmlWriter;

/**
 * This is a special version of JOSM's OsmWriter that makes
 * sure that special tags are never written to JOSM's standard
 * output channels.
 * 
 * In the context of HOT's separate data store, this is very
 * important as otherwise private/confidential information could
 * end up on public servers.
 * 
 * @author Frederik Ramm
 *
 */
public class SdsOsmWriter extends OsmWriter {

	private SeparateDataStorePlugin plugin;
	
    public SdsOsmWriter(SeparateDataStorePlugin plugin, PrintWriter out, boolean osmConform, String version) {
        super(out, osmConform, version);
        this.plugin = plugin;
    }

    @Override
    protected void addTags(Tagged osm, String tagname, boolean tagOpen) {
        if (osm.hasKeys()) {
            if (tagOpen) {
                out.println(">");
            }
            List<Entry<String, String>> entries = new ArrayList<Entry<String,String>>(osm.getKeys().entrySet());
            Collections.sort(entries, byKeyComparator);
            for (Entry<String, String> e : entries) {
            	String key = e.getKey();
                if (!(osm instanceof Changeset) && ("created_by".equals(key))) continue;
                if (key.startsWith(plugin.getIgnorePrefix())) continue;          
                out.println("    <tag k='"+ XmlWriter.encode(e.getKey()) +
                            "' v='"+XmlWriter.encode(e.getValue())+ "' />");
            }
            out.println("  </" + tagname + ">");
        } else if (tagOpen) {
            out.println(" />");
        } else {
            out.println("  </" + tagname + ">");
        }
    }
}
