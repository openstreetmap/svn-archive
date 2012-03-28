// License: GPL. For details, see LICENSE file.
package org.openstreetmap.hot.sds;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parser for answers from SDS. These anwers look like this:
 * 
 * <pre>
<?xml version="1.0" encoding="UTF-8"?>
<osm_sds>
  <osm_shadow osm_id="499770" osm_type="way">
    <tag k="hot:bbb:grant_received_date" v="a"/>
    <tag k="hot:bbb:grant_received" v="b"/>
    <tag k="hot:bbb:home_owner_name" v="lll"/>
  </osm_shadow>
</osm_sds>
 * </pre>
 * @author Frederik Ramm
 */
public class SdsParser extends DefaultHandler
{
    private DataSet dataSet;
    private OsmPrimitive currentPrimitive;
    private SeparateDataStorePlugin plugin;
    private boolean ensureMatch;
    
    public SdsParser(DataSet ds, SeparateDataStorePlugin p, boolean ensureMatch) {
        this.dataSet = ds;
        plugin = p;
        this.ensureMatch = ensureMatch;
    }
    
    public SdsParser(DataSet ds, SeparateDataStorePlugin p) {
        this(ds, p, true);
    }
    
    @Override public void endElement(String namespaceURI, String localName, String qName)
    {
        // after successfully reading a full set of tags from the separate data store,
        // update it in our cache so we can determine changes later.
        if ("osm_shadow".equals(qName) && currentPrimitive != null) {
            plugin.learn(currentPrimitive.save());
        }
    }
    @Override public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException
    {
        if ("osm_shadow".equals(qName))
        {
            String type = atts.getValue("osm_type");
            String id = atts.getValue("osm_id");     	
            currentPrimitive = dataSet.getPrimitiveById(Long.parseLong(id), OsmPrimitiveType.fromApiTypeName(type));
            if (currentPrimitive == null && ensureMatch) {
                throw new SAXException("unexpected object in response");
            }
        }
        else if ("tag".equals(qName))
        {
            String v = atts.getValue("v");
            String k = atts.getValue("k");
            if (currentPrimitive != null) currentPrimitive.put(k, v);
        }
    }
}
