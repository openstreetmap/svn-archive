package de.altenstein.osm;

import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class OsmWay implements OsmObject {
	
	// true if user who created/edited this way agreed to license change
	boolean agreed;
	// holds an integer giving information about license status of way
	int licenseStatus = 99;
	// holds an integer giving information about the licence status of that way's referenced nodes
	int nodeLicenseStatus = 99;
	// holds IDs of all nodes used within this way
	ArrayList<Integer> nodeList = new ArrayList<Integer>();
	// holds attributes of way-tag
	HashMap<String,String> attMap = new HashMap<String,String>();
	// holds key-value-pairs of way-specific tags
	HashMap<String,String> tagMap = new HashMap<String,String>();
	
	/**
	 * Reads one way from the given osm planet file
	 * @param reader XMLStreamReader object pointing on the input osm planet file
	 * @param list AgreeList object should contain all IDs which agreed to license change
	 * @throws XMLStreamException
	 */
	public OsmWay(XMLStreamReader reader, AgreeList list) throws XMLStreamException{
		if (reader.getAttributeCount() > 0){
			// System.out.println("--------------------\nway: id=" + reader.getAttributeValue(null, "id") + ", version=" + reader.getAttributeValue(null, "version"));
			
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
	 * Reads all tags of the given way element.
	 * This includes nd-references which are saved in nodeList AND tags which are saved in tagMap.
	 * @param reader XMLStreamReader object pointing on the input osm planet file
	 * @throws XMLStreamException
	 */
	private void readTags(XMLStreamReader reader) throws XMLStreamException{
		while (reader.hasNext()){
			int type = reader.next();
			switch (type){
			case XMLStreamConstants.START_ELEMENT:
				if (reader.getLocalName() == "nd"){
					// System.out.println("   node " + reader.getAttributeValue(0));
					nodeList.add(Integer.parseInt(reader.getAttributeValue(0)));
				} else if (reader.getLocalName() == "tag"){
					// System.out.println("   <tag " + reader.getAttributeValue(0) + "=" + reader.getAttributeValue(1) + " />");
					tagMap.put(reader.getAttributeValue(0),reader.getAttributeValue(1));
				}
				break;
			case XMLStreamConstants.END_ELEMENT:
				if (reader.getLocalName() == "way"){
					// System.out.println("   way is based on " + nodeList.size() + " nodes");
					return;
				}
			}
		}
	}
	
	/**
	 * Returns true if way contains the given tag-key.
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
		return tagMap.get(key);
	}
	
	/**
	 * Returns the value connected to the given key (attribute map).
	 * @param key
	 * @return
	 */
	public String getAttValue(String key){
		return attMap.get(key);
	}
	
	/**
	 * Returns the object's tagMap.
	 */
	public HashMap<String, String> getTagMap() {
		return tagMap;
	}
	
}
