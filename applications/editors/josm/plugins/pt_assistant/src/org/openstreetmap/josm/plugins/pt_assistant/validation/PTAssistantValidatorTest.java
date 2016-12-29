// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.validation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SelectCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.validation.Severity;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.data.validation.TestError.Builder;
import org.openstreetmap.josm.plugins.pt_assistant.PTAssistantPlugin;
import org.openstreetmap.josm.plugins.pt_assistant.actions.FixTask;
import org.openstreetmap.josm.plugins.pt_assistant.actions.IncompleteMembersDownloadThread;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTRouteDataManager;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTRouteSegment;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTStop;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTWay;
import org.openstreetmap.josm.plugins.pt_assistant.gui.IncompleteMembersDownloadDialog;
import org.openstreetmap.josm.plugins.pt_assistant.gui.PTAssistantLayer;
import org.openstreetmap.josm.plugins.pt_assistant.gui.ProceedDialog;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;
import org.openstreetmap.josm.plugins.pt_assistant.utils.StopToWayAssigner;
import org.openstreetmap.josm.plugins.pt_assistant.utils.StopUtils;
import org.openstreetmap.josm.tools.Utils;

public class PTAssistantValidatorTest extends Test {

	public static final int ERROR_CODE_SORTING = 3711;
	public static final int ERROR_CODE_ROAD_TYPE = 3721;
	public static final int ERROR_CODE_CONSTRUCTION = 3722;
	public static final int ERROR_CODE_DIRECTION = 3731;
	public static final int ERROR_CODE_END_STOP = 3741;
	public static final int ERROR_CODE_SPLIT_WAY = 3742;
	public static final int ERROR_CODE_RELAITON_MEMBER_ROLES = 3743;
	public static final int ERROR_CODE_SOLITARY_STOP_POSITION = 3751;
	public static final int ERROR_CODE_PLATFORM_PART_OF_HIGHWAY = 3752;
	public static final int ERROR_CODE_STOP_NOT_SERVED = 3753;
	public static final int ERROR_CODE_STOP_BY_STOP = 3754;
	public static final int ERROR_CODE_NOT_PART_OF_STOP_AREA = 3761;
	public static final int ERROR_CODE_STOP_AREA_NO_STOPS = 3762;
	public static final int ERROR_CODE_STOP_AREA_NO_PLATFORM = 3763;
	public static final int ERROR_CODE_STOP_AREA_COMPARE_RELATIONS = 3764;

	private PTAssistantLayer layer;

	public PTAssistantValidatorTest() {
		super(tr("Public Transport Assistant tests"),
				tr("Check if route relations are compatible with public transport version 2"));

		layer = PTAssistantLayer.getLayer();
		DataSet.addSelectionListener(layer);

	}

	@Override
	public void visit(Node n) {

		if (n.isIncomplete()) {
			return;
		}

		NodeChecker nodeChecker = new NodeChecker(n, this);

		// select only stop_positions
		if (n.hasTag("public_transport", "stop_position")) {

			// check if stop positions are on a way:
			nodeChecker.performSolitaryStopPositionTest();

			if (Main.pref.getBoolean("pt_assistant.stop-area-tests", true) == true) {
				// check if stop positions are in any stop_area relation:
				nodeChecker.performNodePartOfStopAreaTest();
			}

		}

		// select only platforms
		if (n.hasTag("public_transport", "platform")) {

			// check that platforms are not part of any way:
			nodeChecker.performPlatformPartOfWayTest();

			if (Main.pref.getBoolean("pt_assistant.stop-area-tests", true) == true) {
				// check if platforms are in any stop_area relation:
				nodeChecker.performNodePartOfStopAreaTest();
			}

		}

		this.errors.addAll(nodeChecker.getErrors());

	}

