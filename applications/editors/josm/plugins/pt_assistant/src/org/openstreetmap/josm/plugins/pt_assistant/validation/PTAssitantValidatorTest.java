package org.openstreetmap.josm.plugins.pt_assistant.validation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.OsmUtils;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.validation.Severity;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.gui.dialogs.relation.sort.WayConnectionType;
import org.openstreetmap.josm.gui.dialogs.relation.sort.WayConnectionTypeCalculator;
import org.openstreetmap.josm.plugins.pt_assistant.actions.IncompleteMembersDownloadThread;
import org.openstreetmap.josm.plugins.pt_assistant.gui.IncompleteMembersDownloadDialog;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;

public class PTAssitantValidatorTest extends Test {

	public static final int ERROR_CODE_SORTING = 3711;
	// public static final int ERROR_CODE_OVERSHOOT = 3712;
	// public static final int ERROR_CODE_SPLITTING = 3713;
	// public static final int ERROR_CODE_OTHER_GAP = 3719;
	public static final int ERROR_CODE_ROAD_TYPE = 3721;
	public static final int ERROR_CODE_DIRECTION = 3731;

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

		// Check individual ways using the oneway direction test and the road
		// type test:
		WayChecker wayChecker = new WayChecker(r, this);
		this.errors.addAll(wayChecker.getErrors());
		
		// TODO: ask user if the found problems should be fixed
		
		// Check if the relation is correct, or only has a wrong sorting order:
		RouteChecker routeChecker = new RouteChecker(r, this);
		this.errors.addAll(routeChecker.getErrors());
		

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

			Thread t = new IncompleteMembersDownloadThread(r);
			t.start();
			synchronized (t) {
				try {
					t.wait();
				} catch (InterruptedException e) {
					return false;
				}
			}
			return true;
		}

		return false;
	}

	/**
	 * Checks if the test error is fixable
	 */
	@Override
	public boolean isFixable(TestError testError) {
		return false;
	}

	@Override
	public Command fixError(TestError testError) {
		return null;
	}

	private void performDummyTest(Relation r) {
		List<Relation> primitives = new ArrayList<>(1);
		primitives.add(r);
		errors.add(
				new TestError(this, Severity.WARNING, tr("PT: dummy test warning"), ERROR_CODE_DIRECTION, primitives));
	}

}
