package org.openstreetmap.josm.plugins.turnrestrictions;

import org.openstreetmap.josm.plugins.turnrestrictions.editor.AllEditorTests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllUnitTests {
	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite(AllUnitTests.class.getName());
		suite.addTest(AllEditorTests.suite());
		return suite;
	}
}
