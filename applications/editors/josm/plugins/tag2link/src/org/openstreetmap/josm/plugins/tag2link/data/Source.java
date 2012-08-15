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
package org.openstreetmap.josm.plugins.tag2link.data;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A source of links relative to an OSM primitive or tag, depending of the successful match of its conditions against it.
 * @author Don-vip
 *
 */
public class Source {
    /**
     * The user-friendly source name.
     */
    public final String name;
    
    /**
     * The rules applied against an OSM primitive or tag.
     */
    public final Collection<Rule> rules = new ArrayList<Rule>();

    /**
     * Constructs a new {@code Source}.
     * @param name The user-friendly source name
     */
    public Source(String name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Source [name=" + name + ", rules=" + rules + "]";
    }
}
