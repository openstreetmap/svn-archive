package org.openstreetmap.josm.plugins.pt_assistant.data;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;

/**
 * Creates a representation of a route relation in the pt_assistant data model,
 * then maintains a list of PTStops and PTWays of a route.
 * 
 * @author darya
 *
 */
public class PTRouteDataManager {

	/* The route relation */
	Relation relation;

	/* Stores all relation members that are PTStops */
	private List<PTStop> ptstops = new ArrayList<>();

	/* Stores all relation members that are PTWays */
	private List<PTWay> ptways = new ArrayList<>();

	public PTRouteDataManager(Relation relation) {

		// It is assumed that the relation is a route. Build in a check here
		// (e.g. from class RouteUtils) if you want to invoke this constructor
		// from outside the pt_assitant SegmentChecker)
		
		this.relation = relation;
		
		PTStop prev = null; // stores the last created PTStop

		for (RelationMember member : this.relation.getMembers()) {

			if (RouteUtils.isPTStop(member)) {
				// check if there are consecutive elements that belong to the
				// same stop:
				if (prev != null && prev.getName().equalsIgnoreCase(member.getMember().get("name"))) {
					// this PTStop already exists, so just add a new element:
					prev.addStopElement(member);
					// TODO: something may need to be done if adding the element
					// did not succeed. The failure is a result of the same stop
					// having >1 stop_position, platform or stop_area.
				} else {
					// this PTStop does not exist yet, so create it:
					PTStop ptstop = new PTStop(member);
					ptstops.add(ptstop);
					prev = ptstop;
				}

			} else {
				PTWay ptway = new PTWay(member);
				ptways.add(ptway);
			}
		}
	}
	
	/**
	 * Assigns the given way to a PTWay of this route relation.
	 * @param inputWay Way to be assigned to a PTWAy of this route relation
	 * @return PTWay that contains the geometry of the inputWay, null if not found
	 */
	public PTWay getPTWay(Way inputWay) {
		
		for (PTWay curr: ptways) {
			
			if (curr.isWay() && curr.getWays().get(0) == inputWay) {
				return curr;
			}
			
			if (curr.isRelation()) {
				for (RelationMember rm: curr.getRelation().getMembers()) {
					Way wayInNestedRelation = rm.getWay();
					if (wayInNestedRelation == inputWay) {
						return curr;
					}
				}
			}
		}
		
		return null; // if not found
	}
	
	public List<PTStop> getPTStops() {
		return this.ptstops;
	}
	
	public List<PTWay> getPTWays() {
		return this.ptways;
	}
	
	public int getPTStopCount() {
		return ptstops.size();
	}
	
	public int getPTWayCount() {
		return this.ptways.size();
	}
	
	public PTStop getFirstStop() {
		if (this.ptstops.isEmpty()) {
			return null;
		}
		return this.ptstops.get(0);
	}
	
	public PTStop getLastStop() {
		if (this.ptstops.isEmpty()) {
			return null;
		}
		return this.ptstops.get(ptstops.size()-1);
	}

}
