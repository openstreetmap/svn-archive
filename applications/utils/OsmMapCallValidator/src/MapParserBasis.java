import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class MapParserBasis extends DefaultHandler{
	
	private Entity current = null;
	private int nodeTot = 0;
	private int wayTot = 0;
	private int relTot = 0;
	
	public MapParserBasis(InputStream i) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			// Parse the input
			factory.setValidating(false);
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(i, this);
			System.out.println("OSM XML parser finished (Nodes: " + nodeTot + ", Ways: " + wayTot + ", Relations: " + relTot + ")");
		} catch (IOException e) {
			System.out.println("IOException: " + e);
			e.printStackTrace();
			System.exit(10);
		} catch (SAXException e) {
			System.out.println("SAXException: " + e);
			e.printStackTrace();
			System.exit(10);
		} catch (NumberFormatException nfe) {
			System.out.println("Number Format Excpetion: " + nfe.getMessage());
			nfe.printStackTrace();
		} catch (Exception e) {
			System.out.println("Other Exception: " + e);
			e.printStackTrace();
			System.exit(10);
		} 
	}
	
	
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {		
		if (qName.equals("node")) {
			float node_lat = Float.parseFloat(atts.getValue("lat"));
			float node_lon = Float.parseFloat(atts.getValue("lon"));
			long id = Long.parseLong(atts.getValue("id"));
			int version = Integer.parseInt(atts.getValue("version"));
			int changeset = Integer.parseInt(atts.getValue("changeset"));
			String timestamp = atts.getValue("timestamp");
			int uid = -1;
			try {
				uid = Integer.parseInt(atts.getValue("uid"));
			} catch (NumberFormatException nfe) {
				//Anonymous user
			}
			String user = atts.getValue("user");
			
			current = new Node(id,node_lat, node_lon,version,changeset,uid,user,timestamp);
			
			
		}
		if (qName.equals("way")) {
			long id = Long.parseLong(atts.getValue("id"));
			int version = Integer.parseInt(atts.getValue("version"));
			int changeset = Integer.parseInt(atts.getValue("changeset"));
			String timestamp = atts.getValue("timestamp");
			int uid = -1;
			try {
				uid = Integer.parseInt(atts.getValue("uid"));
			} catch (NumberFormatException nfe) {
				//Anonymous user
			}
			String user = atts.getValue("user");
			current = new Way(id,version,changeset,uid,user,timestamp);
		}
		if (qName.equals("nd")) {
			if (current instanceof Way) {
				Way way = ((Way)current);
				long ref = Long.parseLong(atts.getValue("ref"));
				Node node = OsmMapCallValidator.nodes.get(new Long(ref));
				if (node != null) {
					way.add(node);
				} else {
					System.out.println("Error! Way " + way.id + " is referencing Node " + ref + " but missing in output!");
				}
			}
		}
		if (qName.equals("tag")) {
			if (current != null) {
				String key = atts.getValue("k");
				String val = atts.getValue("v");
				current.setTag(key, val);
			} else {
				System.out.println("Tag at unexpected position " + current);
			}
		}
		if (qName.equals("relation")) {
			long id = Long.parseLong(atts.getValue("id"));
			int version = Integer.parseInt(atts.getValue("version"));
			int changeset = Integer.parseInt(atts.getValue("changeset"));
			String timestamp = atts.getValue("timestamp");
			int uid = -1;
			try {
				uid = Integer.parseInt(atts.getValue("uid"));
			} catch (NumberFormatException nfe) {
				//Anonymous user
			}
			String user = atts.getValue("user");
			current=new Relation(id,version,changeset,uid,user,timestamp);
		}
		if (qName.equals("member")) {
			if (current instanceof Relation) {
				Relation r = (Relation)current;
				long ref = Long.parseLong(atts.getValue("ref"));
				String type = atts.getValue("type");
				String role = atts.getValue("role");
				r.addMember(new RelationMember(ref, type, role));
			} else {
				System.out.println("Internal parsing error");
			}
		}

	} // startElement

	public void endElement(String namespaceURI, String localName, String qName) {		
//		System.out.println("end  " + localName + " " + qName);
		if (qName.equals("node")) {
			Node n = (Node) current;
			nodeTot++;

			if (OsmMapCallValidator.nodes.get(n.id) != null) {
				System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!Error: Contains dupplicate nodes (id = " + n.id +")");
			}
			OsmMapCallValidator.nodes.put(n.id, n);
			current = null;
		} 
		if (qName.equals("way")) {
			wayTot++;
			Way w = (Way) current;
			if (OsmMapCallValidator.ways.get(w.id) != null) {
				System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!Error: Contains dupplicate ways (id = " + w.id +")");
			}
			OsmMapCallValidator.ways.put(w.id, w);

			current = null;
		} 
		if (qName.equals("relation")) {
			relTot++;
			Relation r = (Relation) current;
			if (OsmMapCallValidator.ways.get(r.id) != null) {
				System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!Error: Contains dupplicate relations (id = " + r.id +")");
			}
			OsmMapCallValidator.relations.put(r.id, r);
			
			current = null;
		}
	} // endElement


}
