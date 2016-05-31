package org.openstreetmap.josm.plugins.pt_assistant.validation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.validation.Severity;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.gui.dialogs.relation.sort.RelationSorter;
import org.openstreetmap.josm.gui.dialogs.relation.sort.WayConnectionType;
import org.openstreetmap.josm.gui.dialogs.relation.sort.WayConnectionTypeCalculator;

public class GapTest extends Test {
	
	public static final int ERROR_CODE_SORTING = 3711;
	public static final int ERROR_CODE_OTHER_GAP = 3712;

	public GapTest() {
		super(tr("Gaps"), tr("Checks if there are gaps in the route relation."));
	}

	@Override
	public void visit(Relation r) {

		if (r.hasKey("route")) {

			List<RelationMember> members = r.getMembers();
			final List<RelationMember> waysToCheck = new ArrayList<>();
			for (RelationMember member : members) {
				if (member.hasRole("") && OsmPrimitiveType.WAY.equals(member.getType())) {
					waysToCheck.add(member);
				}
			}

			if (waysToCheck.isEmpty()) {
				return;
			}

			if (hasGap(waysToCheck)) {
				RelationSorter sorter = new RelationSorter();
				List<RelationMember> correctedList = sorter.sortMembers(waysToCheck);
				if (hasGap(correctedList)) {
					System.out.println("other error type");
					errors.add(new TestError(this, Severity.WARNING,
							tr("PT: Route contains a gap that cannot be fixed by sorting the ways."), ERROR_CODE_OTHER_GAP, r));
				} else {
					System.out.println("sorting error");
					errors.add(new TestError(this, Severity.WARNING,
							tr("PT: Route contains a gap that can be fixed by sorting"), ERROR_CODE_SORTING, r));
				}
			}

		}

	}

	/**
	 * Checks if there is a gap for a given list of ways
	 * 
	 * @param waysToCheck
	 * @return
	 */
	private boolean hasGap(List<RelationMember> waysToCheck) {
		WayConnectionTypeCalculator connectionTypeCalculator = new WayConnectionTypeCalculator();
		final List<WayConnectionType> links = connectionTypeCalculator.updateLinks(waysToCheck);
		for (int i = 0; i < links.size(); i++) {
			final WayConnectionType link = links.get(i);
			final boolean hasError = !(i == 0 || link.linkPrev) || !(i == links.size() - 1 || link.linkNext)
					|| link.direction == null || WayConnectionType.Direction.NONE.equals(link.direction);
			if (hasError) {
				return true;

			}
		}

		return false;
	}


}
