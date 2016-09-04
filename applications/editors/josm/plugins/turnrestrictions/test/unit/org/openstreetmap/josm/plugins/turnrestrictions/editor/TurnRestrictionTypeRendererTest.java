// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.swing.JLabel;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.testutils.JOSMTestRules;

public class TurnRestrictionTypeRendererTest {

    @Rule
    public JOSMTestRules rules = new JOSMTestRules().preferences();

    @Test
    public void test_Constructor() {
        TurnRestrictionTypeRenderer renderer = new TurnRestrictionTypeRenderer();

        assertNotNull(renderer.icons);
        assertNotNull(renderer.icons.get(TurnRestrictionType.NO_LEFT_TURN));
    }

    @Test
    public void test_getListCellRendererComponent_1() {
        TurnRestrictionTypeRenderer renderer = new TurnRestrictionTypeRenderer();

        JLabel c = (JLabel) renderer.getListCellRendererComponent(null, null, 0, false, false);
        assertNull(c.getIcon());
        assertNotNull(c.getText());

        c = (JLabel) renderer.getListCellRendererComponent(null, "non-standard-value", 0, false, false);
        assertNull(c.getIcon());
        assertEquals("non-standard-value", c.getText());

        c = (JLabel) renderer.getListCellRendererComponent(null, TurnRestrictionType.NO_LEFT_TURN, 0, false, false);
        assertEquals(renderer.icons.get(TurnRestrictionType.NO_LEFT_TURN), c.getIcon());
        assertEquals(TurnRestrictionType.NO_LEFT_TURN.getDisplayName(), c.getText());
    }
}
