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
import org.openstreetmap.util.Tag;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import uk.co.wilson.xml.MinML2;

public class OxParser extends MinML2 {
	private Collection nodes = new LinkedList();
	private Collection lines = new LinkedList();
  private Node currentNode = null;
  private Line currentSegment = null;
  private String tagsfor = "node";

	public OxParser(InputStream i) {
		System.out.println("OSM XML parser started...");
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
			long node_id = Long.parseLong(atts.getValue("id"));
			currentNode = new Node(node_lat, node_lon, node_id);
      tagsfor = "node";
		}

		if (qName.equals("segment")) {
			long line_id = Long.parseLong(atts.getValue("id"));
			long line_from_id = Long.parseLong(atts.getValue("from"));
			long line_to_id = Long.parseLong(atts.getValue("to"));
			currentSegment = new Line(getNode(line_from_id), getNode(line_to_id), line_id);
      tagsfor = "segment";
		}

    if(qName.equals("tag")) {
      String key = atts.getValue("k");
      String val = atts.getValue("v");
      if( tagsfor.equals("node") ) {
        currentNode.tags.put(key, new Tag(key,val));
      }
      if( tagsfor.equals("segment") ) {
        currentSegment.tags.put(key, new Tag(key,val));
      }
    }

  } // startElement

	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
    if (qName.equals("node")) {
      nodes.add(currentNode);
    }

    if (qName.equals("segment")) {
      lines.add(currentSegment);
    }

    
	} // endElement

	public void characters(char ch[], int start, int length) {
	}

	public void fatalError(SAXParseException e) throws SAXException {
		System.out.println("Error: " + e);
		throw e;
	}

	public Node getNode(long node_id) {
		for (Iterator it = nodes.iterator(); it.hasNext();) {
			Node n = (Node)(it.next());
			if (n.id == node_id)
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
