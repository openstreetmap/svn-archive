package org.openstreetmap.client;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.openstreetmap.util.Line;
import org.openstreetmap.util.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import uk.co.wilson.xml.MinML2;

public class GpxParser extends MinML2 {
	private Collection nodes = new LinkedList();
	private Collection lines = new LinkedList();

	public GpxParser(InputStream i) {
		System.out.println("GPX parser started...");
		try {
			parse(new InputStreamReader(new BufferedInputStream(i, 1024)));
		} catch (IOException e) {
			System.out.println("IOException: " + e);
			e.printStackTrace();
		} catch (SAXException e) {
			System.out.println("SAXException: " + e);
			e.printStackTrace();
		} catch (Throwable e) {
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

	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		if (qName.equals("node")) {
			double node_lat = Double.parseDouble(atts.getValue("lat"));
			double node_lon = Double.parseDouble(atts.getValue("lon"));
			long node_uid = Long.parseLong(atts.getValue("uid"));
			String node_tags = atts.getValue("tags");
			nodes.add(new Node(node_lat, node_lon, node_uid, node_tags));
		}

		if (qName.equals("segment")) {
			long line_uid = Long.parseLong(atts.getValue("uid"));
			long line_from_uid = Long.parseLong(atts.getValue("from"));
			long line_to_uid = Long.parseLong(atts.getValue("to"));
			String line_tags = atts.getValue("tags");
			lines.add(new Line(getNode(line_from_uid), getNode(line_to_uid), line_uid, line_tags));
		}
	}

	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
	}

	public void characters(char ch[], int start, int length) {
	}

	public void fatalError(SAXParseException e) throws SAXException {
		System.out.println("Error: " + e);
		throw e;
	}

	public Node getNode(long node_uid) {
		for (Iterator it = nodes.iterator(); it.hasNext();) {
			Node n = (Node)(it.next());
			if (n.id == node_uid)
				return n;
		}
		return null;
	}

	public Collection getNodes() {
		return nodes;
	}

	public Collection getLines() {
		return lines;
	}
}
