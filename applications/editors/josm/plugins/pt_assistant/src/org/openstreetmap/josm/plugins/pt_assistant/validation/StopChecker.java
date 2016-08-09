package org.openstreetmap.josm.plugins.pt_assistant.validation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.validation.Severity;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.plugins.pt_assistant.utils.StopUtils;

/**
 * Performs tests of the stop area relations
 * 
 * @author 
 *
 */
public class StopChecker extends Checker {

	Set<OsmPrimitive> members;

	protected StopChecker(Relation relation, Test test) {
		super(relation, test);
		
		this.members = relation.getMemberPrimitives();
	}
	
	/**
	 * Checks if the given stop area relation has a stop position.
	 */
	protected void performStopAreaStopPositionTest() {
		
		// No errors if there is a member tagged as stop position.
		for (OsmPrimitive member : members) {
			if (StopUtils.verifyStopAreaStopPosition(member)) {
				return;
			}
		}
		
		// Throw error message
		List<OsmPrimitive> primitives = new ArrayList<>(1);
		primitives.add(relation);
		TestError e = new TestError(this.test, Severity.WARNING, tr("PT: Stop area relation has no stop position"),
				PTAssistantValidatorTest.ERROR_CODE_STOP_AREA_NO_STOPS, primitives);
		errors.add(e);
	}

	/**
	 * Checks if the given stop area relation has more than one stop position.
	 */
	protected void performStopAreaMultiStopPositionTest() {
		
		// Count members tagged as stop position.
		int countStopPosition = 0;
		for (OsmPrimitive member : members) {
			if (StopUtils.verifyStopAreaStopPosition(member)) {
				countStopPosition++;

			}
		}

		// No errors if there are more than one stop position.
		if (countStopPosition <= 1) {
			return;
		}
		
		// Throw error message
		List<OsmPrimitive> primitives = new ArrayList<>(1);
		primitives.add(relation);
		TestError e = new TestError(this.test, Severity.WARNING, tr("PT: Stop area relation has several stop positions"),
				PTAssistantValidatorTest.ERROR_CODE_STOP_AREA_MANY_STOPS, primitives);
		errors.add(e);
		
	}
	
	/**
	 * Checks if the given stop area relation has a platform.
	 */
	protected void performStopAreaPlatformTest() {
		
		// No errors if there is a member tagged as platform.
		for (OsmPrimitive member : members) {
			if (StopUtils.verifyStopAreaPlatform(member)) {
				return;
			}
		}
		
		// Throw error message
		List<OsmPrimitive> primitives = new ArrayList<>(1);
		primitives.add(relation);
		TestError e = new TestError(this.test, Severity.WARNING, tr("PT: Stop area relation has no platform"),
				PTAssistantValidatorTest.ERROR_CODE_STOP_AREA_NO_PLATFORM, primitives);
		errors.add(e);
		
	}
	

	/**
	 * Checks if the given stop area relation has more than one platform. 
	 */
	protected void performStopAreaMultiPlatformTest() {
		
		// Count members tagged as platformn.
		int countPlatform = 0;
		for (OsmPrimitive member : members) {
			if (StopUtils.verifyStopAreaPlatform(member)) {
				countPlatform++;

			}
		}
		
		// No errors if there are more than one platformnn.
		if (countPlatform <= 1) {
			return;
		}
				
		// Throw error message
		List<OsmPrimitive> primitives = new ArrayList<>(1);
		primitives.add(relation);
		TestError e = new TestError(this.test, Severity.WARNING, tr("PT: Stop area relation has several platforms"),
				PTAssistantValidatorTest.ERROR_CODE_STOP_AREA_MANY_PLATFORMS, primitives);
		errors.add(e);
		
	}
	
}
