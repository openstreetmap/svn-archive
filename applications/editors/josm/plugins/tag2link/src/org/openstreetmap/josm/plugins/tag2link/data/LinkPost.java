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
package org.openstreetmap.josm.plugins.tag2link.data;

import java.util.HashMap;
import java.util.Map;

public class LinkPost extends Link {

	public final Map<String, String> headers;
	public final Map<String, String> params;
	
	public LinkPost(LinkPost link) {
		this(new String(link.name), new String(link.url), 
				link.headers == null ? null : new HashMap<String, String>(link.headers), 
				link.params  == null ? null : new HashMap<String, String>(link.params));
	}

	public LinkPost(String name, String url) {
		this(name, url, null, null);
	}

	public LinkPost(String name, String url, Map<String, String> headers, Map<String, String> params) {
		super(name, url);
		this.headers = headers;
		this.params = params;
	}
	
	protected final boolean containsParams(Map<String, String> map) {
		if (map != null) {
			for (String key : map.keySet()) {
				if (containsParams(map.get(key))) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean headersContainsParams() {
		return containsParams(headers);
	}

	public boolean paramsContainsParams() {
		return containsParams(params);
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.tag2link.data.Link#containsParams()
	 */
	@Override
	public boolean containsParams() {
		return super.containsParams() || headersContainsParams() || paramsContainsParams();
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.tag2link.data.Link#clone()
	 */
	@Override
	public LinkPost clone() throws CloneNotSupportedException {
		return new LinkPost(this);
	}
}
