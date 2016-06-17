package org.openstreetmap.josm.plugins.pt_assistant.validation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.validation.Severity;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTRouteDataManager;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTRouteSegment;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTStop;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTWay;
import org.openstreetmap.josm.plugins.pt_assistant.utils.StopToWayAssigner;

/**
 * Performs tests of a route at the level of route segments (the stop-by-stop
 * approach).
 * 
 * @author darya
 *
 */
public class SegmentChecker extends Checker {

	/* PTRouteSegments that have been validated and are correct */
	private static List<PTRouteSegment> correctSegments = new ArrayList<PTRouteSegment>();

	/* Manager of the PTStops and PTWays of the current route */
	private PTRouteDataManager manager;

	/* Assigns PTStops to nearest PTWays and stores that correspondense */
	private StopToWayAssigner assigner;

	public SegmentChecker(Relation relation, Test test) {

		super(relation, test);

		this.manager = new PTRouteDataManager(relation);
		this.assigner = new StopToWayAssigner(manager.getPTWays());

	}

	public void performFirstStopTest() {

		performEndStopTest(manager.getFirstStop());

	}

	public void performLastStopTest() {

		performEndStopTest(manager.getLastStop());

	}

	private void performEndStopTest(PTStop endStop) {
		if (manager.getPTStopCount() < 2) {
			// it does not make sense to check a route that has less than 2
			// stops
			return;
		}

		if (endStop.getStopPosition() == null) {
			List<Relation> primitives = new ArrayList<>(1);
			primitives.add(relation);
			List<OsmPrimitive> highlighted = new ArrayList<>(1);
			highlighted.add(endStop.getPlatform());
			TestError e = new TestError(this.test, Severity.WARNING,
					tr("PT: Route should start and end with a stop_position"),
					PTAssitantValidatorTest.ERROR_CODE_END_STOP, primitives, highlighted);
			this.errors.add(e);
			return;
		}

		PTWay endWay = assigner.get(endStop);

		boolean found = false;
		List<Way> primitivesOfEndWay = endWay.getWays();
		for (Way primitiveWay : primitivesOfEndWay) {
			if (primitiveWay.firstNode() == endStop.getStopPosition()
					|| primitiveWay.lastNode() == endStop.getStopPosition()) {
				found = true;
			}

		}

		if (!found) {
			List<Relation> primitives = new ArrayList<>(1);
			primitives.add(relation);
			List<OsmPrimitive> highlighted = new ArrayList<>();
			for (Way w : endWay.getWays()) {
				if (w.getNodes().contains(endStop.getStopPosition())) {
					highlighted.add(w);
				}
			}
			TestError e = new TestError(this.test, Severity.WARNING,
					tr("PT: First or last way needs to be split"),
					PTAssitantValidatorTest.ERROR_CODE_SPLIT_WAY, primitives, highlighted);
			this.errors.add(e);
		}

	}

	private void performSegmentTest() {
		// TODO
	}

}
