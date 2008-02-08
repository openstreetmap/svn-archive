/*
 *  JOSMng - a Java Open Street Map editor, the next generation.
 * 
 *  Copyright (C) 2008 Petr Nejedly <P.Nejedly@sh.cvut.cz>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */

package org.openstreetmap.josmng.osm.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.openstreetmap.josmng.osm.*;
import org.openstreetmap.josmng.utils.Storage;

/**
 * Parser for the OSM data.
 * INCOMPLETE!
 * TODO:
 *  - parse relations
 *  - parse bbox
 *  - postprocess negative IDs
 * 
 * @author nenik
 */
public class OsmReader extends DefaultHandler {
    private OsmPrimitive current;
    private DataSet constructed = new DataSet();
    private List<Node> wayNodes = new ArrayList<Node>();
    
    private Storage<String> strings = new Storage<String>();
    
    private OsmReader() {
    }
        
    public static DataSet parse(InputStream stream) throws IOException {
        Throwable cause;
        try {
            OsmReader osm = new OsmReader();
            InputSource src = new InputSource(stream);
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            
            parser.parse(src, osm);
            return osm.constructed;
        } catch (ParserConfigurationException ex) {
            cause = ex;
        } catch (SAXException ex) {
            cause = ex;
        }
        IOException ioe = new IOException("Can't read the source");
        ioe.initCause(cause);
        throw ioe;
    }
    
    public @Override void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        if (qName.equals("osm")) {
                if (atts == null || !"0.5".equals(atts.getValue("version")))
                    throw new SAXException("Unknown version");
	} else if (qName.equals("bound")) {
        } else if (qName.equals("node")) {
            //  <node id='704062' timestamp='2007-07-25T09:26:24+01:00' user='Kubajz' visible='true' lat='50.0461188' lon='14.4748857'>
            //    <tag k='created_by' v='JOSM' />
            //  </node>

            // common attribs
            long id = getLong(atts, "id");
            String time = getString(atts, "timestamp");
            String user = atts.getValue("user");
            boolean vis = getBoolean(atts, "visible");
            
            double lat = getDouble(atts, "lat");
            double lon = getDouble(atts, "lon");
            
            Node n = new Node(constructed, id, lat, lon, time, user, vis);
            constructed.addNode(n);
            current = n;
        } else if (qName.equals("way")) {
            // common attribs
            long id = getLong(atts, "id");
            String time = getString(atts, "timestamp");
            String user = atts.getValue("user");
            boolean vis = getBoolean(atts, "visible");

            Way w = new Way(constructed, id, time, user, vis);
            constructed.addWay(w);
            current = w;
        } else if (qName.equals("nd")) {
            assert current instanceof Way;
            long nid = getLong(atts, "ref");
            Node n = constructed.getNode(nid);
            //assert n != null;
            if (n != null) wayNodes.add(n);
        } else if (qName.equals("tag")) {
//            assert current != null;
            if (current != null) current.putTag(getString(atts, "k"), getString(atts, "v"));
        } else if (qName.equals("relation")) {
            // TODO: relation parsing 
        } else if (qName.equals("member")) {
            // TODO
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("tag")) return;
        if (qName.equals("way")) {
            assert current instanceof Way;
            ((Way)current).setNodes(wayNodes);
            wayNodes.clear();
        }
        if (qName.equals("node") || qName.equals("way") || qName.equals("relation")) {
            current = null;
        }
    }
    
    private String getString(Attributes atts, String name) {
        String orig = atts.getValue(name);
        if (orig == null) return null;
        return strings.putUnique(orig);
    }

    private double getDouble(Attributes atts, String name) {
        return Double.parseDouble(atts.getValue(name));
    }
    private long getLong(Attributes atts, String name) {
        return Long.parseLong(atts.getValue(name));
    }
    
    private boolean getBoolean(Attributes atts, String name) {
        return Boolean.parseBoolean(atts.getValue(name));
    }
}
