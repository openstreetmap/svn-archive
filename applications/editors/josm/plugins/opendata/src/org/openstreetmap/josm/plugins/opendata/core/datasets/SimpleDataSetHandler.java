//    JOSM opendata plugin.
//    Copyright (C) 2011-2012 Don-vip
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
package org.openstreetmap.josm.plugins.opendata.core.datasets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.openstreetmap.josm.data.osm.IPrimitive;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.plugins.opendata.core.io.OverpassApi;

import static org.openstreetmap.josm.plugins.opendata.core.io.OverpassApi.OaQueryType.*;
import static org.openstreetmap.josm.plugins.opendata.core.io.OverpassApi.OaRecurseType.*;

public abstract class SimpleDataSetHandler extends AbstractDataSetHandler {

	protected static final Projection wgs84 = PRJ_WGS84.getProjection();

	private final List<Tag> relevantTags = new ArrayList<Tag>();
	private final List<Tag> forbiddenTags = new ArrayList<Tag>();
	
	private final boolean relevantUnion;
	
	public SimpleDataSetHandler() {
		this.relevantUnion = false;
	}
			
	public SimpleDataSetHandler(String relevantTag) {
		addRelevantTag(relevantTag);
		this.relevantUnion = false;
	}
	
	public SimpleDataSetHandler(boolean relevantUnion, String ... relevantTags) {
		addRelevantTag(relevantTags);
		this.relevantUnion = relevantUnion;
	}

	public SimpleDataSetHandler(String ... relevantTags) {
		this(false, relevantTags);
	}

	public SimpleDataSetHandler(Tag relevantTag) {
		addRelevantTag(relevantTag);
		this.relevantUnion = false;
	}
	
	public SimpleDataSetHandler(boolean relevantUnion, Tag ... relevantTags) {
		addRelevantTag(relevantTags);
		this.relevantUnion = relevantUnion;
	}

	public SimpleDataSetHandler(Tag ... relevantTags) {
		this(false, relevantTags);
	}

	public void addRelevantTag(String ... relevantTags) {
		addTags(this.relevantTags, relevantTags);
	}

	public void addRelevantTag(Tag ... relevantTags) {
		addTags(this.relevantTags, relevantTags);
	}

	public void addForbiddenTag(String ... forbiddenTags) {
		addTags(this.forbiddenTags, forbiddenTags);
	}

	public void addForbiddenTag(Tag ... forbiddenTags) {
		addTags(this.forbiddenTags, forbiddenTags);
	}
	
	private final void addTags(final List<Tag> list, String ... tags) {
		if (tags != null) {
			for (String tag : tags) {
				if (tag != null) {
					if (tag.contains("=")) {
						String[] tab = tag.split("=");
						list.add(new Tag(tab[0], tab[1]));
					} else {
						list.add(new Tag(tag));
					}
				}
			}
		}
	}

	private final void addTags(final List<Tag> list, Tag ... tags) {
		if (tags != null) {
			for (Tag tag : tags) {
				if (tag != null) {
					list.add(tag);
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.fr.opendata.data.AbstractConverter#equals(org.openstreetmap.josm.data.osm.IPrimitive, org.openstreetmap.josm.data.osm.IPrimitive)
	 */
	@Override
	public boolean equals(IPrimitive p1, IPrimitive p2) {
		for (Tag tag : this.relevantTags) {
			if (!p1.get(tag.getKey()).equals(p2.get(tag.getKey()))) {
				return false;
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.fr.opendata.data.AbstractConverter#isRelevant(org.openstreetmap.josm.data.osm.IPrimitive)
	 */
	@Override
	public boolean isRelevant(IPrimitive p) {
		for (Tag tag : this.relevantTags) {
			String value = p.get(tag.getKey());
			if (value == null || (tag.getValue() != null && !tag.getValue().equals(value))) {
				return false;
			}
		}
		if (isForbidden(p)) {
			return false;
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.fr.opendata.datasets.AbstractDataSetHandler#isForbidden(org.openstreetmap.josm.data.osm.IPrimitive)
	 */
	@Override
	public boolean isForbidden(IPrimitive p) {
		for (Tag tag : this.forbiddenTags) {
			String value = p.get(tag.getKey());
			if (value != null && (tag.getValue() == null || tag.getValue().equals(value))) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.fr.opendata.datasets.AbstractDataSetHandler#hasForbiddenTags()
	 */
	@Override
	public boolean hasForbiddenTags() {
		return !this.forbiddenTags.isEmpty();
	}

	protected final String[] getOverpassApiConditions() {
		List<String> conditions = new ArrayList<String>();
		for (Tag tag : this.relevantTags) {
			conditions.add(OverpassApi.hasKey(tag.getKey(), tag.getValue()));
		}
		return conditions.toArray(new String[0]);
	}

	protected String getOverpassApiQueries(String bbox, String ... conditions) {
		String[] mpconditions = new String[conditions.length+1];
		mpconditions[0] = OverpassApi.hasKey("type", "multipolygon");
		for (int i=0; i<conditions.length; i++) {
			mpconditions[i+1] = conditions[i];
		}
		return OverpassApi.query(bbox, NODE, conditions) + "\n" + // Nodes 
			OverpassApi.recurse(NODE_RELATION, RELATION_WAY, WAY_NODE) + "\n" +
			OverpassApi.query(bbox, WAY, conditions) + "\n" + // Full ways and their full relations 
			OverpassApi.recurse(WAY_NODE, "nodes") + "\n" +
			OverpassApi.recurse(WAY_RELATION, RELATION_WAY, WAY_NODE) + "\n" +
			OverpassApi.query(bbox, RELATION, mpconditions) + "\n" + // Full multipolygons
			OverpassApi.recurse(RELATION_WAY, WAY_NODE);
	}
	
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.fr.opendata.datasets.AbstractDataSetHandler#getOverpassApiRequest(java.lang.String)
	 */
	@Override
	protected String getOverpassApiRequest(String bbox) {
		String result = "";
		if (this.relevantUnion) {
			for (Tag tag : this.relevantTags) {
				result += getOverpassApiQueries(bbox, OverpassApi.hasKey(tag.getKey(), tag.getValue())); 
			}
			result = OverpassApi.union(result);
		} else {
			result = OverpassApi.union(getOverpassApiQueries(bbox, getOverpassApiConditions()));
		}
		return result + OverpassApi.print();
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.fr.opendata.data.AbstractConverter#getOsmDataUrls(java.lang.String)
	 */
	@Override
	protected Collection<String> getOsmXapiRequests(String bbox) {
		String relevantTags = "";
		for (Tag tag : this.relevantTags) {
			relevantTags += "["+tag.getKey()+"="+(tag.getValue() == null ? "*" : tag.getValue())+"]";
		}
		String forbiddenTags = "";
		for (Tag tag : this.forbiddenTags) {
			forbiddenTags += "[not("+tag.getKey()+"="+(tag.getValue() == null ? "*" : tag.getValue())+")]";
		}
		return Collections.singleton("*[bbox="+bbox+"]"+relevantTags+forbiddenTags+"[@meta]");
	}
}
