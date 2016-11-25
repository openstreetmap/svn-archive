// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.validation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.openstreetmap.josm.actions.AutoScaleAction;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.validation.Severity;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.data.validation.TestError.Builder;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;
import org.openstreetmap.josm.plugins.pt_assistant.utils.StopUtils;

public class NodeChecker extends Checker {

	protected NodeChecker(Node node, Test test) {
		super(node, test);

	}

	/**
	 * Checks if the given stop_position node belongs to any way
	 */
	protected void performSolitaryStopPositionTest() {

		List<OsmPrimitive> referrers = node.getReferrers();

		for (OsmPrimitive referrer : referrers) {
			if (referrer.getType().equals(OsmPrimitiveType.WAY)) {
				Way referrerWay = (Way) referrer;
				if (RouteUtils.isWaySuitableForPublicTransport(referrerWay)) {
					return;
				}

			}
		}

		List<OsmPrimitive> primitives = new ArrayList<>(1);
		primitives.add(node);
		Builder builder = TestError.builder(this.test, Severity.WARNING, PTAssistantValidatorTest.ERROR_CODE_SOLITARY_STOP_POSITION);
		builder.message(tr("PT: Stop_position is not part of a way"));
		builder.primitives(primitives);
		TestError e = builder.build();
		errors.add(e);

	}

	/**
	 * Checks if the given platform node belongs to any way
	 */
	protected void performPlatformPartOfWayTest() {

		List<OsmPrimitive> referrers = node.getReferrers();

		for (OsmPrimitive referrer : referrers) {
			List<Node> primitives = new ArrayList<>(1);
			primitives.add(node);
			if (referrer.getType().equals(OsmPrimitiveType.WAY)) {
				Way referringWay = (Way) referrer;
				if (RouteUtils.isWaySuitableForPublicTransport(referringWay)) {
					Builder builder = TestError.builder(this.test, Severity.WARNING, PTAssistantValidatorTest.ERROR_CODE_PLATFORM_PART_OF_HIGHWAY);
					builder.message(tr("PT: Platform should not be part of a way"));
					builder.primitives(primitives);
					TestError e = builder.build();
					errors.add(e);
					return;
				}
			}
		}
	}

	/**
	 * Checks if the given stop_position node belongs to any stop_area relation
	 * 
	 * @author xamanu
	 */
	protected void performNodePartOfStopAreaTest() {

		if (!StopUtils.verifyIfMemberOfStopArea(node)) {

			List<OsmPrimitive> primitives = new ArrayList<>(1);
			primitives.add(node);
			Builder builder = TestError.builder(this.test, Severity.WARNING, PTAssistantValidatorTest.ERROR_CODE_NOT_PART_OF_STOP_AREA);
			builder.message(tr("PT: Stop position or platform is not part of a stop area relation"));
			builder.primitives(primitives);
			TestError e = builder.build();
			errors.add(e);
		}
	}

	/**
	 * Fixes errors: solitary stop position and platform which is part of a way.
	 * Asks the user first.
	 *
	 * @param testError
	 *            test error
	 * @return fix command
	 */
	protected static Command fixError(TestError testError) {

		if (testError.getCode() != PTAssistantValidatorTest.ERROR_CODE_SOLITARY_STOP_POSITION
				&& testError.getCode() != PTAssistantValidatorTest.ERROR_CODE_PLATFORM_PART_OF_HIGHWAY) {
			return null;
		}

		Node problematicNode = (Node) testError.getPrimitives().iterator().next();

		final int[] userSelection = { JOptionPane.YES_OPTION };
		final TestError errorParameter = testError;
		if (SwingUtilities.isEventDispatchThread()) {

			userSelection[0] = showFixNodeTagDialog(errorParameter);

		} else {

			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						userSelection[0] = showFixNodeTagDialog(errorParameter);
					}
				});
			} catch (InvocationTargetException | InterruptedException e) {
				e.printStackTrace();
				return null;
			}
		}

		if (userSelection[0] == JOptionPane.YES_OPTION) {

			Node modifiedNode = new Node(problematicNode);
			if (testError.getCode() == PTAssistantValidatorTest.ERROR_CODE_SOLITARY_STOP_POSITION) {
				modifiedNode.put("public_transport", "platform");
				ChangeCommand command = new ChangeCommand(problematicNode, modifiedNode);
				return command;
			} else {
				modifiedNode.put("public_transport", "stop_position");
				ChangeCommand command = new ChangeCommand(problematicNode, modifiedNode);
				return command;
			}
		}

		return null;

	}

	private static int showFixNodeTagDialog(TestError e) {
		Node problematicNode = (Node) e.getPrimitives().iterator().next();
		// Main.map.mapView.zoomTo(problematicNode.getCoor());
		// zoom to problem:
		Collection<OsmPrimitive> primitives = new ArrayList<>(1);
		primitives.add(problematicNode);
		AutoScaleAction.zoomTo(primitives);

		String[] options = { tr("Yes"), tr("No") };
		String message;
		if (e.getCode() == PTAssistantValidatorTest.ERROR_CODE_SOLITARY_STOP_POSITION) {
			message = "Do you want to change the tag public_transport=stop_position to public_transport=platform?";
		} else {
			message = "Do you want to change the tag public_transport=platform to public_transport=stop_position?";
		}
		return JOptionPane.showOptionDialog(null, message, tr("PT_Assistant Message"), JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, 0);
	}

}
