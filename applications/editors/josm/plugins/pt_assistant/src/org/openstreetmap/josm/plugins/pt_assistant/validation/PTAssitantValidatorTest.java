package org.openstreetmap.josm.plugins.pt_assistant.validation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
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

		if (r.hasIncompleteMembers()) {

			// IncompleteMembersDownloadDialog incompleteDialog = new
			// IncompleteMembersDownloadDialog(r.getId());
			//
			// int userInput = incompleteDialog.getUserSelection();

			String message = tr("The relation (id=" + r.getId()
					+ ") has incomplete members.\nThey need to be downloaded to proceed with validation of this relation.\nDo you want to download incomplete members?");
			JCheckBox checkbox = new JCheckBox(tr("Remember my choice and don't ask me again in this session"));
			Object[] params = { message, checkbox };
			String[] options = { tr("Yes"), tr("No") };

			int userInput = Integer.MIN_VALUE;
			userInput = JOptionPane.showOptionDialog(null, params, tr("Fetch Request"), JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, options, 0);

			if (userInput == 0) {

				Thread t = new IncompleteMembersDownloadThread(r);
				t.start();
				synchronized (t) {
					try {
						t.wait();
					} catch (InterruptedException e) {
						return;
					}
				}

			}
		}

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
		
//		List<Relation> primitives = new ArrayList<>(1);
//		primitives.add(r);
//		errors.add(new TestError(this, Severity.WARNING, tr("PT: test warning"), ERROR_CODE_DIRECTION, primitives));
		
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

}
