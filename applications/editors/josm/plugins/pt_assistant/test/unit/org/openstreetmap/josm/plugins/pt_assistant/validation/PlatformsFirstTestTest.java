package org.openstreetmap.josm.plugins.pt_assistant.validation;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.plugins.pt_assistant.AbstractTest;
import org.openstreetmap.josm.plugins.pt_assistant.ImportUtils;

public class PlatformsFirstTestTest extends AbstractTest {

	@Test
	public void testBeforeFile() {
		DataSet ds = ImportUtils.importOsmFile(new File(AbstractTest.PATH_TO_DL131_BEFORE), "testLayer");
		
		PlatformsFirstTest pf = new PlatformsFirstTest();
		for (Relation r: ds.getRelations()) {
			pf.visit(r);
		}
		List<TestError> errors = pf.getErrors();

		assertEquals(errors.size(), 1);
		assertEquals(errors.iterator().next().getCode(), PlatformsFirstTest.ERROR_CODE); 
		assertEquals(errors.iterator().next().getTester().getClass().getName(), PlatformsFirstTest.class.getName());
	
	}
	
	@Test
	public void testAfterFile() {
		DataSet ds = ImportUtils.importOsmFile(new File(AbstractTest.PATH_TO_DL131_AFTER), "testLayer");
		
		PlatformsFirstTest pf = new PlatformsFirstTest();
		for (Relation r: ds.getRelations()) {
			pf.visit(r);
		}
		List<TestError> errors = pf.getErrors();
		assertEquals(errors.size(), 0);
		
		
	}
}
