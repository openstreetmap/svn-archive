package org.openstreetmap.josm.plugins.pt_assistant.validation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

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
import org.openstreetmap.josm.plugins.pt_assistant.actions.FixTask;
import org.openstreetmap.josm.plugins.pt_assistant.actions.IncompleteMembersDownloadThread;
import org.openstreetmap.josm.plugins.pt_assistant.gui.IncompleteMembersDownloadDialog;
import org.openstreetmap.josm.plugins.pt_assistant.gui.ProceedDialog;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;

public class PTAssitantValidatorTest extends Test {

	public static final int ERROR_CODE_SORTING = 3711;
	public static final int ERROR_CODE_ROAD_TYPE = 3721;
	public static final int ERROR_CODE_DIRECTION = 3731;
	public static final int ERROR_CODE_END_STOP = 3141;
	public static final int ERROR_CODE_SPLIT_WAY = 3142;
	
	public PTAssitantValidatorTest() {
		super(tr("Public Transport Assistant tests"),
				tr("Check if route relations are compatible with public transport version 2"));

	}

	@Override
	public void visit(Relation r) {
		

		if (!RouteUtils.isTwoDirectionRoute(r)) {
			return;
		}
		

		// Download incomplete members. If the download does not work, finish.
		if (r.hasIncompleteMembers()) {
			boolean downloadSuccessful = this.downloadIncompleteMembers(r);
			if (!downloadSuccessful) {
				return;
			}
		}
		
		if (r.hasIncompleteMembers()) {
			return;
		}

		// Check individual ways using the oneway direction test and the road
		// type test:
		WayChecker wayChecker = new WayChecker(r, this);
		this.errors.addAll(wayChecker.getErrors());
		

		if (this.errors.isEmpty()) {
			proceedWithSorting(r);
		} else {
			this.proceedAfterWayCheckerErrors(r);
		}

	}

	/**
	 * Downloads incomplete relation members in an extra thread (user input
	 * required)
	 * 
	 * @return true if successful, false if not successful
	 */
	private boolean downloadIncompleteMembers(Relation r) {
		IncompleteMembersDownloadDialog incompleteMembersDownloadDialog = new IncompleteMembersDownloadDialog(
				r.getId());

		int userInput = incompleteMembersDownloadDialog.getUserSelection();

		if (userInput == JOptionPane.YES_OPTION) {

			Thread t = new IncompleteMembersDownloadThread();
			t.start();
			synchronized (t) {
				try {
					t.wait();
				} catch (InterruptedException e) {
					// TODO: give the user a message that testing stops
					return false;
				}
			}
			return true;
		}

		return false;
	}

	/**
	 * Gets user input after errors were detected by WayChecker (direction
	 * errors and road type errors)
	 */
	private void proceedAfterWayCheckerErrors(Relation r) {

		// count errors of each type:
		int numberOfDirectionErrors = 0;
		int numberOfRoadTypeErrors = 0;
		for (TestError e : this.errors) {
			if (e.getCode() == ERROR_CODE_DIRECTION) {
				numberOfDirectionErrors++;
			}
			if (e.getCode() == ERROR_CODE_ROAD_TYPE) {
				numberOfRoadTypeErrors++;
			}
		}


		ProceedDialog proceedDialog = new ProceedDialog(r.getId(), numberOfDirectionErrors, numberOfRoadTypeErrors);
		int userInput = proceedDialog.getUserSelection();

		if (userInput == 0) {
			this.fixErrorFromPlugin(this.errors);
			proceedWithSorting(r);
			return;
		}

		if (userInput == 1) {
			// TODO
			JOptionPane.showMessageDialog(null, "This is not implemented yet!");
			return;
		}

		if (userInput == 2) {
			// TODO: should the errors be removed from the error list?
			proceedWithSorting(r);
		}

		// if userInput==-1 (i.e. no input), do nothing and stop testing of the
		// route.

	}
	

	/**
	 * Carries out the second stage of the testing: sorting
	 * @param r
	 */
	private void proceedWithSorting(Relation r) {
		
		// Check if the relation is correct, or only has a wrong sorting order:
		RouteChecker routeChecker = new RouteChecker(r, this);
		List<TestError> routeCheckerErrors = routeChecker.getErrors();
		
		/*- At this point, there are 3 variants: 
		 * 
		 * 1) There are no errors => route is correct
		 * 2) There is only a sorting error (can only be 1), but otherwise
		 * correct.
		 * 3) There are some other errors/gaps that cannot be fixed by
		 * sorting => start further test (stop-by-stop) 
		 * 
		 * */
		
		
		if (!routeCheckerErrors.isEmpty()) {
			// Variant 2
			// If there is only the sorting error, add it and stop testing.
			this.errors.addAll(routeChecker.getErrors());
			return;
		}
		
		if (!routeChecker.getHasGap()) {
			// Variant 1
			// TODO: add the segments of this route to the list correct route segments		
		}
		
		// Variant 3:
		proceedAfterSorting(r);
		
		
	}
	
	
	private void proceedAfterSorting(Relation r) {
		
		
		
		SegmentChecker segmentChecker = new SegmentChecker(r, this);
		segmentChecker.performFirstStopTest();
		segmentChecker.performLastStopTest();
		// TODO: perform segment test
		this.errors.addAll(segmentChecker.getErrors());
//		performDummyTest(r);
	}

