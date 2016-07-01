// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import junit.framework.TestCase;

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
