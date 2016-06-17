package org.openstreetmap.josm.plugins.pt_assistant.validation;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTRouteDataManager;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTRouteSegment;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTStop;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTWay;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;

/**
 * Performs tests of a route at the level of route segments (the stop-by-stop
 * approach).
 * 
 * @author darya
 *
 */
public class SegmentChecker extends Checker {

	/*
	 * PTRouteSegments that have been validated and are correct. They need to
	 * accessible
	 */
	private static List<PTRouteSegment> correctSegments = new ArrayList<PTRouteSegment>();

	private PTRouteDataManager manager;

	public SegmentChecker(Relation relation, Test test) {

		super(relation, test);
		
		this.manager = new PTRouteDataManager(relation);

	}

	private void performEndstopTest() {
		
		if (manager.getPTStopCount() < 2) {
			// it does not make sense to check a route that has less than 2 stops
			return;
		}
		
		PTStop firstStop = manager.getFirstStop();
		PTStop lastStop = manager.getLastStop();
		
		// TODO: we need the stops to be assigned to routes.
	}

	private void performSegmentTest() {
		// TODO
	}

}
