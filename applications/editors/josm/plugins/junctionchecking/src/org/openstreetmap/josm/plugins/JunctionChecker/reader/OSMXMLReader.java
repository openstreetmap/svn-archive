package org.openstreetmap.josm.plugins.JunctionChecker.reader;

import java.io.File;
import java.util.HashMap;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.OSMEntity;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.OSMGraph;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.OSMNode;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.OSMRelation;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.OSMWay;

/**
 * @author  joerg
 */
public class OSMXMLReader extends XMLReader {

	private OSMGraph osmgraph = new OSMGraph();

	public OSMXMLReader(String filename) {
		super(filename);
	}

	public OSMXMLReader(File file) {
		super(file);
	}

	private void readAttributes(OSMEntity entity) {
		String temp = parser.getAttributeValue(null, "changeset");
		if (temp != null) {
			entity.setChangeset(Integer.parseInt(temp));
		}
		entity.setId(Long.parseLong(parser.getAttributeValue(null, "id")));
		entity.setTimestamp(parser.getAttributeValue(null, "timestamp"));

		temp = parser.getAttributeValue(null, "uid");
		if (temp != null)
			entity.setUid(Integer.parseInt(temp));

		temp = parser.getAttributeValue(null, "uid");
		if (temp != null) {
			entity.setUid(Integer.parseInt(temp));
		}
		entity.setUser(parser.getAttributeValue(null, "user"));

		temp = parser.getAttributeValue(null, "visible");
		if (temp != null) {
			if (temp.equals("true") || temp.equals("1")) {
				entity.setVisible(true);
			}
		} else {
			entity.setVisible(false);
		}
		temp = parser.getAttributeValue(null, "version");
		if (temp != null) {
			entity.setversion(Integer.parseInt(temp));
		}
	}

	private void readMember(OSMRelation relation) {
		if (parser.getAttributeValue(null, "type").equals("node")) {
			relation.addMember(osmgraph.getNode(Integer.parseInt(parser
					.getAttributeValue(null, "ref"))), parser
					.getAttributeValue(null, "role"));
		}
		else if (parser.getAttributeValue(null, "type").equals("way")) {
			relation.addMember(osmgraph.getWay(Long.parseLong(parser
					.getAttributeValue(null, "ref"))), parser
					.getAttributeValue(null, "role"));
		}
		else if (parser.getAttributeValue(null, "type").equals("relation")) {
			relation.addMember(osmgraph.getRelation(Integer.parseInt(parser
					.getAttributeValue(null, "ref"))), parser
					.getAttributeValue(null, "role"));
		}
	}

	@Override
	public void parseXML() {
		String xmlelement;
		OSMNode node = new OSMNode();
		OSMWay way = new OSMWay();
		OSMRelation relation = new OSMRelation();
		HashMap<String, String> hashmap = new HashMap<String, String>();
		try {
			while (parser.hasNext()) {
				switch (parser.getEventType()) {

				case XMLStreamConstants.START_ELEMENT:
					xmlelement = parser.getLocalName();

					if (xmlelement.equals("node")) {
						node = new OSMNode();
						hashmap = new HashMap<String, String>();
						readAttributes(node);
						node.setLatitude(Double.parseDouble(parser
								.getAttributeValue(null, "lat")));
						node.setLongitude(Double.parseDouble(parser
								.getAttributeValue(null, "lon")));
					}

					if (xmlelement.equals("way")) {
						way = new OSMWay();
						hashmap = new HashMap<String, String>();
						readAttributes(way);
					}

					if (xmlelement.equals("relation")) {
						relation = new OSMRelation();
						hashmap = new HashMap<String, String>();
						readAttributes(relation);
					}

					if (xmlelement.equals("member")) {
						readMember(relation);
					}

					if (xmlelement.equals("bounds")) {
						osmgraph.setBbbottom(Double.parseDouble(parser
								.getAttributeValue(null, "minlat")));
						osmgraph.setBbtop(Double.parseDouble(parser
								.getAttributeValue(null, "maxlat")));
						osmgraph.setBbleft(Double.parseDouble(parser
								.getAttributeValue(null, "minlon")));
						osmgraph.setBbright(Double.parseDouble(parser
								.getAttributeValue(null, "maxlon")));
					}

					if (xmlelement.equals("nd")) {
						way.addNode(osmgraph.getNode(Integer.parseInt(parser
								.getAttributeValue(0))));
					}

					if (xmlelement.equals("tag")) {
						hashmap.put(parser.getAttributeValue(null, "k"), parser
								.getAttributeValue(null, "v"));
					}

					if (xmlelement.equals("relation")) {
						relation = new OSMRelation();
						hashmap = new HashMap<String, String>();
						readAttributes(relation);
					}

					//TODO: kann wohl wech!
					/*
					if (xmlelement.equals("member")) {
						relation.addMember(parser.getAttributeValue(null,
								"type"), Integer.parseInt(parser
								.getAttributeValue(null, "ref")), parser
								.getAttributeValue(null, "role"));
					}
					 */
					break;

				case XMLStreamConstants.END_ELEMENT:
					if (parser.getLocalName() == "node") {
						node.setHashmap(hashmap);
						osmgraph.addNode(node);
					}

					if (parser.getLocalName() == "way") {
						way.setHashmap(hashmap);
						osmgraph.addWay(way);
					}

					if (parser.getLocalName().equals("relation")) {
						relation.setHashmap(hashmap);
						osmgraph.addRelation(relation);
					}
					break;
				}
				parser.next();
			}
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	public void setOSMGraph(OSMGraph osmgraph) {
		this.osmgraph = osmgraph;
	}

	public OSMGraph getOSMGraph() {
		return osmgraph;
	}
}
