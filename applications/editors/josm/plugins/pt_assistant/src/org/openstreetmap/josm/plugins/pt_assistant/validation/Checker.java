package org.openstreetmap.josm.plugins.pt_assistant.validation;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;

/**
 * Represents tests and fixed of the PT_Assistant plugin
 * 
 * @author darya
 *
 */
public abstract class Checker {

	// test which created this WayChecker:
	protected final Test test;

	// relation that is checked:
	protected Relation relation;

	// stores all found errors:
	protected ArrayList<TestError> errors = new ArrayList<>();

	protected Checker(Relation relation, Test test) {

		this.relation = relation;
		this.test = test;

	}

	/**
	 * Returns errors
	 */
	public List<TestError> getErrors() {

		return errors;
	}

	/**
	 * Returns a list of stop-related route relation members with corrected
	 * roles (if necessary)
	 * 
	 * @return list of stop-related route relation members
	 */
	protected static List<RelationMember> listStopMembers(Relation r) {

		List<RelationMember> resultList = new ArrayList<>();

		for (RelationMember rm : r.getMembers()) {

			if (RouteUtils.isPTStop(rm)) {

				if (rm.getMember().hasTag("public_transport", "stop_position")) {
					if (!rm.hasRole("stop") && !rm.hasRole("stop_entry_only") && !rm.hasRole("stop_exit_only")) {
						RelationMember newMember = new RelationMember("stop", rm.getMember());
						resultList.add(newMember);
					} else {
						resultList.add(rm);
					}
				} else { // if platform
					if (!rm.hasRole("platform") && !rm.hasRole("platform_entry_only")
							&& !rm.hasRole("platform_exit_only")) {
						RelationMember newMember = new RelationMember("platform", rm.getMember());
						resultList.add(newMember);
					} else {
						resultList.add(rm);
					}
				}

			}
		}

		return resultList;
	}

	/**
	 * Returns a list of other (not stop-related) route relation members with
	 * corrected roles (if necessary)
	 * 
	 * @return list of other (not stop-related) route relation members
	 */
	protected static List<RelationMember> listNotStopMembers(Relation r) {

		List<RelationMember> resultList = new ArrayList<RelationMember>();

		for (RelationMember rm : r.getMembers()) {

			if (!RouteUtils.isPTStop(rm)) {

				if (rm.hasRole("forward") || rm.hasRole("backward")) {
					RelationMember newMember = new RelationMember("", rm.getMember());
					resultList.add(newMember);
				} else {

					resultList.add(rm);

				}
			}

		}

		return resultList;
	}

}
