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

public class Link implements Cloneable {
    public String name;
    public String url;
    
    public Link(String name, String url) {
        this.name = name;
        this.url = url;
    }
    
    public Link(Link link) {
        this(new String(link.name), new String(link.url));
    }
    
    protected final boolean containsParams(String s) {
        return s.matches("[^{}]*{[^{}]*}[^{}]*");
    }
    
    public final boolean nameContainsParams() {
        return containsParams(name);
    }
    
    public final boolean urlContainsParams() {
        return containsParams(url);
    }
    
    public boolean containsParams() {
        return nameContainsParams() || urlContainsParams();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Link [name=" + name + ", url=" + url + "]";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Link clone() throws CloneNotSupportedException {
        return new Link(this);
    }
}
