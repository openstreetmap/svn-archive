package unit.org.openstreetmap.josm.plugins.pt_assistant.validation;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.plugins.pt_assistant.validation.GapTest;

import unit.org.openstreetmap.josm.plugins.pt_assistant.AbstractTest;
import unit.org.openstreetmap.josm.plugins.pt_assistant.ImportUtils;

public class PlatformAsWayTest extends AbstractTest{
	
	@Test
	public void sortingTest() {
		File file = new File(AbstractTest.PATH_TO_PLATFORM_AS_WAY);
		DataSet ds = ImportUtils.importOsmFile(file, "testLayer");
		
		GapTest gapTest = new GapTest();
		for (Relation r: ds.getRelations()) {
			gapTest.visit(r);
		}
		
		List<TestError> errors = gapTest.getErrors();
		
		assertEquals(errors.size(), 0);
	}

}
