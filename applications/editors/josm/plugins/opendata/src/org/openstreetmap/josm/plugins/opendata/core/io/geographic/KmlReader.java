//    JOSM opendata plugin.
//    Copyright (C) 2011-2012 Don-vip
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
package org.openstreetmap.josm.plugins.opendata.core.io.geographic;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.AbstractReader;
import org.openstreetmap.josm.io.UTFInputStreamReader;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.io.ProjectionPatterns;

public class KmlReader extends AbstractReader implements OdConstants {

    private XMLStreamReader parser;
    private Map<LatLon, Node> nodes = new HashMap<LatLon, Node>();
    
    public KmlReader(XMLStreamReader parser) {
        this.parser = parser;
    }

	public static DataSet parseDataSet(InputStream in, ProgressMonitor instance) throws IOException, XMLStreamException, FactoryConfigurationError {
        InputStreamReader ir = UTFInputStreamReader.create(in, UTF8);
        XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(ir);
        return new KmlReader(parser).parseDoc();
	}

	private DataSet parseDoc() throws XMLStreamException {
		DataSet ds = new DataSet();
		while (parser.hasNext()) {
            int event = parser.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                if (parser.getLocalName().equals(KML_PLACEMARK)) {
                	parsePlaceMark(ds);
                }
            }
		}
		return ds;
	}
	
	private static boolean keyIsIgnored(String key) {
		for (ProjectionPatterns pp : PROJECTIONS) {
			if (pp.getXPattern().matcher(key).matches() || pp.getYPattern().matcher(key).matches()) {
				return true;
			}
		}
		return false;
	}
	
	private void parsePlaceMark(DataSet ds) throws XMLStreamException {
		List<OsmPrimitive> list = new ArrayList<OsmPrimitive>();
		Way way = null;
		Node node = null;
		Relation relation = null;
		String role = "";
		Map<String, String> tags = new HashMap<String, String>();
		while (parser.hasNext()) {
            int event = parser.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
            	if (parser.getLocalName().equals(KML_COLOR)) {
            		String s = parser.getElementText();
            		// KML color format is aabbggrr, convert it to OSM (web) format: #rrggbb
            		String rgbColor = '#'+s.substring(6,8)+s.substring(4,6)+s.substring(2,4);
            		tags.put(KML_COLOR, rgbColor);
            	} else if (parser.getLocalName().equals(KML_NAME)) {
                	tags.put(KML_NAME, parser.getElementText());
            	} else if (parser.getLocalName().equals(KML_SIMPLE_DATA)) {
            		String key = parser.getAttributeValue(null, "name");
            		if (!keyIsIgnored(key)) {
            			tags.put(key, parser.getElementText());
            		}
            	} else if (parser.getLocalName().equals(KML_POLYGON)) {
            		ds.addPrimitive(relation = new Relation());
            		relation.put("type", "multipolygon");
            		list.add(relation);
            	} else if (parser.getLocalName().equals(KML_OUTER_BOUND)) {
            		role = "outer";
            	} else if (parser.getLocalName().equals(KML_INNER_BOUND)) {
            		role = "inner";
            	} else if (parser.getLocalName().equals(KML_LINEAR_RING)) {
            		if (relation != null) {
            			ds.addPrimitive(way = new Way());
            			relation.addMember(new RelationMember(role, way));
            		}
            	} else if (parser.getLocalName().equals(KML_LINE_STRING)) {
            		ds.addPrimitive(way = new Way());
            		list.add(way);
            	} else if (parser.getLocalName().equals(KML_COORDINATES)) {
            		String[] tab = parser.getElementText().split(" ");
            		for (int i = 0; i < tab.length; i ++) {
            			String[] values = tab[i].split(","); 
            			LatLon ll = new LatLon(Double.valueOf(values[1]), Double.valueOf(values[0])).getRoundedToOsmPrecisionStrict();
            			node = nodes.get(ll);
            			if (node == null) {
            				ds.addPrimitive(node = new Node(ll));
            				nodes.put(ll, node);
            				if (values.length > 2 && !values[2].equals("0")) {
            					node.put("ele", values[2]);
            				}
            			}
            			if (way != null) {
            				way.addNode(node);
            			}
            		}
            	}
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                if (parser.getLocalName().equals(KML_PLACEMARK)) {
                	break;
                } else if (parser.getLocalName().equals(KML_POINT)) {
                	list.add(node);
                }
            }
		}
		for (OsmPrimitive p : list) {
			for (String key : tags.keySet()) {
				p.put(key, tags.get(key));
			}
		}
	}
}
