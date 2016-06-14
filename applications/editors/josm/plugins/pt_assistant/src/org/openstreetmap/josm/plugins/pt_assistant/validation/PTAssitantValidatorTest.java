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

		performDummyTest(r);





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
	
	private void performDirectionTest(Relation r) {
		List<RelationMember> waysToCheck = new ArrayList<>();

		for (RelationMember rm : r.getMembers()) {
			if (RouteUtils.isPTWay(rm) && rm.getType().equals(OsmPrimitiveType.WAY)) {
				waysToCheck.add(rm);
			}
		}

		if (waysToCheck.isEmpty()) {
			return;
		}
		
		WayConnectionTypeCalculator connectionTypeCalculator = new WayConnectionTypeCalculator();
		final List<WayConnectionType> links = connectionTypeCalculator.updateLinks(waysToCheck);

		for (int i = 0; i < links.size(); i++) {
			if ((OsmUtils.isTrue(waysToCheck.get(i).getWay().get("oneway"))
					&& links.get(i).direction.equals(WayConnectionType.Direction.BACKWARD))
					|| (OsmUtils.isReversed(waysToCheck.get(i).getWay().get("oneway"))
							&& links.get(i).direction.equals(WayConnectionType.Direction.FORWARD))) {

				// At this point, the PTWay is going against the oneway
				// direction. Check if this road allows buses to disregard
				// the oneway restriction:

				if (!waysToCheck.get(i).getWay().hasTag("busway", "lane")
						&& !waysToCheck.get(i).getWay().hasTag("oneway:bus", "no")
						&& !waysToCheck.get(i).getWay().hasTag("busway", "opposite_lane")
						&& !waysToCheck.get(i).getWay().hasTag("oneway:psv", "no")
						&& !waysToCheck.get(i).getWay().hasTag("trolley_wire", "backward")) {
					List<Relation> primitives = new ArrayList<>(1);
					primitives.add(r);
					List<Way> highlighted = new ArrayList<>(1);
					highlighted.add(waysToCheck.get(i).getWay());
					errors.add(new TestError(this, Severity.WARNING,
							tr("PT: Route passes a oneway road in wrong direction"), ERROR_CODE_DIRECTION, primitives,
							highlighted));
					return;
				}

			}
		}
	}
	
	private void performDummyTest(Relation r) {
		 List<Relation> primitives = new ArrayList<>(1);
		 primitives.add(r);
		 errors.add(new TestError(this, Severity.WARNING, tr("PT: dummy test warning"), ERROR_CODE_DIRECTION, primitives));
	}

}
