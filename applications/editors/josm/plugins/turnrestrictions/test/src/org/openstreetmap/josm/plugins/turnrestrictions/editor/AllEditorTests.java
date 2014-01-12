package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import junit.framework.TestCase;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	JosmSelectionListModelTest.class,
	TurnRestrictionEditorModelUnitTest.class,
	TurnRestrictionLegEditorUnitTest.class,
	TurnRestrictionTypeRendererTest.class,
	TurnRestrictionTypeTest.class,
	ExceptValueModelTest.class
})
public class AllEditorTests extends TestCase{}
