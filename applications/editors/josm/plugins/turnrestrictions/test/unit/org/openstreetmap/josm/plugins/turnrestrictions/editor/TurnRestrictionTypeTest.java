// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class TurnRestrictionTypeTest {

    @Test
    public void test_fromTagValue() {

        TurnRestrictionType type = TurnRestrictionType.fromTagValue("no_left_turn");
        assertEquals(TurnRestrictionType.NO_LEFT_TURN, type);

        type = TurnRestrictionType.fromTagValue("doesnt_exist");
        assertNull(type);

        type = TurnRestrictionType.fromTagValue(null);
        assertNull(type);
    }
}
