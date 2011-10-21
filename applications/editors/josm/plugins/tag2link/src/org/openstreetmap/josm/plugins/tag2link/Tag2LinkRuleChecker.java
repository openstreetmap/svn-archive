//    JOSM tag2link plugin.
//    Copyright (C) 2011 Don-vip & FrViPofm
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.tag2link.data.Link;
import org.openstreetmap.josm.plugins.tag2link.data.Rule;
import org.openstreetmap.josm.plugins.tag2link.data.Rule.EvalResult;
import org.openstreetmap.josm.plugins.tag2link.data.Rule.MatchingTag;
import org.openstreetmap.josm.plugins.tag2link.data.Source;
import org.openstreetmap.josm.plugins.tag2link.io.SourcesReader;

public class Tag2LinkRuleChecker implements Tag2LinkConstants {

    private static Collection<Source> sources = new ArrayList<Source>();
    
    private static boolean initialized = false;
        
    public static void init() {
        if (!initialized) {
            sources = SourcesReader.readSources();
            initialized = true;
        }
    }
    
    public static Collection<Link> getLinks(OsmPrimitive p) {
        Collection<Link> result = new ArrayList<Link>();
        for (Source source : sources) {
            for (Rule rule : source.rules) {
                EvalResult eval = rule.evaluates(p);
                if (eval.matches()) {
                    Set<String> links = new HashSet<String>();
                    for (MatchingTag tag : eval.matchingTags) {
                        
                    }
                }
            }
        }
        return result;
    }
}
