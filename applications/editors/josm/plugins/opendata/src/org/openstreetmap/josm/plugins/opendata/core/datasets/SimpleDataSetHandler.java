package org.openstreetmap.josm.plugins.opendata.core.datasets;

import static org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler.OaQueryType.NODE;
import static org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler.OaQueryType.WAY;
import static org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler.OaQueryType.RELATION;
import static org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler.OaRecurseType.NODE_RELATION;
import static org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler.OaRecurseType.RELATION_WAY;
import static org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler.OaRecurseType.WAY_NODE;
import static org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler.OaRecurseType.WAY_RELATION;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.openstreetmap.josm.data.osm.IPrimitive;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.data.projection.Projection;

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
			conditions.add(oaHasKey(tag.getKey(), tag.getValue()));
		}
		return conditions.toArray(new String[0]);
	}

	protected String getOverpassApiQueries(String bbox, String ... conditions) {
		String[] mpconditions = new String[conditions.length+1];
		mpconditions[0] = oaHasKey("type", "multipolygon");
		for (int i=0; i<conditions.length; i++) {
			mpconditions[i+1] = conditions[i];
		}
		return oaQuery(bbox, NODE, conditions) + "\n" + // Nodes 
				oaRecurse(NODE_RELATION, RELATION_WAY, WAY_NODE) + "\n" +
				oaQuery(bbox, WAY, conditions) + "\n" + // Full ways and their full relations 
				oaRecurse(WAY_NODE, "nodes") + "\n" +
				oaRecurse(WAY_RELATION, RELATION_WAY, WAY_NODE) + "\n" +
				oaQuery(bbox, RELATION, mpconditions) + "\n" + // Full multipolygons
				oaRecurse(RELATION_WAY, WAY_NODE);
	}
	
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.fr.opendata.datasets.AbstractDataSetHandler#getOverpassApiRequest(java.lang.String)
	 */
	@Override
	protected String getOverpassApiRequest(String bbox) {
		String result = "";
		if (this.relevantUnion) {
			for (Tag tag : this.relevantTags) {
				result += getOverpassApiQueries(bbox, oaHasKey(tag.getKey(), tag.getValue())); 
			}
			result = oaUnion(result);
		} else {
			result = oaUnion(getOverpassApiQueries(bbox, getOverpassApiConditions()));
		}
		return result + oaPrint();
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
