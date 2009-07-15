package org.openstreetmap.josm.plugins.graphview.plugin.data;

import java.util.Collection;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.graphview.core.access.AccessParameters;
import org.openstreetmap.josm.plugins.graphview.core.access.AccessRuleset;
import org.openstreetmap.josm.plugins.graphview.core.property.RoadPropertyType;
import org.openstreetmap.josm.plugins.graphview.core.transition.GenericTransitionStructure;

/**
 * transition structure that retrieves data from a {@link JOSMDataSource}
 */
public class JOSMTransitionStructure extends GenericTransitionStructure<Node, Way, Relation> {

	private static final JOSMDataSource DATA_SOURCE = new JOSMDataSource();

	public JOSMTransitionStructure(AccessParameters accessParameters, AccessRuleset ruleset,
			Collection<RoadPropertyType<?>> properties) {

		super(Node.class, Way.class, Relation.class,
				accessParameters, ruleset,
				DATA_SOURCE,
				properties);

	}

	/** causes an update (as if the DataSource had noticed a change) */
	public void forceUpdate() {
		super.update(DATA_SOURCE);
	}

}
