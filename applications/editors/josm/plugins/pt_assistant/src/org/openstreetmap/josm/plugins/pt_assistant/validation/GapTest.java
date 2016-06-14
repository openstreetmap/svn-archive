package org.openstreetmap.josm.plugins.pt_assistant.validation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.validation.Severity;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.gui.dialogs.relation.sort.RelationSorter;
import org.openstreetmap.josm.gui.dialogs.relation.sort.WayConnectionType;
import org.openstreetmap.josm.gui.dialogs.relation.sort.WayConnectionTypeCalculator;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;

public class GapTest extends Test {

	public static final int ERROR_CODE_SORTING = 3711;
	public static final int ERROR_CODE_OVERSHOOT = 3712;
	public static final int ERROR_CODE_SPLITTING = 3713;
	public static final int ERROR_CODE_OTHER_GAP = 3719;

	private List<RelationMember> overshootList = new ArrayList<>();

	public GapTest() {
		super(tr("Gaps"), tr("Checks if there are gaps in the route relation."));
	}

	@Override
	public void visit(Relation r) {
		
		if (!RouteUtils.isTwoDirectionRoute(r)) {
			return;
		}

		if (RouteUtils.hasIncompleteMembers(r)) {
			return;
		}
		
		List<RelationMember> members = r.getMembers();
		final List<RelationMember> waysToCheck = new ArrayList<>();
		for (RelationMember member : members) {

			if (RouteUtils.isPTWay(member)) {
				waysToCheck.add(member);
			}
		}

		if (waysToCheck.isEmpty()) {
			return;
		}

		if (hasGap(waysToCheck)) {
			RelationSorter sorter = new RelationSorter();
			List<RelationMember> correctedList = sorter.sortMembers(waysToCheck);

			if (!hasGap(correctedList)) {
				errors.add(new TestError(this, Severity.WARNING,
						tr("PT: Route contains a gap that can be fixed by sorting"), ERROR_CODE_SORTING, r));
			} else {
				// List<RelationMember> overshoots =
				// this.getOvershoots(correctedList);
				// if (!overshoots.isEmpty()) {
				// // TODO: make sure that duplicates are removed first
				// for (RelationMember overshoot : overshoots) {
				// List<Relation> primitives = new ArrayList<>(1);
				// primitives.add(r);
				// List<Way> highlighted = new ArrayList<>(1);
				// highlighted.add(overshoot.getWay());
				// errors.add(new TestError(this, Severity.WARNING, tr("PT:
				// Route contains an overshoot"),
				// ERROR_CODE_OVERSHOOT, primitives, highlighted));
				// }
				//
				// } else {
					errors.add(new TestError(this, Severity.WARNING,
							tr("PT: Route contains a gap that cannot be fixed by sorting the ways"),
							ERROR_CODE_OTHER_GAP, r));
//				}
			}

		}

	}

	/**
	 * Checks if there is a gap for a given list of ways. It does not check if
	 * the way actually stands for a public transport platform - that should be
	 * checked beforehand.
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

	/**
	 * Checks if there is a single "hanging" way (perhaps left after a way
	 * split) that can be easily removed.
	 * 
	 * @param sortedWays
	 *            is a list of ways only that should be sorted with the
	 *            RelationSorter before this method is called. No error occurs
	 *            if they are unsorted, but the method call is pointless if they
	 *            are in a wrong order.
	 * @return true is there is such a way, false otherwise
	 */
	private List<RelationMember> getOvershoots(List<RelationMember> sortedWays) {

		List<RelationMember> overshoots = new ArrayList<>();

		if (sortedWays.size() < 5) {
			// the route has to have at least five ways to be able to have an
			// overshoot. I assume that the overshoot cannot touch the very
			// first or the very last way in a route because then it would not
			// be clear which of the ways would be an overshoot.
			/*-
			 *  x              x             x
			 *   \ A           |D           / G
			 *    \            |           /
			 *     x-----------x----------x
			 *    /      C     |    F      \
			 *   / B           |E           \ H
			 *  x			   x             x
			 *  
			 *  Example: ways D and E can be overshoots (from the point of 
			 *  view of this method), but ways A, B, G and H cannot. 
			 *  Therefore, a relation must have at least 4 "good" ways 
			 *  in order to be able to have an overshoot.
			 */

			return overshoots;
		}

		for (int i = 2; i < sortedWays.size() - 3; i++) {
			Way prev = sortedWays.get(i - 1).getWay();
			Way curr = sortedWays.get(i).getWay();
			Way next = sortedWays.get(i + 1).getWay();
			boolean firstNodeConnectedToPrev = (curr.firstNode() == prev.firstNode()
					|| curr.firstNode() == prev.lastNode());
			boolean lastNodeConnectedToPrev = (curr.lastNode() == prev.firstNode()
					|| curr.lastNode() == prev.lastNode());
			boolean firstNodeConnectedToNext = (curr.firstNode() == next.firstNode()
					|| curr.firstNode() == next.lastNode());
			boolean lastNodeConnectedToNext = (curr.lastNode() == next.firstNode()
					|| curr.lastNode() == next.lastNode());
			if ((firstNodeConnectedToPrev && firstNodeConnectedToNext)
					|| (lastNodeConnectedToPrev && lastNodeConnectedToNext)) {
				overshoots.add(sortedWays.get(i));
			}

		}

		return overshoots;
	}

