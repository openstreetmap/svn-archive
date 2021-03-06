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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.openstreetmap.josmng.osm.DataSet;
import org.openstreetmap.josmng.osm.Node;
import org.openstreetmap.josmng.osm.OsmPrimitive;
import org.openstreetmap.josmng.utils.Convertor;
import org.openstreetmap.josmng.utils.DateUtils;
import org.openstreetmap.josmng.utils.Storage;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class OsmFormat extends Convertor<NamedStream,DataSet> {

    public OsmFormat() {
        super(NamedStream.class, DataSet.class);
    }

    public @Override boolean accept(NamedStream source) {
        return source.getName().endsWith(".osm");
    }

    public @Override DataSet convert(NamedStream from) {
        try {
            return read(from.openStream());
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static void write(OutputStream os, DataSet ds) throws IOException {
    }

    public static DataSet read(InputStream is) throws IOException {
        Throwable cause;
        try {
            OsmStreamReader osr = new OsmStreamReader();
            InputSource src = new InputSource(new BufferedInputStream(is));
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            
            parser.parse(src, osr);
            return osr.factory.create();
        } catch (ParserConfigurationException ex) {
            cause = ex;
        } catch (SAXException ex) {
            cause = ex;
        }
        IOException ioe = new IOException("Can't read the source");
        ioe.initCause(cause);
        throw ioe;
        
    }
        
    private static class OsmStreamReader extends DefaultHandler {
        private DataSet.Factory factory = DataSet.factory(1000);
        private List<Node> wayNodes = new ArrayList<Node>();
    
        private Storage<String> strings = new Storage<String>();
        
    
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
                int time = getDate(atts, "timestamp");
                String user = atts.getValue("user");
                boolean vis = getBoolean(atts, "visible", true);

                double lat = getDouble(atts, "lat");
                double lon = getDouble(atts, "lon");

                factory.node(id, lat, lon, time, user, vis);
                updateFlags(getString(atts, "action"));
            } else if (qName.equals("way")) {
                // common attribs
                long id = getLong(atts, "id");
                int time = getDate(atts, "timestamp");
                String user = atts.getValue("user");
                boolean vis = getBoolean(atts, "visible", true);

                factory.way(id, time, user, vis, null);
                updateFlags(getString(atts, "action"));
            } else if (qName.equals("nd")) {
                long nid = getLong(atts, "ref");
                Node n = factory.getNode(nid);
                //assert n != null;
                if (n != null) wayNodes.add(n);
            } else if (qName.equals("tag")) {
                factory.putTag(getString(atts, "k"), getString(atts, "v"));
            } else if (qName.equals("relation")) {
                //   <relation id="560" timestamp="2008-03-10T17:41:30Z" user="robx">
                //    <member type="way" ref="4439126" role="inner"/>
                //    <member type="way" ref="8145371" role="outer"/>
                //    <tag k="type" v="multipolygon"/>
                //  </relation>
                // common attribs
                long id = getLong(atts, "id");
                int time = getDate(atts, "timestamp");
                String user = atts.getValue("user");
                boolean vis = getBoolean(atts, "visible", true);

                factory.relation(id, time, user, vis, null);
                updateFlags(getString(atts, "action"));
            } else if (qName.equals("member")) {
                String type = atts.getValue("type");
                long id = getLong(atts, "ref");
                String role = atts.getValue("role");
                OsmPrimitive member = null;
                if ("node".equals(type)) {
                    member = factory.getNode(id);
                } else if ("way".equals(type)) {
                    member = factory.getWay(id);
                } else if ("relation".equals(type)) {
                    member = factory.getRelation(id);
                }
                // XXX - create incomplete instance
                if (member != null) factory.addMember(member, role);
            }
        }

        private void updateFlags(String action) {
            if ("delete".equals(action)) {
                factory.setFlags(false, true);
            } else if ("modify".equals(action)) {
                factory.setFlags(true, false);
            }
        }
                
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equals("tag")) return;
            if (qName.equals("way")) {
                factory.setNodes(wayNodes);
                wayNodes.clear();
            }
        }
    
        private int getDate(Attributes atts, String name) {
            String orig = atts.getValue(name);
            if (orig == null) return -1;
            return (int)(DateUtils.fromString(orig).getTime()/1000);
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

        private boolean getBoolean(Attributes atts, String name, boolean def) {
            String val = atts.getValue(name);
            return val == null ? def : Boolean.parseBoolean(val);
        }
    }    
}
