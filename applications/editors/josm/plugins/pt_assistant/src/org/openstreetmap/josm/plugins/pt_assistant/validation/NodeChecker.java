package org.openstreetmap.josm.plugins.pt_assistant.validation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.validation.Severity;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;

public class NodeChecker extends Checker {

	protected NodeChecker(Node node, Test test) {
		super(node, test);

	}

	/**
	 * Checks if the given stop_position node belongs to any way
	 * 
	 * @param n
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
		TestError e = new TestError(this.test, Severity.WARNING, tr("PT: Stop_position is not part of a way"),
				PTAssistantValidatorTest.ERROR_CODE_SOLITARY_STOP_POSITION, primitives);
		errors.add(e);

	}

	/**
	 * Checks if the given platform node belongs to any way
	 * 
	 * @param n
	 */
	protected void performPlatformPartOfWayTest() {

		List<OsmPrimitive> referrers = node.getReferrers();

		for (OsmPrimitive referrer : referrers) {
			List<Node> primitives = new ArrayList<>(1);
			primitives.add(node);
			if (referrer.getType().equals(OsmPrimitiveType.WAY)) {
				Way referringWay = (Way) referrer;
				if (RouteUtils.isWaySuitableForPublicTransport(referringWay)) {
					TestError e = new TestError(this.test, Severity.WARNING,
							tr("PT: Platform should not be part of a way"),
							PTAssistantValidatorTest.ERROR_CODE_PLATFORM_PART_OF_HIGHWAY, primitives);
					errors.add(e);
					return;
				}
			}
		}
	}
	
	public static void fixError() {
		
	}

}