	@Override
	public Command fixError(TestError testError) {

		List<Command> commands = new ArrayList<>(50);

		if (testError.getTester().getClass().equals(GapTest.class) && testError.isFixable()) {

			// If this is an error that can be fixed simply by sorting the ways:
			if (testError.getCode() == ERROR_CODE_SORTING) {
				for (OsmPrimitive primitive : testError.getPrimitives()) {
					Relation relation = (Relation) primitive;
					// separate ways from stops (because otherwise the order of
					// stops/platforms can be messed up by the sorter:
					List<RelationMember> members = relation.getMembers();
					final List<RelationMember> stops = new ArrayList<>();
					final List<RelationMember> ways = new ArrayList<>();
					for (RelationMember member : members) {
						if (RouteUtils.isPTWay(member)) {
							if (member.getRole().equals("")) {
								ways.add(member);
							} else {
								RelationMember modifiedMember = new RelationMember("", member.getWay());
								ways.add(modifiedMember);
							}

						} else { // stops:
							if (member.getRole().equals("stop_positon")) {
								// it is not expected that stop_positions could
								// be relations
								if (member.getType().equals(OsmPrimitiveType.NODE)) {
									RelationMember modifiedMember = new RelationMember("stop", member.getNode());
									stops.add(modifiedMember);
								} else { // if it is a primitive of type way:
									RelationMember modifiedMember = new RelationMember("stop", member.getWay());
									stops.add(modifiedMember);
								}
							} else { // if it is not a stop_position:
								stops.add(member);
							}

						}
					}

					// sort the ways:
					RelationSorter sorter = new RelationSorter();
					List<RelationMember> sortedWays = sorter.sortMembers(ways);

					// create a new relation to pass to the command:
					Relation sortedRelation = new Relation(relation);
					List<RelationMember> sortedRelationMembers = new ArrayList<>(members.size());
					for (RelationMember rm : stops) {
						sortedRelationMembers.add(rm);
					}
					for (RelationMember rm : sortedWays) {
						sortedRelationMembers.add(rm);
					}
					sortedRelation.setMembers(sortedRelationMembers);

					ChangeCommand changeCommand = new ChangeCommand(relation, sortedRelation);

					commands.add(changeCommand);

				}

			}

			// if the error is a single overshoot:
//			if (testError.getCode() == ERROR_CODE_OVERSHOOT) {
//
//				for (OsmPrimitive primitive : testError.getPrimitives()) {
//					Relation originalRelation = (Relation) primitive;
//					Relation modifiedRelation = new Relation(originalRelation);
//					List<RelationMember> modifiedMembers = new ArrayList<>();
//					// add stops of a public transport route first:
//					for (RelationMember rm : originalRelation.getMembers()) {
//						if (RouteUtils.isPTStop(rm)) {
//							if (rm.hasRole("stop_position")) {
//								// it is not expected that stop_positions could
//								// be relations
//								if (rm.getType().equals(OsmPrimitiveType.NODE)) {
//									RelationMember modifiedMember = new RelationMember("stop", rm.getNode());
//									modifiedMembers.add(modifiedMember);
//								} else { // if it is a primitive of type "way":
//									RelationMember modifiedMember = new RelationMember("stop", rm.getWay());
//									modifiedMembers.add(modifiedMember);
//								}
//							} else {
//								modifiedMembers.add(rm);
//							}
//
//						}
//
//					}
//					// add ways of a public transport route (if they are not
//					// overshoots):
//					for (RelationMember rm : originalRelation.getMembers()) {
//						if (RouteUtils.isPTWay(rm) && !overshootList.contains(rm)) {
//
//							if (rm.getRole().equals("")) {
//								modifiedMembers.add(rm);
//							} else {
//								RelationMember modifiedMember = new RelationMember("", rm.getWay());
//								modifiedMembers.add(modifiedMember);
//							}
//						}
//					}
//					modifiedRelation.setMembers(modifiedMembers);
//
//					ChangeCommand changeCommand = new ChangeCommand(originalRelation, modifiedRelation);
//					commands.add(changeCommand);
//				}
//
//			}
		}

		if (commands.isEmpty()) {
			return null;
		}

		if (commands.size() == 1) {
			return commands.get(0);
		}

		return new SequenceCommand(tr("Fix gaps in public transport route"), commands);

	}

	/**
	 * Checks if the test error is fixable
	 */
	@Override
	public boolean isFixable(TestError testError) {
		if (testError.getCode() == ERROR_CODE_SORTING ) {
			return true;
		}
		return false;
	}

}