	@Override
	public void visit(Relation r) {

		// Download incomplete members. If the download does not work, return
		// and do not do any testing.
		if (r.hasIncompleteMembers()) {

			boolean downloadSuccessful = this.downloadIncompleteMembers();
			if (!downloadSuccessful) {
				return;
			}
		}

		if (r.hasIncompleteMembers()) {
			return;
		}

		// Do some testing on stop area relations
		if (Main.pref.getBoolean("pt_assistant.stop-area-tests", true) == true && StopUtils.isStopArea(r)) {

			StopChecker stopChecker = new StopChecker(r, this);

			// Check if stop area relation has one stop position.
			stopChecker.performStopAreaStopPositionTest();

			// Check if stop area relation has one platform.
			stopChecker.performStopAreaPlatformTest();

			// Check if stop position(s) belong the same route relation as
			// related platform(s)
			stopChecker.performStopAreaRelationsTest();

			// Attach thrown errors
			this.errors.addAll(stopChecker.getErrors());
		}

		if (!RouteUtils.isTwoDirectionRoute(r)) {
			return;
		}

		// Check individual ways using the oneway direction test and the road
		// type test:
		WayChecker wayChecker = new WayChecker(r, this);
		wayChecker.performDirectionTest();
		wayChecker.performRoadTypeTest();
		this.errors.addAll(wayChecker.getErrors());

		proceedWithSorting(r);

		// This allows to modify the route before the sorting and
		// SegmentChecker are carried out:
		// if (this.errors.isEmpty()) {
		// proceedWithSorting(r);
		// } else {
		// this.proceedAfterWayCheckerErrors(r);
		// }

	}

