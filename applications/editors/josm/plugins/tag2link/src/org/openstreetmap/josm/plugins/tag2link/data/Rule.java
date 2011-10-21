package org.openstreetmap.josm.plugins.tag2link.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import org.openstreetmap.josm.data.osm.OsmPrimitive;

public class Rule {
    public final Collection<Condition> conditions = new ArrayList<Condition>();
    public final Collection<Link> links = new ArrayList<Link>();
    
    public static class MatchingTag {
        public String key;
        public String value;
        public final Map<String, String> params = new HashMap<String, String>();
        public MatchingTag(String key, String value) {
        	this.key = key;
        	this.value = value;
        }
        public void addParams(Matcher m, String prefix) {
    		for (int i = 1; i<=m.groupCount(); i++) {
    			this.params.put(prefix+i, m.group(i));
    		}
        }
    }
    
    public static class EvalResult {
        public final Collection<MatchingTag> matchingTags = new ArrayList<MatchingTag>();
        public boolean matches() {
            return !matchingTags.isEmpty();
        }
    }
    
    public EvalResult evaluates(OsmPrimitive p) {
        EvalResult result = new EvalResult();
        Map<String, String> tags = p.getKeys();
        for (Condition c : conditions) {
            for (String key : tags.keySet()) {
                Matcher keyMatcher = c.keyPattern.matcher(key);
                if (keyMatcher.matches()) {
                	String idPrefix = c.id == null ? "" : c.id+".";
            		MatchingTag tag = new MatchingTag(key, tags.get(key));
            		tag.addParams(keyMatcher, idPrefix+"k.");
                	boolean matchingTag = true;
                	if (c.valPattern != null) {
                		Matcher valMatcher = c.valPattern.matcher(tag.value);
                		if (valMatcher.matches()) {
                			tag.addParams(valMatcher, idPrefix+"v.");
                		} else {
                			matchingTag = false;
                		}
                	}
                	if (matchingTag) {
                		result.matchingTags.add(tag);
                	}
                }
            }
        }
        return result;
    }
}
