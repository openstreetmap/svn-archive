package org.openstreetmap.client;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.openstreetmap.util.Line;
import org.openstreetmap.util.LineOnlyId;
import org.openstreetmap.util.Node;
import org.openstreetmap.util.OsmPrimitive;
import org.openstreetmap.util.Way;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import uk.co.wilson.xml.MinML2;

public class OxParser extends MinML2 {
	/**
	 * The current processed primitive
	 */
	private OsmPrimitive current = null;
	/**
	 * Maps id to already read nodes.
	 * Key: Long   Value: Node
	 */
	private Map nodes = new HashMap();
	/**
	 * Maps id to already read lines.
	 * Key: Long   Value: Line
	 */
	private Map lines = new HashMap();
	private Collection ways = new LinkedList();

	public OxParser(InputStream i) {
		System.out.println("OSM XML parser started...");
		try {
			parse(new InputStreamReader(new BufferedInputStream(i, 1024), "ISO-8859-1"));
		} catch (IOException e) {
			System.out.println("IOException: " + e);
			e.printStackTrace();
		} catch (SAXException e) {
			System.out.println("SAXException: " + e);
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Other Exception: " + e);
			e.printStackTrace();
		}
	}

	public void startDocument() {
		System.out.println("Start of Document");
	}

	public void endDocument() {
		System.out.println("End of Document");
	}

	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
		if (qName.equals("node")) {
			double node_lat = Double.parseDouble(atts.getValue("lat"));
			double node_lon = Double.parseDouble(atts.getValue("lon"));
			long id = Long.parseLong(atts.getValue("id"));
			current = new Node(node_lat, node_lon, id);
		}

		if (qName.equals("segment")) {
			long line_from_id = Long.parseLong(atts.getValue("from"));
			long line_to_id = Long.parseLong(atts.getValue("to"));
			long id = Long.parseLong(atts.getValue("id"));
			current = new Line((Node)nodes.get(new Long(line_from_id)), (Node)nodes.get(new Long(line_to_id)), id);
		}
		
		if (qName.equals("way")) {
			long id = Long.parseLong(atts.getValue("id"));
			current = new Way(id);
		}

		if (qName.equals("seg")) {
			long id = Long.parseLong(atts.getValue("id"));
			Line line = (Line)lines.get(new Long(id));
			if (line == null) {
				line = new LineOnlyId(id);
				lines.put(new Long(id), line);
			}
			((Way)current).lines.add(line);
		}

		if(qName.equals("tag")) {
			String key = atts.getValue("k");
			String val = atts.getValue("v");
			current.tags.put(key, val);
		}

	} // startElement

	public void endElement(String namespaceURI, String localName, String qName) {
		if (qName.equals("node")) {
			nodes.put(new Long(current.id), current);
			current.register();
			current = null;
		} else if (qName.equals("segment")) {
			lines.put(new Long(current.id), current);
			current.register();
			current = null;
		} else if (qName.equals("way")) {
			ways.add(current);
			current.register();
			current = null;
		}


	} // endElement

	public void fatalError(SAXParseException e) throws SAXException {
		System.out.println("Error: " + e);
		throw e;
	}

	public Collection getNodes() {
		return nodes.values();
	}

	public Collection getLines() {
		return lines.values();
	}
	
	public Collection getWays() {
		return ways;
	}
}
