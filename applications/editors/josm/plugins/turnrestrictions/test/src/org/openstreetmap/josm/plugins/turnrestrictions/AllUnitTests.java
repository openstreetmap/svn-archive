package org.openstreetmap.josm.plugins.turnrestrictions;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.openstreetmap.josm.plugins.turnrestrictions.editor.AllEditorTests;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	AllEditorTests.class,
	TurnRestrictionBuilderTest.class
})
public class AllUnitTests {}
