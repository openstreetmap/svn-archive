package org.openstreetmap.josm.plugins.tageditor.preset;

import java.io.IOException;

import org.openstreetmap.josm.plugins.tageditor.util.IndentWriter;


public class Tag {
	private String key;
	private String value;
	private String displayName;
	private boolean optional = false;
	
	public Tag() {		
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public boolean isOptional() {
		return optional;
	}

	public void setOptional(boolean optional) {
		this.optional = optional;
	}
	
	
	public void dump(IndentWriter writer) throws IOException {
		writer.indent();
		writer.write("<tag ");
		writer.write(String.format("key=\"%s\" ", key));
		writer.write(String.format("optional=\"%s\" ", Boolean.toString(optional)));
		if (value != null) {
			writer.write(String.format("value=\"%s\" ", value));
		}
		writer.write(String.format("displayName=\"%s\" ", displayName));
		writer.write("/>");
		writer.write("\n");
	}
	 
	
	
	

}
