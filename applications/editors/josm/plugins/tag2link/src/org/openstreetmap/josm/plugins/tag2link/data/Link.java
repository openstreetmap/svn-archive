package org.openstreetmap.josm.plugins.tag2link.data;

public class Link {
    public String name;
    public String url;
    
    public Link(String name, String url) {
    	this.name = name;
    	this.url = url;
    }
    
    public Link(Link link) {
    	this(link.name, link.url);
    }
    
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
