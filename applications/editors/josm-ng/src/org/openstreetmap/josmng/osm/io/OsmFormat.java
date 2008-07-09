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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.openstreetmap.josmng.osm.DataSet;
import org.openstreetmap.josmng.osm.Node;
import org.openstreetmap.josmng.osm.Relation;
import org.openstreetmap.josmng.osm.Way;
import org.openstreetmap.josmng.utils.Convertor;
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
        private Map<Long,Node> newNodes = new HashMap<Long, Node>();
        private Map<Long,Way> newWays = new HashMap<Long, Way>();
        private Map<Long,Relation> newRels = new HashMap<Long, Relation>();
        
    
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
                
                Node n = factory.node(id < 0 ? 0 : id, lat, lon, time, user, vis);
                if (id < 0) newNodes.put(id, n);

                updateFlags(getString(atts, "action"));
            } else if (qName.equals("way")) {
                // common attribs
                long id = getLong(atts, "id");
                int time = getDate(atts, "timestamp");
                String user = atts.getValue("user");
                boolean vis = getBoolean(atts, "visible", true);

                Way w = factory.way(id < 0 ? 0 : id, time, user, vis, null);
                if (id < 0) newWays.put(id, w);

                updateFlags(getString(atts, "action"));
            } else if (qName.equals("nd")) {
                long nid = getLong(atts, "ref");
                Node n = getNode(nid);
                //assert n != null;
                if (n != null) wayNodes.add(n);
            } else if (qName.equals("tag")) {
                factory.putTag(getString(atts, "k"), getString(atts, "v"));
            } else if (qName.equals("relation")) {
                // TODO: relation parsing 
            } else if (qName.equals("member")) {
                // TODO
            }
        }

        private void updateFlags(String action) {
            if ("delete".equals(action)) {
                factory.setFlags(false, true);
            } else if ("modify".equals(action)) {
                factory.setFlags(true, false);
            }
        }
        
        private Node getNode(long id) {
            if (id < 0) {
                return newNodes.get(id);
            } else {
                return factory.getNode(id);
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
            return (int)(getTimestamp(orig).getTime()/1000);
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

        // An instance reused throughout the lifetime of the parser.
        private GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        { calendar.setTimeInMillis(0);}

        private Date getTimestamp(String str) {
            // "2007-07-25T09:26:24{Z|{+|-}01:00}"
            if (checkLayout(str, "xxxx-xx-xxTxx:xx:xxZ") ||
                    checkLayout(str, "xxxx-xx-xxTxx:xx:xx+xx:00") ||
                    checkLayout(str, "xxxx-xx-xxTxx:xx:xx-xx:00")) {
                calendar.set(
                    parsePart(str, 0, 4),
                    parsePart(str, 5, 2)-1,
                    parsePart(str, 8, 2),
                    parsePart(str, 11, 2),
                    parsePart(str, 14,2),
                    parsePart(str, 17, 2));
                
                if (str.length() == 25) {
                    int plusHr = parsePart(str, 20, 2);
                    int mul = str.charAt(19) == '+' ? -3600000 : 3600000;
                    calendar.setTimeInMillis(calendar.getTimeInMillis()+plusHr*mul);
                }
                
                return calendar.getTime();
            }
            
            try {
                return XML_DATE.newXMLGregorianCalendar((String)str).toGregorianCalendar().getTime();
            } catch (Exception ex) {
                return new Date();
            }
        }
        
        private boolean checkLayout(String text, String pattern) {
            if (text.length() != pattern.length()) return false;
            for (int i=0; i<pattern.length(); i++) {
                if (pattern.charAt(i) == 'x') continue;
                if (pattern.charAt(i) != text.charAt(i)) return false;
            }
            return true;
        }

    }
    
    private static final DatatypeFactory XML_DATE;

    static {
        DatatypeFactory fact = null;
        try {
            fact = DatatypeFactory.newInstance();
        } catch(DatatypeConfigurationException ce) {
            ce.printStackTrace();
        }
        XML_DATE = fact;
    }


    private static int parsePart(String str, int off, int len) {
        return Integer.valueOf(str.substring(off, off+len));
    }
    
}