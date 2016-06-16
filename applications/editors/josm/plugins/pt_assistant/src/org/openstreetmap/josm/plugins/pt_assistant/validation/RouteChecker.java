package org.openstreetmap.josm.plugins.pt_assistant.validation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.validation.Severity;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.gui.dialogs.relation.sort.RelationSorter;
import org.openstreetmap.josm.gui.dialogs.relation.sort.WayConnectionType;
import org.openstreetmap.josm.gui.dialogs.relation.sort.WayConnectionTypeCalculator;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;

/**
 * Performs tests of a route at the level of the whole route: sorting test
 * 
 * @author darya
 *
 */
public class RouteChecker extends Checker {

	private boolean hasGap;

	List<RelationMember> sortedMembers;

	public RouteChecker(Relation relation, Test test) {

		super(relation, test);

		this.hasGap = false;

		performSortingTest();

	}

	private void performSortingTest() {

		final List<RelationMember> waysToCheck = new ArrayList<>();
		for (RelationMember rm : relation.getMembers()) {

			if (RouteUtils.isPTWay(rm) && rm.getType().equals(OsmPrimitiveType.WAY)) {
				waysToCheck.add(rm);
			}
		}

		if (waysToCheck.isEmpty()) {
			return;
		}

		if (hasGap(waysToCheck)) {

			this.hasGap = true;

			RelationSorter sorter = new RelationSorter();
			sortedMembers = sorter.sortMembers(waysToCheck);

			if (!hasGap(sortedMembers)) {
				TestError e = new TestError(this.test, Severity.WARNING,
						tr("PT: Route contains a gap that can be fixed by sorting"),
						PTAssitantValidatorTest.ERROR_CODE_SORTING, relation);
				this.errors.add(e);

			}

		}

	}

	/**
	 * Checks if there is a gap for a given list of ways. It does not check if
	 * the way actually stands for a public transport platform - that should be
	 * checked beforehand.
	 * 
	 * @param waysToCheck
	 * @return true if has gap (in the sense of continuity of ways in the
	 *         Relation Editor), false otherwise
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

	public List<RelationMember> getSortedMembers() {

		return sortedMembers;

	}

	public boolean getHasGap() {

		return this.hasGap;

	}

}
