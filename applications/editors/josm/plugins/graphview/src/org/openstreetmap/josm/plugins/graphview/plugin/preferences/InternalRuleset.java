package org.openstreetmap.josm.plugins.graphview.plugin.preferences;

public enum InternalRuleset {

    DEFAULT("resources/accessRuleset.xml"),
    GERMANY("resources/accessRuleset_de.xml");

    private String resourceName;
    private InternalRuleset(String resourceName) {
        this.resourceName = resourceName;
    }
    public String getResourceName() {
        return resourceName;
    }
}
