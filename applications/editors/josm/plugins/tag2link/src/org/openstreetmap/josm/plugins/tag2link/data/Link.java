package org.openstreetmap.josm.plugins.tag2link.data;

public class Link {
    public String name;
    public String url;
    private boolean containsParams(String s) {
        return s.matches("[^{}]*{[^{}]*}[^{}]*");
    }
    public boolean nameContainsParams() {
        return containsParams(name);
    }
    public boolean urlContainsParams() {
        return containsParams(url);
    }
    public boolean containsParams() {
        return nameContainsParams() || urlContainsParams();
    }
}
