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
                Matcher m = c.keyPattern.matcher(key);
                if (m.matches()) {
                    
                }
            }
        }
        return result;
    }
}
