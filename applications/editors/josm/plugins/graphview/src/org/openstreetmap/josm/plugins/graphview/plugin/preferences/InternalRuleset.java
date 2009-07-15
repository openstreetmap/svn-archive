package org.openstreetmap.josm.plugins.graphview.plugin.preferences;

public enum InternalRuleset {

	DEFAULT("files/accessRuleset.xml"),
	GERMANY("files/accessRuleset_de.xml");

	private String resourceName;
	private InternalRuleset(String resourceName) {
		this.resourceName = resourceName;
	}
	public String getResourceName() {
		return resourceName;
	}
}
