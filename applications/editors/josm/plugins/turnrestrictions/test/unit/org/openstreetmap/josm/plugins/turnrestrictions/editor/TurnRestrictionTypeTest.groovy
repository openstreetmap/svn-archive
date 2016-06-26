package org.openstreetmap.josm.plugins.turnrestrictions.editor;
import static org.junit.Assert.*

import org.junit.*

class TurnRestrictionTypeTest {

    @Test
    public void test_fromTagValue() {

        TurnRestrictionType type = TurnRestrictionType.fromTagValue("no_left_turn")
        assert type == TurnRestrictionType.NO_LEFT_TURN

        type = TurnRestrictionType.fromTagValue("doesnt_exist")
        assert type == null

        type = TurnRestrictionType.fromTagValue(null)
        assert type == null
    }
}
