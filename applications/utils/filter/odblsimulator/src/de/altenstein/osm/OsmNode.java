package de.altenstein.osm;

import java.util.HashMap;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class OsmNode implements OsmObject {
	
	// true if user who created/edited this node agreed to license
	boolean agreed;
	// holds an integer giving information about license status of node
	int licenseStatus = 99;
	// holds attributes of node-tag
	HashMap<String,String> attMap = new HashMap<String,String>();
	// holds key-value-pairs of node-specific tags
	HashMap<String,String> tagMap = new HashMap<String,String>();
	
	public OsmNode(){
		licenseStatus = 999; // just to mark undefined nodes
	}
	
	/**
	 * Creates a new OsmNode by parsing a history planet file osm document.
	 * First all attributes will be read and saved in attMap.
	 * Then all tags of that node will be read and saved in tagMap.
	 * @param reader XMLStreamReader pointing to an OSM history planet file
	 * @param list Containing id's which agreed to license change.
	 * @throws XMLStreamException
	 */
	public OsmNode(XMLStreamReader reader, AgreeList list) throws XMLStreamException{
		if (reader.getAttributeCount() > 0){
			// System.out.println("--------------------\nnode: id=" + reader.getAttributeValue(null, "id") + ", version=" + reader.getAttributeValue(null, "version"));
			
			for (int i = 0; i < reader.getAttributeCount(); i++){
				attMap.put(reader.getAttributeLocalName(i), reader.getAttributeValue(i));
			}
		}
		if (!attMap.containsKey("uid")){
			attMap.put("uid", "0");
		}
		agreed = list.contains(Integer.parseInt(attMap.get("uid")));
		readTags(reader);
	}
	
	/**
	 * Reads all tags of the current node. Saves all tags into tagMap.
	 * @param reader XMLStreamReader pointing to an OSM PlanetHistoryFile
	 * @throws XMLStreamException
	 */
	private void readTags(XMLStreamReader reader) throws XMLStreamException{
		while (reader.hasNext()){
			int type = reader.next();
			switch (type){
			case XMLStreamConstants.START_ELEMENT:
				if (reader.getLocalName() == "tag"){
					// System.out.println("   <tag " + reader.getAttributeValue(0) + "=" + reader.getAttributeValue(1) + " />");
					tagMap.put(reader.getAttributeValue(0),reader.getAttributeValue(1));
				}
				break;
			case XMLStreamConstants.END_ELEMENT:
				if (reader.getLocalName() == "node"){
					// if true, all tags of this node have been read so readTags method will return
					return;
				}
			}
		}
	}
	
	/**
	 * Returns true if node contains the given tag-key.
	 * @param key
	 * @return
	 */
	public boolean hasTag(String key){
		return tagMap.containsKey(key);
	}
	
	/**
	 * Returns the value connected to the given key (tag map).
	 * @param key
	 * @return
	 */
	public String getTagValue(String key){
		return (String)tagMap.get(key);
	}
	
	/**
	 * Returns the value connected to the given key (attribute map).
	 * @param key
	 * @return
	 */
	public String getAttValue(String key){
		return (String)attMap.get(key);
	}
	
	/**
	 * Returns the object's tagMap.
	 */
	public HashMap<String, String> getTagMap() {
		return tagMap;
	}
	
	/* deprecated methods 
	// creates a new OsmNode from an XMLEvent
	public OsmNode(XMLEvent evt){
		id = Integer.parseInt(evt.asStartElement().getAttributeByName(new QName("id")).getValue());
		version = Integer.parseInt(evt.asStartElement().getAttributeByName(new QName("version")).getValue());
		changeset = Integer.parseInt(evt.asStartElement().getAttributeByName(new QName("changeset")).getValue());
		try{
			uid = Integer.parseInt(evt.asStartElement().getAttributeByName(new QName("uid")).getValue());
			lat = Double.parseDouble(evt.asStartElement().getAttributeByName(new QName("lat")).getValue());
			lon = Double.parseDouble(evt.asStartElement().getAttributeByName(new QName("lon")).getValue());
			user = evt.asStartElement().getAttributeByName(new QName("user")).getValue();
			visible = Boolean.parseBoolean(evt.asStartElement().getAttributeByName(new QName("visible")).getValue());
		} catch (Exception e){
			System.out.println("Node" + id + ", version" + version + ": required attributes missing (e.g. uid)");			
			e.printStackTrace();
		}
	}
	*/
	
}
