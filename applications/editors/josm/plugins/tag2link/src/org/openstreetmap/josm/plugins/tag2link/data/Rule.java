package org.openstreetmap.josm.plugins.tag2link.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import org.openstreetmap.josm.data.osm.IPrimitive;
import org.openstreetmap.josm.data.osm.Tag;

public class Rule {
    public final Collection<Condition> conditions = new ArrayList<Condition>();
    public final Collection<Link> links = new ArrayList<Link>();
    
    public static class MatchingTag {
        public String key;
        public String value;
        public final Map<String, String> params;
        private String prefix;
        public MatchingTag(String key, String value, String prefix) {
        	this.key = key;
        	this.value = value;
        	this.params = new HashMap<String, String>();
        	this.prefix = prefix;
        	addKeyValueParams();
        }
        public void addParams(Matcher m, String paramName) {
    		for (int i = 1; i<=m.groupCount(); i++) {
    			params.put(prefix+paramName+"."+i, m.group(i));
    		}
        }
        private void addKeyValueParams() {
        	params.put("k", key);
        	params.put("v", value);
        	if (!prefix.isEmpty()) {
            	params.put(prefix+"k", key);
            	params.put(prefix+"v", value);
        	}
        }
    }
    
    public static class EvalResult {
        public final Collection<MatchingTag> matchingTags = new ArrayList<MatchingTag>();
        public boolean matches() {
            return !matchingTags.isEmpty();
        }
    }
    
    public EvalResult evaluates(Map<String, String> tags) {
        EvalResult result = new EvalResult();
        for (Condition c : conditions) {
            for (String key : tags.keySet()) {
                Matcher keyMatcher = c.keyPattern.matcher(key);
                if (keyMatcher.matches()) {
                	String idPrefix = c.id == null ? "" : c.id+".";
            		MatchingTag tag = new MatchingTag(key, tags.get(key), idPrefix);
            		tag.addParams(keyMatcher, "k");
                	boolean matchingTag = true;
                	if (c.valPattern != null) {
                		Matcher valMatcher = c.valPattern.matcher(tag.value);
                		if (valMatcher.matches()) {
                			tag.addParams(valMatcher, "v");
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
    
    public EvalResult evaluates(IPrimitive p) {
    	return evaluates(p.getKeys());
    }

	public EvalResult evaluates(Tag tag) {
		Map<String, String> map = new HashMap<String, String>();
		map.put(tag.getKey(), tag.getValue());
		return evaluates(map);
	}
}