	/**
	 * Downloads incomplete relation members in an extra thread (user input
	 * required)
	 *
	 * @return true if successful, false if not successful
	 */
	private boolean downloadIncompleteMembers() {

		final int[] userSelection = { 0 };

		try {

			if (SwingUtilities.isEventDispatchThread()) {

				userSelection[0] = showIncompleteMembersDownloadDialog();

			} else {

				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						try {
							userSelection[0] = showIncompleteMembersDownloadDialog();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

					}
				});

			}

		} catch (InterruptedException | InvocationTargetException e) {
			return false;
		}

		if (userSelection[0] == JOptionPane.YES_OPTION) {

			Thread t = new IncompleteMembersDownloadThread();
			t.start();
			synchronized (t) {
				try {
					t.wait();
				} catch (InterruptedException e) {
					return false;
				}
			}

		}

		return true;

	}

	/**
	 * Shows the dialog asking the user about an incomplete member download
	 *
	 * @return user's selection
	 * @throws InterruptedException
	 *             if interrupted
	 */
	private int showIncompleteMembersDownloadDialog() throws InterruptedException {

		if (Main.pref.getBoolean("pt_assistant.download-incomplete", false) == true) {
			return JOptionPane.YES_OPTION;
		}

		if (Main.pref.getBoolean("pt_assistant.download-incomplete", false) == false) {
			return JOptionPane.NO_OPTION;
		}

		IncompleteMembersDownloadDialog incompleteMembersDownloadDialog = new IncompleteMembersDownloadDialog();
		return incompleteMembersDownloadDialog.getUserSelection();

	}

	/**
	 * Gets user input after errors were detected by WayChecker. Although this
	 * method is not used in the current implementation, it can be used to fix
	 * errors from the previous testing stage and modify the route before the
	 * second stage of testing is carried out.
	 */
	@SuppressWarnings("unused")
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

		final int[] userInput = { 0 };
		final long idParameter = r.getId();
		final int directionErrorParameter = numberOfDirectionErrors;
		final int roadTypeErrorParameter = numberOfRoadTypeErrors;

		if (SwingUtilities.isEventDispatchThread()) {

			userInput[0] = showProceedDialog(idParameter, directionErrorParameter, roadTypeErrorParameter);

		} else {

			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						userInput[0] = showProceedDialog(idParameter, directionErrorParameter, roadTypeErrorParameter);

					}
				});
			} catch (InvocationTargetException | InterruptedException e1) {
				e1.printStackTrace();
			}

		}

		if (userInput[0] == 0) {
			this.fixErrorFromPlugin(this.errors);
			proceedWithSorting(r);
			return;
		}

		if (userInput[0] == 1) {
			JOptionPane.showMessageDialog(null, "This is not implemented yet!");
			return;
		}

		if (userInput[0] == 2) {
			proceedWithSorting(r);
		}

		// if userInput==-1 (i.e. no input), do nothing and stop testing of the
		// route.

	}

	private int showProceedDialog(long id, int numberOfDirectionErrors, int numberOfRoadTypeErrors) {

		if (numberOfDirectionErrors == 0 && numberOfRoadTypeErrors == 0) {
			return 2;
		}

		if (Main.pref.getBoolean("pt_assistant.proceed-without-fix", true) == false) {
			return 0;
		}

		if (Main.pref.getBoolean("pt_assistant.proceed-without-fix", true) == true) {
			return 2;
		}

		ProceedDialog proceedDialog = new ProceedDialog(id, numberOfDirectionErrors, numberOfRoadTypeErrors);
		return proceedDialog.getUserSelection();

	}

	/**
	 * Carries out the second stage of the testing: sorting
	 *
	 * @param r
	 *            relation
	 */
	private void proceedWithSorting(Relation r) {

		// Check if the relation is correct, or only has a wrong sorting order:
		RouteChecker routeChecker = new RouteChecker(r, this);
		routeChecker.performSortingTest();
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
			// If there is only the sorting error, add it
			this.errors.addAll(routeChecker.getErrors());
		}

		// if (!routeChecker.getHasGap()) {
		// // Variant 1
		// storeCorrectRouteSegments(r);
		// }

		// Variant 3:
		proceedAfterSorting(r);

	}

	/**
	 * Carries out the stop-by-stop testing which includes building the route
	 * data model.
	 *
	 * @param r
	 *            route relation
	 */
	private void proceedAfterSorting(Relation r) {

		SegmentChecker segmentChecker = new SegmentChecker(r, this);

		// Check if the creation of the route data model in the segment checker
		// worked. If it did not, it means the roles in the route relation do
		// not match the tags of the route members.
		if (!segmentChecker.getErrors().isEmpty()) {
			this.errors.addAll(segmentChecker.getErrors());
		}

		segmentChecker.performFirstStopTest();
		segmentChecker.performLastStopTest();
		segmentChecker.performStopNotServedTest();

		boolean sortingErrorFound = false;
		for (TestError error : this.errors) {
			if (error.getCode() == ERROR_CODE_SORTING) {
				sortingErrorFound = true;
				break;
			}
		}
		if (!sortingErrorFound) {
			segmentChecker.performStopByStopTest();
			segmentChecker.findFixes();
		}

		for (TestError error : segmentChecker.getErrors()) {
			if (error.getCode() != PTAssistantValidatorTest.ERROR_CODE_RELAITON_MEMBER_ROLES) {
				this.errors.add(error);
			}
		}
	}

	/**
	 * Method is called after all primitives has been visited, overrides the
	 * method of the superclass.
	 */
	public void endTest() {
		
		// modify the error messages for the stop-by-stop test:
		SegmentChecker.modifyStopByStopErrorMessages();

		// add the stop-by-stop errors with modified messages:
		for (Entry<Builder, PTRouteSegment> entry : SegmentChecker.wrongSegmentBuilders.entrySet()) {
			TestError error = entry.getKey().build();
			SegmentChecker.wrongSegments.put(error, entry.getValue());
			this.errors.add(error);
		}
		
		// reset the static collections in SegmentChecker:
		SegmentChecker.reset();

		super.endTest();

	}

	/**
	 * Creates the PTRouteSegments of a route that has been found correct and
	 * stores them in the list of correct route segments
	 *
	 * @param r
	 *            route relation
	 */
	@SuppressWarnings("unused")
	private void storeCorrectRouteSegments(Relation r) {
		PTRouteDataManager manager = new PTRouteDataManager(r);
		StopToWayAssigner assigner = new StopToWayAssigner(manager.getPTWays());
		if (manager.getPTStops().size() > 1) {
			for (int i = 1; i < manager.getPTStops().size(); i++) {
				PTStop segmentStartStop = manager.getPTStops().get(i - 1);
				PTStop segmentEndStop = manager.getPTStops().get(i);
				Way segmentStartWay = assigner.get(segmentStartStop);
				Way segmentEndWay = assigner.get(segmentEndStop);
				List<PTWay> waysBetweenStops = manager.getPTWaysBetween(segmentStartWay, segmentEndWay);
				PTRouteSegment routeSegment = new PTRouteSegment(segmentStartStop, segmentEndStop, waysBetweenStops, r);
				SegmentChecker.addCorrectSegment(routeSegment);
			}
		}
	}

	/**
	 * Checks if the test error is fixable
	 */
	@Override
	public boolean isFixable(TestError testError) {
		if (testError.getCode() == ERROR_CODE_DIRECTION || testError.getCode() == ERROR_CODE_ROAD_TYPE
				|| testError.getCode() == ERROR_CODE_CONSTRUCTION || testError.getCode() == ERROR_CODE_SORTING
				|| testError.getCode() == PTAssistantValidatorTest.ERROR_CODE_SOLITARY_STOP_POSITION
				|| testError.getCode() == PTAssistantValidatorTest.ERROR_CODE_PLATFORM_PART_OF_HIGHWAY) {
			return true;
		}

		if (testError.getCode() == ERROR_CODE_STOP_BY_STOP && SegmentChecker.isFixable(testError)) {
			return true;
		}

		return false;
	}

	/**
	 * Fixes the given error
	 */
	@Override
	public Command fixError(TestError testError) {

		// repaint the relation in the pt_assistant layer:
		if (testError.getPrimitives().iterator().next().getType().equals(OsmPrimitiveType.RELATION)) {
			Relation relationToBeFixed = (Relation) testError.getPrimitives().iterator().next();
			this.layer.repaint(relationToBeFixed);
		}

		// reset the last fix:
		PTAssistantPlugin.setLastFix(null);

		List<Command> commands = new ArrayList<>();

		if (testError.getCode() == ERROR_CODE_ROAD_TYPE || testError.getCode() == ERROR_CODE_CONSTRUCTION) {
			commands.add(WayChecker.fixErrorByZooming(testError));
		}

		if (testError.getCode() == ERROR_CODE_DIRECTION) {
			commands.add(WayChecker.fixErrorByZooming(testError));

		}

		if (testError.getCode() == ERROR_CODE_SORTING) {
			commands.add(RouteChecker.fixSortingError(testError));
		}

		if (testError.getCode() == ERROR_CODE_SOLITARY_STOP_POSITION
				|| testError.getCode() == ERROR_CODE_PLATFORM_PART_OF_HIGHWAY) {
			commands.add(NodeChecker.fixError(testError));
		}

		if (testError.getCode() == ERROR_CODE_STOP_BY_STOP) {
			commands.add(SegmentChecker.fixError(testError));
			// make sure the primitives of this testError are selected:
			Collection<OsmPrimitive> primitivesToSelect = new ArrayList<>();
			for (Object obj : testError.getPrimitives()) {
				primitivesToSelect.add((OsmPrimitive) obj);
			}
			SelectCommand selectCommand = new SelectCommand(primitivesToSelect);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					selectCommand.executeCommand();
				}
			});
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
	 */
	private void fixErrorFromPlugin(List<TestError> testErrors) {

		// run fix task asynchronously
		FixTask fixTask = new FixTask(testErrors);

		Thread t = new Thread(fixTask);
		t.start();
		try {
			t.join();
			errors.removeAll(testErrors);

		} catch (InterruptedException e) {
			JOptionPane.showMessageDialog(null, "Error occurred during fixing");
		}

	}

	public void addFixVariants(List<List<PTWay>> fixVariants) {
		layer.addFixVariants(fixVariants);
	}

	public void clearFixVariants() {
		layer.clearFixVariants();
	}

	public List<PTWay> getFixVariant(Character c) {
		return layer.getFixVariant(c);
	}

	@SuppressWarnings("unused")
	private void performDummyTest(Relation r) {
		List<Relation> primitives = new ArrayList<>(1);
		primitives.add(r);
		Builder builder = TestError.builder(this, Severity.WARNING, ERROR_CODE_DIRECTION);
		builder.message(tr("PT: dummy test warning"));
		builder.primitives(primitives);
		errors.add(builder.build());
	}

}
