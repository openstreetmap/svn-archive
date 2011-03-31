package de.altenstein.osm;

import java.util.HashMap;

public abstract interface OsmObject {
	boolean agreed = false;
	// holds an integer giving information about license status of node
	int licenseStatus = 99;
	// holds attributes of node-tag
	HashMap<String,String> attMap = new HashMap<String,String>();
	// holds key-value-pairs of node-specific tags
	HashMap<String,String> tagMap = new HashMap<String,String>();
	
	public boolean hasTag(String key);
	
	public String getTagValue(String value);
	
	public HashMap<String,String> getTagMap();
}
