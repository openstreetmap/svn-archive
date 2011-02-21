import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class MapParserComparator extends DefaultHandler{
		
		private Entity current = null;
		private int nodeTot = 0;
		private int wayTot = 0;
		private int relTot = 0;
		
		public MapParserComparator(InputStream i) {
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
//			System.out.println("end  " + localName + " " + qName);
			if (qName.equals("node")) {
				Node n = (Node) current;
				nodeTot++;

				Node n2 = OsmMapCallValidator.nodes.get(n.id);
				
				
				if (n2 != null) {
					if ((n2.lat != n.lat) || (n2.lon != n.lon)) {
						System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!Error: Coordinate missmatch on node " + n.id + "(" + n.lat + "|" + n.lon + ") != (" + n2.lat + "|" + n2.lon + ")");
					}
					if (!n2.compare(n)) {
						System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!Error: Nodes are not equal!!!");
					}
					if (n2.validated) {
						System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!Error: Contains dupplicate nodes (id = " + n.id +")");
					}
					n2.validated = true;
				} else {
					System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!Error: Spurious Node! " + n.id);
				}
				
				
				current = null;
			} 
			if (qName.equals("way")) {
				wayTot++;
				Way w = (Way) current;
				
				Way w2 = OsmMapCallValidator.ways.get(w.id);
				
				if (w2 != null) {
					if (!w2.compare(w)) {
						System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!Error: Ways are not equal!!!");
					}
					
					if (w.noNodes() != w2.noNodes()) {
						System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!Error: Number of nodes for way are not equal! ");
					}
					for (int i = 0; i < w.noNodes(); i++) {
						if (w.getNode(i) != w2.getNode(i)) {
							System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!Error: Way nodes are not equal!");
						}
					}
					
					if (w2.validated) {
						System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!Error: Contains dupplicate ways (id = " + w.id +")");
					}
					
					w2.validated = true;
					
				} else {
					System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!Error: Spurious Way! " + w.id);
				}
				
				current = null;
			} 
			if (qName.equals("relation")) {
				relTot++;
				Relation r = (Relation) current;
				
				Relation r2 = OsmMapCallValidator.relations.get(r.id);
				
				if (r2 != null) {
					if (!r2.compare(r)) {
						System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!Error: Relations are not equal!!!");
					}
					
					if (r.noMembers() != r2.noMembers()) {
						System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!Error: Number of members for relation are not equal! ");
					}
					for (int i = 0; i < r.noMembers(); i++) {
						RelationMember m = r.getMember(i);
						RelationMember m2 = r2.getMember(i);
						if (m.ref != m2.ref) {
							System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!Error: Relation member ref mismatch");
						}
						if (!m.type.equals(m2.type)) {
							System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!Error: Relation member type mismatch");
						}
						
						if (!m.role.equals(m2.role)) {
							System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!Error: Relation member role mismatch");
						}
					}
					
					if (r2.validated) {
						System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!Error: Contains dupplicate relations (id = " + r.id +")");
					}
					
					r2.validated = true;
				} else {
					System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!Error: Spurious Relation! " + r.id);
				}
				
				current = null;
			}
		} // endElement

}
