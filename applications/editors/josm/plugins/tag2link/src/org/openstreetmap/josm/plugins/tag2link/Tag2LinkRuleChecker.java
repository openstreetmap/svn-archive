//    JOSM tag2link plugin.
//    Copyright (C) 2011-2012 Don-vip & FrViPofm
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
package org.openstreetmap.josm.plugins.tag2link;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.IPrimitive;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.plugins.tag2link.data.Link;
import org.openstreetmap.josm.plugins.tag2link.data.LinkPost;
import org.openstreetmap.josm.plugins.tag2link.data.Rule;
import org.openstreetmap.josm.plugins.tag2link.data.Rule.EvalResult;
import org.openstreetmap.josm.plugins.tag2link.data.Rule.MatchingTag;
import org.openstreetmap.josm.plugins.tag2link.data.Source;
import org.openstreetmap.josm.plugins.tag2link.io.SourcesReader;

/**
 * Class matching rules against a specified OSM primitive in order to get its relevant links. 
 * @author Don-vip
 *
 */
public class Tag2LinkRuleChecker implements Tag2LinkConstants {

    private static Collection<Source> sources = new ArrayList<Source>();
    
    private static boolean initialized = false;
    
    /**
     * Initializes the matching rules mechanism.
     */
    public static void init() {
        if (!initialized) {
            sources = SourcesReader.readSources();
            initialized = true;
        }
    }
    
    private static String findValue(String arg, Collection<MatchingTag> matchingTags) {
        for (MatchingTag tag : matchingTags) {
            if (tag.params.containsKey(arg)) {
                return tag.params.get(arg);
            }
        }
        return null;
    }
    
    private static String replaceParams(String s, EvalResult eval) {
        String result = s;
        Matcher m = Pattern.compile("%([^%]*)%").matcher(s);
        while (m.find()) {
            String arg = m.group(1);
            
            // Search for a standard value
            String val = findValue(arg, eval.matchingTags);
            
            // No standard value found: test lang() function
            if (val == null) {
                Matcher lm = Pattern.compile(".*lang(?:\\((\\p{Lower}{2,})(?:,(\\p{Lower}{2,}))*\\))?.*").matcher(arg);
                if (lm.matches()) {
                    String josmLang = Main.pref.get("language");
                    String jvmLang = (josmLang.isEmpty() ? Locale.getDefault().getLanguage() : josmLang).split("_")[0];
                    if (lm.groupCount() == 0) {
                        val = jvmLang;
                    } else {
                        for (int i = 1; i<=lm.groupCount() && val == null; i++) {
                            if (jvmLang.equals(lm.group(i))) {
                                val = jvmLang;
                            }
                        }
                    }
                }
            }
            
            // Find a default value if set after ":"
            if (val == null && arg.contains(":")) {
                String[] vars = arg.split(":");
                for (int i = 0; val == null && i < vars.length-1; i++) {
                    val = findValue(vars[i], eval.matchingTags);
                }
                if (val == null) {
                    // Default value
                    val = vars[vars.length-1];
                }
            }
            
            // Has a value been found ?
            if (val != null) {
                try {
                    // Special hack for Wikipedia that prevents spaces being replaced by "+" characters, but by "_"
                    if (s.contains("wikipedia.")) {
                        val = val.replaceAll(" ", "_");
                    }
                    // Encode param to be included in the URL, except if it is the URL itself !
                    if (!m.group().equals(s)) {
                        val = URLEncoder.encode(val, UTF8_ENCODING);
                    }
                    // Finally replace parameter
                    result = result.replaceFirst(Pattern.quote(m.group()), val);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                System.err.println("Invalid argument: "+arg);
            }
        }
        return result;
    }
    
    private static void replaceMapParams(Map<String, String> map, EvalResult eval) {
        for (Iterator<String> it = map.keySet().iterator(); it.hasNext(); ) {
            String key = it.next();
            String value = map.get(key);
            String key2 = replaceParams(key, eval);
            String value2 = replaceParams(value, eval);
            if (key.equals(key2) && value.equals(value2)) {
                // Nothing to do
            } else if (key.equals(key2)) {
                // Update value
                map.put(key, value2);
            } else {
                // Update key, and maybe value
                map.remove(key);
                map.put(key2, value2);
            }
        }
    }
    
    private static Collection<Link> processEval(EvalResult eval, Rule rule, Source source) {
        Collection<Link> result = new ArrayList<Link>();
        if (eval.matches()) {
            for (Link link : rule.links) {
                try {
                    Link copy = (Link) link.clone();
                    copy.name = copy.name.replaceAll("%name%", source.name);
                    copy.url = replaceParams(copy.url, eval);
                    if (copy instanceof LinkPost) {
                        LinkPost lp = (LinkPost) copy;
                        replaceMapParams(lp.headers, eval);
                        replaceMapParams(lp.params, eval);
                    }
                    result.add(copy);
                } catch (CloneNotSupportedException e) {
                    System.err.println(e);
                }
            }
        }
        return result;
    }
    
    /**
     * Replies the links relevant to the given OSM primitive.
     * @param p The OSM primitive
     * @return the links relevant to the {@code p}.
     */
    public static Collection<Link> getLinks(IPrimitive p) {
        Collection<Link> result = new ArrayList<Link>();
        for (Source source : sources) {
            for (Rule rule : source.rules) {
                result.addAll(processEval(rule.evaluates(p), rule, source));
            }
        }
        return result;
    }

    /**
     * Replies the links relevant to the given OSM tag.
     * @param tag The OSM tag
     * @return the links relevant to the {@code tag}.
     */
    public static Collection<Link> getLinks(Tag tag) {
        Collection<Link> result = new ArrayList<Link>();
        for (Source source : sources) {
            for (Rule rule : source.rules) {
                result.addAll(processEval(rule.evaluates(tag), rule, source));
            }
        }
        return result;
    }
}
