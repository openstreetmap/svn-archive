package org.openstreetmap.josm.plugins.JunctionChecker.filter;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * @author  joerg
 */
public class Filter {
	
	private HashSet<String> tagValues = new HashSet<String>();
	private String keyValue;
	
	public Filter(String keyname, ArrayList<String> values) {
		this.keyValue = keyname;
		tagValues.addAll(values);
	}
	
	public boolean hasTagValue(String value) {
		return tagValues.contains(value);
	}
	
	public Filter() {	
	}
	
	public String[] getTagValues() {
		return (String[])tagValues.toArray();
	}

	public void setTagValues(HashSet<String> tagValues) {
		this.tagValues = tagValues;
	}
	
	public void setTagValue(String value) {
		tagValues.add(value);
	}

	public String getKeyValue() {
		return keyValue;
	}

	public void setKeyValue(String keyValue) {
		this.keyValue = keyValue;
	}
}