	/**
	 * Checks if the test error is fixable
	 */
	@Override
	public boolean isFixable(TestError testError) {
		if (testError.getCode() == ERROR_CODE_DIRECTION || testError.getCode() == ERROR_CODE_ROAD_TYPE
				|| testError.getCode() == ERROR_CODE_SORTING) {
			return true;
		}
		return false;
	}

	@Override
	public Command fixError(TestError testError) {

		List<Command> commands = new ArrayList<>();

		if (testError.getCode() == ERROR_CODE_DIRECTION || testError.getCode() == ERROR_CODE_ROAD_TYPE) {
			commands.add(fixErrorByRemovingWay(testError));
		}

		if (testError.getCode() == ERROR_CODE_SORTING) {
			commands.add(fixSortingError(testError));
		}

		if (commands.isEmpty()) {
			return null;
		}

		if (commands.size() == 1) {
			return commands.get(0);
		}

		return new SequenceCommand(tr("Fix error"), commands);
	}

	/**
	 * This method is the counterpart of the fixError(TestError testError)
	 * method. The fixError method is invoked from the core validator (e.g. when
	 * user presses the "Fix" button in the validator). This method is invoken
	 * when the fix is initiated from within the plugin (e.g. automated fixes).
	 * 
	 * @return
	 */
	private void fixErrorFromPlugin(List<TestError> testErrors) {

			
			// run fix task asynchronously
			FixTask fixTask = new FixTask(testErrors);
//			Main.worker.submit(fixTask);
			
			Thread t = new Thread(fixTask);
			t.start();
			try {
				t.join();
				errors.removeAll(testErrors);

			} catch (InterruptedException e) {
				JOptionPane.showMessageDialog(null, "Error occurred during fixing");
			}



	}

	private Command fixErrorByRemovingWay(TestError testError) {

		if (testError.getCode() != ERROR_CODE_ROAD_TYPE && testError.getCode() != ERROR_CODE_DIRECTION) {
			return null;
		}

		List<OsmPrimitive> primitives = (List<OsmPrimitive>) testError.getPrimitives();
		Relation originalRelation = (Relation) primitives.get(0);
		List<OsmPrimitive> highlighted = (List<OsmPrimitive>) testError.getHighlighted();
		Way wayToRemove = (Way) highlighted.get(0);

		Relation modifiedRelation = new Relation(originalRelation);
		List<RelationMember> modifiedRelationMembers = new ArrayList<>(originalRelation.getMembersCount() - 1);

		// copy PT stops first, PT ways last:
		for (RelationMember rm : originalRelation.getMembers()) {
			if (RouteUtils.isPTStop(rm)) {

				if (rm.getRole().equals("stop_position")) {
					if (rm.getType().equals(OsmPrimitiveType.NODE)) {
						RelationMember newMember = new RelationMember("stop", rm.getNode());
						modifiedRelationMembers.add(newMember);
					} else { // if it is a way:
						RelationMember newMember = new RelationMember("stop", rm.getWay());
						modifiedRelationMembers.add(newMember);
					}
				} else {
					// if the relation member does not have the role
					// "stop_position":
					modifiedRelationMembers.add(rm);
				}

			}
		}

		// now copy PT ways:
		for (RelationMember rm : originalRelation.getMembers()) {
			if (RouteUtils.isPTWay(rm)) {
				Way wayToCheck = rm.getWay();
				if (wayToCheck != wayToRemove) {
					if (rm.getRole().equals("forward") || rm.getRole().equals("backward")) {
						RelationMember modifiedMember = new RelationMember("", wayToCheck);
						modifiedRelationMembers.add(modifiedMember);
					} else {
						modifiedRelationMembers.add(rm);
					}
				}
			}
		}

		modifiedRelation.setMembers(modifiedRelationMembers);

		ChangeCommand changeCommand = new ChangeCommand(originalRelation, modifiedRelation);

		return changeCommand;
	}

	private Command fixSortingError(TestError testError) {
		if (testError.getCode() != ERROR_CODE_SORTING) {
			return null;
		}

		List<OsmPrimitive> primitives = (List<OsmPrimitive>) testError.getPrimitives();
		Relation originalRelation = (Relation) primitives.get(0);

		// separate ways from stops (because otherwise the order of
		// stops/platforms can be messed up by the sorter:
		List<RelationMember> members = originalRelation.getMembers();
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
		Relation sortedRelation = new Relation(originalRelation);
		List<RelationMember> sortedRelationMembers = new ArrayList<>(members.size());
		for (RelationMember rm : stops) {
			sortedRelationMembers.add(rm);
		}
		for (RelationMember rm : sortedWays) {
			sortedRelationMembers.add(rm);
		}
		sortedRelation.setMembers(sortedRelationMembers);

		ChangeCommand changeCommand = new ChangeCommand(originalRelation, sortedRelation);

		return changeCommand;

	}

	private void performDummyTest(Relation r) {
		List<Relation> primitives = new ArrayList<>(1);
		primitives.add(r);
		errors.add(
				new TestError(this, Severity.WARNING, tr("PT: dummy test warning"), ERROR_CODE_DIRECTION, primitives));
	}

}
