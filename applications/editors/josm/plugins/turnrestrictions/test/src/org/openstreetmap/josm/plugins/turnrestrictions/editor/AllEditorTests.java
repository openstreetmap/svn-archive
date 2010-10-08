package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import groovy.util.GroovyTestSuite;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllEditorTests extends TestCase{

	private static final String TEST_ROOT = "test/src/org/openstreetmap/josm/plugins/turnrestrictions/editor/";
	private static final GroovyTestSuite gsuite = new GroovyTestSuite();
	
	private static <T extends TestCase> Class<T> groovyts(String className) throws Exception {
		return gsuite.compile(TEST_ROOT + className + ".groovy");
	}
	
	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite(AllEditorTests.class.getName());
		suite.addTestSuite(groovyts("JosmSelectionListModelTest"));
		suite.addTestSuite(groovyts("TurnRestrictionEditorModelUnitTest"));
		suite.addTestSuite(groovyts("TurnRestrictionLegEditorUnitTest"));
		suite.addTestSuite(groovyts("TurnRestrictionTypeRendererTest"));
		suite.addTestSuite(groovyts("TurnRestrictionTypeTest"));
		return suite;
	}
}
