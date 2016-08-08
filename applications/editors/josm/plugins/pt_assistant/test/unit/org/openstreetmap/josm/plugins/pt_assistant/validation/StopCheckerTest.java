package org.openstreetmap.josm.plugins.pt_assistant.validation;

import java.io.File;

import org.junit.Test;
import org.junit.Assert;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.plugins.pt_assistant.AbstractTest;
import org.openstreetmap.josm.plugins.pt_assistant.ImportUtils;

public class StopCheckerTest extends AbstractTest {

	@Test
	public void nodePartOfStopAreaTest() {

		// check if stop positions or platforms are in any stop_area relation:
		
		File file = new File(AbstractTest.PATH_TO_STOP_AREA_MEMBERS);
		DataSet ds = ImportUtils.importOsmFile(file, "testLayer");
		PTAssistantValidatorTest test = new PTAssistantValidatorTest();
		Node node = null;
		
		for (Node n : ds.getNodes()) {
			if (n.hasTag("public_transport", "stop_position") | n.hasTag("public_transport", "platform")) {
				node = n;
			}
		}

		NodeChecker nodeChecker = new NodeChecker(node, test);
		nodeChecker.performNodePartOfStopAreaTest();
		Assert.assertEquals(nodeChecker.getErrors().size(), 1);
		Assert.assertEquals(nodeChecker.getErrors().get(0).getCode(),
				PTAssistantValidatorTest.ERROR_CODE_NODE_PART_OF_STOP_AREA);
	}
	


	@Test
	public void stopPositionComparePlatformRelations() {
		
		// Check if stop positions belong the same routes as related platform(s)
		
		File file = new File(AbstractTest.PATH_TO_STOP_AREA_RELATIONS);
		DataSet ds = ImportUtils.importOsmFile(file, "testLayer");
		PTAssistantValidatorTest test = new PTAssistantValidatorTest();
		Node node = null;
		
		for (Node n : ds.getNodes()) {
			if (n.hasTag("public_transport", "stop_position")) {
				node = n;
			}
		}
		
		NodeChecker nodeChecker = new NodeChecker(node, test);
		nodeChecker.performStopPositionComparePlatformRelations();
		Assert.assertEquals(nodeChecker.getErrors().size(), 1);
		Assert.assertEquals(nodeChecker.getErrors().get(0).getCode(),
				PTAssistantValidatorTest.ERROR_CODE_STOP_POSITION_COMPARE_RELATIONS);
			
	}
	

	@Test
	public void stopAreaStopPositionTest() {
		
		// Check if stop area relation has one stop position. 
		
		File file = new File(AbstractTest.PATH_TO_STOP_AREA_NO_STOPS);
		DataSet ds = ImportUtils.importOsmFile(file, "testLayer");
		PTAssistantValidatorTest test = new PTAssistantValidatorTest();
		Relation stopArea = null;

		for (Relation r : ds.getRelations()) {
			if (r.hasTag("public_transport", "stop_area")) {
				stopArea = r;
			}
		}
		
		StopChecker stopChecker = new StopChecker(stopArea, test);
		stopChecker.performStopAreaStopPositionTest();
		Assert.assertEquals(stopChecker.getErrors().size(), 1);
		Assert.assertEquals(stopChecker.getErrors().get(0).getCode(),
				PTAssistantValidatorTest.ERROR_CODE_STOP_AREA_NO_STOPS);

	}
	
	@Test
	public void stopAreaMultiStopPositionTest() {
		
		// Check if stop area relation has more than one stop position. 
		
		File file = new File(AbstractTest.PATH_TO_STOP_AREA_MANY_STOPS);
		DataSet ds = ImportUtils.importOsmFile(file, "testLayer");
		PTAssistantValidatorTest test = new PTAssistantValidatorTest();
		Relation stopArea = null;

		for (Relation r : ds.getRelations()) {
			if (r.hasTag("public_transport", "stop_area")) {
				stopArea = r;
			}
		}

		StopChecker stopChecker = new StopChecker(stopArea, test);
		stopChecker.performStopAreaMultiStopPositionTest();
		Assert.assertEquals(stopChecker.getErrors().size(), 1);
		Assert.assertEquals(stopChecker.getErrors().get(0).getCode(),
				PTAssistantValidatorTest.ERROR_CODE_STOP_AREA_MANY_STOPS);

	}
	

	@Test
	public void stopAreaPlatformTest() {
		
		// Check if stop area relation has one platform. 
		
		File file = new File(AbstractTest.PATH_TO_STOP_AREA_NO_PLATFORMS);
		DataSet ds = ImportUtils.importOsmFile(file, "testLayer");
		PTAssistantValidatorTest test = new PTAssistantValidatorTest();
		Relation stopArea = null;

		for (Relation r : ds.getRelations()) {
			if (r.hasTag("public_transport", "stop_area")) {
				stopArea = r;
			}
		}
		
		StopChecker stopChecker = new StopChecker(stopArea, test);
		stopChecker.performStopAreaPlatformTest();
		Assert.assertEquals(stopChecker.getErrors().size(), 1);
		Assert.assertEquals(stopChecker.getErrors().get(0).getCode(),
				PTAssistantValidatorTest.ERROR_CODE_STOP_AREA_NO_PLATFORM);

	}
	
	@Test
	public void stopAreaMultiPlatformTest() {
		
		// Check if stop area relation has more than one stop position. 
		
		File file = new File(AbstractTest.PATH_TO_STOP_AREA_MANY_PLATFORMS);
		DataSet ds = ImportUtils.importOsmFile(file, "testLayer");
		PTAssistantValidatorTest test = new PTAssistantValidatorTest();
		Relation stopArea = null;

		for (Relation r : ds.getRelations()) {
			if (r.hasTag("public_transport", "stop_area")) {
				stopArea = r;
			}
		}

		StopChecker stopChecker = new StopChecker(stopArea, test);
		stopChecker.performStopAreaMultiPlatformTest();
		Assert.assertEquals(stopChecker.getErrors().size(), 1);
		Assert.assertEquals(stopChecker.getErrors().get(0).getCode(),
				PTAssistantValidatorTest.ERROR_CODE_STOP_AREA_MANY_PLATFORMS);

	}

}
