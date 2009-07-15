package org.openstreetmap.josm.plugins.graphview.core.access;

import static org.openstreetmap.josm.plugins.graphview.core.access.AccessType.UNDEFINED;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.josm.plugins.graphview.core.data.DataSource;
import org.openstreetmap.josm.plugins.graphview.core.data.MapBasedTagGroup;
import org.openstreetmap.josm.plugins.graphview.core.data.Tag;
import org.openstreetmap.josm.plugins.graphview.core.data.TagGroup;
import org.openstreetmap.josm.plugins.graphview.core.property.RoadPropertyType;

/**
 * AccessEvaluator based on a single AccessRuleset
 */
public class RulesetAccessEvaluator<N, W, R> implements AccessEvaluator<N, W> {

	private final DataSource<N, W, R> dataSource;
	private final AccessRuleset ruleset;
	private final AccessParameters parameters;

	/**
	 * @param dataSource  object that allows access to data objects and tags/members; != null
	 * @param ruleset     ruleset that is used for evaluation; != null
	 * @param parameters  parameters object that describes the vehicle
	 *                    and situation to evaluate access for; != null
	 */
	public RulesetAccessEvaluator(DataSource<N, W, R> dataSource, AccessRuleset ruleset, AccessParameters parameters) {
		assert dataSource != null && ruleset != null && parameters != null;

		this.dataSource = dataSource;
		this.ruleset = ruleset;
		this.parameters = parameters;

	}

	public boolean wayUsable(W way, boolean forward,
			Map<RoadPropertyType<?>, Object> segmentPropertyValues) {

		TagGroup wayTags = dataSource.getTagsW(way);

		TagGroup wayTagsWithImplications = new MapBasedTagGroup(wayTags);
		for (Implication implication : ruleset.getImplications()) {
			wayTagsWithImplications = implication.apply(wayTagsWithImplications);
		}

		/* check base tagging */

		boolean usableWay = false;
		for (Tag tag : ruleset.getBaseTags()) {
			if (wayTags.contains(tag)) {
				usableWay = true;
				break;
			}
		}

		if (!usableWay) {
			return false;
		}

		/* evaluate one-way tagging */

		String onewayValue =  wayTagsWithImplications.getValue("oneway");

		if (forward && "-1".equals(onewayValue)
				&& !"foot".equals(parameters.getAccessClass())) {
			return false;
		}

		if (!forward
				&& ("1".equals(onewayValue) || "yes".equals(onewayValue) || "true".equals(onewayValue))
				&& !"foot".equals(parameters.getAccessClass())) {
			return false;
		}

		/* evaluate properties and access tagging */

		return objectUsable(segmentPropertyValues, wayTags);
	}

	public boolean nodeUsable(N node, Map<RoadPropertyType<?>,Object> roadPropertyValues) {

		TagGroup nodeTags = dataSource.getTagsN(node);

		return objectUsable(roadPropertyValues, nodeTags);
	};

	private boolean objectUsable(Map<RoadPropertyType<?>, Object> roadPropertyValues,
			TagGroup tags) {

		/* evaluate road properties */

		for (RoadPropertyType<?> property : roadPropertyValues.keySet()) {
			if (!property.isUsable(roadPropertyValues.get(property), parameters)) {
				return false;
			}
		}

		/* evaluate access type */

		AccessType accessType = UNDEFINED;

		if (tags.size() > 0) {

			Map<String, AccessType> accessTypePerClass =
				createAccessTypePerClassMap(tags, ruleset.getAccessHierarchyAncestors(parameters.getAccessClass()));

			for (String accessClass : ruleset.getAccessHierarchyAncestors(parameters.getAccessClass())) {
				accessType = accessTypePerClass.get(accessClass);
				if (accessType != UNDEFINED) { break; }
			}

		}

		return parameters.getAccessTypeUsable(accessType);
	}

	private Map<String, AccessType> createAccessTypePerClassMap(
			TagGroup wayTags, Collection<String> accessClasses) {

		/*
		 * create map and fill with UNDEFINED values
		 * (this also allows to use keySet instead of accessClasses later)
		 */

		Map<String, AccessType> accessTypePerClass = new HashMap<String, AccessType>();

		for (String accessClass : accessClasses) {
			accessTypePerClass.put(accessClass, AccessType.UNDEFINED);
		}

		/* evaluate implied tagging of base tag */

		Tag baseTag = null;
		for (Tag tag : wayTags) {
			if (ruleset.getBaseTags().contains(tag)) {
				baseTag = tag;
				break;
			}
		}

		if (baseTag != null) {

			TagGroup tagsWithBaseImplications = new MapBasedTagGroup(baseTag);
			for (Implication implication : ruleset.getImplications()) {
				tagsWithBaseImplications = implication.apply(tagsWithBaseImplications);
			}

			setAccessTypesFromTags(accessTypePerClass, tagsWithBaseImplications);

		}

		/* evaluate implied tagging of other tags */

		Map<String, String> tagMap = new HashMap<String, String>();
		for (Tag tag : wayTags) {
			if (!tag.equals(baseTag)) {
				tagMap.put(tag.key, tag.value);
			}
		}

		TagGroup tagsWithOtherImplications = new MapBasedTagGroup(tagMap);
		for (Implication implication : ruleset.getImplications()) {
			tagsWithOtherImplications = implication.apply(tagsWithOtherImplications);
		}

		setAccessTypesFromTags(accessTypePerClass, tagsWithOtherImplications);

		/* evaluate explicit access tagging */

		for (String key : ruleset.getAccessHierarchyAncestors(parameters.getAccessClass())) {
			String value = wayTags.getValue(key);
			if (value != null) {
				AccessType accessType = AccessType.getAccessType(value);
				accessTypePerClass.put(key, accessType);
			}
		}

		return accessTypePerClass;
	}

	/**
	 * adds all access information from a collection of tags to a [access class -> access type] map.
	 * Existing entries will be replaced.
	 */
	private void setAccessTypesFromTags(Map<String, AccessType> accessTypePerClass, TagGroup tags) {
		for (String accessClass : accessTypePerClass.keySet()) {
			String value = tags.getValue(accessClass);
			if (value != null) {
				AccessType accessType = AccessType.getAccessType(value);
				accessTypePerClass.put(accessClass, accessType);
			}
		}
	}

}
