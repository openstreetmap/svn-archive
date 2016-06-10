package org.openstreetmap.josm.plugins.pt_assistant.validation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.List;

import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.validation.Severity;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;

public class PlatformsFirstTest extends Test {

	public static final int ERROR_CODE = 3701;

	/**
	 * Constructs a new {@code InternetTags} test.
	 */
	public PlatformsFirstTest() {
		super(tr("Platforms first"), tr("Checks if platforms are listed before ways in the route relation."));
	}

	@Override
	public void visit(Relation r) {

		if (RouteUtils.isTwoDirectionRoute(r)) {

			List<RelationMember> members = r.getMembers();
			RelationMember prevMember = null;
			for (RelationMember currMember : members) {
				if (prevMember != null) {
					// check if the current member is a platform, while the
					// previous member is a way:
					if (currMember.hasRole("platform") && prevMember.getType().equals(OsmPrimitiveType.WAY)
							&& prevMember.hasRole("")) {
						this.errors.add(new TestError(this, Severity.WARNING,
								tr("PT: route relation(s) contain(s) way(s) before platform(s) in the members list"),
								ERROR_CODE, r));
						return;
					}
				}
				prevMember = currMember;
			}
		}
	}
}
